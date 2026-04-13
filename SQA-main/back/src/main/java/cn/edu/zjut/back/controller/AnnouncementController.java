package cn.edu.zjut.back.controller;

import cn.edu.zjut.back.entity.Announcement;
import cn.edu.zjut.back.service.AnnouncementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公告通知控制器（公共访问）
 */
@RestController
@RequestMapping("/api/announcements")
@CrossOrigin
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    /**
     * 根据角色获取已发布的公告（首页展示）
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<Map<String, Object>> getAnnouncementsByRole(
            @PathVariable String role,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<Announcement> announcements = announcementService.getPublishedAnnouncementsByRole(role, limit);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", announcements);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.ok(error);
        }
    }

    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAnnouncementDetail(@PathVariable Long id) {
        try {
            Announcement announcement = announcementService.getById(id);
            Map<String, Object> response = new HashMap<>();
            if (announcement != null) {
                response.put("success", true);
                response.put("data", announcement);
            } else {
                response.put("success", false);
                response.put("message", "公告不存在");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.ok(error);
        }
    }
}
