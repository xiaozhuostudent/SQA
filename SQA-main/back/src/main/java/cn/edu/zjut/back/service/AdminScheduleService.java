package cn.edu.zjut.back.service;

import cn.edu.zjut.back.dto.ClassScheduleVO;
import cn.edu.zjut.back.dto.ScheduleQueryDTO;
import cn.edu.zjut.back.entity.Schedule;
import cn.edu.zjut.back.mapper.ScheduleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理员排课Service
 */
@Service
public class AdminScheduleService {
    
    @Autowired
    private ScheduleMapper scheduleMapper;
    
    /**
     * 获取班级课表
     */
    public ClassScheduleVO getClassSchedule(String className, String semester) {
        List<Schedule> schedules = scheduleMapper.findByClassAndSemester(className, semester);
        
        ClassScheduleVO vo = new ClassScheduleVO();
        vo.setClassName(className);
        vo.setSemester(semester);
        
        // 将排课按 "dayOfWeek-period" 分组
        Map<String, List<Schedule>> scheduleMap = schedules.stream()
            .collect(Collectors.groupingBy(s -> s.getDayOfWeek() + "-" + s.getPeriod()));
        
        vo.setScheduleMap(scheduleMap);
        return vo;
    }
    
    /**
     * 查询排课列表
     */
    public List<Schedule> querySchedules(ScheduleQueryDTO query) {
        return scheduleMapper.querySchedules(query);
    }
    
    /**
     * 根据ID获取排课
     */
    public Schedule getScheduleById(Long id) {
        return scheduleMapper.findById(id);
    }
    
    /**
     * 添加排课
     */
    @Transactional
    public Schedule addSchedule(Schedule schedule) {
        // 检查时间冲突
        if (scheduleMapper.checkConflict(
                schedule.getClassroom(),
                schedule.getDayOfWeek(),
                schedule.getPeriod(),
                schedule.getSemester(),
                0L) > 0) {
            throw new RuntimeException("该教室在此时间段已有课程安排");
        }
        
        scheduleMapper.insert(schedule);
        return schedule;
    }
    
    /**
     * 更新排课
     */
    @Transactional
    public Schedule updateSchedule(Schedule schedule) {
        // 检查时间冲突
        if (scheduleMapper.checkConflict(
                schedule.getClassroom(),
                schedule.getDayOfWeek(),
                schedule.getPeriod(),
                schedule.getSemester(),
                schedule.getId()) > 0) {
            throw new RuntimeException("该教室在此时间段已有课程安排");
        }
        
        scheduleMapper.update(schedule);
        return scheduleMapper.findById(schedule.getId());
    }
    
    /**
     * 删除排课
     */
    @Transactional
    public void deleteSchedule(Long id) {
        scheduleMapper.delete(id);
    }
    
    /**
     * 获取所有班级列表
     */
    public List<String> getAllClasses() {
        return scheduleMapper.getAllClasses();
    }
    
    /**
     * 获取所有学期列表
     */
    public List<String> getAllSemesters() {
        return scheduleMapper.getAllSemesters();
    }
}
