package com.dailw.model.dto.user;

import lombok.Data;

/**
 * 用户登录请求
 * 
 * @author trave
 */
@Data
public class UserLoginRequest {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
}