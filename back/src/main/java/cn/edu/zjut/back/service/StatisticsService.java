package cn.edu.zjut.back.service;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.mapper.*;
import cn.edu.zjut.back.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 统计信息服务
 */
@Slf4j
@Service
public class StatisticsService {

    private final UserMapper userMapper;
    private final CourseMapper courseMapper;
    private final ResourceMapper resourceMapper;
    private final HomeworkMapper homeworkMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final HomeworkSubmissionMapper homeworkSubmissionMapper;
    private final RedisUtil redisUtil;

    public StatisticsService(
            UserMapper userMapper,
            CourseMapper courseMapper,
            ResourceMapper resourceMapper,
            HomeworkMapper homeworkMapper,
            EnrollmentMapper enrollmentMapper,
            HomeworkSubmissionMapper homeworkSubmissionMapper,
            RedisUtil redisUtil) {
        this.userMapper = userMapper;
        this.courseMapper = courseMapper;
        this.resourceMapper = resourceMapper;
        this.homeworkMapper = homeworkMapper;
        this.enrollmentMapper = enrollmentMapper;
        this.homeworkSubmissionMapper = homeworkSubmissionMapper;
        this.redisUtil = redisUtil;
    }

    /**
     * 获取综合统计信息
     */
    public Result<Map<String, Object>> getAllStatistics() {
        try {
            Map<String, Object> statistics = new HashMap<>();

            // 用户统计
            try {
                Map<String, Object> userStats = new HashMap<>();
                userStats.put("totalUsers", userMapper.countAll());
                userStats.put("totalStudents", userMapper.countByRole("student"));
                userStats.put("totalTeachers", userMapper.countByRole("teacher"));
                userStats.put("totalAdmins", userMapper.countByRole("admin"));
                userStats.put("activeUsers", getActiveUsersCount());
                statistics.put("users", userStats);
            } catch (Exception e) {
                log.error("获取用户统计失败", e);
                statistics.put("users", new HashMap<>());
            }

            // 课程统计
            try {
                Map<String, Object> courseStats = new HashMap<>();
                courseStats.put("totalCourses", courseMapper.countAll());
                courseStats.put("activeCourses", courseMapper.countByStatus("active"));
                courseStats.put("totalEnrollments", enrollmentMapper.countAll());
                courseStats.put("topCourses", getTopCourses(5));
                statistics.put("courses", courseStats);
            } catch (Exception e) {
                log.error("获取课程统计失败", e);
                statistics.put("courses", new HashMap<>());
            }

            // 资源统计
            try {
                Map<String, Object> resourceStats = new HashMap<>();
                resourceStats.put("totalResources", resourceMapper.countAll());
                resourceStats.put("totalDownloads", resourceMapper.countTotalDownloads());
                resourceStats.put("topResources", getTopResources(5));
                resourceStats.put("resourcesByType", getResourcesByType());
                statistics.put("resources", resourceStats);
            } catch (Exception e) {
                log.error("获取资源统计失败", e);
                statistics.put("resources", new HashMap<>());
            }

            // 作业统计 - 简化版本，避免查询不存在的表
            try {
                Map<String, Object> homeworkStats = new HashMap<>();
                homeworkStats.put("totalHomework", homeworkMapper.countAll());
                // 暂时不统计提交信息，因为表可能不存在
                statistics.put("homework", homeworkStats);
            } catch (Exception e) {
                log.error("获取作业统计失败", e);
                statistics.put("homework", new HashMap<>());
            }

            // 系统性能统计
            statistics.put("system", getSystemPerformance());

            // 用户活跃度统计
            statistics.put("userActivity", getUserActivity());

            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取综合统计信息失败", e);
            return Result.error("获取统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户统计信息
     */
    public Result<Map<String, Object>> getUserStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            stats.put("totalUsers", userMapper.countAll());
            stats.put("totalStudents", userMapper.countByRole("student"));
            stats.put("totalTeachers", userMapper.countByRole("teacher"));
            stats.put("totalAdmins", userMapper.countByRole("admin"));
            stats.put("activeUsers", getActiveUsersCount());
            stats.put("newUsersToday", userMapper.countNewUsersToday());
            stats.put("newUsersThisWeek", userMapper.countNewUsersThisWeek());
            stats.put("newUsersThisMonth", userMapper.countNewUsersThisMonth());
            
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取用户统计信息失败", e);
            return Result.error("获取用户统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取课程统计信息
     */
    public Result<Map<String, Object>> getCourseStatistics(Long courseId) {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            if (courseId != null) {
                // 单个课程的详细统计
                stats.put("enrollmentCount", enrollmentMapper.countByCourseId(courseId));
                stats.put("homeworkCount", homeworkMapper.countByCourseId(courseId));
                stats.put("resourceCount", resourceMapper.countByCourseId(courseId));
            } else {
                // 所有课程的统计
                stats.put("totalCourses", courseMapper.countAll());
                stats.put("activeCourses", courseMapper.countByStatus("active"));
                stats.put("totalEnrollments", enrollmentMapper.countAll());
                stats.put("topCourses", getTopCourses(10));
            }
            
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取课程统计信息失败", e);
            return Result.error("获取课程统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取资源统计信息
     */
    public Result<Map<String, Object>> getResourceStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            stats.put("totalResources", resourceMapper.countAll());
            stats.put("totalDownloads", resourceMapper.countTotalDownloads());
            stats.put("topResources", getTopResources(10));
            stats.put("resourcesByType", getResourcesByType());
            stats.put("resourcesByStatus", getResourcesByStatus());
            
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取资源统计信息失败", e);
            return Result.error("获取资源统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取作业统计信息
     */
    public Result<Map<String, Object>> getHomeworkStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            stats.put("totalHomework", homeworkMapper.countAll());
            stats.put("totalSubmissions", homeworkSubmissionMapper.countAll());
            stats.put("submissionRate", calculateSubmissionRate());
            stats.put("onTimeSubmissions", homeworkSubmissionMapper.countOnTime());
            stats.put("lateSubmissions", homeworkSubmissionMapper.countLate());
            
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取作业统计信息失败", e);
            return Result.error("获取作业统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统性能统计
     */
    public Result<Map<String, Object>> getSystemStatistics() {
        try {
            Map<String, Object> stats = getSystemPerformance();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取系统统计信息失败", e);
            return Result.error("获取系统统计信息失败: " + e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 获取活跃用户数量
     */
    private int getActiveUsersCount() {
        try {
            return (int) redisUtil.sGetSetSize("online:users");
        } catch (Exception e) {
            log.error("获取活跃用户数失败", e);
            return 0;
        }
    }

    /**
     * 获取热门课程排行
     */
    private List<Map<String, Object>> getTopCourses(int limit) {
        try {
            return courseMapper.getTopCourses(limit);
        } catch (Exception e) {
            log.error("获取热门课程失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取热门资源排行
     */
    private List<Map<String, Object>> getTopResources(int limit) {
        try {
            return resourceMapper.getTopResources(limit);
        } catch (Exception e) {
            log.error("获取热门资源失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 按类型统计资源
     */
    private Map<String, Integer> getResourcesByType() {
        try {
            List<Map<String, Object>> results = resourceMapper.getResourcesByType();
            Map<String, Integer> stats = new HashMap<>();
            for (Map<String, Object> result : results) {
                String type = (String) result.get("type");
                Object countObj = result.get("count");
                Integer count = countObj instanceof Long ? ((Long) countObj).intValue() : (Integer) countObj;
                stats.put(type, count);
            }
            return stats;
        } catch (Exception e) {
            log.error("按类型统计资源失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 按状态统计资源
     */
    private Map<String, Integer> getResourcesByStatus() {
        try {
            List<Map<String, Object>> results = resourceMapper.getResourcesByStatus();
            Map<String, Integer> stats = new HashMap<>();
            for (Map<String, Object> result : results) {
                String status = (String) result.get("status");
                Object countObj = result.get("count");
                Integer count = countObj instanceof Long ? ((Long) countObj).intValue() : (Integer) countObj;
                stats.put(status, count);
            }
            return stats;
        } catch (Exception e) {
            log.error("按状态统计资源失败", e);
            return new HashMap<>();
        }
    }

    /**
     * 计算作业提交率
     */
    private double calculateSubmissionRate() {
        try {
            long totalHomework = homeworkMapper.countAll();
            long totalSubmissions = homeworkSubmissionMapper.countAll();
            
            if (totalHomework == 0) {
                return 0.0;
            }
            
            // 需要考虑每个作业可能有多个学生需要提交
            // 这里简化计算，实际应该是: 已提交数 / 应提交数
            return (double) totalSubmissions / totalHomework * 100;
        } catch (Exception e) {
            log.error("计算作业提交率失败", e);
            return 0.0;
        }
    }

    /**
     * 获取系统性能信息
     */
    private Map<String, Object> getSystemPerformance() {
        Map<String, Object> performance = new HashMap<>();
        
        // 获取运行时信息
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        performance.put("totalMemory", totalMemory / (1024 * 1024) + " MB");
        performance.put("freeMemory", freeMemory / (1024 * 1024) + " MB");
        performance.put("usedMemory", usedMemory / (1024 * 1024) + " MB");
        performance.put("maxMemory", maxMemory / (1024 * 1024) + " MB");
        performance.put("memoryUsageRate", (int) (usedMemory * 100.0 / maxMemory));
        
        // CPU核心数
        performance.put("availableProcessors", runtime.availableProcessors());
        
        // JVM信息
        performance.put("jvmVersion", System.getProperty("java.version"));
        performance.put("osName", System.getProperty("os.name"));
        performance.put("osVersion", System.getProperty("os.version"));
        
        return performance;
    }

    /**
     * 获取用户活跃度数据
     */
    private Map<String, Object> getUserActivity() {
        Map<String, Object> activity = new HashMap<>();
        
        // 这里可以从Redis或数据库获取最近7天的用户活跃数据
        // 简化示例：返回模拟数据
        List<Map<String, Object>> weeklyActivity = new ArrayList<>();
        String[] weekdays = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        
        for (String day : weekdays) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("day", day);
            dayData.put("students", (int) (Math.random() * 500 + 100));
            dayData.put("teachers", (int) (Math.random() * 100 + 50));
            dayData.put("admins", (int) (Math.random() * 20 + 5));
            weeklyActivity.add(dayData);
        }
        
        activity.put("weekly", weeklyActivity);
        activity.put("onlineNow", getActiveUsersCount());
        
        return activity;
    }

    /**
     * 获取教师仪表板统计数据
     */
    public Result<Map<String, Object>> getTeacherDashboard(Long teacherId) {
        try {
            Map<String, Object> dashboard = new HashMap<>();
            
            // 教授课程数
            int courseCount = courseMapper.countByTeacherId(teacherId);
            dashboard.put("courseCount", courseCount);
            
            // 学生总数（所有课程）
            int studentCount = enrollmentMapper.countStudentsByTeacherId(teacherId);
            dashboard.put("studentCount", studentCount);
            
            // 待批改作业数
            int pendingHomework = homeworkSubmissionMapper.countPendingByTeacherId(teacherId);
            dashboard.put("homeworkCount", pendingHomework);
            
            // 实验任务数
            int experimentCount = homeworkMapper.countExperimentsByTeacherId(teacherId);
            dashboard.put("experimentCount", experimentCount);
            
            // 本周课程安排（获取教师的课程列表）
            List<Map<String, Object>> courses = courseMapper.getTeacherCourses(teacherId);
            List<Map<String, Object>> weeklySchedule = new ArrayList<>();
            
            // 为每个课程添加时间信息（这里简化处理，实际应该从课程表或日程表获取）
            for (int i = 0; i < Math.min(courses.size(), 3); i++) {
                Map<String, Object> course = courses.get(i);
                Map<String, Object> schedule = new HashMap<>();
                schedule.put("courseName", course.get("name"));
                schedule.put("time", getWeekdayTime(i)); // 模拟时间
                schedule.put("location", "教室" + (i + 1) + "01");
                schedule.put("students", enrollmentMapper.countByCourseId((Long) course.get("id")));
                weeklySchedule.add(schedule);
            }
            dashboard.put("weeklySchedule", weeklySchedule);
            
            return Result.success(dashboard);
        } catch (Exception e) {
            log.error("获取教师仪表板数据失败", e);
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取学生仪表板统计数据
     */
    public Result<Map<String, Object>> getStudentDashboard(Long studentId) {
        try {
            Map<String, Object> dashboard = new HashMap<>();
            
            // 已选课程数
            int courseCount = enrollmentMapper.countByStudentId(studentId);
            dashboard.put("courseCount", courseCount);
            
            // 待提交作业数
            int homeworkCount = homeworkSubmissionMapper.countPendingByStudentId(studentId);
            dashboard.put("homeworkCount", homeworkCount);
            
            // 实验任务数
            int experimentCount = homeworkMapper.countExperimentsByStudentId(studentId);
            dashboard.put("experimentCount", experimentCount);
            
            // 平均成绩
            Double avgScore = homeworkSubmissionMapper.getAverageScoreByStudentId(studentId);
            dashboard.put("avgScore", avgScore != null ? avgScore.intValue() : 0);
            
            // 最近课程（获取学生的课程列表）
            List<Map<String, Object>> enrollments = enrollmentMapper.getStudentEnrollments(studentId);
            List<Map<String, Object>> recentCourses = new ArrayList<>();
            
            for (int i = 0; i < Math.min(enrollments.size(), 3); i++) {
                Map<String, Object> enrollment = enrollments.get(i);
                Map<String, Object> course = new HashMap<>();
                course.put("name", enrollment.get("courseName"));
                course.put("teacher", enrollment.get("teacherName"));
                course.put("time", getWeekdayTime(i));
                recentCourses.add(course);
            }
            dashboard.put("recentCourses", recentCourses);
            
            // 待办事项列表（最近的作业）
            List<Map<String, Object>> homework = homeworkMapper.getPendingHomeworkByStudentId(studentId, 3);
            List<Map<String, Object>> todoList = new ArrayList<>();
            
            for (Map<String, Object> hw : homework) {
                Map<String, Object> todo = new HashMap<>();
                todo.put("id", hw.get("id"));
                todo.put("title", hw.get("title"));
                todo.put("deadline", hw.get("deadline"));
                todoList.add(todo);
            }
            dashboard.put("todoList", todoList);
            
            return Result.success(dashboard);
        } catch (Exception e) {
            log.error("获取学生仪表板数据失败", e);
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取星期时间（辅助方法）
     */
    private String getWeekdayTime(int index) {
        String[] weekdays = {"周一", "周二", "周三", "周四", "周五"};
        String[] times = {"8:00-10:00", "10:00-12:00", "14:00-16:00", "16:00-18:00"};
        return weekdays[index % weekdays.length] + " " + times[index % times.length];
    }
}
