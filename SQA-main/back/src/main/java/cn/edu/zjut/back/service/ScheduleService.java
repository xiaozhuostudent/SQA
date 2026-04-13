package cn.edu.zjut.back.service;

import cn.edu.zjut.back.entity.Schedule;
import cn.edu.zjut.back.dto.ScheduleQueryDTO;
import cn.edu.zjut.back.mapper.ScheduleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 排课服务类
 */
@Service
public class ScheduleService {
    
    private final ScheduleMapper scheduleMapper;
    
    public ScheduleService(ScheduleMapper scheduleMapper) {
        this.scheduleMapper = scheduleMapper;
    }
    
    // === 查询方法 ===
    
    /**
     * 根据班级和学期查询课表
     */
    public List<Schedule> getScheduleByClassAndSemester(String className, String semester) {
        return scheduleMapper.findByClassAndSemester(className, semester);
    }
    
    /**
     * 条件查询排课
     */
    public List<Schedule> querySchedules(ScheduleQueryDTO query) {
        return scheduleMapper.querySchedules(query);
    }
    
    /**
     * 根据ID查询排课
     */
    public Schedule getScheduleById(Long id) {
        return scheduleMapper.findById(id);
    }
    
    // === 增删改方法 ===
    
    /**
     * 新增排课
     */
    public int createSchedule(Schedule schedule) {
        return scheduleMapper.insert(schedule);
    }
    
    /**
     * 更新排课
     */
    public int updateSchedule(Schedule schedule) {
        return scheduleMapper.update(schedule);
    }
    
    /**
     * 删除排课
     */
    public int deleteSchedule(Long id) {
        return scheduleMapper.delete(id);
    }
    
    // === 业务方法 ===
    
    /**
     * 检查时间冲突
     */
    public boolean checkTimeConflict(Schedule schedule) {
        Long excludeId = (schedule.getId() != null) ? schedule.getId() : 0L;
        int count = scheduleMapper.checkConflict(
            schedule.getClassroom(),
            schedule.getDayOfWeek(),
            schedule.getPeriod(),
            schedule.getSemester(),
            excludeId
        );
        return count > 0;
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
    
    /**
     * 创建排课（带冲突检查）- 安全版本
     */
    public boolean createScheduleWithConflictCheck(Schedule schedule) {
        // 检查时间冲突
        if (checkTimeConflict(schedule)) {
            return false; // 存在冲突，创建失败
        }
        
        // 无冲突，创建排课
        scheduleMapper.insert(schedule);
        return true;
    }
    
    /**
     * 更新排课（带冲突检查）- 安全版本
     */
    public boolean updateScheduleWithConflictCheck(Schedule schedule) {
        if (schedule.getId() == null) {
            throw new IllegalArgumentException("更新排课时ID不能为空");
        }
        
        // 检查时间冲突（排除自身）
        if (checkTimeConflict(schedule)) {
            return false; // 存在冲突，更新失败
        }
        
        // 无冲突，更新排课
        scheduleMapper.update(schedule);
        return true;
    }
    
    /**
     * 根据课程ID查询排课（需要添加的方法）
     */
    public List<Schedule> getScheduleByCourseId(Long courseId) {
        // 使用条件查询实现
        ScheduleQueryDTO query = new ScheduleQueryDTO();
        query.setCourseId(courseId);
        return scheduleMapper.querySchedules(query);
    }
    
    /**
     * 根据教师ID查询排课（需要添加的方法）
     */
    public List<Schedule> getScheduleByTeacherId(Long teacherId) {
        // 使用条件查询实现
        ScheduleQueryDTO query = new ScheduleQueryDTO();
        query.setTeacherId(teacherId);
        return scheduleMapper.querySchedules(query);
    }
    
    /**
     * 获取所有课表
     */
    public List<Schedule> getAllSchedules() {
        // 使用空查询条件获取所有
        ScheduleQueryDTO query = new ScheduleQueryDTO();
        return scheduleMapper.querySchedules(query);
    }
}