package cn.edu.zjut.back.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 直播词云数据实体类
 */
@Data
public class LiveWordCloud {
    private Long id;
    private Long liveStreamId;
    private String wordData; // JSON格式的词云数据
    private Integer totalMessages;
    private Integer totalWords;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
