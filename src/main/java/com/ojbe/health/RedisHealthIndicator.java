package com.ojbe.health;

import com.ojbe.service.interfaces.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis健康检查指示器
 * 用于监控Redis连接状态和可用性
 * 
 * @author dailw
 */
@Component
public class RedisHealthIndicator implements HealthIndicator {

    @Autowired
    private RedisService redisService;

    @Override
    public Health health() {
        try {
            // 尝试执行一个简单的Redis操作来检查连接
            String testKey = "health:check:" + System.currentTimeMillis();
            redisService.set(testKey, "test", 10, TimeUnit.SECONDS);
            String result = redisService.get(testKey).toString();
            
            if ("test".equals(result)) {
                redisService.delete(testKey);
                return Health.up()
                        .withDetail("status", "Redis is available")
                        .withDetail("database", "Redis")
                        .build();
            } else {
                return Health.down()
                        .withDetail("status", "Redis connection test failed")
                        .withDetail("error", "Test value mismatch")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("status", "Redis is unavailable")
                    .withDetail("error", e.getMessage())
                    .withDetail("exception", e.getClass().getSimpleName())
                    .build();
        }
    }
}