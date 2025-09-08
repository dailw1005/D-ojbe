package com.dailw.service.interfaces;

import org.springframework.data.redis.core.ZSetOperations;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis服务接口
 * 提供Redis各种数据结构的操作方法
 * 
 * @author dailw
 * @since 2024-01-20
 */
public interface RedisService {
    
    // ========== 基础操作 ==========
    
    /**
     * 设置键值对
     * 
     * @param key 键
     * @param value 值
     */
    void set(String key, Object value);
    
    /**
     * 设置键值对并指定过期时间
     * 
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    void set(String key, Object value, long timeout, TimeUnit unit);
    
    /**
     * 获取值
     * 
     * @param key 键
     * @return 值
     */
    Object get(String key);
    
    /**
     * 删除键
     * 
     * @param key 键
     * @return 是否删除成功
     */
    Boolean delete(String key);
    
    /**
     * 批量删除键
     * 
     * @param keys 键集合
     * @return 删除的键数量
     */
    Long delete(Collection<String> keys);
    
    /**
     * 判断键是否存在
     * 
     * @param key 键
     * @return 是否存在
     */
    Boolean hasKey(String key);
    
    /**
     * 设置键的过期时间
     * 
     * @param key 键
     * @param timeout 过期时间
     * @param unit 时间单位
     * @return 是否设置成功
     */
    Boolean expire(String key, long timeout, TimeUnit unit);
    
    /**
     * 获取键的剩余过期时间
     * 
     * @param key 键
     * @return 剩余过期时间（秒）
     */
    Long getExpire(String key);
    
    // ========== 批量操作 ==========
    
    /**
     * 批量设置键值对
     * 
     * @param map 键值对映射
     */
    void multiSet(Map<String, Object> map);
    
    /**
     * 批量获取值
     * 
     * @param keys 键集合
     * @return 值列表
     */
    List<Object> multiGet(Collection<String> keys);
    
    // ========== 字符串操作 ==========
    
    /**
     * 递增
     * 
     * @param key 键
     * @return 递增后的值
     */
    Long increment(String key);
    
    /**
     * 递增指定步长
     * 
     * @param key 键
     * @param delta 步长
     * @return 递增后的值
     */
    Long increment(String key, long delta);
    
    /**
     * 递增指定步长（浮点数）
     * 
     * @param key 键
     * @param delta 步长
     * @return 递增后的值
     */
    Double increment(String key, double delta);
    
    /**
     * 递减
     * 
     * @param key 键
     * @return 递减后的值
     */
    Long decrement(String key);
    
    /**
     * 递减指定步长
     * 
     * @param key 键
     * @param delta 步长
     * @return 递减后的值
     */
    Long decrement(String key, long delta);
    
    // ========== 哈希操作 ==========
    
    /**
     * 设置哈希字段值
     * 
     * @param key 键
     * @param hashKey 哈希字段
     * @param value 值
     */
    void hSet(String key, String hashKey, Object value);
    
    /**
     * 获取哈希字段值
     * 
     * @param key 键
     * @param hashKey 哈希字段
     * @return 值
     */
    Object hGet(String key, String hashKey);
    
    /**
     * 获取所有哈希字段和值
     * 
     * @param key 键
     * @return 哈希映射
     */
    Map<Object, Object> hGetAll(String key);
    
    /**
     * 批量设置哈希字段值
     * 
     * @param key 键
     * @param map 哈希映射
     */
    void hMultiSet(String key, Map<String, Object> map);
    
    /**
     * 批量获取哈希字段值
     * 
     * @param key 键
     * @param hashKeys 哈希字段集合
     * @return 值列表
     */
    List<Object> hMultiGet(String key, Collection<Object> hashKeys);
    
    /**
     * 判断哈希字段是否存在
     * 
     * @param key 键
     * @param hashKey 哈希字段
     * @return 是否存在
     */
    Boolean hHasKey(String key, String hashKey);
    
    /**
     * 删除哈希字段
     * 
     * @param key 键
     * @param hashKeys 哈希字段
     * @return 删除的字段数量
     */
    Long hDelete(String key, Object... hashKeys);
    
    /**
     * 获取哈希字段数量
     * 
     * @param key 键
     * @return 字段数量
     */
    Long hSize(String key);
    
    // ========== 列表操作 ==========
    
    /**
     * 从左侧推入元素
     * 
     * @param key 键
     * @param values 值
     * @return 列表长度
     */
    Long lPush(String key, Object... values);
    
    /**
     * 从右侧推入元素
     * 
     * @param key 键
     * @param values 值
     * @return 列表长度
     */
    Long rPush(String key, Object... values);
    
    /**
     * 从左侧弹出元素
     * 
     * @param key 键
     * @return 弹出的元素
     */
    Object lPop(String key);
    
    /**
     * 从右侧弹出元素
     * 
     * @param key 键
     * @return 弹出的元素
     */
    Object rPop(String key);
    
    /**
     * 获取列表指定范围的元素
     * 
     * @param key 键
     * @param start 开始位置
     * @param end 结束位置
     * @return 元素列表
     */
    List<Object> lRange(String key, long start, long end);
    
    /**
     * 获取列表长度
     * 
     * @param key 键
     * @return 列表长度
     */
    Long lSize(String key);
    
    // ========== 集合操作 ==========
    
    /**
     * 向集合添加元素
     * 
     * @param key 键
     * @param values 值
     * @return 添加的元素数量
     */
    Long sAdd(String key, Object... values);
    
    /**
     * 获取集合所有元素
     * 
     * @param key 键
     * @return 元素集合
     */
    Set<Object> sMembers(String key);
    
    /**
     * 判断元素是否在集合中
     * 
     * @param key 键
     * @param value 值
     * @return 是否存在
     */
    Boolean sIsMember(String key, Object value);
    
    /**
     * 获取集合大小
     * 
     * @param key 键
     * @return 集合大小
     */
    Long sSize(String key);
    
    /**
     * 从集合移除元素
     * 
     * @param key 键
     * @param values 值
     * @return 移除的元素数量
     */
    Long sRemove(String key, Object... values);
    
    // ========== 有序集合操作 ==========
    
    /**
     * 向有序集合添加元素
     * 
     * @param key 键
     * @param value 值
     * @param score 分数
     * @return 是否添加成功
     */
    Boolean zAdd(String key, Object value, double score);
    
    /**
     * 批量向有序集合添加元素
     * 
     * @param key 键
     * @param tuples 元素和分数的元组集合
     * @return 添加的元素数量
     */
    Long zAdd(String key, Set<ZSetOperations.TypedTuple<Object>> tuples);
    
    /**
     * 获取有序集合指定范围的元素
     * 
     * @param key 键
     * @param start 开始位置
     * @param end 结束位置
     * @return 元素集合
     */
    Set<Object> zRange(String key, long start, long end);
    
    /**
     * 根据分数范围获取有序集合元素
     * 
     * @param key 键
     * @param min 最小分数
     * @param max 最大分数
     * @return 元素集合
     */
    Set<Object> zRangeByScore(String key, double min, double max);
    
    /**
     * 从有序集合移除元素
     * 
     * @param key 键
     * @param values 值
     * @return 移除的元素数量
     */
    Long zRemove(String key, Object... values);
    
    /**
     * 获取有序集合大小
     * 
     * @param key 键
     * @return 集合大小
     */
    Long zSize(String key);
}