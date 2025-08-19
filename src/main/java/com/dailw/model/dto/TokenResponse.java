package com.dailw.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Token响应DTO
 * 用于Token刷新等场景
 * 
 * @author trave
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
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