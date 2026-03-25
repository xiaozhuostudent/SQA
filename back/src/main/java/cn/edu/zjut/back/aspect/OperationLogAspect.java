package cn.edu.zjut.back.aspect;

import cn.edu.zjut.back.annotation.OperationLog;
import cn.edu.zjut.back.entity.User;
import cn.edu.zjut.back.mapper.UserMapper;
import cn.edu.zjut.back.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志切面
 * 自动记录用户操作日志
 */
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Around("@annotation(cn.edu.zjut.back.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        
        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLog = method.getAnnotation(OperationLog.class);
        
        // 获取用户信息
        User user = getCurrentUser(request);
        
        // 执行方法
        Object result = null;
        String status = "success";
        String errorMessage = null;
        
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            status = "failed";
            errorMessage = e.getMessage();
            throw e;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 异步记录日志
            if (user != null && operationLog != null) {
                try {
                    saveLog(user, operationLog, request, joinPoint.getArgs(), status, errorMessage, executionTime);
                } catch (Exception e) {
                    // 日志记录失败不影响业务
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 保存操作日志
     */
    private void saveLog(User user, OperationLog operationLog, HttpServletRequest request, 
                        Object[] args, String status, String errorMessage, long executionTime) {
        try {
            String requestUrl = request != null ? request.getRequestURI() : "";
            String requestMethod = request != null ? request.getMethod() : "";
            String ipAddress = getIpAddress(request);
            String requestParams = getRequestParams(args);
            
            String role = user.getRole();
            
            if ("admin".equals(role)) {
                // 管理员日志
                String sql = "INSERT INTO tb_admin_operation_log " +
                           "(admin_name, permission_level, operation, module, target_type, " +
                           "request_method, request_url, request_params, ip_address, status, " +
                           "error_message, operation_time) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                jdbcTemplate.update(sql, 
                    user.getRealName(),
                    user.getPermissionLevel(),
                    operationLog.operation(),
                    operationLog.module(),
                    operationLog.targetType(),
                    requestMethod,
                    requestUrl,
                    requestParams,
                    ipAddress,
                    status,
                    errorMessage,
                    LocalDateTime.now()
                );
                
            } else if ("teacher".equals(role)) {
                // 教师日志
                String sql = "INSERT INTO tb_teacher_operation_log " +
                           "(teacher_name, teacher_number, operation, module, target_type, " +
                           "request_url, request_params, ip_address, execution_time, status, " +
                           "error_message, operation_time) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                jdbcTemplate.update(sql,
                    user.getRealName(),
                    user.getStudentNumber(), // 假设使用studentNumber字段存储工号
                    operationLog.operation(),
                    operationLog.module(),
                    operationLog.targetType(),
                    requestUrl,
                    requestParams,
                    ipAddress,
                    (int) executionTime,
                    status,
                    errorMessage,
                    LocalDateTime.now()
                );
                
            } else if ("student".equals(role)) {
                // 学生日志
                String sql = "INSERT INTO tb_student_operation_log " +
                           "(student_name, student_number, operation, module, target_type, " +
                           "request_url, request_params, ip_address, execution_time, status, " +
                           "error_message, operation_time) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                
                jdbcTemplate.update(sql,
                    user.getRealName(),
                    user.getStudentNumber(),
                    operationLog.operation(),
                    operationLog.module(),
                    operationLog.targetType(),
                    requestUrl,
                    requestParams,
                    ipAddress,
                    (int) executionTime,
                    status,
                    errorMessage,
                    LocalDateTime.now()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取当前登录用户
     */
    private User getCurrentUser(HttpServletRequest request) {
        if (request == null) return null;
        
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        
        token = token.substring(7);
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            return userMapper.findByIdWithDetails(userId);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        if (request == null) return "unknown";
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    /**
     * 获取请求参数JSON字符串
     */
    private String getRequestParams(Object[] args) {
        if (args == null || args.length == 0) {
            return "{}";
        }
        
        try {
            return objectMapper.writeValueAsString(args);
        } catch (Exception e) {
            return "{}";
        }
    }
}
