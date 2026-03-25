package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.Course;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 课程Mapper
 */
@Mapper
public interface CourseMapper {
    
    @Select("SELECT * FROM tb_course WHERE id = #{id}")
    Course findById(Long id);
    
    @Select("SELECT * FROM tb_course WHERE teacher_id = #{teacherId}")
    List<Course> findByTeacherId(Long teacherId);
    
    @Select("SELECT c.* FROM tb_course c " +
            "INNER JOIN tb_course_selection e ON c.id = e.course_id " +
            "WHERE e.student_id = #{studentId} AND e.status = 'selected'")
    List<Course> findByStudentId(Long studentId);
    
    @Select("SELECT * FROM tb_course WHERE status = 'approved'")
    List<Course> findAll();
    
    @Select("SELECT * FROM tb_course WHERE status = 'approved' AND is_open_for_selection = 1")
    List<Course> findAllOpenForSelection();
    
    @Select("SELECT * FROM tb_course ORDER BY create_time DESC")
    List<Course> findAllWithAllStatus();
    
    @Select("<script>" +
            "SELECT * FROM tb_course WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (name LIKE CONCAT('%', #{keyword}, '%') OR course_code LIKE CONCAT('%', #{keyword}, '%') OR teacher_name LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if> " +
            "<if test='status != null and status != \"\"'>AND status = #{status}</if> " +
            "ORDER BY create_time DESC LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<Course> queryCourseWithPagination(@Param("keyword") String keyword, @Param("status") String status, 
                                           @Param("offset") int offset, @Param("pageSize") int pageSize);
    
    @Select("<script>" +
            "SELECT COUNT(*) FROM tb_course WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (name LIKE CONCAT('%', #{keyword}, '%') OR course_code LIKE CONCAT('%', #{keyword}, '%') OR teacher_name LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if> " +
            "<if test='status != null and status != \"\"'>AND status = #{status}</if>" +
            "</script>")
    long countCourses(@Param("keyword") String keyword, @Param("status") String status);
    
    @Select("SELECT COUNT(*) FROM tb_course WHERE course_code = #{courseCode}")
    int checkCourseCodeExists(String courseCode);
    
    @Update("UPDATE tb_course SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    @Update("UPDATE tb_course SET capacity = #{capacity}, update_time = NOW() WHERE id = #{id}")
    int updateCapacity(@Param("id") Long id, @Param("capacity") Integer capacity);
    
    @Insert("INSERT INTO tb_course (course_code, name, teacher_id, teacher_name, description, " +
            "semester, credit, capacity, enrolled, category, status, create_time, update_time) " +
            "VALUES (#{courseCode}, #{name}, #{teacherId}, #{teacherName}, #{description}, " +
            "#{semester}, #{credit}, #{capacity}, #{enrolled}, #{category}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Course course);
    
    @Update("UPDATE tb_course SET course_code = #{courseCode}, name = #{name}, " +
            "description = #{description}, capacity = #{capacity}, credit = #{credit}, " +
            "semester = #{semester}, category = #{category}, update_time = NOW() WHERE id = #{id}")
    int update(Course course);
    
    @Delete("DELETE FROM tb_course WHERE id = #{id}")
    int delete(Long id);
    
    @Update("UPDATE tb_course SET enrolled = enrolled + 1 WHERE id = #{courseId}")
    int incrementStudentCount(Long courseId);

    @Update("UPDATE tb_course SET enrolled = enrolled - 1 WHERE id = #{courseId}")
    int decrementStudentCount(Long courseId);

    @Update({
        "<script>",
        "UPDATE tb_course",
        "SET enrolled = enrolled + 1, version = version + 1",
        "WHERE id = #{courseId} AND version = #{currentVersion}",
        "</script>"
    })
    int incrementStudentCountWithOptimisticLock(@Param("courseId") Long courseId, @Param("currentVersion") Integer currentVersion);

    @Update({
        "<script>",
        "UPDATE tb_course",
        "SET enrolled = enrolled - 1, version = version + 1",
        "WHERE id = #{courseId} AND version = #{currentVersion}",
        "</script>"
    })
    int decrementStudentCountWithOptimisticLock(@Param("courseId") Long courseId, @Param("currentVersion") Integer currentVersion);

    @Update("UPDATE tb_course SET is_open_for_selection = #{isOpen}, update_time = NOW() WHERE id = #{courseId}")
    int updateSelectionStatus(@Param("courseId") Long courseId, @Param("isOpen") Boolean isOpen);
    
    // ==================== 统计方法 ====================
    
    /**
     * 统计所有课程数量
     */
    @Select("SELECT COUNT(*) FROM tb_course")
    int countAll();
    
    /**
     * 按状态统计课程数量
     */
    @Select("SELECT COUNT(*) FROM tb_course WHERE status = #{status}")
    int countByStatus(@Param("status") String status);
    
    /**
     * 获取热门课程排行（按选课人数）
     */
    @Select("SELECT id, name, enrolled, capacity, teacher_name " +
            "FROM tb_course " +
            "ORDER BY enrolled DESC " +
            "LIMIT #{limit}")
    List<java.util.Map<String, Object>> getTopCourses(@Param("limit") int limit);
    
    /**
     * 统计教师的课程数量
     */
    @Select("SELECT COUNT(*) FROM tb_course WHERE teacher_id = #{teacherId}")
    int countByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * 获取教师的课程列表（包含基本信息）
     */
    @Select("SELECT id, name, course_code, teacher_name, enrolled, capacity " +
            "FROM tb_course " +
            "WHERE teacher_id = #{teacherId} AND status = 'approved' " +
            "ORDER BY create_time DESC")
    List<java.util.Map<String, Object>> getTeacherCourses(@Param("teacherId") Long teacherId);
}
