package cn.edu.zjut.back.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试结果详情DTO - 完整的考试结果信息
 */
@Data
public class ExamResultDetail {
    // ========== 考试记录基本信息 ==========
    private Long studentExamId;
    private Long examPaperId;
    private Long studentId;
    private String studentName;
    private String studentNumber;
    private Integer attemptNumber;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private String status;
    private String ipAddress;
    
    // ========== 试卷信息 ==========
    private String examTitle;
    private String courseName;
    private Integer duration;           // 考试时长(分钟)
    private Integer totalScore;         // 试卷满分
    private Integer passScore;          // 及格分数
    
    // ========== 成绩信息 ==========
    private Integer studentTotalScore;      // 学生总得分
    private Integer objectiveScore;         // 客观题得分
    private Integer subjectiveScore;        // 主观题得分
    
    // ========== 批改信息 ==========
    private Long graderId;
    private String graderName;
    private LocalDateTime gradeTime;
    private String feedback;
    
    // ========== 答题详情列表 ==========
    private List<AnswerDetail> answers;
    
    /**
     * 单个答题详情
     */
    @Data
    public static class AnswerDetail {
        // 答题记录信息
        private Long answerId;
        private Integer questionOrder;
        private String studentAnswer;
        private Boolean isCorrect;
        private Integer score;
        private LocalDateTime answerTime;
        
        // 题目信息
        private Long questionId;
        private String content;
        private String questionType;
        private String options;             // JSON字符串
        private String correctAnswer;
        private String analysis;
        private Integer questionScore;      // 题目分值
    }
}
