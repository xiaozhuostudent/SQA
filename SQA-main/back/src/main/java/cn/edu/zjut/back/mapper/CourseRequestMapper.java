package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.CourseRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 课程请求Mapper
 */
@Mapper
public interface CourseRequestMapper {
    
    @Insert("INSERT INTO tb_course_request (request_type, teacher_id, teacher_name, " +
            "course_id, course_name, course_code, credit, capacity, semester, category, description, " +
            "reason, file_name, file_key, status) " +
            "VALUES (#{requestType}, #{teacherId}, #{teacherName}, " +
            "#{courseId}, #{courseName}, #{courseCode}, #{credit}, #{capacity}, #{semester}, #{category}, #{description}, " +
            "#{reason}, #{fileName}, #{fileKey}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CourseRequest request);
    
    @Select("SELECT * FROM tb_course_request WHERE id = #{id}")
    CourseRequest findById(Long id);
    
    @Select("SELECT * FROM tb_course_request WHERE teacher_id = #{teacherId} ORDER BY create_time DESC")
    List<CourseRequest> findByTeacherId(Long teacherId);
    
    @Select("SELECT * FROM tb_course_request WHERE status = #{status} ORDER BY create_time DESC")
    List<CourseRequest> findByStatus(String status);
    
    @Select("SELECT * FROM tb_course_request ORDER BY create_time DESC")
    List<CourseRequest> findAll();
    
    @Update("UPDATE tb_course_request SET " +
            "status = #{status}, admin_id = #{adminId}, admin_name = #{adminName}, " +
            "admin_comment = #{adminComment}, review_time = NOW() " +
            "WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, 
                    @Param("status") String status,
                    @Param("adminId") Long adminId,
                    @Param("adminName") String adminName,
                    @Param("adminComment") String adminComment);
    
    @Delete("DELETE FROM tb_course_request WHERE id = #{id}")
    int deleteById(Long id);
    
    @Select("<script>" +
            "SELECT * FROM tb_course_request WHERE 1=1 " +
            "<if test='status != null'>AND status = #{status}</if> " +
            "<if test='requestType != null'>AND request_type = #{requestType}</if> " +
            "ORDER BY create_time DESC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    List<CourseRequest> queryWithPagination(@Param("status") String status,
                                           @Param("requestType") String requestType,
                                           @Param("offset") int offset,
                                           @Param("pageSize") int pageSize);
    
    @Select("<script>" +
            "SELECT COUNT(*) FROM tb_course_request WHERE 1=1 " +
            "<if test='status != null'>AND status = #{status}</if> " +
            "<if test='requestType != null'>AND request_type = #{requestType}</if>" +
            "</script>")
    long count(@Param("status") String status, @Param("requestType") String requestType);
}
