package com.ojbe.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁实现
 * 提供基于Redis的分布式锁功能，支持锁的获取、释放、续期等操作
 * 
 * @author dailw
 * @since 2024-01-01
 */
@Slf4j
@Component
public class RedisDistributedLock {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String LOCK_PREFIX = "distributed_lock:";
    private static final String UNLOCK_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('del', KEYS[1]) " +
        "else " +
        "    return 0 " +
        "end";
    
    private static final String RENEW_SCRIPT = 
        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
        "    return redis.call('expire', KEYS[1], ARGV[2]) " +
        "else " +
        "    return 0 " +
        "end";

    /**
     * 尝试获取分布式锁
     * 
     * @param lockKey 锁的键名
     * @param expireTime 锁的过期时间（秒）
     * @return 锁信息对象，如果获取失败返回null
     */
    public LockInfo tryLock(String lockKey, long expireTime) {
        return tryLock(lockKey, expireTime, 0, TimeUnit.SECONDS);
    }

    /**
     * 尝试获取分布式锁（带等待时间）
     * 
     * @param lockKey 锁的键名
     * @param expireTime 锁的过期时间（秒）
     * @param waitTime 等待时间
     * @param timeUnit 时间单位
     * @return 锁信息对象，如果获取失败返回null
     */
    public LockInfo tryLock(String lockKey, long expireTime, long waitTime, TimeUnit timeUnit) {
        String fullKey = LOCK_PREFIX + lockKey;
        String lockValue = generateLockValue();
        
        long waitTimeMillis = timeUnit.toMillis(waitTime);
        long startTime = System.currentTimeMillis();
        
        do {
            Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(fullKey, lockValue, expireTime, TimeUnit.SECONDS);
            
            if (Boolean.TRUE.equals(success)) {
                log.debug("成功获取分布式锁: {}, 锁值: {}, 过期时间: {}秒", lockKey, lockValue, expireTime);
                return new LockInfo(fullKey, lockValue, expireTime);
            }
            
            if (waitTimeMillis > 0) {
                try {
                    Thread.sleep(50); // 等待50毫秒后重试
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("获取锁时被中断: {}", lockKey);
                    return null;
                }
            }
        } while (waitTimeMillis > 0 && (System.currentTimeMillis() - startTime) < waitTimeMillis);
        
        log.debug("获取分布式锁失败: {}", lockKey);
        return null;
    }

    /**
     * 释放分布式锁
     * 
     * @param lockInfo 锁信息对象
     * @return 是否释放成功
     */
    public boolean unlock(LockInfo lockInfo) {
        if (lockInfo == null) {
            return false;
        }
        
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(UNLOCK_SCRIPT);
            script.setResultType(Long.class);
            
            Long result = stringRedisTemplate.execute(script, 
                Collections.singletonList(lockInfo.getLockKey()), 
                lockInfo.getLockValue());
            
            boolean success = Long.valueOf(1).equals(result);
            if (success) {
                log.debug("成功释放分布式锁: {}", lockInfo.getLockKey());
            } else {
                log.warn("释放分布式锁失败，锁可能已过期或被其他线程释放: {}", lockInfo.getLockKey());
            }
            return success;
        } catch (Exception e) {
            log.error("释放分布式锁异常: {}", lockInfo.getLockKey(), e);
            return false;
        }
    }

    /**
     * 续期分布式锁
     * 
     * @param lockInfo 锁信息对象
     * @param expireTime 新的过期时间（秒）
     * @return 是否续期成功
     */
    public boolean renewLock(LockInfo lockInfo, long expireTime) {
        if (lockInfo == null) {
            return false;
        }
        
        try {
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptText(RENEW_SCRIPT);
            script.setResultType(Long.class);
            
            Long result = stringRedisTemplate.execute(script,
                Collections.singletonList(lockInfo.getLockKey()),
                lockInfo.getLockValue(), String.valueOf(expireTime));
            
            boolean success = Long.valueOf(1).equals(result);
            if (success) {
                lockInfo.setExpireTime(expireTime);
                log.debug("成功续期分布式锁: {}, 新过期时间: {}秒", lockInfo.getLockKey(), expireTime);
            } else {
                log.warn("续期分布式锁失败，锁可能已过期或被其他线程释放: {}", lockInfo.getLockKey());
            }
            return success;
        } catch (Exception e) {
            log.error("续期分布式锁异常: {}", lockInfo.getLockKey(), e);
            return false;
        }
    }

    /**
     * 检查锁是否存在
     * 
     * @param lockKey 锁的键名
     * @return 是否存在
     */
    public boolean isLocked(String lockKey) {
        String fullKey = LOCK_PREFIX + lockKey;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(fullKey));
    }

    /**
     * 获取锁的剩余过期时间
     * 
     * @param lockKey 锁的键名
     * @return 剩余过期时间（秒），-1表示永不过期，-2表示键不存在
     */
    public long getLockTtl(String lockKey) {
        String fullKey = LOCK_PREFIX + lockKey;
        return stringRedisTemplate.getExpire(fullKey, TimeUnit.SECONDS);
    }

    /**
     * 强制释放锁（危险操作，仅在特殊情况下使用）
     * 
     * @param lockKey 锁的键名
     * @return 是否释放成功
     */
    public boolean forceUnlock(String lockKey) {
        String fullKey = LOCK_PREFIX + lockKey;
        Boolean result = stringRedisTemplate.delete(fullKey);
        log.warn("强制释放分布式锁: {}, 结果: {}", lockKey, result);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 生成锁值
     * 
     * @return 唯一的锁值
     */
    private String generateLockValue() {
        return Thread.currentThread().getId() + ":" + UUID.randomUUID().toString();
    }

    /**
     * 锁信息类
     */
    public static class LockInfo {
        private final String lockKey;
        private final String lockValue;
        private long expireTime;

        public LockInfo(String lockKey, String lockValue, long expireTime) {
            this.lockKey = lockKey;
            this.lockValue = lockValue;
            this.expireTime = expireTime;
        }

        public String getLockKey() {
            return lockKey;
        }

        public String getLockValue() {
            return lockValue;
        }

        public long getExpireTime() {
            return expireTime;
        }

        public void setExpireTime(long expireTime) {
            this.expireTime = expireTime;
        }

        @Override
        public String toString() {
            return "LockInfo{" +
                    "lockKey='" + lockKey + '\'' +
                    ", lockValue='" + lockValue + '\'' +
                    ", expireTime=" + expireTime +
                    '}';
        }
    }
}