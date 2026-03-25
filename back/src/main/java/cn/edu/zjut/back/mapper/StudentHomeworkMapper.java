package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.StudentHomework;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 学生作业记录Mapper接口
 */
@Mapper
public interface StudentHomeworkMapper {
    
    @Select("SELECT * FROM tb_student_homework WHERE id = #{id}")
    StudentHomework findById(Long id);
    
    @Select("SELECT * FROM tb_student_homework WHERE homework_id = #{homeworkId}")
    List<StudentHomework> findByHomeworkId(Long homeworkId);
    
    @Select("SELECT * FROM tb_student_homework WHERE student_id = #{studentId}")
    List<StudentHomework> findByStudentId(Long studentId);
    
    @Select("SELECT * FROM tb_student_homework WHERE homework_id = #{homeworkId} AND student_id = #{studentId}")
    StudentHomework findByHomeworkAndStudent(@Param("homeworkId") Long homeworkId, @Param("studentId") Long studentId);
    
    @Select("SELECT * FROM tb_student_homework WHERE homework_id = #{homeworkId} AND status IN ('submitted','graded')")
    List<StudentHomework> findSubmittedByHomeworkId(Long homeworkId);
    
    @Insert("INSERT INTO tb_student_homework (homework_id, student_id, student_name, student_number, " +
            "start_time, submit_time, score, status, is_late, teacher_comment) VALUES " +
            "(#{homeworkId}, #{studentId}, #{studentName}, #{studentNumber}, #{startTime}, #{submitTime}, " +
            "#{score}, #{status}, #{isLate}, #{teacherComment})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(StudentHomework studentHomework);
    
    @Update("UPDATE tb_student_homework SET start_time = #{startTime}, submit_time = #{submitTime}, " +
            "score = #{score}, status = #{status}, is_late = #{isLate}, teacher_comment = #{teacherComment} " +
            "WHERE id = #{id}")
    int update(StudentHomework studentHomework);
}
