package cn.edu.zjut.back.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 课程实体类
 */
@Data
public class Course {
    private Long id;
    private String courseCode;  // course_code
    private String name;  // name (不是course_name)
    private Long teacherId;  // teacher_id
    private String teacherName;  // teacher_name
    private String description;
    private String semester;
    private Double credit;  // DECIMAL(3,1)
    private Integer capacity;  // capacity (不是max_students)
    private Integer enrolled;  // enrolled (不是current_students)
    private String category;
    private String status;  // ENUM('pending', 'approved', 'rejected')
    private Boolean isOpenForSelection;  // 是否开放选课
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer version; // 乐观锁版本号

}
