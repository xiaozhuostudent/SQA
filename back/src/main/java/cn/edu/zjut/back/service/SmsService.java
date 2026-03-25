package cn.edu.zjut.back.service;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 短信服务
 * 使用阿里云短信服务
 */
@Service
public class SmsService {
    
    @Value("${aliyun.sms.access-key-id:}")
    private String accessKeyId;
    
    @Value("${aliyun.sms.access-key-secret:}")
    private String accessKeySecret;
    
    @Value("${aliyun.sms.sign-name:}")
    private String signName;
    
    @Value("${aliyun.sms.template-code:}")
    private String templateCode;
    
    /**
     * 发送短信验证码（异步）
     */
    @Async
    public void sendVerificationCode(String phoneNumber, String code) {
        // 如果未配置阿里云密钥，使用开发模式（输出到控制台）
        if (accessKeyId == null || accessKeyId.isEmpty()) {
            System.out.println("=================================");
            System.out.println("短信验证码（开发模式）");
            System.out.println("手机号: " + phoneNumber);
            System.out.println("验证码: " + code);
            System.out.println("有效期: 5分钟");
            System.out.println("=================================");
            return;
        }
        
        try {
            // 创建阿里云短信客户端
            Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret)
                .setEndpoint("dysmsapi.aliyuncs.com");
            
            Client client = new Client(config);
            
            // 构建请求
            SendSmsRequest request = new SendSmsRequest()
                .setPhoneNumbers(phoneNumber)
                .setSignName(signName)
                .setTemplateCode(templateCode)
                .setTemplateParam("{\"code\":\"" + code + "\"}");
            
            // 发送短信
            SendSmsResponse response = client.sendSms(request);
            
            if (!"OK".equals(response.getBody().getCode())) {
                throw new RuntimeException("短信发送失败: " + response.getBody().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("短信发送失败: " + e.getMessage());
        }
    }
}
