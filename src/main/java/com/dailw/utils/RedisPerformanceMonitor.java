package com.dailw.utils;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Redis性能监控组件
 * 用于监控Redis操作的性能指标
 * 
 * @author dailw
 * @since 2024-01-20
 */
@Component
@Slf4j
public class RedisPerformanceMonitor {
    
    private final MeterRegistry meterRegistry;
    private final Map<String, Timer> operationTimers = new ConcurrentHashMap<>();
    private final AtomicLong totalOperations = new AtomicLong(0);
    private final AtomicLong failedOperations = new AtomicLong(0);
    
    /**
     * 构造函数
     * 
     * @param meterRegistry 指标注册器
     */
    public RedisPerformanceMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    /**
     * 记录操作执行时间
     * 
     * @param operation 操作名称
     * @param executionTime 执行时间（毫秒）
     */
    public void recordOperation(String operation, long executionTime) {
        totalOperations.incrementAndGet();
        
        // 获取或创建操作计时器
        Timer timer = operationTimers.computeIfAbsent(operation, 
            op -> Timer.builder("redis.operation.duration")
                      .tag("operation", op)
                      .description("Redis operation execution time")
                      .register(meterRegistry));
        
        // 记录执行时间
        timer.record(executionTime, TimeUnit.MILLISECONDS);
        
        // 检测慢查询
        if (executionTime > 1000) {
            log.warn("Redis慢查询检测: operation={}, duration={}ms", operation, executionTime);
        }
        
        log.debug("Redis操作记录: operation={}, duration={}ms", operation, executionTime);
    }
    
    /**
     * 记录操作失败
     * 
     * @param operation 操作名称
     */
    public void recordFailure(String operation) {
        failedOperations.incrementAndGet();
        
        // 记录失败计数
        Counter.builder("redis.operation.failures")
               .tag("operation", operation)
               .description("Redis operation failures count")
               .register(meterRegistry)
               .increment();
        
        log.warn("Redis操作失败: operation={}", operation);
    }
    
    /**
     * 记录操作成功
     * 
     * @param operation 操作名称
     */
    public void recordSuccess(String operation) {
        Counter.builder("redis.operation.success")
               .tag("operation", operation)
               .description("Redis operation success count")
               .register(meterRegistry)
               .increment();
        
        log.debug("Redis操作成功: operation={}", operation);
    }
    
    /**
     * 获取总操作数
     * 
     * @return 总操作数
     */
    public long getTotalOperations() {
        return totalOperations.get();
    }
    
    /**
     * 获取失败操作数
     * 
     * @return 失败操作数
     */
    public long getFailedOperations() {
        return failedOperations.get();
    }
    
    /**
     * 获取成功率
     * 
     * @return 成功率（百分比）
     */
    public double getSuccessRate() {
        long total = totalOperations.get();
        if (total == 0) {
            return 100.0;
        }
        long failed = failedOperations.get();
        return (double)(total - failed) / total * 100;
    }
    
    /**
     * 定时输出统计信息
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000)
    public void logStatistics() {
        long total = totalOperations.get();
        long failed = failedOperations.get();
        double successRate = getSuccessRate();
        
        if (total > 0) {
            log.info("Redis操作统计 - 总操作数: {}, 失败数: {}, 成功率: {}%",
                    total, failed, successRate);
            
            // 重置计数器（可选，根据需求决定是否重置）
            // totalOperations.set(0);
            // failedOperations.set(0);
        }
    }
    
    /**
     * 获取操作平均执行时间
     * 
     * @param operation 操作名称
     * @return 平均执行时间（毫秒）
     */
    public double getAverageExecutionTime(String operation) {
        Timer timer = operationTimers.get(operation);
        if (timer != null) {
            return timer.mean(TimeUnit.MILLISECONDS);
        }
        return 0.0;
    }
    
    /**
     * 获取操作最大执行时间
     * 
     * @param operation 操作名称
     * @return 最大执行时间（毫秒）
     */
    public double getMaxExecutionTime(String operation) {
        Timer timer = operationTimers.get(operation);
        if (timer != null) {
            return timer.max(TimeUnit.MILLISECONDS);
        }
        return 0.0;
    }
    
    /**
     * 获取所有监控的操作名称
     * 
     * @return 操作名称集合
     */
    public java.util.Set<String> getMonitoredOperations() {
        return operationTimers.keySet();
    }
}