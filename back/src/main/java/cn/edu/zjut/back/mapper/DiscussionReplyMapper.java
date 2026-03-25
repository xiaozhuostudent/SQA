package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.DiscussionReply;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 讨论回复Mapper接口
 */
@Mapper
public interface DiscussionReplyMapper {
    
    @Select("SELECT * FROM tb_discussion_reply WHERE discussion_id = #{discussionId} AND status = 'active' " +
            "ORDER BY is_accepted DESC, like_count DESC, create_time ASC")
    List<DiscussionReply> findByDiscussionId(Long discussionId);
    
    @Insert("INSERT INTO tb_discussion_reply (discussion_id, parent_id, content, author_id, " +
            "author_name, author_role, status) VALUES " +
            "(#{discussionId}, #{parentId}, #{content}, #{authorId}, #{authorName}, #{authorRole}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DiscussionReply reply);
    
    @Update("UPDATE tb_discussion_reply SET is_accepted = #{isAccepted}, like_count = #{likeCount} WHERE id = #{id}")
    int update(DiscussionReply reply);
    
    @Update("UPDATE tb_discussion_reply SET like_count = like_count + 1 WHERE id = #{id}")
    int incrementLikeCount(Long id);
    
    @Delete("DELETE FROM tb_discussion_reply WHERE id = #{id}")
    int deleteById(Long id);
}
