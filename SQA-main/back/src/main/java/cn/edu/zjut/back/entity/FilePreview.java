package cn.edu.zjut.back.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文件预览记录实体类
 */
@Data
public class FilePreview {
    private Long id;
    private Long resourceId;
    private String resourceType; // resource, homework, experiment, other
    private String fileUrl;
    private String previewUrl;
    private String fileType; // pdf/doc/ppt/xls/jpg/mp4等
    private Long fileSize;
    private String previewType; // online, download, stream
    private String conversionStatus; // pending, processing, success, failed
    private Long viewerId;
    private String viewerName;
    private Integer viewCount;
    private LocalDateTime lastViewTime;
    private LocalDateTime conversionTime;
    private LocalDateTime createTime;
}
