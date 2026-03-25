package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.dto.OperationLogQueryDTO;
import cn.edu.zjut.back.entity.OperationLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 操作日志Mapper
 */
@Mapper
public interface OperationLogMapper {

    /**
     * 分页查询操作日志
     */
    @Select("<script>" +
            "SELECT * FROM tb_operation_log " +
            "WHERE 1=1 " +
            "<if test='query.username != null and query.username != \"\"'>" +
            "  AND username LIKE CONCAT('%', #{query.username}, '%') " +
            "</if>" +
            "<if test='query.operation != null and query.operation != \"\"'>" +
            "  AND operation LIKE CONCAT('%', #{query.operation}, '%') " +
            "</if>" +
            "<if test='query.status != null and query.status != \"\"'>" +
            "  AND status = #{query.status} " +
            "</if>" +
            "<if test='query.ip != null and query.ip != \"\"'>" +
            "  AND ip = #{query.ip} " +
            "</if>" +
            "<if test='query.startTime != null and query.startTime != \"\"'>" +
            "  AND create_time &gt;= #{query.startTime} " +
            "</if>" +
            "<if test='query.endTime != null and query.endTime != \"\"'>" +
            "  AND create_time &lt;= #{query.endTime} " +
            "</if>" +
            "ORDER BY create_time DESC " +
            "LIMIT #{query.offset}, #{query.size}" +
            "</script>")
    List<OperationLog> queryLogs(@Param("query") OperationLogQueryDTO query);

    /**
     * 统计日志总数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM tb_operation_log " +
            "WHERE 1=1 " +
            "<if test='query.username != null and query.username != \"\"'>" +
            "  AND username LIKE CONCAT('%', #{query.username}, '%') " +
            "</if>" +
            "<if test='query.operation != null and query.operation != \"\"'>" +
            "  AND operation LIKE CONCAT('%', #{query.operation}, '%') " +
            "</if>" +
            "<if test='query.status != null and query.status != \"\"'>" +
            "  AND status = #{query.status} " +
            "</if>" +
            "<if test='query.ip != null and query.ip != \"\"'>" +
            "  AND ip = #{query.ip} " +
            "</if>" +
            "<if test='query.startTime != null and query.startTime != \"\"'>" +
            "  AND create_time &gt;= #{query.startTime} " +
            "</if>" +
            "<if test='query.endTime != null and query.endTime != \"\"'>" +
            "  AND create_time &lt;= #{query.endTime} " +
            "</if>" +
            "</script>")
    int countLogs(@Param("query") OperationLogQueryDTO query);

    /**
     * 根据ID查询日志详情
     */
    @Select("SELECT * FROM tb_operation_log WHERE id = #{id}")
    OperationLog findById(Long id);

    /**
     * 插入操作日志
     */
    @Insert("INSERT INTO tb_operation_log (user_id, username, operation, method, params, ip, location, status, error_msg, execution_time) " +
            "VALUES (#{userId}, #{username}, #{operation}, #{method}, #{params}, #{ip}, #{location}, #{status}, #{errorMsg}, #{executionTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OperationLog log);

    /**
     * 批量删除日志
     */
    @Delete("<script>" +
            "DELETE FROM tb_operation_log WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "  #{id}" +
            "</foreach>" +
            "</script>")
    int batchDelete(@Param("ids") List<Long> ids);

    /**
     * 清空日志
     */
    @Delete("TRUNCATE TABLE tb_operation_log")
    int clearAll();

    /**
     * 获取操作类型统计
     */
    @Select("SELECT operation, COUNT(*) as count FROM tb_operation_log " +
            "GROUP BY operation ORDER BY count DESC LIMIT 10")
    List<java.util.Map<String, Object>> getOperationStats();

    /**
     * 获取用户操作统计
     */
    @Select("SELECT username, COUNT(*) as count FROM tb_operation_log " +
            "WHERE username IS NOT NULL " +
            "GROUP BY username ORDER BY count DESC LIMIT 10")
    List<java.util.Map<String, Object>> getUserStats();
}
