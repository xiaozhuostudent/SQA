package cn.edu.zjut.back.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学生答题详情 DTO
 * 包含题目信息和学生答案
 */
@Data
public class StudentAnswerDetail {
    private Long id;
    private Long studentExamId;
    private Long questionId;
    private Integer questionOrder;
    
    // 学生答案信息
    private String studentAnswer;
    private Boolean isCorrect;
    private Integer scoreObtained;  // 得分
    
    // 题目信息
    private String content;         // 题目内容
    private String questionType;    // 题目类型
    private String options;         // 选项（JSON字符串）
    private String correctAnswer;   // 正确答案
    private String analysis;        // 解析
    private Integer score;          // 题目分值
    
    private LocalDateTime answerTime;
}
