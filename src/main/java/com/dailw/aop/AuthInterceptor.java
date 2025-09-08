package com.dailw.aop;

import com.dailw.annotation.AuthCheck;
import com.dailw.common.ErrorCode;
import com.dailw.constant.UserConstant;
import com.dailw.exception.BusinessException;
import com.dailw.interceptor.JwtAuthenticationInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 权限校验 AOP
 */
@Aspect
@Component
@Slf4j
public class AuthInterceptor {

    /**
     * 执行拦截
     *
     * @param joinPoint 切入点
     * @param authCheck 权限校验注解
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 获取当前请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            log.warn("无法获取当前请求上下文");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "无法获取当前请求上下文");
        }
        
        HttpServletRequest request = attributes.getRequest();
        
        // 从请求中获取当前用户信息（由JWT拦截器设置）
        Long currentUserId = JwtAuthenticationInterceptor.getCurrentUserId(request);
        String currentUsername = JwtAuthenticationInterceptor.getCurrentUsername(request);
        String currentUserRole = JwtAuthenticationInterceptor.getCurrentRole(request);
        
        // 检查用户是否已登录
        if (currentUserId == null || StringUtils.isBlank(currentUsername) || StringUtils.isBlank(currentUserRole)) {
            log.warn("用户未登录或用户信息不完整，拒绝访问: {}", request.getRequestURI());
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        
        // 获取注解中要求的角色
        String mustRole = authCheck.mustRole();
        
        // 如果注解中没有指定必须的角色，则只需要登录即可
        if (StringUtils.isBlank(mustRole)) {
            log.debug("用户 {} (角色: {}) 通过权限校验（无特定角色要求）: {}", currentUsername, currentUserRole, request.getRequestURI());
            return joinPoint.proceed();
        }
        
        // 检查用户角色是否满足要求
        if (!hasPermission(currentUserRole, mustRole)) {
            log.warn("用户 {} (角色: {}) 权限不足，要求角色: {}，拒绝访问: {}", 
                    currentUsername, currentUserRole, mustRole, request.getRequestURI());
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "权限不足，需要" + getRoleDisplayName(mustRole) + "权限");
        }
        
        log.info("用户 {} (角色: {}) 通过权限校验，要求角色: {}，访问: {}", 
                currentUsername, currentUserRole, mustRole, request.getRequestURI());
        
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
    
    /**
     * 检查用户是否有指定权限
     *
     * @param userRole 用户角色
     * @param requiredRole 要求的角色
     * @return 是否有权限
     */
    private boolean hasPermission(String userRole, String requiredRole) {
        if (StringUtils.isBlank(userRole) || StringUtils.isBlank(requiredRole)) {
            return false;
        }
        
        // 管理员拥有所有权限
        if (UserConstant.ADMIN_ROLE.equals(userRole)) {
            return true;
        }
        
        // 被封号用户没有任何权限
        if (UserConstant.BAN_ROLE.equals(userRole)) {
            return false;
        }
        
        // 精确匹配角色
        return requiredRole.equals(userRole);
    }
    
    /**
     * 获取角色显示名称
     *
     * @param role 角色代码
     * @return 角色显示名称
     */
    private String getRoleDisplayName(String role) {
        if (StringUtils.isBlank(role)) {
            return "未知";
        }
        
        switch (role) {
            case UserConstant.ADMIN_ROLE:
                return "管理员";
            case UserConstant.DEFAULT_ROLE:
                return "普通用户";
            case UserConstant.BAN_ROLE:
                return "封禁用户";
            default:
                return role;
        }
    }
}