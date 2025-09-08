package com.dailw.model.dto.user;

import lombok.Data;

import java.util.Date;

/**
 * 用户信息更新请求DTO
 * 用于修改个人信息
 * 
 * @author trave
 */
@Data
public class UserUpdateRequest {

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 生日
     */
    private Date birthday;
}