package com.dailw.service.impl;

import com.dailw.exception.RedisOperationException;
import com.dailw.service.interfaces.RedisService;
import com.dailw.utils.RedisPerformanceMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis服务实现类
 * 实现Redis各种数据结构的操作方法
 * 
 * @author dailw
 * @since 2024-01-20
 */
@Service
@Slf4j
public class RedisServiceImpl implements RedisService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    @Autowired
    private RedisPerformanceMonitor performanceMonitor;
    
    // ========== 基础操作 ==========
    
    @Override
    public void set(String key, Object value) {
        try {
            long startTime = System.currentTimeMillis();
            redisTemplate.opsForValue().set(key, value);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("SET", executionTime);
            performanceMonitor.recordSuccess("SET");
            log.debug("Redis SET操作成功: key={}", key);
        } catch (Exception e) {
            performanceMonitor.recordFailure("SET");
            log.error("Redis SET操作失败: key={}", key, e);
            throw new RedisOperationException("SET操作失败", e);
        }
    }
    
    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            long startTime = System.currentTimeMillis();
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("SETEX", executionTime);
            performanceMonitor.recordSuccess("SETEX");
            log.debug("Redis SETEX操作成功: key={}, timeout={} {}", key, timeout, unit);
        } catch (Exception e) {
            performanceMonitor.recordFailure("SETEX");
            log.error("Redis SETEX操作失败: key={}", key, e);
            throw new RedisOperationException("SETEX操作失败", e);
        }
    }
    
    @Override
    public Object get(String key) {
        try {
            long startTime = System.currentTimeMillis();
            Object result = redisTemplate.opsForValue().get(key);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("GET", executionTime);
            performanceMonitor.recordSuccess("GET");
            log.debug("Redis GET操作成功: key={}", key);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("GET");
            log.error("Redis GET操作失败: key={}", key, e);
            throw new RedisOperationException("GET操作失败", e);
        }
    }
    
    @Override
    public Boolean delete(String key) {
        try {
            long startTime = System.currentTimeMillis();
            Boolean result = redisTemplate.delete(key);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("DEL", executionTime);
            performanceMonitor.recordSuccess("DEL");
            log.debug("Redis DELETE操作成功: key={}", key);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("DEL");
            log.error("Redis DELETE操作失败: key={}", key, e);
            throw new RedisOperationException("DELETE操作失败", e);
        }
    }
    
    @Override
    public Long delete(Collection<String> keys) {
        try {
            long startTime = System.currentTimeMillis();
            Long result = redisTemplate.delete(keys);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("DEL_BATCH", executionTime);
            performanceMonitor.recordSuccess("DEL_BATCH");
            log.debug("Redis批量DELETE操作成功: keys={}", keys);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("DEL_BATCH");
            log.error("Redis批量DELETE操作失败: keys={}", keys, e);
            throw new RedisOperationException("批量DELETE操作失败", e);
        }
    }
    
    @Override
    public Boolean hasKey(String key) {
        try {
            long startTime = System.currentTimeMillis();
            Boolean result = redisTemplate.hasKey(key);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("EXISTS", executionTime);
            performanceMonitor.recordSuccess("EXISTS");
            log.debug("Redis EXISTS操作成功: key={}", key);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("EXISTS");
            log.error("Redis EXISTS操作失败: key={}", key, e);
            throw new RedisOperationException("EXISTS操作失败", e);
        }
    }
    
    @Override
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            long startTime = System.currentTimeMillis();
            Boolean result = redisTemplate.expire(key, timeout, unit);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("EXPIRE", executionTime);
            performanceMonitor.recordSuccess("EXPIRE");
            log.debug("Redis EXPIRE操作成功: key={}, timeout={} {}", key, timeout, unit);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("EXPIRE");
            log.error("Redis EXPIRE操作失败: key={}", key, e);
            throw new RedisOperationException("EXPIRE操作失败", e);
        }
    }
    
    @Override
    public Long getExpire(String key) {
        try {
            long startTime = System.currentTimeMillis();
            Long result = redisTemplate.getExpire(key);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("TTL", executionTime);
            performanceMonitor.recordSuccess("TTL");
            log.debug("Redis TTL操作成功: key={}", key);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("TTL");
            log.error("Redis TTL操作失败: key={}", key, e);
            throw new RedisOperationException("TTL操作失败", e);
        }
    }
    
    // ========== 批量操作 ==========
    
    @Override
    public void multiSet(Map<String, Object> map) {
        try {
            long startTime = System.currentTimeMillis();
            redisTemplate.opsForValue().multiSet(map);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("MSET", executionTime);
            performanceMonitor.recordSuccess("MSET");
            log.debug("Redis MSET操作成功: size={}", map.size());
        } catch (Exception e) {
            performanceMonitor.recordFailure("MSET");
            log.error("Redis MSET操作失败", e);
            throw new RedisOperationException("MSET操作失败", e);
        }
    }
    
    @Override
    public List<Object> multiGet(Collection<String> keys) {
        try {
            long startTime = System.currentTimeMillis();
            List<Object> result = redisTemplate.opsForValue().multiGet(keys);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("MGET", executionTime);
            performanceMonitor.recordSuccess("MGET");
            log.debug("Redis MGET操作成功: keys={}", keys);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("MGET");
            log.error("Redis MGET操作失败: keys={}", keys, e);
            throw new RedisOperationException("MGET操作失败", e);
        }
    }
    
    // ========== 字符串操作 ==========
    
    @Override
    public Long increment(String key) {
        try {
            long startTime = System.currentTimeMillis();
            Long result = redisTemplate.opsForValue().increment(key);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("INCR", executionTime);
            performanceMonitor.recordSuccess("INCR");
            log.debug("Redis INCR操作成功: key={}", key);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("INCR");
            log.error("Redis INCR操作失败: key={}", key, e);
            throw new RedisOperationException("INCR操作失败", e);
        }
    }
    
    @Override
    public Long increment(String key, long delta) {
        try {
            long startTime = System.currentTimeMillis();
            Long result = redisTemplate.opsForValue().increment(key, delta);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("INCRBY", executionTime);
            performanceMonitor.recordSuccess("INCRBY");
            log.debug("Redis INCRBY操作成功: key={}, delta={}", key, delta);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("INCRBY");
            log.error("Redis INCRBY操作失败: key={}, delta={}", key, delta, e);
            throw new RedisOperationException("INCRBY操作失败", e);
        }
    }
    
    @Override
    public Double increment(String key, double delta) {
        try {
            long startTime = System.currentTimeMillis();
            Double result = redisTemplate.opsForValue().increment(key, delta);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("INCRBYFLOAT", executionTime);
            performanceMonitor.recordSuccess("INCRBYFLOAT");
            log.debug("Redis INCRBYFLOAT操作成功: key={}, delta={}", key, delta);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("INCRBYFLOAT");
            log.error("Redis INCRBYFLOAT操作失败: key={}, delta={}", key, delta, e);
            throw new RedisOperationException("INCRBYFLOAT操作失败", e);
        }
    }
    
    @Override
    public Long decrement(String key) {
        try {
            long startTime = System.currentTimeMillis();
            Long result = redisTemplate.opsForValue().decrement(key);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("DECR", executionTime);
            performanceMonitor.recordSuccess("DECR");
            log.debug("Redis DECR操作成功: key={}", key);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("DECR");
            log.error("Redis DECR操作失败: key={}", key, e);
            throw new RedisOperationException("DECR操作失败", e);
        }
    }
    
    @Override
    public Long decrement(String key, long delta) {
        try {
            long startTime = System.currentTimeMillis();
            Long result = redisTemplate.opsForValue().decrement(key, delta);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("DECRBY", executionTime);
            performanceMonitor.recordSuccess("DECRBY");
            log.debug("Redis DECRBY操作成功: key={}, delta={}", key, delta);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("DECRBY");
            log.error("Redis DECRBY操作失败: key={}, delta={}", key, delta, e);
            throw new RedisOperationException("DECRBY操作失败", e);
        }
    }
    
    // ========== 哈希操作 ==========
    
    @Override
    public void hSet(String key, String hashKey, Object value) {
        try {
            long startTime = System.currentTimeMillis();
            redisTemplate.opsForHash().put(key, hashKey, value);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("HSET", executionTime);
            performanceMonitor.recordSuccess("HSET");
            log.debug("Redis HSET操作成功: key={}, hashKey={}", key, hashKey);
        } catch (Exception e) {
            performanceMonitor.recordFailure("HSET");
            log.error("Redis HSET操作失败: key={}, hashKey={}", key, hashKey, e);
            throw new RedisOperationException("HSET操作失败", e);
        }
    }
    
    @Override
    public Object hGet(String key, String hashKey) {
        try {
            long startTime = System.currentTimeMillis();
            Object result = redisTemplate.opsForHash().get(key, hashKey);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("HGET", executionTime);
            performanceMonitor.recordSuccess("HGET");
            log.debug("Redis HGET操作成功: key={}, hashKey={}", key, hashKey);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("HGET");
            log.error("Redis HGET操作失败: key={}, hashKey={}", key, hashKey, e);
            throw new RedisOperationException("HGET操作失败", e);
        }
    }
    
    @Override
    public Map<Object, Object> hGetAll(String key) {
        try {
            long startTime = System.currentTimeMillis();
            Map<Object, Object> result = redisTemplate.opsForHash().entries(key);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("HGETALL", executionTime);
            performanceMonitor.recordSuccess("HGETALL");
            log.debug("Redis HGETALL操作成功: key={}", key);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("HGETALL");
            log.error("Redis HGETALL操作失败: key={}", key, e);
            throw new RedisOperationException("HGETALL操作失败", e);
        }
    }
    
    @Override
    public void hMultiSet(String key, Map<String, Object> map) {
        try {
            long startTime = System.currentTimeMillis();
            redisTemplate.opsForHash().putAll(key, map);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("HMSET", executionTime);
            performanceMonitor.recordSuccess("HMSET");
            log.debug("Redis HMSET操作成功: key={}, size={}", key, map.size());
        } catch (Exception e) {
            performanceMonitor.recordFailure("HMSET");
            log.error("Redis HMSET操作失败: key={}", key, e);
            throw new RedisOperationException("HMSET操作失败", e);
        }
    }
    
    @Override
    public List<Object> hMultiGet(String key, Collection<Object> hashKeys) {
        try {
            long startTime = System.currentTimeMillis();
            List<Object> result = redisTemplate.opsForHash().multiGet(key, hashKeys);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("HMGET", executionTime);
            performanceMonitor.recordSuccess("HMGET");
            log.debug("Redis HMGET操作成功: key={}, hashKeys={}", key, hashKeys);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("HMGET");
            log.error("Redis HMGET操作失败: key={}, hashKeys={}", key, hashKeys, e);
            throw new RedisOperationException("HMGET操作失败", e);
        }
    }
    
    @Override
    public Boolean hHasKey(String key, String hashKey) {
        try {
            long startTime = System.currentTimeMillis();
            Boolean result = redisTemplate.opsForHash().hasKey(key, hashKey);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("HEXISTS", executionTime);
            performanceMonitor.recordSuccess("HEXISTS");
            log.debug("Redis HEXISTS操作成功: key={}, hashKey={}", key, hashKey);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("HEXISTS");
            log.error("Redis HEXISTS操作失败: key={}, hashKey={}", key, hashKey, e);
            throw new RedisOperationException("HEXISTS操作失败", e);
        }
    }
    
    @Override
    public Long hDelete(String key, Object... hashKeys) {
        try {
            long startTime = System.currentTimeMillis();
            Long result = redisTemplate.opsForHash().delete(key, hashKeys);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("HDEL", executionTime);
            performanceMonitor.recordSuccess("HDEL");
            log.debug("Redis HDEL操作成功: key={}, hashKeys={}", key, hashKeys);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("HDEL");
            log.error("Redis HDEL操作失败: key={}, hashKeys={}", key, hashKeys, e);
            throw new RedisOperationException("HDEL操作失败", e);
        }
    }
    
    @Override
    public Long hSize(String key) {
        try {
            long startTime = System.currentTimeMillis();
            Long result = redisTemplate.opsForHash().size(key);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("HLEN", executionTime);
            performanceMonitor.recordSuccess("HLEN");
            log.debug("Redis HLEN操作成功: key={}", key);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("HLEN");
            log.error("Redis HLEN操作失败: key={}", key, e);
            throw new RedisOperationException("HLEN操作失败", e);
        }
    }
    
    // ========== 列表操作 ==========
    
    @Override
    public Long lPush(String key, Object... values) {
        try {
            long startTime = System.currentTimeMillis();
            Long result = redisTemplate.opsForList().leftPushAll(key, values);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("LPUSH", executionTime);
            performanceMonitor.recordSuccess("LPUSH");
            log.debug("Redis LPUSH操作成功: key={}, count={}", key, values.length);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("LPUSH");
            log.error("Redis LPUSH操作失败: key={}", key, e);
            throw new RedisOperationException("LPUSH操作失败", e);
        }
    }
    
    @Override
    public Long rPush(String key, Object... values) {
        try {
            long startTime = System.currentTimeMillis();
            Long result = redisTemplate.opsForList().rightPushAll(key, values);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("RPUSH", executionTime);
            performanceMonitor.recordSuccess("RPUSH");
            log.debug("Redis RPUSH操作成功: key={}, count={}", key, values.length);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("RPUSH");
            log.error("Redis RPUSH操作失败: key={}", key, e);
            throw new RedisOperationException("RPUSH操作失败", e);
        }
    }
    
    @Override
    public Object lPop(String key) {
        try {
            long startTime = System.currentTimeMillis();
            Object result = redisTemplate.opsForList().leftPop(key);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("LPOP", executionTime);
            performanceMonitor.recordSuccess("LPOP");
            log.debug("Redis LPOP操作成功: key={}", key);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("LPOP");
            log.error("Redis LPOP操作失败: key={}", key, e);
            throw new RedisOperationException("LPOP操作失败", e);
        }
    }
    
    @Override
    public Object rPop(String key) {
        try {
            long startTime = System.currentTimeMillis();
            Object result = redisTemplate.opsForList().rightPop(key);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("RPOP", executionTime);
            performanceMonitor.recordSuccess("RPOP");
            log.debug("Redis RPOP操作成功: key={}", key);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("RPOP");
            log.error("Redis RPOP操作失败: key={}", key, e);
            throw new RedisOperationException("RPOP操作失败", e);
        }
    }
    
    @Override
    public List<Object> lRange(String key, long start, long end) {
        try {
            long startTime = System.currentTimeMillis();
            List<Object> result = redisTemplate.opsForList().range(key, start, end);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("LRANGE", executionTime);
            performanceMonitor.recordSuccess("LRANGE");
            log.debug("Redis LRANGE操作成功: key={}, start={}, end={}", key, start, end);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("LRANGE");
            log.error("Redis LRANGE操作失败: key={}, start={}, end={}", key, start, end, e);
            throw new RedisOperationException("LRANGE操作失败", e);
        }
    }
    
    @Override
    public Long lSize(String key) {
        try {
            long startTime = System.currentTimeMillis();
            Long result = redisTemplate.opsForList().size(key);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("LLEN", executionTime);
            performanceMonitor.recordSuccess("LLEN");
            log.debug("Redis LLEN操作成功: key={}", key);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("LLEN");
            log.error("Redis LLEN操作失败: key={}", key, e);
            throw new RedisOperationException("LLEN操作失败", e);
        }
    }
    
    // ========== 集合操作 ==========
    
    @Override
    public Long sAdd(String key, Object... values) {
        try {
            long startTime = System.currentTimeMillis();
            Long result = redisTemplate.opsForSet().add(key, values);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("SADD", executionTime);
            performanceMonitor.recordSuccess("SADD");
            log.debug("Redis SADD操作成功: key={}, count={}", key, values.length);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("SADD");
            log.error("Redis SADD操作失败: key={}", key, e);
            throw new RedisOperationException("SADD操作失败", e);
        }
    }
    
    @Override
    public Set<Object> sMembers(String key) {
        try {
            long startTime = System.currentTimeMillis();
            Set<Object> result = redisTemplate.opsForSet().members(key);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("SMEMBERS", executionTime);
            performanceMonitor.recordSuccess("SMEMBERS");
            log.debug("Redis SMEMBERS操作成功: key={}", key);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("SMEMBERS");
            log.error("Redis SMEMBERS操作失败: key={}", key, e);
            throw new RedisOperationException("SMEMBERS操作失败", e);
        }
    }
    
    @Override
    public Boolean sIsMember(String key, Object value) {
        try {
            long startTime = System.currentTimeMillis();
            Boolean result = redisTemplate.opsForSet().isMember(key, value);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("SISMEMBER", executionTime);
            performanceMonitor.recordSuccess("SISMEMBER");
            log.debug("Redis SISMEMBER操作成功: key={}", key);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("SISMEMBER");
            log.error("Redis SISMEMBER操作失败: key={}", key, e);
            throw new RedisOperationException("SISMEMBER操作失败", e);
        }
    }
    
    @Override
    public Long sSize(String key) {
        try {
            long startTime = System.currentTimeMillis();
            Long result = redisTemplate.opsForSet().size(key);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("SCARD", executionTime);
            performanceMonitor.recordSuccess("SCARD");
            log.debug("Redis SCARD操作成功: key={}", key);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("SCARD");
            log.error("Redis SCARD操作失败: key={}", key, e);
            throw new RedisOperationException("SCARD操作失败", e);
        }
    }
    
    @Override
    public Long sRemove(String key, Object... values) {
        try {
            long startTime = System.currentTimeMillis();
            Long result = redisTemplate.opsForSet().remove(key, values);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("SREM", executionTime);
            performanceMonitor.recordSuccess("SREM");
            log.debug("Redis SREM操作成功: key={}, count={}", key, values.length);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("SREM");
            log.error("Redis SREM操作失败: key={}", key, e);
            throw new RedisOperationException("SREM操作失败", e);
        }
    }
    
    // ========== 有序集合操作 ==========
    
    @Override
    public Boolean zAdd(String key, Object value, double score) {
        try {
            long startTime = System.currentTimeMillis();
            Boolean result = redisTemplate.opsForZSet().add(key, value, score);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("ZADD", executionTime);
            performanceMonitor.recordSuccess("ZADD");
            log.debug("Redis ZADD操作成功: key={}, score={}", key, score);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("ZADD");
            log.error("Redis ZADD操作失败: key={}, score={}", key, score, e);
            throw new RedisOperationException("ZADD操作失败", e);
        }
    }
    
    @Override
    public Long zAdd(String key, Set<ZSetOperations.TypedTuple<Object>> tuples) {
        try {
            long startTime = System.currentTimeMillis();
            Long result = redisTemplate.opsForZSet().add(key, tuples);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("ZADD_BATCH", executionTime);
            performanceMonitor.recordSuccess("ZADD_BATCH");
            log.debug("Redis批量ZADD操作成功: key={}, count={}", key, tuples.size());
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("ZADD_BATCH");
            log.error("Redis批量ZADD操作失败: key={}", key, e);
            throw new RedisOperationException("批量ZADD操作失败", e);
        }
    }
    
    @Override
    public Set<Object> zRange(String key, long start, long end) {
        try {
            long startTime = System.currentTimeMillis();
            Set<Object> result = redisTemplate.opsForZSet().range(key, start, end);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("ZRANGE", executionTime);
            performanceMonitor.recordSuccess("ZRANGE");
            log.debug("Redis ZRANGE操作成功: key={}, start={}, end={}", key, start, end);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("ZRANGE");
            log.error("Redis ZRANGE操作失败: key={}, start={}, end={}", key, start, end, e);
            throw new RedisOperationException("ZRANGE操作失败", e);
        }
    }
    
    @Override
    public Set<Object> zRangeByScore(String key, double min, double max) {
        try {
            long startTime = System.currentTimeMillis();
            Set<Object> result = redisTemplate.opsForZSet().rangeByScore(key, min, max);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("ZRANGEBYSCORE", executionTime);
            performanceMonitor.recordSuccess("ZRANGEBYSCORE");
            log.debug("Redis ZRANGEBYSCORE操作成功: key={}, min={}, max={}", key, min, max);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("ZRANGEBYSCORE");
            log.error("Redis ZRANGEBYSCORE操作失败: key={}, min={}, max={}", key, min, max, e);
            throw new RedisOperationException("ZRANGEBYSCORE操作失败", e);
        }
    }
    
    @Override
    public Long zRemove(String key, Object... values) {
        try {
            long startTime = System.currentTimeMillis();
            Long result = redisTemplate.opsForZSet().remove(key, values);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("ZREM", executionTime);
            performanceMonitor.recordSuccess("ZREM");
            log.debug("Redis ZREM操作成功: key={}, count={}", key, values.length);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("ZREM");
            log.error("Redis ZREM操作失败: key={}", key, e);
            throw new RedisOperationException("ZREM操作失败", e);
        }
    }
    
    @Override
    public Long zSize(String key) {
        try {
            long startTime = System.currentTimeMillis();
            Long result = redisTemplate.opsForZSet().size(key);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("ZCARD", executionTime);
            performanceMonitor.recordSuccess("ZCARD");
            log.debug("Redis ZCARD操作成功: key={}", key);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("ZCARD");
            log.error("Redis ZCARD操作失败: key={}", key, e);
            throw new RedisOperationException("ZCARD操作失败", e);
        }
    }

    @Override
    public Long zRank(String key, Object value) {
        try {
            long startTime = System.currentTimeMillis();
            Long result = redisTemplate.opsForZSet().rank(key, value);
            long executionTime = System.currentTimeMillis() - startTime;
            performanceMonitor.recordOperation("ZRANK", executionTime);
            performanceMonitor.recordSuccess("ZRANK");
            log.debug("Redis ZRANK操作成功: key={}, value={}", key, value);
            return result;
        } catch (Exception e) {
            performanceMonitor.recordFailure("ZRANK");
            log.error("Redis ZRANK操作失败: key={}, value={}", key, e);
            throw new RedisOperationException("ZRANK操作失败", e);
        }
    }
}