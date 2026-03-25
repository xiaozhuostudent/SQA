package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.LiveStream;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 直播Mapper接口
 */
@Mapper
public interface LiveStreamMapper {
    
    @Select("SELECT * FROM live_streams WHERE id = #{id}")
    LiveStream findById(Long id);
    
    @Select("SELECT * FROM live_streams ORDER BY create_time DESC")
    List<LiveStream> findAll();
    
    @Select("SELECT * FROM live_streams WHERE teacher_id = #{teacherId} ORDER BY create_time DESC")
    List<LiveStream> findByTeacherId(Long teacherId);
    
    @Select("SELECT * FROM live_streams WHERE status = #{status} ORDER BY scheduled_time DESC")
    List<LiveStream> findByStatus(String status);
    
    @Select("SELECT * FROM live_streams WHERE course_id = #{courseId} ORDER BY create_time DESC")
    List<LiveStream> findByCourseId(Long courseId);
    
    @Insert("INSERT INTO live_streams (title, description, teacher_id, teacher_name, course_id, " +
            "course_name, status, stream_key, stream_url, play_url, cover_image, scheduled_time, " +
            "viewer_count, total_views, create_time, update_time) " +
            "VALUES (#{title}, #{description}, #{teacherId}, #{teacherName}, #{courseId}, " +
            "#{courseName}, #{status}, #{streamKey}, #{streamUrl}, #{playUrl}, #{coverImage}, " +
            "#{scheduledTime}, #{viewerCount}, #{totalViews}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(LiveStream liveStream);
    
    @Update("UPDATE live_streams SET title = #{title}, description = #{description}, " +
            "course_id = #{courseId}, course_name = #{courseName}, scheduled_time = #{scheduledTime}, " +
            "cover_image = #{coverImage}, update_time = NOW() WHERE id = #{id}")
    int update(LiveStream liveStream);
    
    @Update("UPDATE live_streams SET status = #{status}, start_time = #{startTime}, " +
            "update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, 
                     @Param("startTime") LocalDateTime startTime);
    
    @Update("UPDATE live_streams SET status = 'ended', end_time = NOW(), " +
            "update_time = NOW() WHERE id = #{id}")
    int endLive(Long id);
    
    @Update("UPDATE live_streams SET viewer_count = #{viewerCount}, " +
            "total_views = #{totalViews}, update_time = NOW() WHERE id = #{id}")
    int updateViewerStats(@Param("id") Long id, @Param("viewerCount") Integer viewerCount, 
                          @Param("totalViews") Integer totalViews);
    
    @Delete("DELETE FROM live_streams WHERE id = #{id}")
    int delete(Long id);
}