package cn.edu.zjut.back.service;

import cn.edu.zjut.back.entity.FilePreview;
import cn.edu.zjut.back.mapper.FilePreviewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件预览服务类
 */
@Service
public class FilePreviewService {
    
    @Autowired
    private FilePreviewMapper filePreviewMapper;
    
    /**
     * 记录文件预览
     */
    @Transactional
    public FilePreview recordPreview(Long viewerId, String viewerName, Long resourceId, 
                                      String resourceType, String fileUrl, String fileType, 
                                      Long fileSize, String previewType) {
        // 检查是否已有预览记录
        FilePreview existing = filePreviewMapper.findByViewerAndResource(viewerId, resourceId);
        if (existing != null) {
            // 更新已有记录
            existing.setViewCount(existing.getViewCount() + 1);
            existing.setLastViewTime(LocalDateTime.now());
            filePreviewMapper.update(existing);
            return existing;
        }
        
        // 创建新记录
        FilePreview filePreview = new FilePreview();
        filePreview.setViewerId(viewerId);
        filePreview.setViewerName(viewerName);
        filePreview.setResourceId(resourceId);
        filePreview.setResourceType(resourceType);
        filePreview.setFileUrl(fileUrl);
        filePreview.setFileType(fileType);
        filePreview.setFileSize(fileSize);
        filePreview.setPreviewType(previewType != null ? previewType : "online");
        filePreview.setConversionStatus("pending");
        filePreview.setViewCount(1);
        filePreview.setLastViewTime(LocalDateTime.now());
        
        filePreviewMapper.insert(filePreview);
        return filePreview;
    }
    
    /**
     * 更新转换状态
     */
    public void updateConversionStatus(Long id, String status, String previewUrl) {
        FilePreview filePreview = filePreviewMapper.findById(id);
        if (filePreview != null) {
            filePreview.setConversionStatus(status);
            filePreview.setPreviewUrl(previewUrl);
            filePreview.setConversionTime(LocalDateTime.now());
            filePreviewMapper.update(filePreview);
        }
    }
    
    /**
     * 获取用户预览记录
     */
    public List<FilePreview> getViewerPreviews(Long viewerId) {
        return filePreviewMapper.findByViewerId(viewerId);
    }
    
    /**
     * 获取资源预览记录
     */
    public FilePreview getResourcePreview(Long viewerId, Long resourceId) {
        return filePreviewMapper.findByViewerAndResource(viewerId, resourceId);
    }
}
