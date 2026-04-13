package cn.edu.zjut.back.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 邮件服务
 */
@Service
public class EmailService {
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:}")
    private String from;
    
    /**
     * 发送验证码邮件（异步）
     */
    @Async
    public void sendVerificationCode(String to, String code) {
        // 如果未配置邮件服务，输出到控制台（开发模式）
        if (mailSender == null || from == null || from.isEmpty()) {
            System.out.println("=================================");
            System.out.println("邮箱验证码（开发模式）");
            System.out.println("收件人: " + to);
            System.out.println("验证码: " + code);
            System.out.println("有效期: 5分钟");
            System.out.println("=================================");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("【教学管理系统】登录验证码");
            message.setText(buildEmailContent(code));
            
            System.out.println("正在发送邮件到: " + to);
            mailSender.send(message);
            System.out.println("邮件发送成功");
        } catch (Exception e) {
            System.err.println("邮件发送失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("邮件发送失败，请检查邮箱配置或稍后再试");
        }
    }
    
    /**
     * 构建邮件内容
     */
    private String buildEmailContent(String code) {
        return "尊敬的用户：\n\n" +
               "您正在使用邮箱验证码登录教学管理系统。\n\n" +
               "您的验证码是：" + code + "\n\n" +
               "验证码有效期为5分钟，请尽快使用。\n\n" +
               "如果这不是您本人的操作，请忽略此邮件。\n\n" +
               "---\n" +
               "教学管理系统";
    }
}
