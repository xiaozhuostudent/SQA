package cn.edu.zjut.back.service;

import cn.edu.zjut.back.entity.Course;
import cn.edu.zjut.back.mapper.CourseMapper;
import cn.edu.zjut.back.vo.PageResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 管理员-课程管理Service
 */
@Service
public class AdminCourseService {
    
    @Autowired
    private CourseMapper courseMapper;
    
    /**
     * 获取所有课程列表（包含所有状态）
     */
    public List<Course> getAllCourses() {
        return courseMapper.findAllWithAllStatus();
    }
    
    /**
     * 分页查询课程
     */
    public PageResultVO<Course> getCourseList(String keyword, String status, Integer page, Integer pageSize) {
        int offset = (page - 1) * pageSize;
        List<Course> courses = courseMapper.queryCourseWithPagination(keyword, status, offset, pageSize);
        long total = courseMapper.countCourses(keyword, status);
        return new PageResultVO<>(courses, total, page, pageSize);
    }
    
    /**
     * 添加课程
     */
    @Transactional
    public Course addCourse(Course course) {
        // 验证课程代码唯一性
        if (courseMapper.checkCourseCodeExists(course.getCourseCode()) > 0) {
            throw new RuntimeException("课程代码已存在");
        }
        
        // 设置默认值
        if (course.getEnrolled() == null) {
            course.setEnrolled(0);
        }
        if (course.getStatus() == null) {
            course.setStatus("approved");
        }
        
        courseMapper.insert(course);
        return course;
    }
    
    /**
     * 更新课程信息
     */
    @Transactional
    public Course updateCourse(Course course) {
        Course existing = courseMapper.findById(course.getId());
        if (existing == null) {
            throw new RuntimeException("课程不存在");
        }
        
        courseMapper.update(course);
        return courseMapper.findById(course.getId());
    }
    
    /**
     * 删除课程
     */
    @Transactional
    public void deleteCourse(Long id) {
        Course course = courseMapper.findById(id);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }
        
        // 检查是否有学生选课
        if (course.getEnrolled() > 0) {
            throw new RuntimeException("该课程已有学生选课，无法删除");
        }
        
        courseMapper.delete(id);
    }
    
    /**
     * 审核课程
     */
    @Transactional
    public void approveCourse(Long id, String status, String reason) {
        Course course = courseMapper.findById(id);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }
        
        if (!"approved".equals(status) && !"rejected".equals(status)) {
            throw new RuntimeException("无效的审核状态");
        }
        
        courseMapper.updateStatus(id, status);
    }
    
    /**
     * 调整课程容量
     */
    @Transactional
    public void updateCapacity(Long id, Integer newCapacity) {
        Course course = courseMapper.findById(id);
        if (course == null) {
            throw new RuntimeException("课程不存在");
        }
        
        if (newCapacity < course.getEnrolled()) {
            throw new RuntimeException("新容量不能小于已选人数");
        }
        
        courseMapper.updateCapacity(id, newCapacity);
    }
    
    /**
     * 获取课程详情
     */
    public Course getCourseById(Long id) {
        return courseMapper.findById(id);
    }
}
