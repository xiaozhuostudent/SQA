package cn.edu.zjut.back.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生作业记录实体类 (tb_student_homework)
 */
@Data
public class StudentHomework {
    private Long id;
    private Long homeworkId;
    private Long studentId;
    private String studentName;
    private String studentNumber;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private BigDecimal score;
    private String status; // not_started, in_progress, submitted, graded
    private Boolean isLate;
    private String teacherComment;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
