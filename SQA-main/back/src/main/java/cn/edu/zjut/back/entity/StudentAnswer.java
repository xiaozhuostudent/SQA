package cn.edu.zjut.back.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学生答题详情实体类
 */
@Data
public class StudentAnswer {
    private Long id;
    private Long studentExamId;
    private Long questionId;
    private Integer questionOrder;
    private String studentAnswer;
    private Boolean isCorrect;
    private Integer score;
    private LocalDateTime answerTime;
    private Integer attemptNumber; // 添加尝试次数字段
}