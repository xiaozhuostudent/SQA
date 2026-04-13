package cn.edu.zjut.back.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生作业答题记录实体类 (tb_student_homework_answer)
 */
@Data
public class StudentHomeworkAnswer {
    private Long id;
    private Long studentHomeworkId;
    private Long questionId;
    private String studentAnswer;
    private Boolean isCorrect;
    private BigDecimal score;
    private String teacherComment;
    private LocalDateTime answerTime;
    private BigDecimal aiScore;
    private String aiFeedback;
}
