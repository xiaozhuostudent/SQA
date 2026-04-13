package cn.edu.zjut.back.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 教学资源实体类
 */
@Data
public class Resource {
    private Long id;
    private Long courseId;  // course_id
    private String courseName;  // course_name
    private String name;  // name (不是title)
    private String type;  // ENUM('document', 'video', 'code', 'other')
    private String description;
    private String fileUrl;  // file_url
    private String previewUrl;  // preview_url (PDF预览文件URL)
    private Long fileSize;  // file_size
    private Long uploaderId;  // uploader_id
    private String uploaderName;  // uploader_name
    private Integer downloadCount;  // download_count
    private String tags;  // tags
    private String status;  // ENUM('pending', 'approved', 'rejected')
    private LocalDateTime createTime;
}
