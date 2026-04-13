package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.StudentExam;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 学生考试记录Mapper接口
 */
@Mapper
public interface StudentExamMapper {
    
    @Select("SELECT * FROM tb_student_exam WHERE id = #{id}")
    StudentExam findById(Long id);
    
    @Select("SELECT * FROM tb_student_exam WHERE exam_paper_id = #{examPaperId} AND student_id = #{studentId}")
    StudentExam findByExamAndStudent(@Param("examPaperId") Long examPaperId, @Param("studentId") Long studentId);
    
    @Select("SELECT * FROM tb_student_exam WHERE exam_paper_id = #{examPaperId} AND student_id = #{studentId} ORDER BY attempt_number ASC")
    List<StudentExam> findByExamAndStudentAll(@Param("examPaperId") Long examPaperId, @Param("studentId") Long studentId);
    
    @Select("SELECT * FROM tb_student_exam WHERE student_id = #{studentId} ORDER BY start_time DESC")
    List<StudentExam> findByStudentId(Long studentId);
    
    @Select("SELECT * FROM tb_student_exam WHERE exam_paper_id = #{examPaperId} ORDER BY submit_time DESC")
    List<StudentExam> findByExamPaperId(Long examPaperId);
    
    @Insert("INSERT INTO tb_student_exam (exam_paper_id, student_id, student_name, student_number, " +
            "attempt_number, start_time, status, ip_address) VALUES " +
            "(#{examPaperId}, #{studentId}, #{studentName}, #{studentNumber}, #{attemptNumber}, #{startTime}, #{status}, #{ipAddress})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(StudentExam studentExam);
    
    @Update("UPDATE tb_student_exam SET status = #{status}, submit_time = #{submitTime}, " +
            "total_score = #{totalScore}, objective_score = #{objectiveScore}, subjective_score = #{subjectiveScore}, " +
            "grader_id = #{graderId}, grader_name = #{graderName}, grade_time = #{gradeTime}, " +
            "feedback = #{feedback}, duration = #{duration}, pass_score = #{passScore} WHERE id = #{id}")
    int update(StudentExam studentExam);
}