package cn.edu.zjut.back.controller.admin;

import cn.edu.zjut.back.entity.Course;
import cn.edu.zjut.back.service.AdminCourseService;
import cn.edu.zjut.back.vo.PageResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员-课程管理Controller
 */
@RestController
@RequestMapping("/api/admin/courses")
public class AdminCourseController {
    
    @Autowired
    private AdminCourseService courseService;
    
    /**
     * 分页查询课程列表
     */
    @GetMapping
    public ResponseEntity<PageResultVO<Course>> getCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResultVO<Course> result = courseService.getCourseList(keyword, status, page, pageSize);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 获取课程详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourse(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        return ResponseEntity.ok(course);
    }
    
    /**
     * 添加课程
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addCourse(@RequestBody Course course) {
        Map<String, Object> response = new HashMap<>();
        try {
            Course newCourse = courseService.addCourse(course);
            response.put("success", true);
            response.put("message", "课程添加成功");
            response.put("data", newCourse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 更新课程信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        Map<String, Object> response = new HashMap<>();
        try {
            course.setId(id);
            Course updatedCourse = courseService.updateCourse(course);
            response.put("success", true);
            response.put("message", "课程信息更新成功");
            response.put("data", updatedCourse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除课程
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCourse(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            courseService.deleteCourse(id);
            response.put("success", true);
            response.put("message", "课程删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 审核课程
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<Map<String, Object>> approveCourse(
            @PathVariable Long id, 
            @RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String status = request.get("status");
            String reason = request.getOrDefault("reason", "");
            courseService.approveCourse(id, status, reason);
            response.put("success", true);
            response.put("message", "审核成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 调整课程容量
     */
    @PutMapping("/{id}/capacity")
    public ResponseEntity<Map<String, Object>> updateCapacity(
            @PathVariable Long id, 
            @RequestBody Map<String, Integer> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer capacity = request.get("capacity");
            courseService.updateCapacity(id, capacity);
            response.put("success", true);
            response.put("message", "容量调整成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
