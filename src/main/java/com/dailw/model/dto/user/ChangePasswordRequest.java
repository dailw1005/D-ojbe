package com.dailw.model.dto.user;

import lombok.Data;

/**
 * 修改密码请求DTO
 * 
 * @author trave
 */
@Data
public class ChangePasswordRequest {

    /**
     * 旧密码
     */
    private String oldPassword;

    /**
     * 新密码
     */
    private String newPassword;

    /**
     * 确认新密码
     */
    private String confirmPassword;
}