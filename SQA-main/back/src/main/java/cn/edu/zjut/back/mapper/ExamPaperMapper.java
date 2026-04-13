package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.ExamPaper;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 试卷Mapper接口
 */
@Mapper
public interface ExamPaperMapper {
    
    @Select("SELECT * FROM tb_exam_paper WHERE id = #{id}")
    ExamPaper findById(Long id);
    
    @Select("SELECT * FROM tb_exam_paper WHERE course_id = #{courseId} ORDER BY create_time DESC")
    List<ExamPaper> findByCourseId(Long courseId);
    
    @Select("SELECT * FROM tb_exam_paper WHERE status = #{status} ORDER BY create_time DESC")
    List<ExamPaper> findByStatus(String status);
    
    @Select("SELECT * FROM tb_exam_paper WHERE creator_id = #{creatorId} ORDER BY create_time DESC")
    List<ExamPaper> findByCreatorId(Long creatorId);
    
    @Insert("INSERT INTO tb_exam_paper (course_id, course_name, title, description, total_score, pass_score, " +
            "duration, start_time, end_time, exam_type, shuffle_questions, shuffle_options, show_answer, " +
            "allow_review, face_recognition_enabled, creator_id, creator_name, status) VALUES " +
            "(#{courseId}, #{courseName}, #{title}, #{description}, #{totalScore}, #{passScore}, " +
            "#{duration}, #{startTime}, #{endTime}, #{examType}, #{shuffleQuestions}, #{shuffleOptions}, " +
            "#{showAnswer}, #{allowReview}, #{faceRecognitionEnabled}, #{creatorId}, #{creatorName}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ExamPaper examPaper);
    
    @Update("UPDATE tb_exam_paper SET course_id = #{courseId}, course_name = #{courseName}, " +
            "title = #{title}, description = #{description}, total_score = #{totalScore}, " +
            "pass_score = #{passScore}, duration = #{duration}, start_time = #{startTime}, " +
            "end_time = #{endTime}, face_recognition_enabled = #{faceRecognitionEnabled}, " +
            "status = #{status}, paper_url = #{paperUrl} WHERE id = #{id}")
    int update(ExamPaper examPaper);
    
    @Delete("DELETE FROM tb_exam_paper WHERE id = #{id}")
    int delete(Long id);
}
