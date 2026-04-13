package cn.edu.zjut.back.controller.admin;

import cn.edu.zjut.back.annotation.RequirePermission;
import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.entity.Resource;
import cn.edu.zjut.back.service.ResourceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员资源管理控制器
 */
@RestController
@RequestMapping("/api/admin/resources")
public class AdminResourceController {

    private final ResourceService resourceService;

    public AdminResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    /**
     * 获取所有资源列表(分页)
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> getResourceList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return resourceService.getResourceList(page, pageSize, type, keyword);
    }

    /**
     * 审核资源
     */
    @RequirePermission(level = 2, permissions = {"resource:approve"}, description = "审核资源")
    @PostMapping("/approve/{id}")
    public Result<String> approveResource(@PathVariable Long id) {
        return resourceService.approveResource(id);
    }

    /**
     * 拒绝资源
     */
    @RequirePermission(level = 2, permissions = {"resource:approve"}, description = "拒绝资源")
    @PostMapping("/reject/{id}")
    public Result<String> rejectResource(@PathVariable Long id, @RequestParam String reason) {
        return resourceService.rejectResource(id, reason);
    }

    /**
     * 删除资源
     */
    @RequirePermission(level = 2, permissions = {"resource:delete"}, description = "删除资源")
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteResource(@PathVariable Long id) {
        return resourceService.deleteResource(id);
    }

    /**
     * 批量审核
     */
    @PostMapping("/batch-approve")
    public Result<String> batchApprove(@RequestBody List<Long> ids) {
        return resourceService.batchApprove(ids);
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/batch-delete")
    public Result<String> batchDelete(@RequestBody List<Long> ids) {
        return resourceService.batchDelete(ids);
    }

    /**
     * 获取存储统计
     */
    @GetMapping("/storage-stats")
    public Result<Map<String, Object>> getStorageStats() {
        return resourceService.getStorageStats();
    }

    /**
     * 获取资源统计（按类型）
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getResourceStats() {
        return resourceService.getResourceStats();
    }
}
