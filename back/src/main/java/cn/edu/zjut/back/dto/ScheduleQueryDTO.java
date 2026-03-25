package cn.edu.zjut.back.dto;

import lombok.Data;

/**
 * 排课查询DTO
 */
@Data
public class ScheduleQueryDTO {
    private String className;  // 班级名称
    private String semester;   // 学期
    private Long courseId;     // 课程ID
    private Long teacherId;    // 教师ID
    private Integer dayOfWeek; // 星期
}
