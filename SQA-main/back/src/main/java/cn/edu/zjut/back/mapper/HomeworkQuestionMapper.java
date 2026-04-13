package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.HomeworkQuestion;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 作业题目关联Mapper接口
 */
@Mapper
public interface HomeworkQuestionMapper {
    
    @Select("SELECT * FROM tb_homework_question WHERE homework_id = #{homeworkId} ORDER BY question_order")
    List<HomeworkQuestion> findByHomeworkId(Long homeworkId);
    
    @Insert("INSERT INTO tb_homework_question (homework_id, question_id, question_order, question_score) " +
            "VALUES (#{homeworkId}, #{questionId}, #{questionOrder}, #{questionScore})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(HomeworkQuestion homeworkQuestion);
    
    @Delete("DELETE FROM tb_homework_question WHERE homework_id = #{homeworkId}")
    int deleteByHomeworkId(Long homeworkId);
    
    @Delete("DELETE FROM tb_homework_question WHERE id = #{id}")
    int delete(Long id);
    
    @Select("SELECT * FROM tb_homework_question WHERE homework_id = #{homeworkId} AND question_id = #{questionId}")
    HomeworkQuestion findByHomeworkIdAndQuestionId(@Param("homeworkId") Long homeworkId, @Param("questionId") Long questionId);
}
