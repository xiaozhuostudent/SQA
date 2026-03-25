package cn.edu.zjut.back.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 直播聊天消息实体类
 */
@Data
public class LiveChatMessage {
    private Long id;
    private Long liveStreamId;
    private Long userId;
    private String userName;
    private String userRole; // teacher/student
    private String content;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
