package cn.edu.zjut.back.controller;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.dto.CourseDTO;
import cn.edu.zjut.back.entity.Course;
import cn.edu.zjut.back.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程控制器
 */
@CrossOrigin
@RestController
@RequestMapping("/api/course")
public class CourseController {
    
    private final CourseService courseService;
    
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }
    
    /**
     * 创建课程(教师)
     * TODO: 从JWT Token中获取teacherId
     */
    @PostMapping("/create")
    public Result<String> createCourse(@Valid @RequestBody CourseDTO courseDTO, @RequestParam Long teacherId) {
        return courseService.createCourse(courseDTO, teacherId);
    }
    
    /**
     * 更新课程(教师)
     */
    @PutMapping("/update")
    public Result<String> updateCourse(@Valid @RequestBody CourseDTO courseDTO, @RequestParam Long teacherId) {
        return courseService.updateCourse(courseDTO, teacherId);
    }
    
    /**
     * 删除课程(教师)
     */
    @DeleteMapping("/delete/{courseId}")
    public Result<String> deleteCourse(@PathVariable Long courseId, @RequestParam Long teacherId) {
        return courseService.deleteCourse(courseId, teacherId);
    }
    
    /**
     * 获取教师的课程
     */
    @GetMapping("/teacher-courses")
    public Result<List<Course>> getTeacherCourses(@RequestParam Long teacherId) {
        return courseService.getTeacherCourses(teacherId);
    }
    
    /**
     * 获取学生的课程
     */
    @GetMapping("/my-courses")
    public Result<List<Course>> getStudentCourses(@RequestParam Long studentId) {
        return courseService.getStudentCourses(studentId);
    }
    
    /**
     * 获取所有课程
     */
    @GetMapping("/all")
    public Result<List<Course>> getAllCourses() {
        return courseService.getAllCourses();
    }
    
    /**
     * 获取所有课程（管理员用，包含未开放选课的）
     */
    @GetMapping("/all-for-admin")
    public Result<List<Course>> getAllCoursesForAdmin() {
        return courseService.getAllCoursesForAdmin();
    }
    
    /**
     * 更新课程选课开放状态（管理员）
     */
    @PutMapping("/selection-status/{courseId}")
    public Result<String> updateCourseSelectionStatus(
            @PathVariable Long courseId, 
            @RequestParam Boolean isOpen) {
        return courseService.updateCourseSelectionStatus(courseId, isOpen);
    }
    
    /**
     * 批量更新课程选课开放状态（管理员）
     */
    @PutMapping("/batch-selection-status")
    public Result<String> batchUpdateCourseSelectionStatus(
            @RequestBody List<Long> courseIds, 
            @RequestParam Boolean isOpen) {
        return courseService.batchUpdateCourseSelectionStatus(courseIds, isOpen);
    }
    
    /**
     * 选课(学生)
     */
    @PostMapping("/enroll/{courseId}")
    public Result<String> enrollCourse(@PathVariable Long courseId, @RequestParam Long studentId) {
        return courseService.enrollCourse(courseId, studentId);
    }
    
    /**
     * 退课(学生)
     */
    @PostMapping("/withdraw/{courseId}")
    public Result<String> withdrawCourse(@PathVariable Long courseId, @RequestParam Long studentId) {
        return courseService.withdrawCourse(courseId, studentId);
    }
}
