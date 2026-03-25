package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.LearningProgress;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 学习进度Mapper接口
 */
@Mapper
public interface LearningProgressMapper {
    
    @Select("SELECT * FROM tb_learning_progress WHERE student_id = #{studentId} AND resource_id = #{resourceId}")
    LearningProgress findByStudentAndResource(@Param("studentId") Long studentId, @Param("resourceId") Long resourceId);
    
    @Select("SELECT * FROM tb_learning_progress WHERE student_id = #{studentId} AND course_id = #{courseId}")
    List<LearningProgress> findByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);
    
    @Insert("INSERT INTO tb_learning_progress (student_id, course_id, resource_id, resource_type, " +
            "progress_percent, duration_seconds, last_position, is_completed) VALUES " +
            "(#{studentId}, #{courseId}, #{resourceId}, #{resourceType}, #{progressPercent}, " +
            "#{durationSeconds}, #{lastPosition}, #{isCompleted})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LearningProgress progress);
    
    @Update("UPDATE tb_learning_progress SET progress_percent = #{progressPercent}, " +
            "duration_seconds = #{durationSeconds}, last_position = #{lastPosition}, " +
            "is_completed = #{isCompleted}, complete_time = #{completeTime} WHERE id = #{id}")
    int update(LearningProgress progress);
}
