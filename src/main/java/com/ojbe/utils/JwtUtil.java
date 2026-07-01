package com.ojbe.utils;

import com.ojbe.config.JwtConfig;
import com.ojbe.service.interfaces.RedisService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * JWT工具类
 * 提供Token生成、验证、解析等功能
 * 
 * @author trave
 */
@Slf4j
@Component
public class JwtUtil {
    
    @Autowired
    private JwtConfig jwtConfig;
    
    @Autowired
    private RedisService redisService;
    
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    
    /**
     * 生成访问Token
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @return JWT Token字符串
     */
    public String generateAccessToken(Long userId, String username, String role) {
        return generateToken(userId, username, role, jwtConfig.getAccessTokenExpiration());
    }
    
    /**
     * 生成刷新Token
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param role 用户角色
     * @return JWT Token字符串
     */
    public String generateRefreshToken(Long userId, String username, String role) {
        String refreshToken = generateToken(userId, username, role, jwtConfig.getRefreshTokenExpiration());
        
        // 将刷新Token存储到Redis中，过期时间与Token过期时间一致
        String redisKey = REFRESH_TOKEN_PREFIX + userId;
        long expirationSeconds = jwtConfig.getRefreshTokenExpiration() / 1000;
        
        try {
            redisService.set(redisKey, refreshToken, expirationSeconds, TimeUnit.SECONDS);
            log.info("刷新Token已存储到Redis: userId={}, key={}", userId, redisKey);
        } catch (Exception e) {
            log.error("存储刷新Token到Redis失败: userId={}, error={}", userId, e.getMessage(), e);
            // 这里可以选择抛出异常或者继续，根据业务需求决定
            // 如果Redis存储失败，Token仍然可以使用，但无法进行Redis验证
        }
        
        return refreshToken;
    }
    
    /**
     * 生成Token
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param expiration 过期时间（毫秒）
     * @return JWT Token字符串
     */
    private String generateToken(Long userId, String username, String role, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("userId", userId)
                .claim("role", role)
                .issuer(jwtConfig.getIssuer())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * 验证Token是否有效
     * 
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT Token验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 从Token中获取用户ID
     * 
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }
    
    /**
     * 从Token中获取用户名
     * 
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token JWT Token
     * @return 用户角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("role", String.class);
    }
    
    /**
     * 从Token中获取所有Claims
     * 
     * @param token JWT Token
     * @return Claims对象
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * 检查Token是否过期
     * 
     * @param token JWT Token
     * @return 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("检查Token过期时间失败: {}", e.getMessage());
            return true;
        }
    }
    
    /**
     * 获取Token剩余有效时间（毫秒）
     * 
     * @param token JWT Token
     * @return 剩余有效时间，如果Token无效返回0
     */
    public long getTokenRemainingTime(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remaining);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("获取Token剩余时间失败: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * 验证Redis中是否存在指定的刷新Token
     * 
     * @param userId 用户ID
     * @param refreshToken 刷新Token
     * @return 是否存在且匹配
     */
    public boolean validateRefreshTokenInRedis(Long userId, String refreshToken) {
        if (userId == null || refreshToken == null) {
            return false;
        }
        
        String redisKey = REFRESH_TOKEN_PREFIX + userId;
        
        try {
            Object storedToken = redisService.get(redisKey);
            if (storedToken != null && refreshToken.equals(storedToken.toString())) {
                log.debug("Redis中刷新Token验证成功: userId={}", userId);
                return true;
            } else {
                log.warn("Redis中刷新Token验证失败: userId={}, token存在={}", userId, storedToken != null);
                return false;
            }
        } catch (Exception e) {
            log.error("验证Redis中刷新Token时发生异常: userId={}, error={}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 从Redis中删除刷新Token
     * 
     * @param userId 用户ID
     * @return 是否删除成功
     */
    public boolean removeRefreshTokenFromRedis(Long userId) {
        if (userId == null) {
            return false;
        }
        
        String redisKey = REFRESH_TOKEN_PREFIX + userId;
        
        try {
            Boolean deleted = redisService.delete(redisKey);
            if (Boolean.TRUE.equals(deleted)) {
                log.info("成功从Redis中删除刷新Token: userId={}", userId);
                return true;
            } else {
                log.warn("从Redis中删除刷新Token失败: userId={}", userId);
                return false;
            }
        } catch (Exception e) {
            log.error("从Redis中删除刷新Token时发生异常: userId={}, error={}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 获取签名密钥
     * 
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}