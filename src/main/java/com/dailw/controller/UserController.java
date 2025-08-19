package com.dailw.controller;

import com.dailw.common.BaseResponse;
import com.dailw.common.ErrorCode;
import com.dailw.common.ResultUtils;
import com.dailw.config.JwtConfig;
import com.dailw.exception.BusinessException;
import com.dailw.model.dto.LoginResponse;
import com.dailw.model.dto.TokenRefreshRequest;
import com.dailw.model.dto.TokenResponse;
import com.dailw.model.dto.user.UserLoginRequest;
import com.dailw.model.dto.user.UserRegisterRequest;
import com.dailw.model.vo.UserVO;
import com.dailw.service.interfaces.UserService;
import com.dailw.utils.JwtUtil;
import com.dailw.interceptor.JwtAuthenticationInterceptor;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private JwtConfig jwtConfig;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求体
     * @return 注册用户id
     */
    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long result = userService.register(userRegisterRequest);
        return ResultUtils.success(result);
    }
    
    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求体
     * @param request HTTP请求
     * @return 登录响应（包含用户信息和JWT Token）
     */
    @PostMapping("/login")
    public BaseResponse<LoginResponse> login(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userName = userLoginRequest.getUsername();
        String password = userLoginRequest.getPassword();
        if (StringUtils.isAnyBlank(userName, password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 调用用户服务进行登录验证
        UserVO userVO = userService.login(userLoginRequest, request);
        
        // 生成JWT Token
        String accessToken = jwtUtil.generateAccessToken(userVO.getId(), userVO.getUsername(), userVO.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(userVO.getId(), userVO.getUsername(), userVO.getRole());
        
        // 计算Token过期时间
        long currentTime = System.currentTimeMillis();
        long accessTokenExpires = currentTime + jwtConfig.getAccessTokenExpiration();
        long refreshTokenExpires = currentTime + jwtConfig.getRefreshTokenExpiration();
        
        // 构建登录响应
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setUser(userVO);
        loginResponse.setAccessToken(accessToken);
        loginResponse.setRefreshToken(refreshToken);
        loginResponse.setAccessTokenExpires(accessTokenExpires);
        loginResponse.setRefreshTokenExpires(refreshTokenExpires);
        
        log.info("用户 {} 登录成功，生成JWT Token", userVO.getUsername());
        
        return ResultUtils.success(loginResponse);
    }
    
    /**
     * 刷新Token
     *
     * @param tokenRefreshRequest Token刷新请求体
     * @return 新的Token信息
     */
    @PostMapping("/refresh-token")
    public BaseResponse<TokenResponse> refreshToken(@RequestBody TokenRefreshRequest tokenRefreshRequest) {
        if (tokenRefreshRequest == null || StringUtils.isBlank(tokenRefreshRequest.getRefreshToken())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "刷新Token不能为空");
        }
        
        String refreshToken = tokenRefreshRequest.getRefreshToken();
        
        try {
            // 验证刷新Token的有效性
            if (!jwtUtil.validateToken(refreshToken)) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "刷新Token无效");
            }
            
            // 检查刷新Token是否过期
            if (jwtUtil.isTokenExpired(refreshToken)) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "刷新Token已过期，请重新登录");
            }
            
            // 从刷新Token中提取用户信息
            Long userId = jwtUtil.getUserIdFromToken(refreshToken);
            String username = jwtUtil.getUsernameFromToken(refreshToken);
            String role = jwtUtil.getRoleFromToken(refreshToken);

            if (userId == null || StringUtils.isBlank(username)) {
                throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "刷新Token中用户信息无效");
            }
            
            // 生成新的Token
            String newAccessToken = jwtUtil.generateAccessToken(userId, username, role);
            String newRefreshToken = jwtUtil.generateRefreshToken(userId, username, role);
            
            // 计算新Token的过期时间
            long currentTime = System.currentTimeMillis();
            long accessTokenExpires = currentTime + jwtConfig.getAccessTokenExpiration();
            long refreshTokenExpires = currentTime + jwtConfig.getRefreshTokenExpiration();
            
            // 构建Token响应
            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.setAccessToken(newAccessToken);
            tokenResponse.setRefreshToken(newRefreshToken);
            tokenResponse.setAccessTokenExpires(accessTokenExpires);
            tokenResponse.setRefreshTokenExpires(refreshTokenExpires);
            
            log.info("用户 {} (ID: {}) 刷新Token成功", username, userId);
            
            return ResultUtils.success(tokenResponse);
            
        } catch (BusinessException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            log.error("Token刷新过程中发生异常: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Token刷新失败");
        }
    }
    
    /**
     * 获取当前登录用户信息
     *
     * @param request HTTP请求
     * @return 当前用户信息
     */
    @GetMapping("/get/login")
    public BaseResponse<UserVO> getCurrentUser(HttpServletRequest request) {
        // 从JWT拦截器中获取当前用户ID
        Long currentUserId = JwtAuthenticationInterceptor.getCurrentUserId(request);
        
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }

        UserVO userVO = userService.getLoginUser(currentUserId);

        log.debug("获取当前用户信息成功: {} (ID: {})", userVO.getUsername(), currentUserId);
        
        return ResultUtils.success(userVO);
    }
}
