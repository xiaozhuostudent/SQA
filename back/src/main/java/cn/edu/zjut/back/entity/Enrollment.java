package cn.edu.zjut.back.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 选课记录实体类
 */
@Data
public class Enrollment {
    private Long id;
    private Long studentId;  // student_id
    private String studentName;  // student_name
    private Long courseId;  // course_id
    private String courseName;  // course_name
    private LocalDateTime selectTime;  // select_time (不是enroll_time)
    private String status;  // ENUM('selected', 'dropped', 'completed')
    private Double finalScore;  // DECIMAL(5,2) final_score
    private Integer version; // 乐观锁版本号

}
