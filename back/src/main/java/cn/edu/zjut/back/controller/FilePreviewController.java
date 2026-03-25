package cn.edu.zjut.back.controller;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.entity.FilePreview;
import cn.edu.zjut.back.service.FilePreviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 文件预览Controller
 */
@RestController
@RequestMapping("/api/file")
@CrossOrigin
public class FilePreviewController {
    
    @Autowired
    private FilePreviewService filePreviewService;
    
    /**
     * 记录文件预览
     */
    @PostMapping("/preview")
    public Result<FilePreview> recordPreview(@RequestBody Map<String, Object> params) {
        try {
            Long viewerId = Long.valueOf(params.get("viewerId").toString());
            String viewerName = params.get("viewerName").toString();
            Long resourceId = Long.valueOf(params.get("resourceId").toString());
            String resourceType = params.get("resourceType").toString();
            String fileUrl = params.get("fileUrl").toString();
            String fileType = params.get("fileType").toString();
            Long fileSize = Long.valueOf(params.get("fileSize").toString());
            String previewType = params.get("previewType") != null ? 
                params.get("previewType").toString() : "online";
            
            FilePreview preview = filePreviewService.recordPreview(viewerId, viewerName, resourceId, 
                resourceType, fileUrl, fileType, fileSize, previewType);
            return Result.success(preview);
        } catch (Exception e) {
            return Result.error("记录预览失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新转换状态
     */
    @PostMapping("/conversion/status")
    public Result<String> updateStatus(@RequestBody Map<String, Object> params) {
        try {
            Long id = Long.valueOf(params.get("id").toString());
            String status = params.get("status").toString();
            String previewUrl = params.get("previewUrl") != null ? 
                params.get("previewUrl").toString() : null;
            
            filePreviewService.updateConversionStatus(id, status, previewUrl);
            return Result.success("状态更新成功");
        } catch (Exception e) {
            return Result.error("状态更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户预览记录
     */
    @GetMapping("/previews/{viewerId}")
    public Result<List<FilePreview>> getViewerPreviews(@PathVariable Long viewerId) {
        try {
            List<FilePreview> previews = filePreviewService.getViewerPreviews(viewerId);
            return Result.success(previews);
        } catch (Exception e) {
            return Result.error("获取预览记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取资源预览信息
     */
    @GetMapping("/preview/{viewerId}/{resourceId}")
    public Result<FilePreview> getResourcePreview(@PathVariable Long viewerId, 
                                                    @PathVariable Long resourceId) {
        try {
            FilePreview preview = filePreviewService.getResourcePreview(viewerId, resourceId);
            return Result.success(preview);
        } catch (Exception e) {
            return Result.error("获取预览信息失败: " + e.getMessage());
        }
    }
}
