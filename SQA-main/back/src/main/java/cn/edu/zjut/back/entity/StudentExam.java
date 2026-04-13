package cn.edu.zjut.back.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学生考试记录实体类
 */
@Data
public class StudentExam {
    private Long id;
    private Long examPaperId;
    private Long studentId;
    private String studentName;
    private String studentNumber;
    private Integer attemptNumber; // 添加尝试次数字段
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private Integer totalScore;
    private Integer objectiveScore;
    private Integer subjectiveScore;
    private String status; // not_started, in_progress, submitted, graded
    private String ipAddress;
    private String answerSheet; // JSON字符串
    private Long graderId;
    private String graderName;
    private LocalDateTime gradeTime;
    private String feedback;
    
    // 添加试卷信息字段
    private String title;       // 考试名称
    private String courseName;  // 课程名称
    private Integer duration;   // 考试时长（分钟）
    private Integer passScore;  // 及格分
}