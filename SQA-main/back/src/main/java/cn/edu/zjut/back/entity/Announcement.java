package cn.edu.zjut.back.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 公告通知实体类
 */
@Data
public class Announcement {
    private Long id;
    private String title;
    private String content;
    private String type; // system, course, exam, other
    private String priority; // low, medium, high
    private String targetRole; // all, student, teacher, admin
    private Long publisherId;
    private String publisherName;
    private LocalDateTime publishTime;
    private String status; // draft, published, archived
}
