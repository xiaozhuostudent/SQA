package cn.edu.zjut.back.controller.admin;

import cn.edu.zjut.back.dto.OperationLogQueryDTO;
import cn.edu.zjut.back.entity.OperationLog;
import cn.edu.zjut.back.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员-操作日志Controller
 */
@RestController
@RequestMapping("/api/admin/logs")
public class AdminOperationLogController {

    @Autowired
    private OperationLogService logService;

    /**
     * 分页查询操作日志
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> queryLogs(OperationLogQueryDTO query) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> result = logService.queryLogs(query);
            response.put("success", true);
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取日志详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getLogDetail(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            OperationLog log = logService.getLogById(id);
            if (log == null) {
                response.put("success", false);
                response.put("message", "日志不存在");
                return ResponseEntity.badRequest().body(response);
            }
            response.put("success", true);
            response.put("data", log);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 批量删除日志
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchDeleteLogs(@RequestBody Map<String, List<Long>> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Long> ids = request.get("ids");
            if (ids == null || ids.isEmpty()) {
                response.put("success", false);
                response.put("message", "请选择要删除的日志");
                return ResponseEntity.badRequest().body(response);
            }
            logService.batchDeleteLogs(ids);
            response.put("success", true);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 清空所有日志
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAllLogs() {
        Map<String, Object> response = new HashMap<>();
        try {
            logService.clearAllLogs();
            response.put("success", true);
            response.put("message", "清空成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取操作类型统计
     */
    @GetMapping("/stats/operations")
    public ResponseEntity<Map<String, Object>> getOperationStats() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> stats = logService.getOperationStats();
            response.put("success", true);
            response.put("data", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取用户操作统计
     */
    @GetMapping("/stats/users")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Map<String, Object>> stats = logService.getUserStats();
            response.put("success", true);
            response.put("data", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
