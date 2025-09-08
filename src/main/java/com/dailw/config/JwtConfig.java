package com.dailw.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置类
 * 用于读取application.yml中的JWT相关配置
 * 
 * @author trave
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    
    /**
     * JWT密钥
     */
    private String secret = "nH9QZ8+7LpX2Rk5mS7tA3fG8jK2pN5qR8tU1vX4sZ6wD9gB2hC5jE7kF9mH0pJ";
    
    /**
     * 访问Token过期时间（毫秒）
     * 默认2小时
     */
    private long accessTokenExpiration = 7200000L;
    
    /**
     * 刷新Token过期时间（毫秒）
     * 默认7天
     */
    private long refreshTokenExpiration = 604800000L;
    
    /**
     * JWT签发者
     */
    private String issuer = "dailw-system";
}