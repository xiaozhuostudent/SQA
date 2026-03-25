package cn.edu.zjut.back.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 作业提交记录实体类
 */
@Data
public class HomeworkSubmission {
    private Long id;
    private Long homeworkId;  // homework_id
    private Long studentId;  // student_id
    private String studentName;  // student_name
    private String studentNumber;  // student_number (新增)
    private String content;
    private String files;  // JSON字符串 (不是attachment_url)
    private LocalDateTime submitTime;  // submit_time
    private Double grade;  // DECIMAL(5,2) (不是score)
    private String feedback;
    private String status;  // ENUM('submitted', 'graded')
}
