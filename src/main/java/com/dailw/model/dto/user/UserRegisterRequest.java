package com.dailw.model.dto.user;

import lombok.Data;

@Data
public class UserRegisterRequest {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 密码校验
     */
    private String checkPassword;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;
}
