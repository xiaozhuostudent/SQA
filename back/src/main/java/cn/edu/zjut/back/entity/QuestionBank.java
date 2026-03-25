package cn.edu.zjut.back.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 题库实体类
 */
@Data
public class QuestionBank {
    private Long id;
    private Long courseId;
    private String courseName; // 课程名称（关联查询获取）
    private String questionType; // single_choice, multiple_choice, true_false, fill_blank, short_answer, programming
    private String difficulty; // easy, medium, hard
    private String content;
    private String options; // JSON字符串
    private String answer;
    private String explanation;
    private Integer score;
    private String tags;
    private String knowledgePoints;
    private Integer usageCount;
    private Long creatorId;
    private String creatorName;
    private String status; // active, inactive
    private Integer isVisible; // 学生是否可见(1=可见,0=不可见,教师全部可见)
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
