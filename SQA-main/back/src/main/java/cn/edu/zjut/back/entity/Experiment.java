package cn.edu.zjut.back.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 实验实体类
 */
@Data
public class Experiment {
    private Long id;
    private Long courseId;  // course_id
    private String courseName;  // course_name
    private String title;
    private String description;
    private String requirements;
    private String steps;
    private LocalDateTime startTime;  // 开始时间 start_time
    private LocalDateTime deadline;  // 截止时间
    private String environmentType;  // ENUM('cloud', 'local')
    private String environmentConfig;  // JSON字符串
    private String resources;  // JSON字符串
    private String allowedLanguages;  // 允许的编程语言(逗号分隔)
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
