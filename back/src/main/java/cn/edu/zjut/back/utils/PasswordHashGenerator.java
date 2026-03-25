package cn.edu.zjut.back.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码哈希生成工具
 */
public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String password = "123456";
        String hash = encoder.encode(password);
        
        System.out.println("==================================");
        System.out.println("密码: " + password);
        System.out.println("BCrypt哈希: " + hash);
        System.out.println("==================================");
        
        // 验证现有哈希
        String existingHash = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lZzaxYZfxP5dRBqEi";
        System.out.println("\n验证数据库中的哈希:");
        System.out.println("哈希: " + existingHash);
        System.out.println("密码'123456'匹配: " + encoder.matches("123456", existingHash));
        System.out.println("密码'password'匹配: " + encoder.matches("password", existingHash));
        System.out.println("密码'admin'匹配: " + encoder.matches("admin", existingHash));
        System.out.println("密码'student'匹配: " + encoder.matches("student", existingHash));
        
        // 生成SQL更新语句
        System.out.println("\n==================================");
        System.out.println("SQL更新语句（将所有用户密码更新为123456）:");
        System.out.println("==================================");
        System.out.println("UPDATE tb_user SET password = '" + hash + "' WHERE username LIKE 'student%' OR username LIKE 'teacher%' OR username LIKE 'admin%';");
    }
}
