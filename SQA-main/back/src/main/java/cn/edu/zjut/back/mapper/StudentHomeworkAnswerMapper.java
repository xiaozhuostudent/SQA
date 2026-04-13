package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.StudentHomeworkAnswer;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 学生作业答题记录Mapper接口
 */
@Mapper
public interface StudentHomeworkAnswerMapper {
    
    @Select("SELECT * FROM tb_student_homework_answer WHERE id = #{id}")
    StudentHomeworkAnswer findById(Long id);
    
    @Select("SELECT * FROM tb_student_homework_answer WHERE student_homework_id = #{studentHomeworkId}")
    List<StudentHomeworkAnswer> findByStudentHomeworkId(Long studentHomeworkId);
    
    @Select("SELECT * FROM tb_student_homework_answer WHERE student_homework_id = #{studentHomeworkId} AND question_id = #{questionId}")
    StudentHomeworkAnswer findByStudentHomeworkAndQuestion(@Param("studentHomeworkId") Long studentHomeworkId, @Param("questionId") Long questionId);
    
    @Insert("INSERT INTO tb_student_homework_answer (student_homework_id, question_id, student_answer, " +
            "is_correct, score, teacher_comment, answer_time, ai_score, ai_feedback) VALUES " +
            "(#{studentHomeworkId}, #{questionId}, #{studentAnswer}, #{isCorrect}, #{score}, #{teacherComment}, #{answerTime}, #{aiScore}, #{aiFeedback})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(StudentHomeworkAnswer answer);
    
    @Update("UPDATE tb_student_homework_answer SET student_answer = #{studentAnswer}, is_correct = #{isCorrect}, " +
            "score = #{score}, teacher_comment = #{teacherComment}, answer_time = #{answerTime}, " +
            "ai_score = #{aiScore}, ai_feedback = #{aiFeedback} WHERE id = #{id}")
    int update(StudentHomeworkAnswer answer);
    
    @Delete("DELETE FROM tb_student_homework_answer WHERE student_homework_id = #{studentHomeworkId}")
    int deleteByStudentHomeworkId(Long studentHomeworkId);
}
