package cn.edu.zjut.back.controller.student;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.service.StatisticsService;
import cn.edu.zjut.back.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 学生统计控制器
 */
@RestController
@RequestMapping("/api/student/statistics")
public class StudentStatisticsController {

    private final StatisticsService statisticsService;
    private final JwtUtil jwtUtil;

    public StudentStatisticsController(StatisticsService statisticsService, JwtUtil jwtUtil) {
        this.statisticsService = statisticsService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 获取学生仪表板统计数据
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getStudentDashboard(HttpServletRequest request) {
        try {
            Long studentId = jwtUtil.getUserIdFromRequest(request);
            return statisticsService.getStudentDashboard(studentId);
        } catch (Exception e) {
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }
}
