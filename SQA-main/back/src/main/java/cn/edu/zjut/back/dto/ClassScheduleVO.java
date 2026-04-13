package cn.edu.zjut.back.dto;

import cn.edu.zjut.back.entity.Schedule;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 班级课表VO
 */
@Data
public class ClassScheduleVO {
    private String className;    // 班级名称
    private String semester;     // 学期
    private Map<String, List<Schedule>> scheduleMap;  // 课表数据，key为"dayOfWeek-period"
}
