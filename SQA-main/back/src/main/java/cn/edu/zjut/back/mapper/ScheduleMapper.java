package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.Schedule;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 排课Mapper
 */
@Mapper
public interface ScheduleMapper {
    
    /**
     * 根据班级和学期查询课表
     */
    @Select("<script>" +
            "SELECT s.* FROM tb_schedule s " +
            "INNER JOIN tb_course c ON s.course_id = c.id " +
            "INNER JOIN tb_course_selection cs ON cs.course_id = c.id " +
            "INNER JOIN tb_student st ON st.user_id = cs.student_id " +
            "WHERE 1=1 " +
            "<if test='className != null and className != \"\"'>AND st.class_name = #{className}</if> " +
            "<if test='semester != null and semester != \"\"'>AND s.semester = #{semester}</if> " +
            "GROUP BY s.id " +
            "ORDER BY s.day_of_week, s.period" +
            "</script>")
    List<Schedule> findByClassAndSemester(@Param("className") String className, @Param("semester") String semester);
    
    /**
     * 查询所有排课
     */
    @Select("<script>" +
            "SELECT * FROM tb_schedule " +
            "WHERE 1=1 " +
            "<if test='query.semester != null and query.semester != \"\"'>AND semester = #{query.semester}</if> " +
            "<if test='query.courseId != null'>AND course_id = #{query.courseId}</if> " +
            "<if test='query.teacherId != null'>AND teacher_id = #{query.teacherId}</if> " +
            "<if test='query.dayOfWeek != null'>AND day_of_week = #{query.dayOfWeek}</if> " +
            "ORDER BY day_of_week, period" +
            "</script>")
    List<Schedule> querySchedules(@Param("query") cn.edu.zjut.back.dto.ScheduleQueryDTO query);
    
    /**
     * 根据ID查询排课
     */
    @Select("SELECT * FROM tb_schedule WHERE id = #{id}")
    Schedule findById(Long id);
    
    /**
     * 插入排课
     */
    @Insert("INSERT INTO tb_schedule (course_id, course_name, teacher_id, teacher_name, classroom, " +
            "day_of_week, period, start_week, end_week, semester) " +
            "VALUES (#{courseId}, #{courseName}, #{teacherId}, #{teacherName}, #{classroom}, " +
            "#{dayOfWeek}, #{period}, #{startWeek}, #{endWeek}, #{semester})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Schedule schedule);
    
    /**
     * 更新排课
     */
    @Update("UPDATE tb_schedule SET " +
            "classroom = #{classroom}, " +
            "day_of_week = #{dayOfWeek}, " +
            "period = #{period}, " +
            "start_week = #{startWeek}, " +
            "end_week = #{endWeek} " +
            "WHERE id = #{id}")
    int update(Schedule schedule);
    
    /**
     * 删除排课
     */
    @Delete("DELETE FROM tb_schedule WHERE id = #{id}")
    int delete(Long id);
    
    /**
     * 检查时间冲突（同一教室、同一时间）
     */
    @Select("SELECT COUNT(*) FROM tb_schedule " +
            "WHERE classroom = #{classroom} " +
            "AND day_of_week = #{dayOfWeek} " +
            "AND period = #{period} " +
            "AND semester = #{semester} " +
            "AND id != #{excludeId}")
    int checkConflict(@Param("classroom") String classroom, 
                     @Param("dayOfWeek") Integer dayOfWeek,
                     @Param("period") Integer period, 
                     @Param("semester") String semester,
                     @Param("excludeId") Long excludeId);
    
    /**
     * 获取所有班级列表
     */
    @Select("SELECT DISTINCT class_name FROM tb_student WHERE class_name IS NOT NULL ORDER BY class_name")
    List<String> getAllClasses();
    
    /**
     * 获取所有学期列表
     */
    @Select("SELECT DISTINCT semester FROM tb_schedule ORDER BY semester DESC")
    List<String> getAllSemesters();
}
