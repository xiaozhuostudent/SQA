package cn.edu.zjut.back.mapper;

import cn.edu.zjut.back.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper {
    
    @Select("SELECT * FROM tb_user WHERE id = #{id}")
    User findById(Long id);
    
    @Select("SELECT * FROM tb_user WHERE username = #{username}")
    User findByUsername(String username);
    
    @Select("SELECT * FROM tb_user WHERE email = #{email}")
    User findByEmail(String email);
    
    @Select("SELECT * FROM tb_user WHERE phone = #{phone}")
    User findByPhone(String phone);
    
    @Select("SELECT * FROM tb_user WHERE role = #{role}")
    List<User> findByRole(String role);
    
    @Select("SELECT * FROM tb_user")
    List<User> findAll();
    
    @Insert("INSERT INTO tb_user (username, password, real_name, email, phone, role, avatar, status, create_time, update_time) " +
            "VALUES (#{username}, #{password}, #{realName}, #{email}, #{phone}, #{role}, #{avatar}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
    
    @Update("UPDATE tb_user SET password = #{password}, real_name = #{realName}, gender = #{gender}, email = #{email}, phone = #{phone}, " +
            "avatar = #{avatar}, status = #{status}, update_time = NOW() WHERE id = #{id}")
    int update(User user);
    
    @Delete("DELETE FROM tb_user WHERE id = #{id}")
    int delete(Long id);
    
    @Update("UPDATE tb_user SET password = #{password}, update_time = NOW() WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);
    
    @Update("UPDATE tb_user SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    /**
     * 根据ID查询用户详细信息(包含学生或教师扩展信息)
     */
    @Select("SELECT u.*, " +
            "s.student_number, s.major, s.class_name, s.enrollment_year, s.grade, " +
            "t.teacher_number, t.department, t.title, t.research_field, " +
            "a.admin_number, a.permission_level " +
            "FROM tb_user u " +
            "LEFT JOIN tb_student s ON u.id = s.user_id " +
            "LEFT JOIN tb_teacher t ON u.id = t.user_id " +
            "LEFT JOIN tb_admin a ON u.id = a.user_id " +
            "WHERE u.id = #{id}")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "username", column = "username"),
        @Result(property = "password", column = "password"),
        @Result(property = "role", column = "role"),
        @Result(property = "realName", column = "real_name"),
        @Result(property = "gender", column = "gender"),
        @Result(property = "email", column = "email"),
        @Result(property = "phone", column = "phone"),
        @Result(property = "avatar", column = "avatar"),
        @Result(property = "status", column = "status"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time"),
        @Result(property = "studentNumber", column = "student_number"),
        @Result(property = "major", column = "major"),
        @Result(property = "className", column = "class_name"),
        @Result(property = "enrollmentYear", column = "enrollment_year"),
        @Result(property = "grade", column = "grade"),
        @Result(property = "teacherNumber", column = "teacher_number"),
        @Result(property = "department", column = "department"),
        @Result(property = "title", column = "title"),
        @Result(property = "researchField", column = "research_field"),
        @Result(property = "adminNumber", column = "admin_number"),
        @Result(property = "permissionLevel", column = "permission_level")
    })
    User findByIdWithDetails(Long id);
    
    /**
     * 更新学生扩展信息
     */
    @Update("<script>" +
            "UPDATE tb_student SET " +
            "<if test='updates.studentNumber != null'>student_number = #{updates.studentNumber},</if>" +
            "<if test='updates.major != null'>major = #{updates.major},</if>" +
            "<if test='updates.className != null'>class_name = #{updates.className},</if>" +
            "<if test='updates.enrollmentYear != null'>enrollment_year = #{updates.enrollmentYear},</if>" +
            "<if test='updates.grade != null'>grade = #{updates.grade},</if>" +
            "user_id = #{userId} " +
            "WHERE user_id = #{userId}" +
            "</script>")
    int updateStudentInfo(@Param("userId") Long userId, @Param("updates") java.util.Map<String, Object> updates);
    
    /**
     * 更新教师扩展信息
     */
    @Update("<script>" +
            "UPDATE tb_teacher SET " +
            "<if test='updates.teacherNumber != null'>teacher_number = #{updates.teacherNumber},</if>" +
            "<if test='updates.department != null'>department = #{updates.department},</if>" +
            "<if test='updates.title != null'>title = #{updates.title},</if>" +
            "<if test='updates.researchField != null'>research_field = #{updates.researchField},</if>" +
            "user_id = #{userId} " +
            "WHERE user_id = #{userId}" +
            "</script>")
    int updateTeacherInfo(@Param("userId") Long userId, @Param("updates") java.util.Map<String, Object> updates);
    
    /**
     * 更新管理员扩展信息
     */
    @Update("<script>" +
            "UPDATE tb_admin SET " +
            "<if test='updates.adminNumber != null'>admin_number = #{updates.adminNumber},</if>" +
            "<if test='updates.permissionLevel != null'>permission_level = #{updates.permissionLevel},</if>" +
            "user_id = #{userId} " +
            "WHERE user_id = #{userId}" +
            "</script>")
    int updateAdminInfo(@Param("userId") Long userId, @Param("updates") java.util.Map<String, Object> updates);
    
    /**
     * 更新头像
     */
    @Update("UPDATE tb_user SET avatar = #{avatar}, update_time = NOW() WHERE id = #{id}")
    int updateAvatar(@Param("id") Long id, @Param("avatar") String avatar);
    
    /**
     * 根据条件分页查询用户（管理员）
     */
    @Select("<script>" +
            "SELECT u.*, " +
            "s.student_number, s.major, s.class_name, s.enrollment_year, s.grade, " +
            "t.teacher_number, t.department, t.title, t.research_field " +
            "FROM tb_user u " +
            "LEFT JOIN tb_student s ON u.id = s.user_id AND u.role = 'student' " +
            "LEFT JOIN tb_teacher t ON u.id = t.user_id AND u.role = 'teacher' " +
            "WHERE 1=1 " +
            "<if test='query.role != null and query.role != \"\"'>AND u.role = #{query.role}</if> " +
            "<if test='query.gender != null and query.gender != \"\"'>AND u.gender = #{query.gender}</if> " +
            "<if test='query.status != null and query.status != \"\"'>AND u.status = #{query.status}</if> " +
            "<if test='query.major != null and query.major != \"\"'>AND s.major = #{query.major}</if> " +
            "<if test='query.className != null and query.className != \"\"'>AND s.class_name = #{query.className}</if> " +
            "<if test='query.keyword != null and query.keyword != \"\"'>" +
            "AND (u.username LIKE CONCAT('%', #{query.keyword}, '%') " +
            "OR u.real_name LIKE CONCAT('%', #{query.keyword}, '%') " +
            "OR u.email LIKE CONCAT('%', #{query.keyword}, '%'))" +
            "</if> " +
            "ORDER BY u.create_time DESC " +
            "LIMIT #{offset}, #{query.pageSize}" +
            "</script>")
    List<User> queryUsersWithPagination(@Param("query") cn.edu.zjut.back.dto.UserQueryDTO query, @Param("offset") int offset);
    
    /**
     * 统计查询结果总数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM tb_user u " +
            "LEFT JOIN tb_student s ON u.id = s.user_id AND u.role = 'student' " +
            "WHERE 1=1 " +
            "<if test='query.role != null and query.role != \"\"'>AND u.role = #{query.role}</if> " +
            "<if test='query.gender != null and query.gender != \"\"'>AND u.gender = #{query.gender}</if> " +
            "<if test='query.status != null and query.status != \"\"'>AND u.status = #{query.status}</if> " +
            "<if test='query.major != null and query.major != \"\"'>AND s.major = #{query.major}</if> " +
            "<if test='query.className != null and query.className != \"\"'>AND s.class_name = #{query.className}</if> " +
            "<if test='query.keyword != null and query.keyword != \"\"'>" +
            "AND (u.username LIKE CONCAT('%', #{query.keyword}, '%') " +
            "OR u.real_name LIKE CONCAT('%', #{query.keyword}, '%') " +
            "OR u.email LIKE CONCAT('%', #{query.keyword}, '%'))" +
            "</if>" +
            "</script>")
    long countUsersByQuery(@Param("query") cn.edu.zjut.back.dto.UserQueryDTO query);
    
    /**
     * 批量插入用户
     */
    @Insert("<script>" +
            "INSERT INTO tb_user (username, password, real_name, email, phone, gender, role, status, create_time, update_time) VALUES " +
            "<foreach collection='users' item='user' separator=','>" +
            "(#{user.username}, #{user.password}, #{user.realName}, #{user.email}, #{user.phone}, #{user.gender}, #{user.role}, 'active', NOW(), NOW())" +
            "</foreach>" +
            "</script>")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int batchInsert(@Param("users") List<User> users);
    
    /**
     * 插入学生扩展信息
     */
    @Insert("INSERT INTO tb_student (user_id, student_number, major, class_name, enrollment_year, grade) " +
            "VALUES (#{userId}, #{studentNumber}, #{major}, #{className}, #{enrollmentYear}, #{grade})")
    int insertStudentInfo(@Param("userId") Long userId, @Param("studentNumber") String studentNumber, 
                         @Param("major") String major, @Param("className") String className,
                         @Param("enrollmentYear") Integer enrollmentYear, @Param("grade") Integer grade);
    
    /**
     * 插入教师扩展信息
     */
    @Insert("INSERT INTO tb_teacher (user_id, teacher_number, department, title, research_field) " +
            "VALUES (#{userId}, #{teacherNumber}, #{department}, #{title}, #{researchField})")
    int insertTeacherInfo(@Param("userId") Long userId, @Param("teacherNumber") String teacherNumber,
                         @Param("department") String department, @Param("title") String title,
                         @Param("researchField") String researchField);
    
    /**
     * 查询教师授课统计
     */
    @Select("SELECT u.id as teacherId, u.real_name as teacherName, u.username, " +
            "COUNT(DISTINCT c.id) as courseCount, " +
            "COUNT(DISTINCT sc.student_id) as studentCount, " +
            "COUNT(DISTINCT CASE WHEN sc.status = 'active' THEN sc.student_id END) as activeStudentCount " +
            "FROM tb_user u " +
            "LEFT JOIN tb_course c ON u.id = c.teacher_id " +
            "LEFT JOIN tb_student_course sc ON c.id = sc.course_id " +
            "WHERE u.role = 'teacher' AND u.id = #{teacherId} " +
            "GROUP BY u.id, u.real_name, u.username")
    cn.edu.zjut.back.vo.TeacherStatsVO getTeacherStats(@Param("teacherId") Long teacherId);
    
    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(*) FROM tb_user WHERE username = #{username}")
    int checkUsernameExists(String username);
    
    /**
     * 获取所有专业列表（去重）
     */
    @Select("SELECT DISTINCT major FROM tb_student WHERE major IS NOT NULL ORDER BY major")
    List<String> getAllMajors();
    
    /**
     * 获取所有班级列表（去重）
     */
    @Select("SELECT DISTINCT class_name FROM tb_student WHERE class_name IS NOT NULL ORDER BY class_name")
    List<String> getAllClasses();
    
    // ==================== 统计方法 ====================
    
    /**
     * 统计所有用户数量
     */
    @Select("SELECT COUNT(*) FROM tb_user")
    int countAll();
    
    /**
     * 按角色统计用户数量
     */
    @Select("SELECT COUNT(*) FROM tb_user WHERE role = #{role}")
    int countByRole(@Param("role") String role);
    
    /**
     * 统计今日新增用户数
     */
    @Select("SELECT COUNT(*) FROM tb_user WHERE DATE(create_time) = CURDATE()")
    int countNewUsersToday();
    
    /**
     * 统计本周新增用户数
     */
    @Select("SELECT COUNT(*) FROM tb_user WHERE YEARWEEK(create_time) = YEARWEEK(NOW())")
    int countNewUsersThisWeek();
    
    /**
     * 统计本月新增用户数
     */
    @Select("SELECT COUNT(*) FROM tb_user WHERE YEAR(create_time) = YEAR(NOW()) AND MONTH(create_time) = MONTH(NOW())")
    int countNewUsersThisMonth();
    
    /**
     * 通过用户ID获取学生ID
     */
    @Select("SELECT id FROM tb_student WHERE user_id = #{userId}")
    Long getStudentIdByUserId(@Param("userId") Long userId);
    
    /**
     * 通过学生ID获取用户ID
     */
    @Select("SELECT user_id FROM tb_student WHERE id = #{studentId}")
    Long getUserIdByStudentId(@Param("studentId") Long studentId);
}
