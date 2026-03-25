package cn.edu.zjut.back.dto;

import lombok.Data;

/**
 * 导入用户数据DTO
 */
@Data
public class ImportUserDTO {
    private String username;  // 用户名
    private String password;  // 密码
    private String name;  // 姓名
    private String email;  // 邮箱
    private String phone;  // 电话
    private String gender;  // 性别
    private String major;  // 专业（学生）
    private String className;  // 班级（学生）
    private String role;  // 角色
    private Integer rowNumber;  // Excel行号（用于错误提示）
}
