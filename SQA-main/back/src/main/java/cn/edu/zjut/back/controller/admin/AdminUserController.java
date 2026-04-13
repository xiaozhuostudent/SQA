package cn.edu.zjut.back.controller.admin;

import cn.edu.zjut.back.annotation.RequirePermission;
import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.dto.UserQueryDTO;
import cn.edu.zjut.back.entity.User;
import cn.edu.zjut.back.service.AdminUserService;
import cn.edu.zjut.back.vo.PageResultVO;
import cn.edu.zjut.back.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员-用户管理Controller
 */
@RestController
@RequestMapping("/api/admin")
public class AdminUserController {
    
    @Autowired
    private AdminUserService adminUserService;
    
    /**
     * 分页查询用户列表
     */
    @RequirePermission(level = 1, description = "查看用户列表")
    @GetMapping("/users")
    public Result<PageResultVO<UserVO>> getUserList(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        UserQueryDTO query = new UserQueryDTO();
        query.setRole(role);
        query.setStatus(status);
        query.setKeyword(keyword);
        query.setPage(page);
        query.setSize(size);
        
        PageResultVO<UserVO> result = adminUserService.getUserList(query);
        return Result.success(result);
    }
    
    /**
     * 获取用户详情
     */
    @RequirePermission(level = 1, description = "查看用户详情")
    @GetMapping("/user/{id}")
    public Result<UserVO> getUserDetail(@PathVariable Long id) {
        UserVO user = adminUserService.getUserDetail(id);
        return Result.success(user);
    }
    
    /**
     * 创建用户
     */
    @RequirePermission(level = 3, description = "创建用户")
    @PostMapping("/user/create")
    public Result<UserVO> createUser(@RequestBody User user) {
        UserVO newUser = adminUserService.createUser(user);
        return Result.success(newUser);
    }
    
    /**
     * 更新用户信息
     */
    @RequirePermission(level = 3, description = "更新用户信息")
    @PutMapping("/user/update/{id}")
    public Result<UserVO> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        UserVO updatedUser = adminUserService.updateUser(user);
        return Result.success(updatedUser);
    }
    
    /**
     * 删除用户
     */
    @RequirePermission(level = 5, description = "删除用户")
    @DeleteMapping("/user/delete/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return Result.success();
    }
    
    /**
     * 重置用户密码
     */
    @RequirePermission(level = 3, description = "重置用户密码")
    @PostMapping("/user/reset-password/{id}")
    public Result<Map<String, String>> resetPassword(@PathVariable Long id) {
        String newPassword = adminUserService.resetPassword(id);
        Map<String, String> data = new HashMap<>();
        data.put("newPassword", newPassword);
        return Result.success(data);
    }
    
    /**
     * 更新用户状态
     */
    @RequirePermission(level = 3, description = "更新用户状态")
    @PutMapping("/user/status/{id}")
    public Result<Void> updateUserStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, String> body
    ) {
        String status = body.get("status");
        adminUserService.updateUserStatus(id, status);
        return Result.success();
    }
}
