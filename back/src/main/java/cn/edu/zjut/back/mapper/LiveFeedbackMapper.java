package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.LiveFeedback;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface LiveFeedbackMapper {

    @Insert("INSERT INTO live_feedback (live_stream_id, user_id, user_name, user_role, phase, content, created_at) " +
            "VALUES (#{liveStreamId}, #{userId}, #{userName}, #{userRole}, #{phase}, #{content}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LiveFeedback feedback);

    @Select("SELECT * FROM live_feedback WHERE live_stream_id = #{liveStreamId} ORDER BY created_at DESC")
    List<LiveFeedback> findByLiveStreamId(Long liveStreamId);

    @Delete("DELETE FROM live_feedback WHERE live_stream_id = #{liveStreamId}")
    int deleteByLiveStreamId(Long liveStreamId);
}
