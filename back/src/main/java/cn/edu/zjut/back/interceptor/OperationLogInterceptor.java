package cn.edu.zjut.back.interceptor;

import cn.edu.zjut.back.entity.User;
import cn.edu.zjut.back.mapper.UserMapper;
import cn.edu.zjut.back.utils.JwtUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

/**
 * 操作日志拦截器 - 自动记录所有用户操作
 */
@Component
public class OperationLogInterceptor implements HandlerInterceptor {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;
    
    @PostConstruct
    public void init() {
        System.out.println("==================================================");
        System.out.println("【日志拦截器】OperationLogInterceptor 已成功加载！");
        System.out.println("==================================================");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("========================================");
        System.out.println("[日志拦截器-preHandle] 拦截到请求: " + request.getRequestURI());
        System.out.println("[日志拦截器-preHandle] 请求方法: " + request.getMethod());
        System.out.println("[日志拦截器-preHandle] Authorization: " + (request.getHeader("Authorization") != null ? "存在" : "不存在"));
        System.out.println("========================================");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) throws Exception {
        String uri = request.getRequestURI();
        System.out.println("========================================");
        System.out.println("[日志拦截器-afterCompletion] 完成请求: " + uri);
        System.out.println("[日志拦截器-afterCompletion] 响应状态: " + response.getStatus());
        System.out.println("========================================");
        
        // 同步记录日志，便于调试
        try {
            recordLog(request, response, ex);
        } catch (Exception e) {
            System.err.println("[日志拦截器] 记录操作日志失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void recordLog(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        try {
            // 获取用户信息
            User user = getCurrentUser(request);
            if (user == null) {
                System.out.println("[日志拦截器] 未找到当前用户，跳过记录");
                return; // 未登录用户不记录
            }

            String uri = request.getRequestURI();
            String method = request.getMethod();
            String ip = getIpAddress(request);
            String status = (ex == null && response.getStatus() < 400) ? "success" : "failed";
            String errorMsg = ex != null ? ex.getMessage() : null;
            
            // 解析操作信息
            String operation = parseOperation(uri, method);
            String module = parseModule(uri);
            String targetType = parseTargetType(uri);
            
            // 根据角色记录到不同的表
            String role = user.getRole();
            
            System.out.println(String.format("[日志拦截器] 用户=%s(ID=%d), 角色=%s, 操作=%s, 模块=%s, URI=%s", 
                user.getUsername(), user.getId(), role, operation, module, uri));
            
            if ("admin".equals(role)) {
                recordAdminLog(user, operation, module, targetType, method, uri, ip, status, errorMsg);
                System.out.println("[日志拦截器] ✓ 管理员日志已记录");
            } else if ("teacher".equals(role)) {
                recordTeacherLog(user, operation, module, targetType, method, uri, ip, status, errorMsg);
                System.out.println("[日志拦截器] ✓ 教师日志已记录");
            } else if ("student".equals(role)) {
                recordStudentLog(user, operation, module, targetType, method, uri, ip, status, errorMsg);
                System.out.println("[日志拦截器] ✓ 学生日志已记录");
            }
        } catch (Exception e) {
            // 打印异常以便调试
            System.err.println("[日志拦截器] 记录日志失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void recordAdminLog(User user, String operation, String module, String targetType,
                                String method, String uri, String ip, String status, String errorMsg) {
        String sql = "INSERT INTO tb_admin_operation_log " +
                    "(admin_id, admin_name, operation, module, request_url, ip_address) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        jdbcTemplate.update(sql,
            user.getId(),
            user.getRealName() != null ? user.getRealName() : user.getUsername(),
            operation,
            module,
            uri,
            ip
        );
    }

    private void recordTeacherLog(User user, String operation, String module, String targetType,
                                  String method, String uri, String ip, String status, String errorMsg) {
        String sql = "INSERT INTO tb_teacher_operation_log " +
                    "(teacher_id, teacher_name, operation, module, request_url, ip_address) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        jdbcTemplate.update(sql,
            user.getId(),
            user.getRealName() != null ? user.getRealName() : user.getUsername(),
            operation,
            module,
            uri,
            ip
        );
    }

    private void recordStudentLog(User user, String operation, String module, String targetType,
                                  String method, String uri, String ip, String status, String errorMsg) {
        String sql = "INSERT INTO tb_student_operation_log " +
                    "(student_id, student_name, operation, module, request_url, ip_address) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        jdbcTemplate.update(sql,
            user.getId(),
            user.getRealName() != null ? user.getRealName() : user.getUsername(),
            operation,
            module,
            uri,
            ip
        );
    }

    private User getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        System.out.println("[日志拦截器] Authorization header: " + (token != null ? "存在" : "不存在"));
        
        if (token == null || !token.startsWith("Bearer ")) {
            System.out.println("[日志拦截器] Token为空或格式不正确");
            return null;
        }
        
        token = token.substring(7);
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            System.out.println("[日志拦截器] 从Token解析到用户ID: " + userId);
            User user = userMapper.findByIdWithDetails(userId);
            System.out.println("[日志拦截器] 查询到用户: " + (user != null ? user.getUsername() + ", 角色:" + user.getRole() : "null"));
            return user;
        } catch (Exception e) {
            System.err.println("[日志拦截器] 获取用户失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip : "127.0.0.1";
    }

    private String parseOperation(String uri, String method) {
        // 优先判断特殊操作
        if (uri.contains("/login")) return "登录";
        if (uri.contains("/logout")) return "登录";
        if (uri.contains("/register")) return "新增";
        
        // 根据HTTP方法判断操作类型
        if ("POST".equals(method)) return "新增";
        if ("PUT".equals(method)) return "修改";
        if ("DELETE".equals(method)) return "删除";
        if ("GET".equals(method)) return "查询";
        
        return "查询";
    }

    private String parseModule(String uri) {
        // 精确匹配，优先级从高到低
        // 资源管理相关（避免被其他规则误判）
        if (uri.matches(".*/admin/resource.*")) return "资源管理";
        if (uri.matches(".*/teacher/resource.*")) return "资源管理";
        if (uri.matches(".*/student/resource.*")) return "资源管理";        if (uri.matches(".*/resource.*")) return "资源管理";        
        // 课程管理
        if (uri.matches(".*/course.*")) return "课程管理";
        
        // 作业管理
        if (uri.matches(".*/homework.*")) return "作业管理";
        
        // 考试管理
        if (uri.matches(".*/exam.*")) return "考试管理";
        
        // 讨论管理
        if (uri.matches(".*/discussion.*")) return "讨论管理";
        
        // 直播管理
        if (uri.matches(".*/live.*")) return "直播管理";
        
        // 日志管理
        if (uri.matches(".*/log.*")) return "日志管理";
        
        // 用户管理（必须在student/teacher之前）
        if (uri.matches(".*/admin/user.*")) return "用户管理";
        
        // 学生管理
        if (uri.matches(".*/admin/student.*")) return "学生管理";
        
        // 教师管理
        if (uri.matches(".*/admin/teacher.*")) return "教师管理";
        
        // 认证授权
        if (uri.matches(".*/auth.*")) return "认证授权";
        
        return "系统管理";
    }

    private String parseTargetType(String uri) {
        return parseModule(uri);
    }
}
