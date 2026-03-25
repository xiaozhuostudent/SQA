package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.Discussion;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 讨论Mapper接口
 */
@Mapper
public interface DiscussionMapper {
    
    @Select("SELECT * FROM tb_discussion WHERE id = #{id}")
    Discussion findById(Long id);
    
    @Select("SELECT * FROM tb_discussion WHERE course_id = #{courseId} AND status = 'active' " +
            "ORDER BY is_pinned DESC, create_time DESC")
    List<Discussion> findByCourseId(Long courseId);
    
    @Insert("INSERT INTO tb_discussion (course_id, course_name, title, content, topic_type, " +
            "author_id, author_name, author_role, status) VALUES " +
            "(#{courseId}, #{courseName}, #{title}, #{content}, #{topicType}, " +
            "#{authorId}, #{authorName}, #{authorRole}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Discussion discussion);
    
    @Update("UPDATE tb_discussion SET title = #{title}, content = #{content}, is_resolved = #{isResolved}, " +
            "view_count = #{viewCount}, reply_count = #{replyCount} WHERE id = #{id}")
    int update(Discussion discussion);
    
    @Update("UPDATE tb_discussion SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(Long id);
    
    @Update("UPDATE tb_discussion SET reply_count = reply_count + 1 WHERE id = #{id}")
    int incrementReplyCount(Long id);
    
    @Update("UPDATE tb_discussion SET reply_count = reply_count - 1 WHERE id = #{id} AND reply_count > 0")
    int decrementReplyCount(Long id);
    
    @Delete("DELETE FROM tb_discussion WHERE id = #{id}")
    int delete(Long id);
}
