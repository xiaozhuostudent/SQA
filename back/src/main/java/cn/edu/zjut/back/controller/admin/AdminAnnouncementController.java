package cn.edu.zjut.back.controller.admin;

import cn.edu.zjut.back.dto.AnnouncementQueryDTO;
import cn.edu.zjut.back.entity.Announcement;
import cn.edu.zjut.back.service.AnnouncementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员-公告管理控制器
 */
@RestController
@RequestMapping("/api/admin/announcements")
@CrossOrigin
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;

    public AdminAnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    /**
     * 分页查询公告列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAnnouncementList(AnnouncementQueryDTO query) {
        try {
            Map<String, Object> result = announcementService.queryAnnouncements(query);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", result);
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

    /**
     * 创建公告
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAnnouncement(@RequestBody Announcement announcement) {
        try {
            Long id = announcementService.createAnnouncement(announcement);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "创建成功");
            response.put("data", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "创建失败: " + e.getMessage());
            return ResponseEntity.ok(error);
        }
    }

    /**
     * 更新公告
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAnnouncement(
            @PathVariable Long id,
            @RequestBody Announcement announcement) {
        try {
            announcement.setId(id);
            announcementService.updateAnnouncement(announcement);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.ok(error);
        }
    }

    /**
     * 发布公告
     */
    @PutMapping("/{id}/publish")
    public ResponseEntity<Map<String, Object>> publishAnnouncement(@PathVariable Long id) {
        try {
            announcementService.publishAnnouncement(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "发布成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "发布失败: " + e.getMessage());
            return ResponseEntity.ok(error);
        }
    }

    /**
     * 归档公告
     */
    @PutMapping("/{id}/archive")
    public ResponseEntity<Map<String, Object>> archiveAnnouncement(@PathVariable Long id) {
        try {
            announcementService.archiveAnnouncement(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "归档成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "归档失败: " + e.getMessage());
            return ResponseEntity.ok(error);
        }
    }

    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAnnouncement(@PathVariable Long id) {
        try {
            announcementService.deleteAnnouncement(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.ok(error);
        }
    }
}
