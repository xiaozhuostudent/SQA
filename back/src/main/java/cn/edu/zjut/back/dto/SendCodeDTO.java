package cn.edu.zjut.back.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 发送验证码DTO
 */
public class SendCodeDTO {
    
    @NotBlank(message = "接收方不能为空")
    private String receiver; // 邮箱或手机号
    
    @NotBlank(message = "类型不能为空")
    private String type; // email 或 sms
    
    // Getters and Setters
    public String getReceiver() {
        return receiver;
    }
    
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
}
