package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.Resource;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 教学资源Mapper
 */
@Mapper
public interface ResourceMapper {
    
    @Select("<script>" +
            "SELECT * FROM tb_resource " +
            "WHERE status = 'approved' " +
            "<if test='type != null and type != \"all\"'> AND type = #{type} </if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            " AND (name LIKE CONCAT('%', #{keyword}, '%') " +
            " OR course_name LIKE CONCAT('%', #{keyword}, '%') " +
            " OR tags LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "ORDER BY create_time DESC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<Resource> selectResourceList(@Param("type") String type, 
                                       @Param("keyword") String keyword,
                                       @Param("offset") Integer offset, 
                                       @Param("pageSize") Integer pageSize);
    
    @Select("<script>" +
            "SELECT COUNT(*) FROM tb_resource " +
            "WHERE status = 'approved' " +
            "<if test='type != null and type != \"all\"'> AND type = #{type} </if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            " AND (name LIKE CONCAT('%', #{keyword}, '%') " +
            " OR course_name LIKE CONCAT('%', #{keyword}, '%') " +
            " OR tags LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "</script>")
    long countResourceList(@Param("type") String type, @Param("keyword") String keyword);
    
    @Select("SELECT * FROM tb_resource WHERE id = #{id}")
    Resource selectById(Long id);
    
    @Select("SELECT * FROM tb_resource WHERE course_id = #{courseId} AND status = 'approved' ORDER BY create_time DESC")
    List<Resource> selectByCourseId(Long courseId);
    
    @Update("UPDATE tb_resource SET download_count = download_count + 1 WHERE id = #{id}")
    int incrementDownloadCount(Long id);
    
    @Select("SELECT * FROM tb_resource WHERE uploader_id = #{uploaderId} ORDER BY create_time DESC")
    List<Resource> selectByUploaderId(Long uploaderId);
    
    @Select("<script>" +
            "SELECT * FROM tb_resource " +
            "WHERE course_id IN " +
            "<foreach collection='courseIds' item='courseId' open='(' separator=',' close=')'>" +
            "#{courseId}" +
            "</foreach>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<Resource> selectByCourseIds(@Param("courseIds") List<Long> courseIds);
    
    @Insert("INSERT INTO tb_resource (course_id, course_name, name, type, description, file_url, " +
            "file_size, uploader_id, uploader_name, download_count, tags, status, create_time) " +
            "VALUES (#{courseId}, #{courseName}, #{name}, #{type}, #{description}, #{fileUrl}, " +
            "#{fileSize}, #{uploaderId}, #{uploaderName}, 0, #{tags}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Resource resource);
    
    @Update("<script>" +
            "UPDATE tb_resource " +
            "<set>" +
            "<if test='name != null'>name = #{name},</if>" +
            "<if test='description != null'>description = #{description},</if>" +
            "<if test='tags != null'>tags = #{tags},</if>" +
            "<if test='status != null'>status = #{status},</if>" +
            "</set>" +
            "WHERE id = #{id}" +
            "</script>")
    int update(Resource resource);
    
    @Delete("DELETE FROM tb_resource WHERE id = #{id}")
    int deleteById(Long id);
    
    @Select("SELECT COUNT(*) FROM tb_resource WHERE type = #{type} AND status = 'approved'")
    long countByType(String type);
    
    // ==================== 统计方法 ====================
    
    /**
     * 统计所有资源数量
     */
    @Select("SELECT COUNT(*) FROM tb_resource")
    int countAll();
    
    /**
     * 统计总下载次数
     */
    @Select("SELECT COALESCE(SUM(download_count), 0) FROM tb_resource")
    long countTotalDownloads();
    
    /**
     * 获取热门资源排行（按下载量）
     */
    @Select("SELECT id, name, download_count, type, uploader_name " +
            "FROM tb_resource " +
            "WHERE status = 'approved' " +
            "ORDER BY download_count DESC " +
            "LIMIT #{limit}")
    List<java.util.Map<String, Object>> getTopResources(@Param("limit") int limit);
    
    /**
     * 按类型统计资源数量
     */
    @Select("SELECT type, COUNT(*) as count " +
            "FROM tb_resource " +
            "GROUP BY type")
    List<java.util.Map<String, Object>> getResourcesByType();
    
    /**
     * 按状态统计资源数量
     */
    @Select("SELECT status, COUNT(*) as count " +
            "FROM tb_resource " +
            "GROUP BY status")
    List<java.util.Map<String, Object>> getResourcesByStatus();
    
    /**
     * 统计某课程的资源数量
     */
    @Select("SELECT COUNT(*) FROM tb_resource WHERE course_id = #{courseId}")
    int countByCourseId(@Param("courseId") Long courseId);
}
