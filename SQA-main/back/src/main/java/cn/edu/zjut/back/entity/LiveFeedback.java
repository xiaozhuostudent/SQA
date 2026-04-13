package cn.edu.zjut.back.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 直播反馈实体
 */
@Data
public class LiveFeedback {
    private Long id;
    private Long liveStreamId;
    private Long userId;
    private String userName;
    private String userRole;
    /**
     * 反馈阶段：live（直播中）/post（直播结束后）
     */
    private String phase;
    private String content;
    private LocalDateTime createdAt;
}
