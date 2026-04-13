package cn.edu.zjut.back.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务
 * 用于生成、存储和验证验证码
 */
@Service
public class VerificationCodeService {
    
    // 存储验证码：key=receiver(邮箱或手机号), value=Map{code, expireTime}
    private final Map<String, CodeInfo> codeStore = new ConcurrentHashMap<>();
    
    // 验证码有效期（5分钟）
    private static final long CODE_EXPIRE_TIME = 5 * 60 * 1000;
    
    // 验证码长度
    private static final int CODE_LENGTH = 6;
    
    /**
     * 生成6位数字验证码
     */
    public String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
    
    /**
     * 存储验证码
     */
    public void saveCode(String receiver, String code) {
        long expireTime = System.currentTimeMillis() + CODE_EXPIRE_TIME;
        codeStore.put(receiver, new CodeInfo(code, expireTime));
        
        // 清理过期验证码
        cleanExpiredCodes();
    }
    
    /**
     * 验证验证码
     */
    public boolean verifyCode(String receiver, String code) {
        CodeInfo codeInfo = codeStore.get(receiver);
        
        if (codeInfo == null) {
            return false;
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() > codeInfo.getExpireTime()) {
            codeStore.remove(receiver);
            return false;
        }
        
        // 验证码正确，删除已使用的验证码
        if (codeInfo.getCode().equals(code)) {
            codeStore.remove(receiver);
            return true;
        }
        
        return false;
    }
    
    /**
     * 检查是否可以发送验证码（防止频繁发送）
     */
    public boolean canSendCode(String receiver) {
        CodeInfo codeInfo = codeStore.get(receiver);
        if (codeInfo == null) {
            return true;
        }
        
        // 如果上一个验证码还未过期，且发送时间在1分钟内，则不允许重复发送
        long sendInterval = 60 * 1000; // 1分钟
        long timeSinceLastSend = System.currentTimeMillis() - (codeInfo.getExpireTime() - CODE_EXPIRE_TIME);
        
        return timeSinceLastSend >= sendInterval;
    }
    
    /**
     * 清理过期验证码
     */
    private void cleanExpiredCodes() {
        long now = System.currentTimeMillis();
        codeStore.entrySet().removeIf(entry -> now > entry.getValue().getExpireTime());
    }
    
    /**
     * 验证码信息内部类
     */
    private static class CodeInfo {
        private final String code;
        private final long expireTime;
        
        public CodeInfo(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }
        
        public String getCode() {
            return code;
        }
        
        public long getExpireTime() {
            return expireTime;
        }
    }
}
