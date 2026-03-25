package cn.edu.zjut.back.vo;

import lombok.Data;

/**
 * 用户信息VO
 */
@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String role;
    private String avatar;
    private String status;  // 用户状态：active-启用, inactive-禁用
    
    // 管理员扩展字段
    private String adminNumber;
    private Integer permissionLevel;
    
    // 学生扩展字段
    private String studentNumber;
    private String major;
    private String className;
    
    // 教师扩展字段
    private String teacherNumber;
    private String department;
    private String title;
}
