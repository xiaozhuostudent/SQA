package cn.edu.zjut.back.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程请求实体
 */
@Data
public class CourseRequest {
    private Long id;
    
    // 请求类型：create-创建课程, delete-删除课程
    private String requestType;
    
    // 教师信息
    private Long teacherId;
    private String teacherName;
    
    // 课程信息
    private Long courseId;
    private String courseName;
    private String courseCode;
    private BigDecimal credit;
    private Integer capacity;
    private String semester;
    private String category;
    private String description;
    
    // 请求信息
    private String reason;
    private String fileName;
    private String fileKey;
    
    // 审核信息
    private String status; // pending-待审核, approved-已批准, rejected-已拒绝
    private Long adminId;
    private String adminName;
    private String adminComment;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
