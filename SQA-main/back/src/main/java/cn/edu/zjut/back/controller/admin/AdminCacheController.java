package cn.edu.zjut.back.controller.admin;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.entity.Announcement;
import cn.edu.zjut.back.entity.Course;
import cn.edu.zjut.back.entity.User;
import cn.edu.zjut.back.mapper.AnnouncementMapper;
import cn.edu.zjut.back.mapper.CourseMapper;
import cn.edu.zjut.back.mapper.UserMapper;
import cn.edu.zjut.back.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 缓存管理控制器
 */
@RestController
@RequestMapping("/api/admin/cache")
public class AdminCacheController {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Autowired
    private UserMapper userMapper;

    private static final int CACHE_EXPIRE_SECONDS = 1800; // 30分钟

    /**
     * 预热所有缓存
     */
    @PostMapping("/warmup")
    public Result<Map<String, Object>> warmupCache() {
        Map<String, Object> result = new HashMap<>();
        int count = 0;

        try {
            // 1. 预热课程数据
            List<Course> allCourses = courseMapper.findAll();
            if (allCourses != null && !allCourses.isEmpty()) {
                redisUtil.set("courses:all", allCourses, CACHE_EXPIRE_SECONDS);
                count++;

                // 按教师分组缓存
                Map<Long, List<Course>> coursesByTeacher = new HashMap<>();
                for (Course course : allCourses) {
                    if (course.getTeacherId() != null) {
                        coursesByTeacher.computeIfAbsent(course.getTeacherId(), k -> new java.util.ArrayList<>()).add(course);
                    }
                }
                for (Map.Entry<Long, List<Course>> entry : coursesByTeacher.entrySet()) {
                    redisUtil.set("courses:teacher:" + entry.getKey(), entry.getValue(), CACHE_EXPIRE_SECONDS);
                    count++;
                }
            }

            // 2. 预热公告数据
            String[] roles = {"admin", "teacher", "student"};
            for (String role : roles) {
                List<Announcement> announcements = announcementMapper.getPublishedAnnouncementsByRole(role, 10);
                if (announcements != null && !announcements.isEmpty()) {
                    redisUtil.set("announcements:role:" + role + ":10", announcements, CACHE_EXPIRE_SECONDS);
                    count++;
                }
            }

            // 3. 预热用户数据（只缓存活跃用户的基本信息）
            List<User> activeUsers = userMapper.findAll();
            if (activeUsers != null) {
                for (User user : activeUsers) {
                    if ("active".equals(user.getStatus())) {
                        redisUtil.set("user:" + user.getId(), user, CACHE_EXPIRE_SECONDS);
                        count++;
                    }
                }
            }

            result.put("success", true);
            result.put("cachedItems", count);
            result.put("message", "成功预热 " + count + " 个缓存项");
            return Result.success(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("cachedItems", count);
            result.put("error", e.getMessage());
            return Result.error("预热失败: " + e.getMessage());
        }
    }

    /**
     * 清除所有缓存
     */
    @DeleteMapping("/clear-all")
    public Result<String> clearAllCache() {
        try {
            redisUtil.deleteByPattern("courses:*");
            redisUtil.deleteByPattern("announcements:*");
            redisUtil.deleteByPattern("user:*");
            redisUtil.deleteByPattern("discussion:*");
            redisUtil.deleteByPattern("session:*");
            redisUtil.delete("online:users");
            return Result.success("已清除所有缓存");
        } catch (Exception e) {
            return Result.error("清除失败: " + e.getMessage());
        }
    }

    /**
     * 获取在线用户数
     */
    @GetMapping("/online-users")
    public Result<Map<String, Object>> getOnlineUsers() {
        try {
            Long count = redisUtil.sGetSetSize("online:users");
            Map<String, Object> data = new HashMap<>();
            data.put("count", count);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("获取失败: " + e.getMessage());
        }
    }
}
