package cn.edu.zjut.back.controller.teacher;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.service.StatisticsService;
import cn.edu.zjut.back.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 教师统计控制器
 */
@RestController
@RequestMapping("/api/teacher/statistics")
public class TeacherStatisticsController {

    private final StatisticsService statisticsService;
    private final JwtUtil jwtUtil;

    public TeacherStatisticsController(StatisticsService statisticsService, JwtUtil jwtUtil) {
        this.statisticsService = statisticsService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 获取教师仪表板统计数据
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getTeacherDashboard(HttpServletRequest request) {
        try {
            Long teacherId = jwtUtil.getUserIdFromRequest(request);
            return statisticsService.getTeacherDashboard(teacherId);
        } catch (Exception e) {
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }
}
