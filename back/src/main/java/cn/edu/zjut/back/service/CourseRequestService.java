package cn.edu.zjut.back.service;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.entity.Course;
import cn.edu.zjut.back.entity.CourseRequest;
import cn.edu.zjut.back.entity.User; // 添加User类导入
import cn.edu.zjut.back.mapper.CourseMapper;
import cn.edu.zjut.back.mapper.CourseRequestMapper;
import cn.edu.zjut.back.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程请求服务
 */
@Slf4j
@Service
public class CourseRequestService {
    
    @Autowired
    private CourseRequestMapper courseRequestMapper;
    
    @Autowired
    private CourseMapper courseMapper;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private UserService userService; // 注入UserService以获取教师姓名
    
    /**
     * 提交课程请求（创建或删除）
     */
    public Result<CourseRequest> submitRequest(CourseRequest request) {
        try {
            // 新增：根据teacherId查询用户信息并设置teacherName
            if (request.getTeacherId() != null) {
                Result<User> userResult = userService.getUserInfoWithDetails(request.getTeacherId());
                if (userResult.getCode() == 200) { // 使用code判断成功状态
                    User teacher = userResult.getData();
                    String nameToUse = teacher.getRealName() != null ? teacher.getRealName() : teacher.getUsername();
                    request.setTeacherName(nameToUse);
                } else {
                    log.warn("无法获取教师信息，ID: {}", request.getTeacherId());
                    return Result.error("无法获取教师姓名，请检查教师账号是否存在");
                }
            }
            
            request.setStatus("pending");
            courseRequestMapper.insert(request);
            log.info("教师 {} 提交了{}请求，ID: {}", request.getTeacherName(), 
                    request.getRequestType().equals("create") ? "创建课程" : "删除课程", 
                    request.getId());
            return Result.success("请求已提交，等待管理员审核", request);
        } catch (Exception e) {
            log.error("提交课程请求失败", e);
            return Result.error("提交请求失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取教师的请求列表
     */
    public Result<List<CourseRequest>> getTeacherRequests(Long teacherId) {
        try {
            List<CourseRequest> requests = courseRequestMapper.findByTeacherId(teacherId);
            return Result.success(requests);
        } catch (Exception e) {
            log.error("获取教师请求列表失败", e);
            return Result.error("获取请求列表失败");
        }
    }
    
    /**
     * 获取待审核的请求列表
     */
    public Result<List<CourseRequest>> getPendingRequests() {
        try {
            List<CourseRequest> requests = courseRequestMapper.findByStatus("pending");
            return Result.success(requests);
        } catch (Exception e) {
            log.error("获取待审核请求列表失败", e);
            return Result.error("获取请求列表失败");
        }
    }
    
    /**
     * 获取所有请求（分页）
     */
    public Result<Map<String, Object>> getAllRequests(String status, String requestType, 
                                                     int page, int pageSize) {
        try {
            int offset = (page - 1) * pageSize;
            List<CourseRequest> requests = courseRequestMapper.queryWithPagination(
                    status, requestType, offset, pageSize);
            long total = courseRequestMapper.count(status, requestType);
            
            Map<String, Object> result = new HashMap<>();
            result.put("list", requests);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取请求列表失败", e);
            return Result.error("获取请求列表失败");
        }
    }
    
    /**
     * 审核请求（批准或拒绝）
     */
    @Transactional
    public Result<String> reviewRequest(Long requestId, String action, 
                                       Long adminId, String adminName, String comment) {
        try {
            CourseRequest request = courseRequestMapper.findById(requestId);
            if (request == null) {
                return Result.error("请求不存在");
            }
            
            if (!"pending".equals(request.getStatus())) {
                return Result.error("该请求已被处理");
            }
            
            String status = "approved".equals(action) ? "approved" : "rejected";
            courseRequestMapper.updateStatus(requestId, status, adminId, adminName, comment);
            
            // 如果批准，执行相应操作
            if ("approved".equals(status)) {
                if ("create".equals(request.getRequestType())) {
                    // 创建课程
                    Course course = new Course();
                    course.setName(request.getCourseName());
                    course.setCourseCode(request.getCourseCode());
                    course.setTeacherId(request.getTeacherId());
                    course.setTeacherName(request.getTeacherName());
                    course.setDescription(request.getDescription());
                    course.setSemester(request.getSemester());
                    course.setCredit(request.getCredit().doubleValue());
                    course.setCapacity(request.getCapacity());
                    course.setCategory(request.getCategory());
                    course.setStatus("approved");
                    courseMapper.insert(course);
                    log.info("管理员 {} 批准了课程创建请求，课程：{}", adminName, course.getName());
                } else if ("delete".equals(request.getRequestType())) {
                    // 删除课程
                    courseMapper.delete(request.getCourseId());
                    log.info("管理员 {} 批准了课程删除请求，课程ID：{}", adminName, request.getCourseId());
                }
                
                // 清除相关缓存
                redisUtil.delete("courses:all");
                redisUtil.delete("courses:teacher:" + request.getTeacherId());
            }
            
            String message = "approved".equals(status) ? "请求已批准" : "请求已拒绝";
            return Result.success(message);
        } catch (Exception e) {
            log.error("审核请求失败", e);
            return Result.error("审核失败：" + e.getMessage());
        }
    }
}
