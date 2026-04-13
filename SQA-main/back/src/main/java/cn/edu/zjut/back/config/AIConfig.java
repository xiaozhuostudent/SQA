package cn.edu.zjut.back.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * AI相关配置类
 */
@Configuration
public class AIConfig {
    
    @Bean
    public RestTemplate aiRestTemplate() {
        return new RestTemplate();
    }
}