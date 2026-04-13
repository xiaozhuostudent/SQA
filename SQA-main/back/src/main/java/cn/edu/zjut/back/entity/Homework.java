package cn.edu.zjut.back.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 作业实体类 (tb_homework) - 重新设计支持题库选题
 */
@Data
public class Homework {
    private Long id;
    private Long courseId;
    private String courseName;
    private String title;
    private String description;
    private Integer totalScore;
    private LocalDateTime deadline;
    private Boolean allowLateSubmission;
    private Integer latePenalty;
    private Boolean showAnswer;
    private Long creatorId;
    private String creatorName;
    private String status; // draft, published, closed, archived
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
