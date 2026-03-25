package cn.edu.zjut.back.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 在线讨论实体类 (tb_discussion)
 */
@Data
public class Discussion {
    private Long id;
    private Long courseId;  // course_id
    private String courseName;  // course_name
    private String title;
    private String content;
    private String topicType;  // ENUM('question', 'discussion', 'sharing', 'notice')
    private Long authorId;  // author_id
    private String authorName;  // author_name
    private String authorRole;  // ENUM('student', 'teacher', 'admin')
    private Integer viewCount;  // view_count
    private Integer replyCount;  // reply_count
    private Boolean isPinned;  // is_pinned (TINYINT(1))
    private Boolean isResolved;  // is_resolved (TINYINT(1))
    private String tags;
    private String status;  // ENUM('active', 'hidden', 'deleted')
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
