package cn.edu.zjut.back.config;

import cn.edu.zjut.back.interceptor.AdminPermissionInterceptor;
import cn.edu.zjut.back.interceptor.OperationLogInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 * 配置静态资源访问和跨域支持
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired(required = false)
    private AdminPermissionInterceptor adminPermissionInterceptor;
    
    @Autowired(required = false)
    private OperationLogInterceptor operationLogInterceptor;

    /**
     * 配置静态资源映射
     * 将/storage/**的请求映射到项目根目录下的storage目录
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射storage目录下的资源文件
        registry.addResourceHandler("/storage/**")
                .addResourceLocations("file:./storage/")
                .setCachePeriod(604800); // 缓存7天
        
        // 映射上传资源文件
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("file:./storage/resources/")
                .setCachePeriod(604800); // 缓存7天
    }

    /**
     * 配置跨域访问
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 操作日志拦截器 - 记录所有API操作
        if (operationLogInterceptor != null) {
            registry.addInterceptor(operationLogInterceptor)
                    .addPathPatterns("/api/**")
                    .excludePathPatterns(
                            "/api/auth/login",
                            "/api/auth/register",
                            "/api/auth/send-code"
                    )
                    .order(1); // 最先执行
        }
        
        // 管理员权限拦截器
        if (adminPermissionInterceptor != null) {
            registry.addInterceptor(adminPermissionInterceptor)
                    .addPathPatterns("/api/admin/**")
                    .excludePathPatterns(
                            "/api/admin/login",
                            "/api/admin/logout"
                    )
                    .order(2); // 后执行
        }
    }
}
