package cn.edu.zjut.back.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学习进度实体类
 */
@Data
public class LearningProgress {
    private Long id;
    private Long studentId;
    private Long courseId;
    private Long resourceId;
    private String resourceType; // video, document, code, other
    private Integer progressPercent;
    private Integer durationSeconds;
    private String lastPosition;
    private Boolean isCompleted;
    private LocalDateTime completeTime;
    private LocalDateTime lastStudyTime;
}
