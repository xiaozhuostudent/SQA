package cn.edu.zjut.back.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 实验报告实体类 (tb_experiment_report)
 */
@Data
public class ExperimentSubmission {
    private Long id;
    private Long experimentId;  // experiment_id
    private Long studentId;  // student_id
    private String studentName;  // student_name
    private String studentNumber;  // student_number (新增)
    private String content;  // content (不是reportUrl)
    private String files;  // files (JSON字符串)
    private LocalDateTime submitTime;  // submit_time
    private String status;  // ENUM('submitted', 'completed')
}
