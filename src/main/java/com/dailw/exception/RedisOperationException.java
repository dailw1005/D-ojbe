package com.dailw.exception;

/**
 * Redis操作异常类
 * 用于封装Redis操作过程中发生的异常
 * 
 * @author dailw
 * @since 2024-01-20
 */
public class RedisOperationException extends RuntimeException {
    
    /**
     * 构造函数
     * 
     * @param message 异常消息
     */
    public RedisOperationException(String message) {
        super(message);
    }
    
    /**
     * 构造函数
     * 
     * @param message 异常消息
     * @param cause 原始异常
     */
    public RedisOperationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 构造函数
     * 
     * @param cause 原始异常
     */
    public RedisOperationException(Throwable cause) {
        super(cause);
    }
}