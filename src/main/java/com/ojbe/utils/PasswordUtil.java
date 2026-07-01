package com.ojbe.utils;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.stereotype.Component;

/**
 * 密码加密工具类
 * 使用Argon2算法进行密码加密和验证
 * 
 * @author trave
 */
@Component
public class PasswordUtil {
    
    private static final Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
    
    // Argon2参数配置
    private static final int ITERATIONS = 2;      // 迭代次数
    private static final int MEMORY = 65536;      // 内存使用量 (KB)
    private static final int PARALLELISM = 1;     // 并行度
    
    /**
     * 加密密码
     * 
     * @param password 原始密码
     * @return 加密后的密码哈希
     */
    public static String encryptPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        try {
            return argon2.hash(ITERATIONS, MEMORY, PARALLELISM, password.toCharArray());
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }
    
    /**
     * 验证密码
     * 
     * @param password 原始密码
     * @param hash 存储的密码哈希
     * @return 密码是否匹配
     */
    public static boolean verifyPassword(String password, String hash) {
        if (password == null || hash == null) {
            return false;
        }
        
        try {
            return argon2.verify(hash, password.toCharArray());
        } catch (Exception e) {
            return false;
        } finally {
            // 清理内存中的密码
            argon2.wipeArray(password.toCharArray());
        }
    }
    
    /**
     * 检查密码是否需要重新哈希
     * 当参数发生变化时，可能需要重新哈希密码
     * 
     * @param hash 当前密码哈希
     * @return 是否需要重新哈希
     */
    public static boolean needsRehash(String hash) {
        if (hash == null || hash.isEmpty()) {
            return true;
        }
        
        try {
            // 检查哈希格式是否为Argon2
            return !hash.startsWith("$argon2");
        } catch (Exception e) {
            return true;
        }
    }
}