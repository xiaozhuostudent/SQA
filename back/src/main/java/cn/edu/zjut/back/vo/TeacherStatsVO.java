package cn.edu.zjut.back.vo;

import lombok.Data;

/**
 * 教师授课统计VO
 */
@Data
public class TeacherStatsVO {
    private Long teacherId;
    private String teacherName;
    private String username;
    private Integer courseCount;  // 授课数量
    private Integer studentCount;  // 学生总数
    private Integer activeStudentCount;  // 活跃学生数
}
