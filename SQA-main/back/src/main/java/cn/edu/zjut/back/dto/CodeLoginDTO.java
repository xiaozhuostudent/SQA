package cn.edu.zjut.back.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 验证码登录DTO
 */
public class CodeLoginDTO {
    
    @NotBlank(message = "接收方不能为空")
    private String receiver; // 邮箱或手机号
    
    @NotBlank(message = "验证码不能为空")
    private String code;
    
    @NotBlank(message = "类型不能为空")
    private String type; // email 或 sms
    
    @NotBlank(message = "角色不能为空")
    private String role;
    
    // Getters and Setters
    public String getReceiver() {
        return receiver;
    }
    
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
}
