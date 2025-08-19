package com.dailw.service.interfaces;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dailw.model.dto.user.UserLoginRequest;
import com.dailw.model.dto.user.UserRegisterRequest;
import com.dailw.model.entity.User;
import com.dailw.model.vo.UserVO;
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


}
