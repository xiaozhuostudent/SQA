package cn.edu.zjut.back.annotation;

import java.lang.annotation.*;

/**
 * 管理员权限注解
 * 用于标记需要特定权限等级的接口
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {
    
    /**
     * 所需最低权限等级 (1-5)
     */
    int level() default 1;
    
    /**
     * 具体权限标识 (如 "user:delete", "course:create")
     */
    String[] permissions() default {};
    
    /**
     * 权限描述
     */
    String description() default "";
}
