package cn.edu.zjut.back.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 代码提交记录实体类
 */
@Data
public class CodeSubmission {
    private Long id;
    private Long experimentId;
    private Long experimentProblemId;
    private Long studentId;
    private String studentName;
    private String language;
    private String code;
    private String stdin;
    private String expectedOutput;
    private String stdout;
    private String stderr;
    private String pistonResponse; // JSON
    private String result; // 'AC', 'WA', 'CE', 'RE', 'TLE', 'MLE', 'PENDING'
    private Integer runTimeMs; // ms
    private Integer memoryKb; // KB
    private LocalDateTime submitTime;
}
