package cn.edu.zjut.back.service;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.common.ResultCode;
import cn.edu.zjut.back.dto.CourseDTO;
import cn.edu.zjut.back.entity.Course;
import cn.edu.zjut.back.entity.Enrollment;
import cn.edu.zjut.back.entity.User;
import cn.edu.zjut.back.mapper.CourseMapper;
import cn.edu.zjut.back.mapper.EnrollmentMapper;
import cn.edu.zjut.back.mapper.UserMapper;
import cn.edu.zjut.back.utils.RedisUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 课程服务
 */
@Service
public class CourseService {
    
    private final CourseMapper courseMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final UserMapper userMapper;
    private final RedisUtil redisUtil;
    
    // Redis 缓存键前缀
    private static final String COURSE_CACHE_KEY = "course:";
    private static final String ALL_COURSES_CACHE_KEY = "courses:all";
    private static final String TEACHER_COURSES_CACHE_KEY = "courses:teacher:";
    private static final String STUDENT_COURSES_CACHE_KEY = "courses:student:";
    private static final int CACHE_EXPIRE_SECONDS = 1800; // 30分钟
    
    public CourseService(CourseMapper courseMapper, EnrollmentMapper enrollmentMapper, 
                        UserMapper userMapper, RedisUtil redisUtil) {
        this.courseMapper = courseMapper;
        this.enrollmentMapper = enrollmentMapper;
        this.userMapper = userMapper;
        this.redisUtil = redisUtil;
    }
    
    /**
     * 创建课程(教师)
     */
    public Result<String> createCourse(CourseDTO courseDTO, Long teacherId) {
        User teacher = userMapper.findById(teacherId);
        
        Course course = new Course();
        BeanUtils.copyProperties(courseDTO, course);
        course.setTeacherId(teacherId);
        course.setTeacherName(teacher.getRealName());
        course.setEnrolled(0);
        
        courseMapper.insert(course);
        
        // 清除相关缓存
        clearCourseCaches(teacherId);
        
        return Result.success("课程创建成功");
    }
    
    /**
     * 更新课程(教师)
     */
    public Result<String> updateCourse(CourseDTO courseDTO, Long teacherId) {
        Course existCourse = courseMapper.findById(courseDTO.getId());
        if (existCourse == null) {
            return Result.error(ResultCode.COURSE_NOT_FOUND);
        }
        
        if (!existCourse.getTeacherId().equals(teacherId)) {
            return Result.error(ResultCode.FORBIDDEN);
        }
        
        Course course = new Course();
        BeanUtils.copyProperties(courseDTO, course);
        courseMapper.update(course);
        
        // 清除相关缓存
        redisUtil.delete(COURSE_CACHE_KEY + courseDTO.getId());
        clearCourseCaches(teacherId);
        
        return Result.success("课程更新成功");
    }
    
    /**
     * 删除课程(教师)
     */
    public Result<String> deleteCourse(Long courseId, Long teacherId) {
        Course course = courseMapper.findById(courseId);
        if (course == null) {
            return Result.error(ResultCode.COURSE_NOT_FOUND);
        }
        
        if (!course.getTeacherId().equals(teacherId)) {
            return Result.error(ResultCode.FORBIDDEN);
        }
        
        courseMapper.delete(courseId);
        
        // 清除相关缓存
        redisUtil.delete(COURSE_CACHE_KEY + courseId);
        clearCourseCaches(teacherId);
        
        return Result.success("课程删除成功");
    }
    
    /**
     * 获取教师的课程
     */
    @SuppressWarnings("unchecked")
    public Result<List<Course>> getTeacherCourses(Long teacherId) {
        String cacheKey = TEACHER_COURSES_CACHE_KEY + teacherId;
        
        // 尝试从缓存获取
        Object cached = redisUtil.get(cacheKey);
        if (cached != null) {
            return Result.success((List<Course>) cached);
        }
        
        // 从数据库查询
        List<Course> courses = courseMapper.findByTeacherId(teacherId);
        
        // 写入缓存
        redisUtil.set(cacheKey, courses, CACHE_EXPIRE_SECONDS);
        
        return Result.success(courses);
    }
    
    /**
     * 获取学生的课程
     */
    @SuppressWarnings("unchecked")
    public Result<List<Course>> getStudentCourses(Long studentId) {
        String cacheKey = STUDENT_COURSES_CACHE_KEY + studentId;
        
        // 尝试从缓存获取
        Object cached = redisUtil.get(cacheKey);
        if (cached != null) {
            return Result.success((List<Course>) cached);
        }
        
        // 从数据库查询
        List<Course> courses = courseMapper.findByStudentId(studentId);
        
        // 写入缓存
        redisUtil.set(cacheKey, courses, CACHE_EXPIRE_SECONDS);
        
        return Result.success(courses);
    }
    
    /**
     * 获取所有课程
     */
    @SuppressWarnings("unchecked")
    public Result<List<Course>> getAllCourses() {
        // 尝试从缓存获取
        Object cached = redisUtil.get(ALL_COURSES_CACHE_KEY);
        if (cached != null) {
            return Result.success((List<Course>) cached);
        }
        
        // 从数据库查询 - 只返回开放选课的课程
        List<Course> courses = courseMapper.findAllOpenForSelection();
        
        // 写入缓存
        redisUtil.set(ALL_COURSES_CACHE_KEY, courses, CACHE_EXPIRE_SECONDS);
        
        return Result.success(courses);
    }
    
    /**
     * 获取所有课程（管理员用，包含未开放选课的课程）
     */
    @SuppressWarnings("unchecked")
    public Result<List<Course>> getAllCoursesForAdmin() {
        // 从数据库查询所有已批准的课程
        List<Course> courses = courseMapper.findAll();
        return Result.success(courses);
    }
    
    /**
     * 更新课程选课开放状态（仅管理员）
     */
    @Transactional
    public Result<String> updateCourseSelectionStatus(Long courseId, Boolean isOpen) {
        Course course = courseMapper.findById(courseId);
        if (course == null) {
            return Result.error(ResultCode.COURSE_NOT_FOUND);
        }
        
        int result = courseMapper.updateSelectionStatus(courseId, isOpen);
        if (result > 0) {
            // 清除缓存
            redisUtil.delete(COURSE_CACHE_KEY + courseId);
            redisUtil.delete(ALL_COURSES_CACHE_KEY);
            
            String status = isOpen ? "开放" : "关闭";
            return Result.success("课程选课状态已" + status);
        }
        
        return Result.error("更新失败");
    }
    
    /**
     * 批量更新课程选课开放状态（仅管理员）
     */
    @Transactional
    public Result<String> batchUpdateCourseSelectionStatus(List<Long> courseIds, Boolean isOpen) {
        int successCount = 0;
        for (Long courseId : courseIds) {
            int result = courseMapper.updateSelectionStatus(courseId, isOpen);
            if (result > 0) {
                successCount++;
                redisUtil.delete(COURSE_CACHE_KEY + courseId);
            }
        }
        
        // 清除全局缓存
        redisUtil.delete(ALL_COURSES_CACHE_KEY);
        
        String status = isOpen ? "开放" : "关闭";
        return Result.success(String.format("成功%s %d 门课程的选课", status, successCount));
    }
    
    /**
     * 清除课程相关缓存
     */
    private void clearCourseCaches(Long teacherId) {
        redisUtil.delete(ALL_COURSES_CACHE_KEY);
        redisUtil.delete(TEACHER_COURSES_CACHE_KEY + teacherId);
    }
    
    /**
     * 选课(学生)
     */
    @Transactional
    public Result<String> enrollCourse(Long courseId, Long studentId) {
        Course course = courseMapper.findById(courseId);
        if (course == null) {
            return Result.error(ResultCode.COURSE_NOT_FOUND);
        }
        // 检查课程是否开放选课
        if (course.getIsOpenForSelection() == null || !course.getIsOpenForSelection()) {
            return Result.error("该课程暂未开放选课");
        }

        // 检查是否已选
        Enrollment existEnrollment = enrollmentMapper.findByStudentAndCourse(studentId, courseId);
        if (existEnrollment != null) {
            if ("selected".equals(existEnrollment.getStatus())) {
                return Result.error(ResultCode.ALREADY_ENROLLED);
            } else if ("dropped".equals(existEnrollment.getStatus())) {
                // 如果之前退过课，使用乐观锁更新状态为重新选课
                int updated = enrollmentMapper.reEnrollWithOptimisticLock(studentId, courseId, existEnrollment.getVersion());
                if (updated == 0) {
                    return Result.error("选课冲突，请重试");
                }
                
                // 使用乐观锁更新课程人数
                int courseUpdated = courseMapper.incrementStudentCountWithOptimisticLock(courseId, course.getVersion());
                if (courseUpdated == 0) {
                    throw new RuntimeException("课程更新冲突，请重试");
                }
                
                // 清除相关缓存
                redisUtil.delete(COURSE_CACHE_KEY + courseId);
                redisUtil.delete(STUDENT_COURSES_CACHE_KEY + studentId);
                redisUtil.delete(ALL_COURSES_CACHE_KEY);
                
                return Result.success("选课成功");
            }
        }

        // 检查人数
        if (course.getEnrolled() >= course.getCapacity()) {
            return Result.error("课程人数已满");
        }

        User student = userMapper.findById(studentId);
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setStudentName(student.getRealName());
        enrollment.setCourseId(courseId);
        enrollment.setCourseName(course.getName());
        enrollment.setStatus("selected");
        enrollment.setVersion(0); // 新记录版本号从0开始

        enrollmentMapper.insert(enrollment);
        
        // 使用乐观锁更新课程人数
        int updated = courseMapper.incrementStudentCountWithOptimisticLock(courseId, course.getVersion());
        if (updated == 0) {
            throw new RuntimeException("选课失败：课程信息已被其他用户修改，请刷新后重试");
        }

        // 清除相关缓存
        redisUtil.delete(COURSE_CACHE_KEY + courseId);
        redisUtil.delete(STUDENT_COURSES_CACHE_KEY + studentId);
        redisUtil.delete(ALL_COURSES_CACHE_KEY);
        
        return Result.success("选课成功");
    }
    
    /**
     * 退课(学生)
     */
    @Transactional
    public Result<String> withdrawCourse(Long courseId, Long studentId) {
        Enrollment enrollment = enrollmentMapper.findByStudentAndCourse(studentId, courseId);
        if (enrollment == null || !"selected".equals(enrollment.getStatus())) {
            return Result.error("未选该课程");
        }
        
        // 使用乐观锁更新选课记录
        int updated = enrollmentMapper.withdrawWithOptimisticLock(studentId, courseId, enrollment.getVersion());
        if (updated == 0) {
            return Result.error("退课冲突，请重试");
        }
        
        // 使用乐观锁更新课程人数
        Course course = courseMapper.findById(courseId);
        int courseUpdated = courseMapper.decrementStudentCountWithOptimisticLock(courseId, course.getVersion());
        if (courseUpdated == 0) {
            throw new RuntimeException("课程更新冲突，请重试");
        }

        // 清除相关缓存
        redisUtil.delete(COURSE_CACHE_KEY + courseId);
        redisUtil.delete(STUDENT_COURSES_CACHE_KEY + studentId);
        redisUtil.delete(ALL_COURSES_CACHE_KEY);
        
        return Result.success("退课成功");
    }
}
