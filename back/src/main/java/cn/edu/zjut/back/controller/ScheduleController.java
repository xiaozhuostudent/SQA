package cn.edu.zjut.back.controller;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.entity.Schedule;
import cn.edu.zjut.back.service.ScheduleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课表控制器
 */
@CrossOrigin
@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {
    
    private final ScheduleService scheduleService;
    
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }
    
    /**
     * 根据课程ID获取课表
     */
    @GetMapping("/course/{courseId}")
    public Result<List<Schedule>> getScheduleByCourseId(@PathVariable Long courseId) {
        try {
            List<Schedule> schedules = scheduleService.getScheduleByCourseId(courseId);
            return Result.success(schedules);
        } catch (Exception e) {
            return Result.error("获取课表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有课表
     */
    @GetMapping("/list")
    public Result<List<Schedule>> getScheduleList() {
        try {
            List<Schedule> schedules = scheduleService.getAllSchedules();
            return Result.success(schedules);
        } catch (Exception e) {
            return Result.error("获取课表列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建课表
     */
    @PostMapping("/create")
    public Result<String> createSchedule(@RequestBody Schedule schedule) {
        try {
            scheduleService.createSchedule(schedule);
            return Result.success("创建课表成功");
        } catch (Exception e) {
            return Result.error("创建课表失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新课表
     */
    @PutMapping("/update/{id}")
    public Result<String> updateSchedule(@PathVariable Long id, @RequestBody Schedule schedule) {
        try {
            schedule.setId(id);
            scheduleService.updateSchedule(schedule);
            return Result.success("更新课表成功");
        } catch (Exception e) {
            return Result.error("更新课表失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除课表
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteSchedule(@PathVariable Long id) {
        try {
            scheduleService.deleteSchedule(id);
            return Result.success("删除课表成功");
        } catch (Exception e) {
            return Result.error("删除课表失败: " + e.getMessage());
        }
    }
}
