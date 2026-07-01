package com.ojbe.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

/**
 * Redis配置类
 * 配置RedisTemplate和StringRedisTemplate的序列化方式
 * 
 * @author dailw
 * @since 2024-01-20
 */
@Configuration
@EnableCaching
@Slf4j
public class RedisConfig implements CachingConfigurer {
    
    /**
     * 配置RedisTemplate
     * 使用Jackson2JsonRedisSerializer进行序列化
     * 
     * @param connectionFactory Redis连接工厂
     * @return RedisTemplate实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 设置Jackson序列化器 - 使用GenericJackson2JsonRedisSerializer替代已弃用的方法
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);
        
        // 设置key和value的序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        
        template.afterPropertiesSet();
        log.info("RedisTemplate配置完成");
        return template;
    }
    
    /**
     * 配置StringRedisTemplate
     * 用于字符串操作，性能更好
     * 
     * @param connectionFactory Redis连接工厂
     * @return StringRedisTemplate实例
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate(connectionFactory);
        log.info("StringRedisTemplate配置完成");
        return template;
    }

    /**
     * 配置RedisCacheManager — 按 cache 名设置差异化 TTL。
     *
     * 启用 activateDefaultTyping(NON_FINAL) 确保 Page、VO 等非 final 类型
     * 反序列化时能正确还原原始类型（而非 LinkedHashMap），避免 ClassCastException。
     * 所有 @Cacheable 方法的返回类型均为具体 POJO/Page，不存在 Map<String,Long>
     * 等 ImmutableCollections 类型，因此不会触发序列化格式不一致的问题。
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL);
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues();

        Map<String, Duration> ttlMap = Map.of(
                "questionPage", Duration.ofSeconds(30),
                "question", Duration.ofMinutes(10),
                "questionInfo", Duration.ofMinutes(5),
                "userInfo", Duration.ofMinutes(30),
                "tagList", Duration.ofHours(1),
                "template", Duration.ofHours(1),
                "solutionStats", Duration.ofMinutes(5),
                "submitStats", Duration.ofMinutes(5),
                "dashboard", Duration.ofMinutes(5)
        );

        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig);

        ttlMap.forEach((name, ttl) ->
                builder.withCacheConfiguration(name,
                        defaultConfig.entryTtl(ttl)));

        RedisCacheManager cacheManager = builder.build();

        log.info("RedisCacheManager配置完成（9 个 cache 区域独立 TTL）");
        return cacheManager;
    }

    /**
     * 缓存异常处理器：反序列化失败时当作缓存未命中，避免因 Redis 中残留的旧格式缓存数据导致 500 错误。
     */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis 缓存读取失败(key={}), 视为缓存未命中: {}", key, exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.error("Redis 缓存写入失败(key={}): {}", key, exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.error("Redis 缓存清除失败(key={}): {}", key, exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.error("Redis 缓存清空失败: {}", exception.getMessage());
            }
        };
    }
}