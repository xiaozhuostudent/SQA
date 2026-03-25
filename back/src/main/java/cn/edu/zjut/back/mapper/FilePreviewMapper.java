package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.FilePreview;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 文件预览Mapper
 */
@Mapper
public interface FilePreviewMapper {
    
    @Select("SELECT * FROM tb_file_preview WHERE id = #{id}")
    FilePreview findById(Long id);
    
    @Select("SELECT * FROM tb_file_preview WHERE viewer_id = #{viewerId}")
    List<FilePreview> findByViewerId(Long viewerId);
    
    @Select("SELECT * FROM tb_file_preview WHERE viewer_id = #{viewerId} AND resource_id = #{resourceId}")
    FilePreview findByViewerAndResource(@Param("viewerId") Long viewerId, @Param("resourceId") Long resourceId);
    
    @Insert("INSERT INTO tb_file_preview(resource_id, resource_type, file_url, file_type, file_size, " +
            "preview_type, conversion_status, viewer_id, viewer_name, view_count, last_view_time) " +
            "VALUES(#{resourceId}, #{resourceType}, #{fileUrl}, #{fileType}, #{fileSize}, " +
            "#{previewType}, #{conversionStatus}, #{viewerId}, #{viewerName}, #{viewCount}, #{lastViewTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FilePreview filePreview);
    
    @Update("UPDATE tb_file_preview SET conversion_status = #{conversionStatus}, " +
            "preview_url = #{previewUrl}, view_count = #{viewCount}, last_view_time = #{lastViewTime} " +
            "WHERE id = #{id}")
    int update(FilePreview filePreview);
    
    @Delete("DELETE FROM tb_file_preview WHERE id = #{id}")
    int delete(Long id);
}
