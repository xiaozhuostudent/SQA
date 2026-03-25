package cn.edu.zjut.back.controller.admin;

import cn.edu.zjut.back.annotation.RequirePermission;
import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.service.LogService;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 日志管理控制器
 * 只有日志管理员(权限等级4)和超级管理员(权限等级5)可以访问
 */
@CrossOrigin
@RestController
@RequestMapping("/api/admin/log")
@RequirePermission(level = 4, description = "日志管理")
public class LogController {

    @Autowired
    private LogService logService;

    /**
     * 获取管理员操作日志
     */
    @GetMapping("/admin")
    public Result getAdminLogs(
            @RequestParam(required = false) String adminName,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        List<Map<String, Object>> records = logService.getAdminLogs(
            page, size, startDate, endDate, adminName, operation, module);
        int total = logService.getAdminLogsCount(startDate, endDate, adminName, operation, module);
        
        Map<String, Object> data = Map.of(
            "records", records,
            "total", total,
            "page", page,
            "size", size
        );
        return Result.success(data);
    }

    /**
     * 获取教师操作日志
     */
    @GetMapping("/teacher")
    public Result getTeacherLogs(
            @RequestParam(required = false) String teacherName,
            @RequestParam(required = false) String teacherNumber,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        List<Map<String, Object>> records = logService.getTeacherLogs(
            page, size, startDate, endDate, teacherName, operation, module);
        int total = logService.getTeacherLogsCount(startDate, endDate, teacherName, operation, module);
        
        Map<String, Object> data = Map.of(
            "records", records,
            "total", total,
            "page", page,
            "size", size
        );
        return Result.success(data);
    }

    /**
     * 获取学生操作日志
     */
    @GetMapping("/student")
    public Result getStudentLogs(
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String studentNumber,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        List<Map<String, Object>> records = logService.getStudentLogs(
            page, size, startDate, endDate, studentName, operation, module);
        int total = logService.getStudentLogsCount(startDate, endDate, studentName, operation, module);
        
        Map<String, Object> data = Map.of(
            "records", records,
            "total", total,
            "page", page,
            "size", size
        );
        return Result.success(data);
    }

    /**
     * 导出日志
     */
    @GetMapping("/export")
    public void exportLogs(
            @RequestParam(defaultValue = "admin") String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response) throws Exception {
        
        byte[] data = logService.exportLogs(type, startDate, endDate);
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", 
            "attachment; filename=" + type + "_logs_" + System.currentTimeMillis() + ".xlsx");
        response.getOutputStream().write(data);
        response.getOutputStream().flush();
    }

    /**
     * 清理旧日志
     */
    @DeleteMapping("/clear")
    public Result clearLogs(@RequestParam(defaultValue = "90") Integer days) {
        int deletedCount = logService.clearOldLogs(days);
        return Result.success(Map.of("deletedCount", deletedCount));
    }
}
