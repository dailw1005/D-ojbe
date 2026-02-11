package com.dailw.config;

import com.dailw.interceptor.JwtAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 配置拦截器、跨域等Web相关设置
 * 
 * @author trave
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private JwtAuthenticationInterceptor jwtAuthenticationInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationInterceptor)
                // 拦截所有请求
                .addPathPatterns("/**")
                // 排除不需要认证的路径
                .excludePathPatterns(
                        // 用户注册和登录
                        "/user/register",
                        "/user/login",
                        "/user/refresh-token",
                        // Swagger文档相关
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        // 静态资源
                        "/static/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/favicon.ico",
                        // 健康检查
                        "/actuator/**",
                        // 错误页面
                        "/error",
                        // 头像上传路径
                        "/uploads/avatar/**",
                        "/question/query"
                );
    }
    
    /**
     * 配置跨域请求处理
     * 允许前端应用访问后端API
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许的源地址
                .allowedOriginPatterns("*")
                // 允许的HTTP方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许的请求头
                .allowedHeaders("*")
                // 是否允许发送Cookie
                .allowCredentials(true)
                // 预检请求的缓存时间（秒）
                .maxAge(3600);
    }
}