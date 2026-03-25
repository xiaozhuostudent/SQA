package cn.edu.zjut.back.service;

import java.util.List;
import java.util.Map;

public interface LogService {
    
    /**
     * 获取管理员操作日志
     */
    List<Map<String, Object>> getAdminLogs(Integer page, Integer size, String startDate, String endDate, String adminName, String operation, String module);
    
    /**
     * 获取管理员操作日志总数
     */
    int getAdminLogsCount(String startDate, String endDate, String adminName, String operation, String module);
    
    /**
     * 获取教师操作日志
     */
    List<Map<String, Object>> getTeacherLogs(Integer page, Integer size, String startDate, String endDate, String teacherName, String operation, String module);
    
    /**
     * 获取教师操作日志总数
     */
    int getTeacherLogsCount(String startDate, String endDate, String teacherName, String operation, String module);
    
    /**
     * 获取学生操作日志
     */
    List<Map<String, Object>> getStudentLogs(Integer page, Integer size, String startDate, String endDate, String studentName, String operation, String module);
    
    /**
     * 获取学生操作日志总数
     */
    int getStudentLogsCount(String startDate, String endDate, String studentName, String operation, String module);
    
    /**
     * 导出所有日志（根据类型）
     */
    byte[] exportLogs(String type, String startDate, String endDate);
    
    /**
     * 清理旧日志（保留指定天数）
     */
    int clearOldLogs(int days);
}
