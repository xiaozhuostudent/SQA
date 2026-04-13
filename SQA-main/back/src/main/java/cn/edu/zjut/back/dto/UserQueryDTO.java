package cn.edu.zjut.back.dto;

import lombok.Data;

/**
 * 用户查询条件DTO
 */
@Data
public class UserQueryDTO {
    private Integer page = 1;
    private Integer pageSize = 10;
    private Integer size = 10;  // 每页数量(兼容前端)
    private String keyword;  // 搜索关键词（用户名、姓名、邮箱）
    private String role;  // 角色筛选：teacher, student
    private String gender;  // 性别筛选
    private String major;  // 专业筛选（学生）
    private String className;  // 班级筛选（学生）
    private String status;  // 账号状态：active, inactive
}
