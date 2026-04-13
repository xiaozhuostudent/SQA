package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.StudentAnswer;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 学生答题详情Mapper接口
 */
@Mapper
public interface StudentAnswerMapper {
    
    @Select("SELECT * FROM tb_student_answer WHERE student_exam_id = #{studentExamId} ORDER BY question_order")
    List<StudentAnswer> findByStudentExamId(Long studentExamId);
    
    @Insert("INSERT INTO tb_student_answer (student_exam_id, question_id, question_order, " +
            "student_answer, answer_time, attempt_number) VALUES " +
            "(#{studentExamId}, #{questionId}, #{questionOrder}, #{studentAnswer}, #{answerTime}, #{attemptNumber})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(StudentAnswer answer);
    
    @Update("UPDATE tb_student_answer SET student_answer = #{studentAnswer}, is_correct = #{isCorrect}, " +
            "score = #{score}, answer_time = #{answerTime} WHERE id = #{id}")
    int update(StudentAnswer answer);
    
    @Update("UPDATE tb_student_answer SET is_correct = #{isCorrect}, score = #{score} " +
            "WHERE student_exam_id = #{studentExamId} AND question_id = #{questionId}")
    int gradeAnswer(@Param("studentExamId") Long studentExamId, @Param("questionId") Long questionId,
                    @Param("isCorrect") Boolean isCorrect, @Param("score") Integer score);
    
    // 添加根据考试ID和题目ID查找答案的方法
    @Select("SELECT * FROM tb_student_answer WHERE student_exam_id = #{studentExamId} AND question_id = #{questionId}")
    StudentAnswer findByExamIdAndQuestionId(@Param("studentExamId") Long studentExamId, @Param("questionId") Long questionId);
}