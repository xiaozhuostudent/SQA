package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.dto.AnnouncementQueryDTO;
import cn.edu.zjut.back.entity.Announcement;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 公告通知数据访问层
 */
@Mapper
public interface AnnouncementMapper {

    /**
     * 分页查询公告列表
     */
    @Select("<script>" +
            "SELECT * FROM tb_announcement " +
            "WHERE 1=1 " +
            "<if test='query.keyword != null and query.keyword != \"\"'>" +
            "AND (title LIKE CONCAT('%', #{query.keyword}, '%') " +
            "OR content LIKE CONCAT('%', #{query.keyword}, '%')) " +
            "</if>" +
            "<if test='query.type != null and query.type != \"\"'>" +
            "AND type = #{query.type} " +
            "</if>" +
            "<if test='query.priority != null and query.priority != \"\"'>" +
            "AND priority = #{query.priority} " +
            "</if>" +
            "<if test='query.targetRole != null and query.targetRole != \"\"'>" +
            "AND target_role = #{query.targetRole} " +
            "</if>" +
            "<if test='query.status != null and query.status != \"\"'>" +
            "AND status = #{query.status} " +
            "</if>" +
            "ORDER BY priority DESC, publish_time DESC " +
            "LIMIT #{offset}, #{query.size}" +
            "</script>")
    List<Announcement> queryAnnouncements(@Param("query") AnnouncementQueryDTO query, @Param("offset") int offset);

    /**
     * 统计公告总数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM tb_announcement " +
            "WHERE 1=1 " +
            "<if test='query.keyword != null and query.keyword != \"\"'>" +
            "AND (title LIKE CONCAT('%', #{query.keyword}, '%') " +
            "OR content LIKE CONCAT('%', #{query.keyword}, '%')) " +
            "</if>" +
            "<if test='query.type != null and query.type != \"\"'>" +
            "AND type = #{query.type} " +
            "</if>" +
            "<if test='query.priority != null and query.priority != \"\"'>" +
            "AND priority = #{query.priority} " +
            "</if>" +
            "<if test='query.targetRole != null and query.targetRole != \"\"'>" +
            "AND target_role = #{query.targetRole} " +
            "</if>" +
            "<if test='query.status != null and query.status != \"\"'>" +
            "AND status = #{query.status} " +
            "</if>" +
            "</script>")
    long countAnnouncements(@Param("query") AnnouncementQueryDTO query);

    /**
     * 根据角色获取已发布的公告（首页展示）
     */
    @Select("SELECT * FROM tb_announcement " +
            "WHERE status = 'published' " +
            "AND (target_role = #{role} OR target_role = 'all') " +
            "ORDER BY priority DESC, publish_time DESC " +
            "LIMIT #{limit}")
    List<Announcement> getPublishedAnnouncementsByRole(@Param("role") String role, @Param("limit") int limit);

    /**
     * 根据ID查询公告
     */
    @Select("SELECT * FROM tb_announcement WHERE id = #{id}")
    Announcement findById(@Param("id") Long id);

    /**
     * 插入公告
     */
    @Insert("INSERT INTO tb_announcement (title, content, type, priority, target_role, " +
            "publisher_id, publisher_name, status) " +
            "VALUES (#{title}, #{content}, #{type}, #{priority}, #{targetRole}, " +
            "#{publisherId}, #{publisherName}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Announcement announcement);

    /**
     * 更新公告
     */
    @Update("UPDATE tb_announcement SET " +
            "title = #{title}, " +
            "content = #{content}, " +
            "type = #{type}, " +
            "priority = #{priority}, " +
            "target_role = #{targetRole}, " +
            "status = #{status} " +
            "WHERE id = #{id}")
    int update(Announcement announcement);

    /**
     * 发布公告（修改状态）
     */
    @Update("UPDATE tb_announcement SET status = 'published', publish_time = NOW() WHERE id = #{id}")
    int publish(@Param("id") Long id);

    /**
     * 归档公告
     */
    @Update("UPDATE tb_announcement SET status = 'archived' WHERE id = #{id}")
    int archive(@Param("id") Long id);

    /**
     * 删除公告
     */
    @Delete("DELETE FROM tb_announcement WHERE id = #{id}")
    int delete(@Param("id") Long id);
}
