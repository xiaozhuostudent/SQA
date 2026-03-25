package cn.edu.zjut.back.controller;

import cn.edu.zjut.back.common.Result;
import cn.edu.zjut.back.dto.CodeLoginDTO;
import cn.edu.zjut.back.dto.LoginDTO;
import cn.edu.zjut.back.dto.RegisterDTO;
import cn.edu.zjut.back.dto.SendCodeDTO;
import cn.edu.zjut.back.service.EmailService;
import cn.edu.zjut.back.service.SmsService;
import cn.edu.zjut.back.service.UserService;
import cn.edu.zjut.back.service.VerificationCodeService;
import cn.edu.zjut.back.vo.LoginVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserService userService;
    
    @Autowired
    private VerificationCodeService verificationCodeService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SmsService smsService;
    
    public AuthController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }
    
    /**
     * 验证码登录
     */
    @PostMapping("/login/code")
    public Result<LoginVO> loginWithCode(@Valid @RequestBody CodeLoginDTO codeLoginDTO) {
        return userService.loginWithCode(codeLoginDTO);
    }
    
    /**
     * 发送验证码
     */
    @PostMapping("/send-code")
    public Result<String> sendCode(@Valid @RequestBody SendCodeDTO sendCodeDTO) {
        try {
            String receiver = sendCodeDTO.getReceiver();
            String type = sendCodeDTO.getType();
            
            // 检查是否可以发送（防止频繁发送）
            if (!verificationCodeService.canSendCode(receiver)) {
                return Result.error("验证码发送过于频繁，请稍后再试");
            }
            
            // 生成验证码
            String code = verificationCodeService.generateCode();
            
            // 存储验证码
            verificationCodeService.saveCode(receiver, code);
            
            // 根据类型发送验证码
            if ("email".equals(type)) {
                emailService.sendVerificationCode(receiver, code);
                return Result.success("验证码已发送到邮箱，请查收");
            } else if ("sms".equals(type)) {
                smsService.sendVerificationCode(receiver, code);
                return Result.success("验证码已发送到手机，请查收");
            } else {
                return Result.error("不支持的验证码类型");
            }
        } catch (Exception e) {
            return Result.error("验证码发送失败: " + e.getMessage());
        }
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return userService.register(registerDTO);
    }
}

