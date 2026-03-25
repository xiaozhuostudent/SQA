package cn.edu.zjut.back.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String role; // student, teacher, admin
    private String realName;
    private String gender; // male, female
    private String email;
    private String phone;
    private String avatar;
    private String status; // active, inactive
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 扩展字段（关联查询时使用）
    private Long studentId; // 学生ID（从tb_student表查询）
    private String studentNumber; // 学号
    private String major; // 专业
    private String className; // 班级
    private Integer enrollmentYear; // 入学年份
    private Integer grade; // 年级（1-4，对应大一到大四）
    
    private String teacherNumber; // 工号（教师）
    private String department; // 部门
    private String title; // 职称
    private String researchField; // 研究方向
    
    private String adminNumber; // 管理员编号
    private Integer permissionLevel; // 权限等级
}
