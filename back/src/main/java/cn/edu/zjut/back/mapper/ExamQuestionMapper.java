package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.ExamQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 试卷题目关联Mapper
 */
@Mapper
public interface ExamQuestionMapper {
    
    @Insert("INSERT INTO tb_exam_question (exam_paper_id, question_id, question_order, question_score, is_required) " +
            "VALUES (#{examPaperId}, #{questionId}, #{questionOrder}, #{questionScore}, #{isRequired})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ExamQuestion examQuestion);
    
    @Select("SELECT * FROM tb_exam_question WHERE exam_paper_id = #{examPaperId} ORDER BY question_order")
    @Results({
        @Result(property = "examPaperId", column = "exam_paper_id"),
        @Result(property = "questionId", column = "question_id"),
        @Result(property = "questionOrder", column = "question_order"),
        @Result(property = "questionScore", column = "question_score"),
        @Result(property = "isRequired", column = "is_required")
    })
    List<ExamQuestion> findByExamPaperId(Long examPaperId);
    
    @Delete("DELETE FROM tb_exam_question WHERE exam_paper_id = #{examPaperId}")
    int deleteByExamPaperId(Long examPaperId);
    
    @Delete("DELETE FROM tb_exam_question WHERE exam_paper_id = #{examPaperId} AND question_id = #{questionId}")
    int deleteQuestion(@Param("examPaperId") Long examPaperId, @Param("questionId") Long questionId);
    
    @Delete("DELETE FROM tb_exam_question WHERE question_id = #{questionId}")
    int deleteByQuestionId(Long questionId);
    
    @Update("UPDATE tb_exam_question SET question_order = #{questionOrder}, question_score = #{questionScore}, is_required = #{isRequired} WHERE id = #{id}")
    int update(ExamQuestion examQuestion);
    
    @Select("SELECT COUNT(*) FROM tb_exam_question WHERE exam_paper_id = #{examPaperId}")
    int countByExamPaperId(Long examPaperId);
}
