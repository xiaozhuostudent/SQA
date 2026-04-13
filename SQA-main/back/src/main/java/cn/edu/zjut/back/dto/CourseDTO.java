package cn.edu.zjut.back.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 课程创建/编辑DTO
 */
@Data
public class CourseDTO {
    private Long id;
    
    @NotBlank(message = "课程名称不能为空")
    private String name;  // 匹配 Course 实体的 name 字段
    
    @NotBlank(message = "课程代码不能为空")
    private String courseCode;
    
    private String description;
    
    @NotNull(message = "容量不能为空")
    private Integer capacity;  // 匹配 Course 实体的 capacity 字段
    
    @NotNull(message = "学分不能为空")
    private Double credit;  // 修改为 Double 以匹配数据库 DECIMAL(3,1)
    
    @NotBlank(message = "学期不能为空")
    private String semester;
    
    private String category;  // 课程类别
}
