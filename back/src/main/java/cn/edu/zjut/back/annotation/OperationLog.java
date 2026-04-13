package cn.edu.zjut.back.annotation;




import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {
    
    /**
     * 操作类型（如：创建、更新、删除、查询等）
     */
    String operation() default "";
    
    /**
     * 操作模块（如：user、course、homework、exam等）
     */
    String module() default "";
    
    /**
     * 目标类型（如：user、course、homework等）
     */
    String targetType() default "";
}
