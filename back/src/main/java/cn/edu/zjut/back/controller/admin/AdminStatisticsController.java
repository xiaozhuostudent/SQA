package cn.edu.zjut.back.controller.admin;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.service.StatisticsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员统计信息控制器
 */
@RestController
@RequestMapping("/api/admin/statistics")
public class AdminStatisticsController {

    private final StatisticsService statisticsService;

    public AdminStatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * 获取综合统计信息
     */
    @GetMapping("/all")
    public Result<Map<String, Object>> getAllStatistics() {
        return statisticsService.getAllStatistics();
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/users")
    public Result<Map<String, Object>> getUserStatistics() {
        return statisticsService.getUserStatistics();
    }

    /**
     * 获取课程统计信息
     */
    @GetMapping("/courses")
    public Result<Map<String, Object>> getCourseStatistics(@RequestParam(required = false) Long courseId) {
        return statisticsService.getCourseStatistics(courseId);
    }

    /**
     * 获取资源统计信息
     */
    @GetMapping("/resources")
    public Result<Map<String, Object>> getResourceStatistics() {
        return statisticsService.getResourceStatistics();
    }

    /**
     * 获取作业统计信息
     */
    @GetMapping("/homework")
    public Result<Map<String, Object>> getHomeworkStatistics() {
        return statisticsService.getHomeworkStatistics();
    }

    /**
     * 获取系统性能统计
     */
    @GetMapping("/system")
    public Result<Map<String, Object>> getSystemStatistics() {
        return statisticsService.getSystemStatistics();
    }
}
