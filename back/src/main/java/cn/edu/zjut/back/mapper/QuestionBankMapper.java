package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.QuestionBank;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface QuestionBankMapper {
    @Select("SELECT q.*, c.name as courseName FROM tb_question_bank q " +
            "LEFT JOIN tb_course c ON q.course_id = c.id " +
            "ORDER BY q.create_time DESC")
    List<QuestionBank> findAll();
    
    @Select("SELECT q.*, c.name as courseName FROM tb_question_bank q " +
            "LEFT JOIN tb_course c ON q.course_id = c.id " +
            "WHERE q.is_visible = 1 " +
            "ORDER BY q.create_time DESC")
    List<QuestionBank> findAllVisibleForStudent();
    
    @Select("SELECT q.*, c.name as courseName FROM tb_question_bank q " +
            "LEFT JOIN tb_course c ON q.course_id = c.id " +
            "WHERE q.id = #{id}")
    QuestionBank findById(Long id);
    
    @Select("SELECT q.*, c.name as courseName FROM tb_question_bank q " +
            "LEFT JOIN tb_course c ON q.course_id = c.id " +
            "WHERE q.course_id = #{courseId} " +
            "ORDER BY q.create_time DESC")
    List<QuestionBank> findByCourseId(Long courseId);
    
    @Select("SELECT q.*, c.name as courseName FROM tb_question_bank q " +
            "LEFT JOIN tb_course c ON q.course_id = c.id " +
            "WHERE q.course_id = #{courseId} AND q.is_visible = 1 " +
            "ORDER BY q.create_time DESC")
    List<QuestionBank> findByCourseIdVisibleForStudent(Long courseId);
    
    @Select("SELECT q.* FROM tb_question_bank q " +
            "JOIN tb_exam_question eq ON q.id = eq.question_id " +
            "WHERE eq.exam_paper_id = #{examPaperId}")
    List<QuestionBank> findByExamPaperId(Long examPaperId);
    
    @Select("SELECT q.*, c.name as courseName FROM tb_question_bank q " +
            "LEFT JOIN tb_course c ON q.course_id = c.id " +
            "WHERE q.question_type = #{questionType} " +
            "ORDER BY q.create_time DESC")
    List<QuestionBank> findByQuestionType(String questionType);
    
    @Insert("INSERT INTO tb_question_bank(course_id, question_type, difficulty, content, options, answer, " +
            "explanation, score, tags, knowledge_points, usage_count, creator_id, creator_name, status, is_visible) " +
            "VALUES(#{courseId}, #{questionType}, #{difficulty}, #{content}, #{options}, #{answer}, " +
            "#{explanation}, #{score}, #{tags}, #{knowledgePoints}, #{usageCount}, #{creatorId}, #{creatorName}, #{status}, #{isVisible})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(QuestionBank questionBank);
    
    @Update("UPDATE tb_question_bank SET course_id=#{courseId}, question_type=#{questionType}, difficulty=#{difficulty}, " +
            "content=#{content}, options=#{options}, answer=#{answer}, explanation=#{explanation}, score=#{score}, " +
            "tags=#{tags}, knowledge_points=#{knowledgePoints}, status=#{status}, is_visible=#{isVisible} " +
            "WHERE id=#{id}")
    void update(QuestionBank questionBank);
    
    @Delete("DELETE FROM tb_question_bank WHERE id=#{id}")
    void delete(Long id);
}