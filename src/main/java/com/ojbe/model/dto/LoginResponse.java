package com.ojbe.model.dto;

import com.ojbe.model.vo.UserVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户登录响应DTO
 * 包含用户信息和JWT Token
 * 
 * @author trave
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户信息（脱敏后）
     */
    private UserVO user;
    
    /**
     * 访问Token
     */
    private String accessToken;
    
    /**
     * 刷新Token
     */
    private String refreshToken;
    
    /**
     * Token类型，固定为"Bearer"
     */
    private String tokenType = "Bearer";
    
    /**
     * 访问Token过期时间（毫秒时间戳）
     */
    private Long accessTokenExpires;
    
    /**
     * 刷新Token过期时间（毫秒时间戳）
     */
    private Long refreshTokenExpires;
}