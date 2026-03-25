package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.HomeworkSubmission;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 作业提交Mapper
 */
@Mapper
public interface HomeworkSubmissionMapper {
    
    @Select("SELECT * FROM tb_student_homework WHERE homework_id = #{homeworkId} AND student_id = #{studentId}")
    HomeworkSubmission findByHomeworkAndStudent(@Param("homeworkId") Long homeworkId, @Param("studentId") Long studentId);
    
    @Select("SELECT * FROM tb_student_homework WHERE homework_id = #{homeworkId}")
    List<HomeworkSubmission> findByHomeworkId(Long homeworkId);
    
    @Select("SELECT * FROM tb_student_homework WHERE student_id = #{studentId}")
    List<HomeworkSubmission> findByStudentId(Long studentId);
    
    @Insert("INSERT INTO tb_student_homework (homework_id, student_id, student_name, student_number, " +
            "content, files, submit_time, status) VALUES (#{homeworkId}, #{studentId}, #{studentName}, " +
            "#{studentNumber}, #{content}, #{files}, NOW(), 'submitted')")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(HomeworkSubmission submission);
    
    @Update("UPDATE tb_student_homework SET score = #{grade}, teacher_comment = #{feedback}, status = 'graded' " +
            "WHERE id = #{id}")
    int grade(HomeworkSubmission submission);
    
    // ==================== 统计方法 ====================
    
    /**
     * 统计所有作业提交数量
     */
    @Select("SELECT COUNT(*) FROM tb_student_homework")
    int countAll();
    
    /**
     * 统计按时提交的作业数量
     */
    @Select("SELECT COUNT(*) FROM tb_student_homework hs " +
            "INNER JOIN tb_homework h ON hs.homework_id = h.id " +
            "WHERE hs.submit_time <= h.deadline")
    int countOnTime();
    
    /**
     * 统计延迟提交的作业数量
     */
    @Select("SELECT COUNT(*) FROM tb_student_homework hs " +
            "INNER JOIN tb_homework h ON hs.homework_id = h.id " +
            "WHERE hs.submit_time > h.deadline")
    int countLate();
    
    /**
     * 统计教师待批改作业数
     */
    @Select("SELECT COUNT(*) FROM tb_student_homework hs " +
            "INNER JOIN tb_homework h ON hs.homework_id = h.id " +
            "INNER JOIN tb_course c ON h.course_id = c.id " +
            "WHERE c.teacher_id = #{teacherId} AND hs.status = 'submitted'")
    int countPendingByTeacherId(@Param("teacherId") Long teacherId);
    
    /**
     * 统计学生待提交作业数（未提交的）
     */
    @Select("SELECT COUNT(DISTINCT h.id) FROM tb_homework h " +
            "INNER JOIN tb_course_selection cs ON h.course_id = cs.course_id " +
            "LEFT JOIN tb_student_homework hs ON h.id = hs.homework_id AND hs.student_id = #{studentId} " +
            "WHERE cs.student_id = #{studentId} AND h.status = 'published' AND hs.id IS NULL")
    int countPendingByStudentId(@Param("studentId") Long studentId);
    
    /**
     * 获取学生的平均成绩
     */
    @Select("SELECT AVG(score) FROM tb_student_homework " +
            "WHERE student_id = #{studentId} AND status = 'graded' AND score IS NOT NULL")
    Double getAverageScoreByStudentId(@Param("studentId") Long studentId);
}
