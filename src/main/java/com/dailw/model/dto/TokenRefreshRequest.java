package com.dailw.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Token刷新请求DTO
 * 
 * @author trave
 */
@Data
public class TokenRefreshRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 刷新Token
     */
    private String refreshToken;
}