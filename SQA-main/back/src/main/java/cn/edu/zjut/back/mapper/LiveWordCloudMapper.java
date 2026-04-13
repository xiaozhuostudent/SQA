package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.LiveWordCloud;
import org.apache.ibatis.annotations.*;

/**
 * 直播词云Mapper
 */
@Mapper
public interface LiveWordCloudMapper {
    
    /**
     * 保存词云数据
     */
    @Insert("INSERT INTO live_word_clouds (live_stream_id, word_data, total_messages, total_words) " +
            "VALUES (#{liveStreamId}, #{wordData}, #{totalMessages}, #{totalWords})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LiveWordCloud wordCloud);
    
    /**
     * 更新词云数据
     */
    @Update("UPDATE live_word_clouds SET word_data = #{wordData}, total_messages = #{totalMessages}, " +
            "total_words = #{totalWords} WHERE live_stream_id = #{liveStreamId}")
    int update(LiveWordCloud wordCloud);
    
    /**
     * 根据直播ID获取词云数据
     */
    @Select("SELECT * FROM live_word_clouds WHERE live_stream_id = #{liveStreamId}")
    LiveWordCloud findByLiveStreamId(Long liveStreamId);
    
    /**
     * 删除词云数据
     */
    @Delete("DELETE FROM live_word_clouds WHERE live_stream_id = #{liveStreamId}")
    int deleteByLiveStreamId(Long liveStreamId);
}
