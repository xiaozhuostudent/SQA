package cn.edu.zjut.back.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 讨论回复实体类 (tb_discussion_reply)
 */
@Data
public class DiscussionReply {
    private Long id;
    private Long discussionId;  // discussion_id
    private Long parentId;  // parent_id
    private String content;
    private Long authorId;  // author_id
    private String authorName;  // author_name
    private String authorRole;  // ENUM('student', 'teacher', 'admin')
    private Boolean isAccepted;  // is_accepted (TINYINT(1))
    private Integer likeCount;  // like_count
    private String status;  // ENUM('active', 'hidden', 'deleted')
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
