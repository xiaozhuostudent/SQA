package cn.edu.zjut.back.entity;

import lombok.Data;

/**
 * 课程排班实体类
 */
@Data
public class Schedule {
    private Long id;
    private Long courseId;  // course_id
    private String courseName;  // course_name
    private Long teacherId;  // teacher_id (新增)
    private String teacherName;  // teacher_name (新增)
    private String classroom;
    private Integer dayOfWeek;  // day_of_week (TINYINT 1-7)
    private Integer period;  // period (TINYINT 1-5)
    private Integer startWeek;  // start_week
    private Integer endWeek;  // end_week
    private String semester;  // semester (新增)
}
