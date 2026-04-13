package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.LiveChatMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 直播聊天消息Mapper
 */
@Mapper
public interface LiveChatMessageMapper {
    
    /**
     * 保存聊天消息
     */
    @Insert("INSERT INTO live_chat_messages (live_stream_id, user_id, user_name, user_role, content, created_at) " +
            "VALUES (#{liveStreamId}, #{userId}, #{userName}, #{userRole}, #{content}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LiveChatMessage message);
    
    /**
     * 根据直播ID获取所有聊天消息
     */
    @Select("SELECT * FROM live_chat_messages WHERE live_stream_id = #{liveStreamId} ORDER BY created_at ASC")
    List<LiveChatMessage> findByLiveStreamId(Long liveStreamId);
    
    /**
     * 获取直播的消息数量
     */
    @Select("SELECT COUNT(*) FROM live_chat_messages WHERE live_stream_id = #{liveStreamId}")
    int countByLiveStreamId(Long liveStreamId);
    
    /**
     * 删除直播的所有消息
     */
    @Delete("DELETE FROM live_chat_messages WHERE live_stream_id = #{liveStreamId}")
    int deleteByLiveStreamId(Long liveStreamId);
}
