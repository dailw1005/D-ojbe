package com.dailw.interceptor;

import com.dailw.common.ErrorCode;
import com.dailw.exception.BusinessException;
import com.dailw.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT认证拦截器
 * 拦截需要认证的请求，验证JWT Token的有效性
 * 
 * @author trave
 */
@Slf4j
@Component
public class JwtAuthenticationInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * Token前缀
     */
    private static final String TOKEN_PREFIX = "Bearer ";
    
    /**
     * Authorization请求头名称
     */
    private static final String AUTHORIZATION_HEADER = "Authorization";
    
    /**
     * 用户ID属性名
     */
    public static final String USER_ID_ATTRIBUTE = "userId";
    
    /**
     * 用户名属性名
     */
    public static final String USERNAME_ATTRIBUTE = "username";

    /**
     * 角色属性名
     */
    public static final String ROLE_ATTRIBUTE = "role";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取Authorization请求头
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        // 检查请求头是否存在且格式正确
        if (StringUtils.isBlank(authorizationHeader) || !authorizationHeader.startsWith(TOKEN_PREFIX)) {
            log.warn("请求缺少有效的Authorization头: {}", request.getRequestURI());
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请求缺少有效的Authorization头");
        }
        
        // 提取Token
        String token = authorizationHeader.substring(TOKEN_PREFIX.length());
        
        if (StringUtils.isBlank(token)) {
            log.warn("Token为空: {}", request.getRequestURI());
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "Token不能为空");
        }
        
        try {
            // 验证Token有效性
            if (!jwtUtil.validateToken(token)) {
                log.warn("Token验证失败: {}", request.getRequestURI());
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "Token无效或已过期");
            }
            
            // 检查Token是否过期
            if (jwtUtil.isTokenExpired(token)) {
                log.warn("Token已过期: {}", request.getRequestURI());
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "Token已过期，请重新登录");
            }
            
            // 从Token中提取用户信息
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);
            
            if (userId == null || StringUtils.isBlank(username) || StringUtils.isBlank(role)) {
                log.warn("Token中用户信息无效: {}", request.getRequestURI());
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "Token中用户信息无效");
            }
            
            // 将用户信息存储到请求属性中，供后续使用
            request.setAttribute(USER_ID_ATTRIBUTE, userId);
            request.setAttribute(USERNAME_ATTRIBUTE, username);
            request.setAttribute(ROLE_ATTRIBUTE, role);

            log.info("用户 {} (ID: {}) (角色: {}) 通过JWT认证: {}", username, userId, role, request.getRequestURI());
            
            return true;
            
        } catch (BusinessException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            log.error("JWT认证过程中发生异常: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "认证失败，请重新登录");
        }
    }
    
    /**
     * 从请求中获取当前用户ID
     * 
     * @param request HTTP请求
     * @return 用户ID，如果未认证返回null
     */
    public static Long getCurrentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute(USER_ID_ATTRIBUTE);
        return userId instanceof Long ? (Long) userId : null;
    }
    
    /**
     * 从请求中获取当前用户名
     * 
     * @param request HTTP请求
     * @return 用户名，如果未认证返回null
     */
    public static String getCurrentUsername(HttpServletRequest request) {
        Object username = request.getAttribute(USERNAME_ATTRIBUTE);
        return username instanceof String ? (String) username : null;
    }

    /**
     * 从请求中获取当前角色
     *
     * @param request HTTP请求
     * @return 角色，如果未认证返回null
     */
    public static String getCurrentRole(HttpServletRequest request) {
        Object role = request.getAttribute(ROLE_ATTRIBUTE);
        return role instanceof String ? (String) role : null;
    }
}