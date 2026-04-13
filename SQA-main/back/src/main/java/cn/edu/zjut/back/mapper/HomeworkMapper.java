package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.Homework;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 作业Mapper - 支持题库选题
 */
@Mapper
public interface HomeworkMapper {
    
    @Select("SELECT * FROM tb_homework WHERE id = #{id}")
    Homework findById(Long id);
    
    @Select("SELECT * FROM tb_homework WHERE course_id = #{courseId} ORDER BY create_time DESC")
    List<Homework> findByCourseId(Long courseId);
    
    @Select("SELECT * FROM tb_homework WHERE creator_id = #{creatorId} ORDER BY create_time DESC")
    List<Homework> findByCreatorId(Long creatorId);
    
    @Select("SELECT DISTINCT h.* FROM tb_homework h " +
            "INNER JOIN tb_course_selection cs ON h.course_id = cs.course_id " +
            "WHERE cs.student_id = #{studentId} AND h.status = 'published' " +
            "ORDER BY h.create_time DESC")
    List<Homework> findByStudentId(Long studentId);
    
    @Insert("INSERT INTO tb_homework (course_id, course_name, title, description, total_score, deadline, " +
            "allow_late_submission, late_penalty, show_answer, creator_id, creator_name, status) VALUES " +
            "(#{courseId}, #{courseName}, #{title}, #{description}, #{totalScore}, #{deadline}, " +
            "#{allowLateSubmission}, #{latePenalty}, #{showAnswer}, #{creatorId}, #{creatorName}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Homework homework);
    
    @Update("UPDATE tb_homework SET course_id = #{courseId}, course_name = #{courseName}, " +
            "title = #{title}, description = #{description}, total_score = #{totalScore}, " +
            "deadline = #{deadline}, allow_late_submission = #{allowLateSubmission}, " +
            "late_penalty = #{latePenalty}, show_answer = #{showAnswer}, status = #{status} " +
            "WHERE id = #{id}")
    int update(Homework homework);
    
    @Delete("DELETE FROM tb_homework WHERE id = #{id}")
    int delete(Long id);
    
    // ==================== 统计方法 ====================
    
    /**
     * 统计所有作业数量
     */
    @Select("SELECT COUNT(*) FROM tb_homework")
    int countAll();
    
    /**
     * 统计某课程的作业数量
     */
    @Select("SELECT COUNT(*) FROM tb_homework WHERE course_id = #{courseId}")
    int countByCourseId(@Param("courseId") Long courseId);
    
    /**
     * 统计教师的实验任务数（类型为实验的作业）
     */
    @Select("SELECT COUNT(*) FROM tb_homework h " +
            "INNER JOIN tb_course c ON h.course_id = c.id " +
            "WHERE c.teacher_id = #{teacherId} AND h.title LIKE '%实验%'")
    int countExperimentsByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * 统计学生的实验任务数
     */
    @Select("SELECT COUNT(DISTINCT h.id) FROM tb_homework h " +
            "INNER JOIN tb_course_selection cs ON h.course_id = cs.course_id " +
            "WHERE cs.student_id = #{studentId} AND h.title LIKE '%实验%' AND h.status = 'published'")
    int countExperimentsByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 获取学生的待办作业列表
     */
    @Select("SELECT h.id, h.title, h.deadline, h.course_name " +
            "FROM tb_homework h " +
            "INNER JOIN tb_course_selection cs ON h.course_id = cs.course_id " +
            "LEFT JOIN tb_student_homework hs ON h.id = hs.homework_id AND hs.student_id = #{studentId} " +
            "WHERE cs.student_id = #{studentId} AND h.status = 'published' AND hs.id IS NULL " +
            "ORDER BY h.deadline ASC " +
            "LIMIT #{limit}")
    List<java.util.Map<String, Object>> getPendingHomeworkByStudentId(@Param("studentId") Long studentId, @Param("limit") int limit);
}
