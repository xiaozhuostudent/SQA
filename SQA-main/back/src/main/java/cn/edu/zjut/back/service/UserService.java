package cn.edu.zjut.back.service;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.common.ResultCode;
import cn.edu.zjut.back.dto.CodeLoginDTO;
import cn.edu.zjut.back.dto.LoginDTO;
import cn.edu.zjut.back.dto.RegisterDTO;
import cn.edu.zjut.back.entity.User;
import cn.edu.zjut.back.mapper.UserMapper;
import cn.edu.zjut.back.utils.JwtUtil;
import cn.edu.zjut.back.utils.RedisUtil;
import cn.edu.zjut.back.vo.LoginVO;
import cn.edu.zjut.back.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * 用户服务
 */
@Service
public class UserService {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private VerificationCodeService verificationCodeService;
    
    // Redis 缓存键
    private static final String USER_CACHE_KEY = "user:";
    private static final String USER_SESSION_KEY = "session:";
    private static final String ONLINE_USERS_KEY = "online:users";
    private static final int USER_CACHE_EXPIRE_SECONDS = 1800; // 30分钟
    private static final int SESSION_EXPIRE_SECONDS = 7200; // 2小时
    
    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, RedisUtil redisUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.redisUtil = redisUtil;
    }
    
    /**
     * 用户登录
     */
    public Result<LoginVO> login(LoginDTO loginDTO) {
        System.out.println("=== 登录请求 ===");
        System.out.println("用户名: " + loginDTO.getUsername());
        System.out.println("密码长度: " + (loginDTO.getPassword() != null ? loginDTO.getPassword().length() : 0));
        
        // 使用 findByIdWithDetails 获取包含扩展信息的完整用户数据
        User user = userMapper.findByUsername(loginDTO.getUsername());
        if (user == null) {
            System.out.println("错误: 用户不存在");
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        
        // 获取完整的用户详情(包含 permissionLevel 等扩展字段)
        User userWithDetails = userMapper.findByIdWithDetails(user.getId());
        
        System.out.println("找到用户: " + userWithDetails.getUsername() + ", 角色: " + userWithDetails.getRole());
        System.out.println("数据库密码哈希: " + userWithDetails.getPassword());
        
        if (!passwordEncoder.matches(loginDTO.getPassword(), userWithDetails.getPassword())) {
            System.out.println("错误: 密码不匹配");
            return Result.error(ResultCode.PASSWORD_ERROR);
        }
        
        System.out.println("密码验证成功！");
        
        if ("inactive".equals(userWithDetails.getStatus())) {
            return Result.error("账号已被禁用");
        }
        
        // 生成Token
        String token = jwtUtil.generateToken(userWithDetails.getId(), userWithDetails.getUsername(), userWithDetails.getRole());
        
        // 存储用户会话信息到 Redis
        String sessionKey = USER_SESSION_KEY + userWithDetails.getId();
        redisUtil.set(sessionKey, token, SESSION_EXPIRE_SECONDS);
        
        // 记录在线用户
        redisUtil.sAdd(ONLINE_USERS_KEY, userWithDetails.getId().toString());
        
        // 构建UserVO (包含permissionLevel等扩展字段)
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userWithDetails, userVO);
        
        // 记录登录日志
        recordLoginLog(userWithDetails, "success", null);
        
        LoginVO loginVO = new LoginVO(token, userVO);
        return Result.success(loginVO);
    }
    
    /**
     * 验证码登录
     */
    public Result<LoginVO> loginWithCode(CodeLoginDTO codeLoginDTO) {
        // 验证验证码
        boolean isValid = verificationCodeService.verifyCode(
            codeLoginDTO.getReceiver(), 
            codeLoginDTO.getCode()
        );
        
        if (!isValid) {
            return Result.error("验证码错误或已过期");
        }
        
        // 根据邮箱或手机号查找用户
        User user;
        if ("email".equals(codeLoginDTO.getType())) {
            user = userMapper.findByEmail(codeLoginDTO.getReceiver());
        } else if ("sms".equals(codeLoginDTO.getType())) {
            user = userMapper.findByPhone(codeLoginDTO.getReceiver());
        } else {
            return Result.error("不支持的登录类型");
        }
        
        if (user == null) {
            return Result.error("用户不存在，请先注册");
        }
        
        // 检查角色是否匹配
        if (!user.getRole().equals(codeLoginDTO.getRole())) {
            return Result.error("角色不匹配，请选择正确的角色");
        }
        
        if ("inactive".equals(user.getStatus())) {
            return Result.error("账号已被禁用");
        }
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        // 存储用户会话信息到 Redis
        String sessionKey = USER_SESSION_KEY + user.getId();
        redisUtil.set(sessionKey, token, SESSION_EXPIRE_SECONDS);
        
        // 记录在线用户
        redisUtil.sAdd(ONLINE_USERS_KEY, user.getId().toString());
        
        // 构建UserVO
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        
        LoginVO loginVO = new LoginVO(token, userVO);
        return Result.success(loginVO);
    }
    
    /**
     * 用户注册
     */
    public Result<String> register(RegisterDTO registerDTO) {
        User existUser = userMapper.findByUsername(registerDTO.getUsername());
        if (existUser != null) {
            return Result.error(ResultCode.USER_EXIST);
        }
        
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setStatus("active");
        
        userMapper.insert(user);
        return Result.success("注册成功");
    }
    
    /**
     * 获取用户信息
     */
    public Result<UserVO> getUserInfo(Long userId) {
        String cacheKey = USER_CACHE_KEY + userId;
        
        // 尝试从缓存获取
        UserVO cachedUser = (UserVO) redisUtil.get(cacheKey);
        if (cachedUser != null) {
            return Result.success(cachedUser);
        }
        
        // 从数据库查询
        User user = userMapper.findById(userId);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        
        // 存入缓存
        redisUtil.set(cacheKey, userVO, USER_CACHE_EXPIRE_SECONDS);
        
        return Result.success(userVO);
    }
    
    /**
     * 更新用户信息
     */
    public Result<String> updateUser(User user) {
        userMapper.update(user);
        // 清除用户缓存
        redisUtil.delete(USER_CACHE_KEY + user.getId());
        return Result.success("更新成功");
    }
    
    /**
     * 获取所有用户(管理员)
     */
    public Result<List<User>> getAllUsers() {
        List<User> users = userMapper.findAll();
        return Result.success(users);
    }
    
    /**
     * 删除用户(管理员)
     */
    public Result<String> deleteUser(Long userId) {
        userMapper.delete(userId);
        return Result.success("删除成功");
    }
    
    /**
     * 重置密码(管理员)
     */
    public Result<String> resetPassword(Long userId, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        userMapper.updatePassword(userId, encodedPassword);
        return Result.success("密码重置成功");
    }
    
    /**
     * 获取用户详细信息(包含学生或教师扩展信息)
     */
    public Result<User> getUserInfoWithDetails(Long userId) {
        User user = userMapper.findByIdWithDetails(userId);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        // 密码不返回给前端
        user.setPassword(null);
        return Result.success(user);
    }
    
    /**
     * 更新用户信息(包含扩展信息)
     */
    public Result<String> updateUserInfo(Long userId, java.util.Map<String, Object> updates) {
        User user = userMapper.findById(userId);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        
        // 更新基本信息
        if (updates.containsKey("realName")) {
            user.setRealName((String) updates.get("realName"));
        }
        if (updates.containsKey("gender")) {
            user.setGender((String) updates.get("gender"));
        }
        if (updates.containsKey("email")) {
            user.setEmail((String) updates.get("email"));
        }
        if (updates.containsKey("phone")) {
            user.setPhone((String) updates.get("phone"));
        }
        
        // 更新用户基本信息
        userMapper.update(user);
        
        // 根据角色更新扩展信息
        if ("student".equals(user.getRole())) {
            userMapper.updateStudentInfo(userId, updates);
        } else if ("teacher".equals(user.getRole())) {
            userMapper.updateTeacherInfo(userId, updates);
        } else if ("admin".equals(user.getRole())) {
            userMapper.updateAdminInfo(userId, updates);
        }
        
        return Result.success("更新成功");
    }
    
    /**
     * 修改密码
     */
    public Result<String> changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.findById(userId);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.error("原密码错误");
        }
        
        // 更新新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        userMapper.updatePassword(userId, encodedPassword);
        return Result.success("密码修改成功");
    }
    
    /**
     * 更新头像
     */
    public Result<String> updateAvatar(Long userId, String avatarUrl) {
        User user = userMapper.findById(userId);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_FOUND);
        }
        
        userMapper.updateAvatar(userId, avatarUrl);
        return Result.success("头像更新成功");
    }
    
    /**
     * 记录登录日志
     */
    private void recordLoginLog(User user, String status, String errorMessage) {
        try {
            String sql = null;
            String role = user.getRole();
            
            if ("admin".equals(role)) {
                sql = "INSERT INTO tb_admin_operation_log " +
                     "(admin_id, admin_name, operation, module, request_url, ip_address) " +
                     "VALUES (?, ?, '登录', '认证授权', '/api/auth/login', ?)";
                
                jdbcTemplate.update(sql,
                    user.getId(),
                    user.getRealName() != null ? user.getRealName() : user.getUsername(),
                    getClientIp()
                );
            } else if ("teacher".equals(role)) {
                sql = "INSERT INTO tb_teacher_operation_log " +
                     "(teacher_id, teacher_name, operation, module, request_url, ip_address) " +
                     "VALUES (?, ?, '登录', '认证授权', '/api/auth/login', ?)";
                
                jdbcTemplate.update(sql,
                    user.getId(),
                    user.getRealName() != null ? user.getRealName() : user.getUsername(),
                    getClientIp()
                );
            } else if ("student".equals(role)) {
                sql = "INSERT INTO tb_student_operation_log " +
                     "(student_id, student_name, operation, module, request_url, ip_address) " +
                     "VALUES (?, ?, '登录', '认证授权', '/api/auth/login', ?)";
                
                jdbcTemplate.update(sql,
                    user.getId(),
                    user.getRealName() != null ? user.getRealName() : user.getUsername(),
                    getClientIp()
                );
            }
        } catch (Exception e) {
            // 日志记录失败不影响业务
            System.err.println("记录登录日志失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getHeader("X-Real-IP");
                }
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
                return ip;
            }
        } catch (Exception e) {
            // ignore
        }
        return "127.0.0.1";
    }
}
