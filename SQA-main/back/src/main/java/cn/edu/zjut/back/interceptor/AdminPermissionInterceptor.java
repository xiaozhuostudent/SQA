package cn.edu.zjut.back.interceptor;

import cn.edu.zjut.back.annotation.RequirePermission;
import cn.edu.zjut.back.entity.User;
import cn.edu.zjut.back.mapper.UserMapper;
import cn.edu.zjut.back.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员权限拦截器
 * 检查管理员是否有足够的权限等级
 */
@Component
public class AdminPermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 检查是否是控制器方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // 2. 检查方法或类上是否有 @RequirePermission 注解
        RequirePermission methodPermission = handlerMethod.getMethodAnnotation(RequirePermission.class);
        RequirePermission classPermission = handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
        
        RequirePermission permission = methodPermission != null ? methodPermission : classPermission;
        
        // 如果没有权限注解,放行
        if (permission == null) {
            return true;
        }

        // 3. 获取当前登录用户
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return sendError(response, 401, "未登录或登录已过期");
        }

        token = token.substring(7);
        Long userId;
        try {
            userId = jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return sendError(response, 401, "Token无效");
        }

        User user = userMapper.findByIdWithDetails(userId);
        
        // 4. 检查是否是管理员
        if (user == null || !"admin".equals(user.getRole())) {
            return sendError(response, 403, "需要管理员权限");
        }

        // 5. 检查权限等级
        Integer permissionLevel = user.getPermissionLevel();
        if (permissionLevel == null) {
            permissionLevel = 1; // 默认最低权限
        }

        int requiredLevel = permission.level();
        
        if (permissionLevel < requiredLevel) {
            String roleName = getRoleName(permissionLevel);
            String requiredRoleName = getRoleName(requiredLevel);
            return sendError(response, 403, 
                String.format("权限不足！当前角色[%s(等级%d)]，需要[%s(等级%d)]或更高权限", 
                    roleName, permissionLevel, requiredRoleName, requiredLevel));
        }

        // 6. 检查具体权限(如果指定了permissions)
        String[] requiredPermissions = permission.permissions();
        if (requiredPermissions.length > 0 && permissionLevel < 5) {
            // 超级管理员跳过具体权限检查
            // TODO: 这里可以实现更细粒度的权限检查
            // 例如从数据库或配置中读取该用户的具体权限列表
        }

        return true;
    }

    /**
     * 发送错误响应
     */
    private boolean sendError(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", status);
        result.put("message", message);
        result.put("data", null);
        
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(result));
        
        return false;
    }

    /**
     * 获取角色名称
     */
    private String getRoleName(int level) {
        switch (level) {
            case 5: return "超级管理员";
            case 4: return "系统管理员";
            case 3: return "教务管理员";
            case 2: return "内容管理员";
            case 1: return "数据查看员";
            default: return "未知角色";
        }
    }
}
