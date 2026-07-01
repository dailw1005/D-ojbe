package com.ojbe.service.interfaces;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ojbe.model.dto.user.ChangePasswordRequest;
import com.ojbe.model.dto.user.UserLoginRequest;
import com.ojbe.model.dto.user.UserRegisterRequest;
import com.ojbe.model.dto.user.UserUpdateRequest;
import com.ojbe.model.entity.User;
import com.ojbe.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author trave
* @description 针对表【user(用户信息表)】的数据库操作Service
* @createDate 2025-08-11 14:42:47
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    Long register(UserRegisterRequest userRegisterRequest);
    
    /**
     * 用户登录
     */
    UserVO login(UserLoginRequest userLoginRequest, HttpServletRequest request);

    /**
     * 获取当前登录用户
     */
    UserVO getLoginUser(Long userId);

    /**
     * 更新用户信息
     * 只能修改当前登录用户的信息
     */
    UserVO updateUserInfo(Long userId, UserUpdateRequest userUpdateRequest);

    /**
     * 修改密码
     * 验证旧密码并设置新密码
     */
    boolean changePassword(Long userId, ChangePasswordRequest changePasswordRequest);


}
