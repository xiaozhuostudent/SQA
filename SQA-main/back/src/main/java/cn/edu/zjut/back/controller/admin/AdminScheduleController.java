package cn.edu.zjut.back.controller.admin;

import cn.edu.zjut.back.dto.ClassScheduleVO;
import cn.edu.zjut.back.dto.ScheduleQueryDTO;
import cn.edu.zjut.back.entity.Schedule;
import cn.edu.zjut.back.service.AdminScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员-排课管理Controller
 */
@RestController
@RequestMapping("/api/admin/schedules")
public class AdminScheduleController {
    
    @Autowired
    private AdminScheduleService scheduleService;
    
    /**
     * 获取班级课表
     */
    @GetMapping("/class-schedule")
    public ResponseEntity<Map<String, Object>> getClassSchedule(
            @RequestParam String className,
            @RequestParam String semester) {
        Map<String, Object> response = new HashMap<>();
        try {
            ClassScheduleVO schedule = scheduleService.getClassSchedule(className, semester);
            response.put("success", true);
            response.put("data", schedule);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * 查询排课列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> querySchedules(ScheduleQueryDTO query) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Schedule> schedules = scheduleService.querySchedules(query);
            response.put("success", true);
            response.put("data", schedules);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * 根据ID获取排课
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getSchedule(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Schedule schedule = scheduleService.getScheduleById(id);
            response.put("success", true);
            response.put("data", schedule);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * 添加排课
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addSchedule(@RequestBody Schedule schedule) {
        Map<String, Object> response = new HashMap<>();
        try {
            Schedule newSchedule = scheduleService.addSchedule(schedule);
            response.put("success", true);
            response.put("message", "排课添加成功");
            response.put("data", newSchedule);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 更新排课
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateSchedule(
            @PathVariable Long id,
            @RequestBody Schedule schedule) {
        Map<String, Object> response = new HashMap<>();
        try {
            schedule.setId(id);
            Schedule updatedSchedule = scheduleService.updateSchedule(schedule);
            response.put("success", true);
            response.put("message", "排课更新成功");
            response.put("data", updatedSchedule);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除排课
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSchedule(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            scheduleService.deleteSchedule(id);
            response.put("success", true);
            response.put("message", "排课删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取所有班级列表
     */
    @GetMapping("/classes")
    public ResponseEntity<Map<String, Object>> getAllClasses() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<String> classes = scheduleService.getAllClasses();
            response.put("success", true);
            response.put("data", classes);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取所有学期列表
     */
    @GetMapping("/semesters")
    public ResponseEntity<Map<String, Object>> getAllSemesters() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<String> semesters = scheduleService.getAllSemesters();
            response.put("success", true);
            response.put("data", semesters);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}
