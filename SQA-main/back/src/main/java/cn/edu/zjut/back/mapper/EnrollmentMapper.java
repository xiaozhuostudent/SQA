package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.Enrollment;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 选课Mapper
 */
@Mapper
public interface EnrollmentMapper {
    
    @Select("SELECT * FROM tb_course_selection WHERE student_id = #{studentId} AND course_id = #{courseId}")
    Enrollment findByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    @Select("SELECT * FROM tb_course_selection WHERE course_id = #{courseId} AND status = 'selected'")
    List<Enrollment> findByCourseId(Long courseId);
    
    @Insert("INSERT INTO tb_course_selection (student_id, student_name, course_id, course_name, select_time, status, version) " +
            "VALUES (#{studentId}, #{studentName}, #{courseId}, #{courseName}, NOW(), 'selected', 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Enrollment enrollment);
    
    @Update({
        "<script>",
        "UPDATE tb_course_selection",
        "SET status = 'selected', select_time = NOW(), version = version + 1",
        "WHERE student_id = #{studentId} AND course_id = #{courseId} AND version = #{currentVersion}",
        "</script>"
    })
    int reEnrollWithOptimisticLock(@Param("studentId") Long studentId, @Param("courseId") Long courseId, @Param("currentVersion") Integer currentVersion);
    
    @Update({
        "<script>",
        "UPDATE tb_course_selection",
        "SET status = 'dropped', version = version + 1",
        "WHERE student_id = #{studentId} AND course_id = #{courseId} AND version = #{currentVersion}",
        "</script>"
    })
    int withdrawWithOptimisticLock(@Param("studentId") Long studentId, @Param("courseId") Long courseId, @Param("currentVersion") Integer currentVersion);
    
    // ==================== 统计方法 ====================
    
    /**
     * 统计所有选课记录
     */
    @Select("SELECT COUNT(*) FROM tb_course_selection WHERE status = 'selected'")
    int countAll();
    
    /**
     * 统计某课程的选课人数
     */
    @Select("SELECT COUNT(*) FROM tb_course_selection WHERE course_id = #{courseId} AND status = 'selected'")
    int countByCourseId(@Param("courseId") Long courseId);
    
    /**
     * 统计学生的选课数量
     */
    @Select("SELECT COUNT(*) FROM tb_course_selection WHERE student_id = #{studentId} AND status = 'selected'")
    int countByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 统计教师所有课程的学生总数
     */
    @Select("SELECT COUNT(DISTINCT e.student_id) " +
            "FROM tb_course_selection e " +
            "INNER JOIN tb_course c ON e.course_id = c.id " +
            "WHERE c.teacher_id = #{teacherId} AND e.status = 'selected'")
    int countStudentsByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * 获取学生的选课列表（包含课程和教师信息）
     */
    @Select("SELECT e.*, c.name as courseName, c.teacher_name as teacherName " +
            "FROM tb_course_selection e " +
            "INNER JOIN tb_course c ON e.course_id = c.id " +
            "WHERE e.student_id = #{studentId} AND e.status = 'selected' " +
            "ORDER BY e.select_time DESC")
    List<java.util.Map<String, Object>> getStudentEnrollments(@Param("studentId") Long studentId);
    
    /**
     * 获取课程的所有学生
     */
    @Select("SELECT u.*, s.id as studentId FROM tb_user u " +
            "INNER JOIN tb_student s ON u.id = s.user_id " +
            "INNER JOIN tb_course_selection e ON s.id = e.student_id " +
            "WHERE e.course_id = #{courseId} AND e.status = 'selected' AND u.role = 'student' " +
            "ORDER BY u.username")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "studentId", column = "studentId")
    })
    List<cn.edu.zjut.back.entity.User> findStudentsByCourseId(@Param("courseId") Long courseId);
}
