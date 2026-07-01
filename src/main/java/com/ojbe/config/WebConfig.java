package com.ojbe.config;

import com.ojbe.interceptor.JwtAuthenticationInterceptor;
import com.ojbe.interceptor.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/user/login");

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
                        "/question/query",
                        "/question_template/get",
                        // 题解公开读接口
                        "/question_solution/list/page",
                        "/question_solution/get",
                        "/question_solution/view",
                        "/question_solution/total/count",
                        // 比赛公开读接口
                        "/contest/list",
                        "/contest/*/ranking"
                );
    }
    
    @Value("${cors.allowed-origins:http://localhost:5173}")
    private String allowedOriginsConfig;

    /**
     * 配置跨域请求处理
     * 允许前端应用访问后端API
     *
     * 注意：当 allowCredentials(true) 时，allowedOriginPatterns 不能使用 "*"，
     * 必须指定具体的源地址列表，否则浏览器会拒绝跨域请求。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins = allowedOriginsConfig.split(",");
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}