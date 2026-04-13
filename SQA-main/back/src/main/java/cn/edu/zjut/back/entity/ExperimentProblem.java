package cn.edu.zjut.back.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 实验题目实体类
 */
@Data
public class ExperimentProblem {
    private Long id;
    private Long experimentId;
    private String title;
    private String description;
    private String inputFormat;
    private String outputFormat;
    private String samples; // JSON格式存储测试样例
    private String difficulty; // 'easy', 'medium', 'hard'
    private Integer score;
    private Integer timeLimit; // ms
    private Integer memoryLimit; // MB
    private Integer sortOrder;
    private Long createdBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
