package cn.edu.zjut.back.service;

import cn.edu.zjut.back.dto.UserQueryDTO;
import cn.edu.zjut.back.entity.User;
import cn.edu.zjut.back.mapper.UserMapper;
import cn.edu.zjut.back.vo.PageResultVO;
import cn.edu.zjut.back.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 管理员-用户管理Service
 */
@Service
public class AdminUserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    /**
     * 分页查询用户列表
     */
    public PageResultVO<UserVO> getUserList(UserQueryDTO query) {
        // 获取所有用户
        List<User> allUsers = userMapper.findAll();
        
        // 过滤条件
        List<User> filteredUsers = allUsers.stream()
                .filter(user -> {
                    // 角色过滤
                    if (StringUtils.hasText(query.getRole()) && 
                        !query.getRole().equals(user.getRole())) {
                        return false;
                    }
                    // 状态过滤
                    if (StringUtils.hasText(query.getStatus()) && 
                        !query.getStatus().equals(user.getStatus())) {
                        return false;
                    }
                    // 关键词搜索 (姓名或账号)
                    if (StringUtils.hasText(query.getKeyword())) {
                        String keyword = query.getKeyword().toLowerCase();
                        boolean matchUsername = user.getUsername() != null && 
                                              user.getUsername().toLowerCase().contains(keyword);
                        boolean matchRealName = user.getRealName() != null && 
                                               user.getRealName().toLowerCase().contains(keyword);
                        return matchUsername || matchRealName;
                    }
                    return true;
                })
                .collect(Collectors.toList());
        
        // 计算分页
        int total = filteredUsers.size();
        int page = query.getPage() != null ? query.getPage() : 1;
        int size = query.getSize() != null ? query.getSize() : 10;
        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, total);
        
        List<User> pageUsers;
        if (startIndex >= total) {
            pageUsers = List.of();
        } else {
            pageUsers = filteredUsers.subList(startIndex, endIndex);
        }
        
        // 转换为 VO
        List<UserVO> userVOs = pageUsers.stream()
                .map(this::convertToUserVO)
                .collect(Collectors.toList());
        
        // 构建分页结果
        PageResultVO<UserVO> result = new PageResultVO<>();
        result.setList(userVOs);
        result.setTotal((long) total);
        result.setPage(page);
        result.setSize(size);
        
        return result;
    }
    
    /**
     * 获取用户详情
     */
    public UserVO getUserDetail(Long id) {
        User user = userMapper.findByIdWithDetails(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return convertToUserVO(user);
    }
    
    /**
     * 创建用户
     */
    @Transactional
    public UserVO createUser(User user) {
        // 检查用户名是否已存在
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (StringUtils.hasText(user.getEmail()) && 
            userMapper.findByEmail(user.getEmail()) != null) {
            throw new RuntimeException("邮箱已被使用");
        }
        
        // 设置默认值
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword("123456"); // 默认密码
        }
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置默认状态
        if (!StringUtils.hasText(user.getStatus())) {
            user.setStatus("active");
        }
        
        // 插入用户
        userMapper.insert(user);
        
        return convertToUserVO(user);
    }
    
    /**
     * 更新用户信息
     */
    @Transactional
    public UserVO updateUser(User user) {
        User existingUser = userMapper.findById(user.getId());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 检查邮箱是否被其他用户使用
        if (StringUtils.hasText(user.getEmail())) {
            User userWithEmail = userMapper.findByEmail(user.getEmail());
            if (userWithEmail != null && !userWithEmail.getId().equals(user.getId())) {
                throw new RuntimeException("邮箱已被其他用户使用");
            }
        }
        
        // 更新用户信息 (不更新密码)
        existingUser.setRealName(user.getRealName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhone(user.getPhone());
        existingUser.setGender(user.getGender());
        existingUser.setAvatar(user.getAvatar());
        
        userMapper.update(existingUser);
        
        return convertToUserVO(existingUser);
    }
    
    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        userMapper.delete(id);
    }
    
    /**
     * 重置用户密码 - 统一重置为 123456
     */
    @Transactional
    public String resetPassword(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 固定密码为 123456
        String newPassword = "123456";
        String encodedPassword = passwordEncoder.encode(newPassword);
        
        // 更新密码
        userMapper.updatePassword(id, encodedPassword);
        
        return newPassword;
    }
    
    /**
     * 更新用户状态
     */
    @Transactional
    public void updateUserStatus(Long id, String status) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 直接更新状态字段
        int rows = userMapper.updateStatus(id, status);
        if (rows == 0) {
            throw new RuntimeException("更新用户状态失败");
        }
    }
    
    /**
     * 转换为 UserVO
     */
    private UserVO convertToUserVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
    
    /**
     * 生成随机密码
     */
    private String generateRandomPassword() {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }
}
