package cn.edu.zjut.back.controller;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.entity.User;
import cn.edu.zjut.back.service.UserService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * 获取用户信息（包含学生/教师扩展信息）
     */
    @GetMapping("/info/{userId}")
    public Result<User> getUserInfo(@PathVariable Long userId) {
        try {
            return userService.getUserInfoWithDetails(userId);
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取当前用户信息（通过参数）
     */
    @GetMapping("/info")
    public Result<User> getUserInfoByParam(@RequestParam Long userId) {
        try {
            return userService.getUserInfoWithDetails(userId);
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新用户基本信息
     */
    @PutMapping("/update")
    public Result<String> updateUser(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("id").toString());
            return userService.updateUserInfo(userId, params);
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<String> changePassword(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            String oldPassword = params.get("oldPassword").toString();
            String newPassword = params.get("newPassword").toString();
            
            return userService.changePassword(userId, oldPassword, newPassword);
        } catch (Exception e) {
            return Result.error("修改密码失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传头像
     */
    @PostMapping("/upload-avatar")
    public Result<String> uploadAvatar(@RequestParam Long userId, @RequestParam String avatarUrl) {
        try {
            return userService.updateAvatar(userId, avatarUrl);
        } catch (Exception e) {
            return Result.error("上传头像失败: " + e.getMessage());
        }
    }
}
