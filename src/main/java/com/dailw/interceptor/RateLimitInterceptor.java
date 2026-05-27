package com.dailw.interceptor;

import com.dailw.common.ErrorCode;
import com.dailw.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * 登录限流拦截器
 * 基于 Redis 滑动窗口计数器，限制每个 IP 在时间窗口内的登录尝试次数
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${rate-limit.login.max-attempts:10}")
    private int maxAttempts;

    @Value("${rate-limit.login.window-seconds:60}")
    private int windowSeconds;

    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:login:";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String clientIp = getClientIp(request);
        String key = RATE_LIMIT_KEY_PREFIX + clientIp;

        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count == null) {
            return true;
        }

        if (count == 1) {
            stringRedisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }

        if (count > maxAttempts) {
            log.warn("登录限流触发: IP={}, 窗口内尝试次数={}", clientIp, count);
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
