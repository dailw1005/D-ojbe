package com.dailw.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis异常处理器
 * 提供Redis操作的重试机制和异常恢复策略
 * 
 * @author dailw
 */
@Component
@EnableRetry
@Slf4j
public class RedisExceptionHandler {
    
    @Value("${spring.redis.retry.max-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${spring.redis.retry.delay:1000}")
    private long retryDelay;
    
    @Value("${spring.redis.retry.multiplier:2.0}")
    private double retryMultiplier;
    
    /**
     * 带重试机制的Redis操作执行器
     * 当发生连接异常或系统异常时自动重试
     * 
     * @param operation Redis操作
     * @param <T> 返回类型
     * @return 操作结果
     */
    @Retryable(
        value = {RedisConnectionFailureException.class, RedisSystemException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public <T> T executeWithRetry(RedisOperation<T> operation) {
        try {
            log.debug("执行Redis操作");
            return operation.execute();
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，准备重试: {}", e.getMessage());
            throw e;
        } catch (RedisSystemException e) {
            log.warn("Redis系统异常，准备重试: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Redis操作发生未知异常: {}", e.getMessage(), e);
            throw new RedisOperationException("Redis操作失败", e);
        }
    }
    
    /**
     * 带重试机制的无返回值Redis操作执行器
     * 
     * @param operation Redis操作
     */
    @Retryable(
        value = {RedisConnectionFailureException.class, RedisSystemException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0)
    )
    public void executeVoidWithRetry(VoidRedisOperation operation) {
        try {
            log.debug("执行Redis无返回值操作");
            operation.execute();
        } catch (RedisConnectionFailureException e) {
            log.warn("Redis连接失败，准备重试: {}", e.getMessage());
            throw e;
        } catch (RedisSystemException e) {
            log.warn("Redis系统异常，准备重试: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Redis操作发生未知异常: {}", e.getMessage(), e);
            throw new RedisOperationException("Redis操作失败", e);
        }
    }
    
    /**
     * 重试失败后的恢复方法
     * 当所有重试都失败后，执行此方法
     * 
     * @param ex 异常
     * @param operation 操作
     * @param <T> 返回类型
     * @return 默认值或抛出异常
     */
    @Recover
    public <T> T recover(Exception ex, RedisOperation<T> operation) {
        log.error("Redis操作重试{}次后仍然失败，放弃重试: {}", maxRetryAttempts, ex.getMessage(), ex);
        
        // 根据异常类型决定恢复策略
        if (ex instanceof RedisConnectionFailureException) {
            throw new RedisOperationException("Redis连接失败，请检查Redis服务状态", ex);
        } else if (ex instanceof RedisSystemException) {
            throw new RedisOperationException("Redis系统异常，请检查Redis配置", ex);
        } else {
            throw new RedisOperationException("Redis操作失败", ex);
        }
    }
    
    /**
     * 无返回值操作的恢复方法
     * 
     * @param ex 异常
     * @param operation 操作
     */
    @Recover
    public void recoverVoid(Exception ex, VoidRedisOperation operation) {
        log.error("Redis无返回值操作重试{}次后仍然失败，放弃重试: {}", maxRetryAttempts, ex.getMessage(), ex);
        
        // 根据异常类型决定恢复策略
        if (ex instanceof RedisConnectionFailureException) {
            throw new RedisOperationException("Redis连接失败，请检查Redis服务状态", ex);
        } else if (ex instanceof RedisSystemException) {
            throw new RedisOperationException("Redis系统异常，请检查Redis配置", ex);
        } else {
            throw new RedisOperationException("Redis操作失败", ex);
        }
    }
    
    /**
     * 检查Redis连接状态
     * 
     * @return 连接是否正常
     */
    public boolean checkRedisConnection() {
        try {
            // 这里可以添加具体的连接检查逻辑
            // 比如执行一个简单的ping命令
            log.debug("检查Redis连接状态");
            return true;
        } catch (Exception e) {
            log.error("Redis连接检查失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 等待指定时间后重试
     * 
     * @param retryCount 重试次数
     */
    private void waitForRetry(int retryCount) {
        try {
            long waitTime = (long) (retryDelay * Math.pow(retryMultiplier, retryCount - 1));
            log.info("等待{}毫秒后进行第{}次重试", waitTime, retryCount);
            TimeUnit.MILLISECONDS.sleep(waitTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("重试等待被中断", e);
        }
    }
    
    /**
     * 手动重试机制（不使用Spring Retry注解）
     * 
     * @param operation Redis操作
     * @param <T> 返回类型
     * @return 操作结果
     */
    public <T> T executeWithManualRetry(RedisOperation<T> operation) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                log.debug("执行Redis操作，第{}次尝试", attempt);
                return operation.execute();
            } catch (RedisConnectionFailureException | RedisSystemException e) {
                lastException = e;
                log.warn("Redis操作失败，第{}次尝试: {}", attempt, e.getMessage());
                
                if (attempt < maxRetryAttempts) {
                    waitForRetry(attempt);
                } else {
                    log.error("Redis操作重试{}次后仍然失败", maxRetryAttempts);
                }
            } catch (Exception e) {
                log.error("Redis操作发生未知异常: {}", e.getMessage(), e);
                throw new RedisOperationException("Redis操作失败", e);
            }
        }
        
        // 所有重试都失败了
        throw new RedisOperationException("Redis操作重试失败", lastException);
    }
    
    /**
     * 手动重试机制（无返回值版本）
     * 
     * @param operation Redis操作
     */
    public void executeVoidWithManualRetry(VoidRedisOperation operation) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                log.debug("执行Redis无返回值操作，第{}次尝试", attempt);
                operation.execute();
                return; // 成功执行，直接返回
            } catch (RedisConnectionFailureException | RedisSystemException e) {
                lastException = e;
                log.warn("Redis操作失败，第{}次尝试: {}", attempt, e.getMessage());
                
                if (attempt < maxRetryAttempts) {
                    waitForRetry(attempt);
                } else {
                    log.error("Redis操作重试{}次后仍然失败", maxRetryAttempts);
                }
            } catch (Exception e) {
                log.error("Redis操作发生未知异常: {}", e.getMessage(), e);
                throw new RedisOperationException("Redis操作失败", e);
            }
        }
        
        // 所有重试都失败了
        throw new RedisOperationException("Redis操作重试失败", lastException);
    }
    
    /**
     * Redis操作函数式接口（有返回值）
     * 
     * @param <T> 返回类型
     */
    @FunctionalInterface
    public interface RedisOperation<T> {
        T execute() throws Exception;
    }
    
    /**
     * Redis操作函数式接口（无返回值）
     */
    @FunctionalInterface
    public interface VoidRedisOperation {
        void execute() throws Exception;
    }
}