package cn.edu.zjut.back.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 实验题目提交记录实体类
 */
@Data
public class ExperimentProblemSubmission {
    private Long id;
    private Long experimentId;
    private Long problemId;
    private Long studentId;
    private String studentName;
    private String code;
    private String language;
    private String status; // pending, running, passed, failed, error
    private Integer passRate; // 0-100
    private String output;
    private String errorMessage;
    private LocalDateTime submitTime;
    private Integer executeTime; // ms
}
