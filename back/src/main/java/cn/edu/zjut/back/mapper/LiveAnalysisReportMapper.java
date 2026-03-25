package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.LiveAnalysisReport;
import org.apache.ibatis.annotations.*;

@Mapper
public interface LiveAnalysisReportMapper {

    @Select("SELECT * FROM live_analysis_reports WHERE live_stream_id = #{liveStreamId} LIMIT 1")
    LiveAnalysisReport findByLiveStreamId(Long liveStreamId);

    @Insert("INSERT INTO live_analysis_reports (live_stream_id, report_content, ai_provider, prompt_snapshot, generated_at, updated_at) " +
            "VALUES (#{liveStreamId}, #{reportContent}, #{aiProvider}, #{promptSnapshot}, #{generatedAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LiveAnalysisReport report);

    @Update("UPDATE live_analysis_reports SET report_content = #{reportContent}, ai_provider = #{aiProvider}, " +
            "prompt_snapshot = #{promptSnapshot}, updated_at = #{updatedAt} WHERE id = #{id}")
    int update(LiveAnalysisReport report);

    @Delete("DELETE FROM live_analysis_reports WHERE live_stream_id = #{liveStreamId}")
    int deleteByLiveStreamId(Long liveStreamId);
}
