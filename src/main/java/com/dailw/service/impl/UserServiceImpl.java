package com.dailw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dailw.common.ErrorCode;
import com.dailw.exception.BusinessException;
import com.dailw.model.dto.user.ChangePasswordRequest;
import com.dailw.model.dto.user.UserLoginRequest;
import com.dailw.model.dto.user.UserRegisterRequest;
import com.dailw.model.dto.user.UserUpdateRequest;
import com.dailw.service.interfaces.UserService;
import com.dailw.model.entity.User;
import com.dailw.model.vo.UserVO;
import com.dailw.mapper.UserMapper;
import com.dailw.utils.PasswordUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author trave
 * @description 针对表【user(用户信息表)】的数据库操作Service实现
 * @createDate 2025-08-11 14:42:47
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Override
    public Long register(UserRegisterRequest userRegisterRequest) {
        // 校验
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "注册参数为空");
        }
        // 校验密码是否相同
        String password = userRegisterRequest.getPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (!password.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
        }
        // 校验用户名是否重复
        String username = userRegisterRequest.getUsername();
        long count = this.count(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名已存在");
        }

        // 密码加密
        password = PasswordUtil.encryptPassword(password);
        // 插入数据
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(userRegisterRequest.getEmail());
        user.setPhone(userRegisterRequest.getPhone());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }
    
    @Override
    public UserVO login(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        // 校验参数
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "登录参数为空");
        }
        
        String username = userLoginRequest.getUsername();
        String password = userLoginRequest.getPassword();
        
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名或密码不能为空");
        }
        
        // 查询用户
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getDeleted, 0));
        
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        
        // 验证密码
        if (!PasswordUtil.verifyPassword(password, user.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        
        // 检查账户状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账户已被禁用或锁定");
        }
        
        // 记录登录信息
        user.setLastLoginTime(new java.util.Date());
        user.setLastLoginIp(getClientIpAddress(request));
        user.setLoginCount(user.getLoginCount() == null ? 1 : user.getLoginCount() + 1);
        this.updateById(user);
        
        return getUserVO(user);
    }

    @Override
    public UserVO getLoginUser(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账户已被禁用或锁定");
        }

        // 转换为VO对象并返回
        return getUserVO(user);
    }

    @Override
    public UserVO updateUserInfo(Long userId, UserUpdateRequest userUpdateRequest) {
        // 校验参数
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        }
        if (userUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "更新参数不能为空");
        }
        
        // 查询当前用户
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        
        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账户已被禁用或锁定");
        }

        
        // 更新用户信息（只更新非空字段）
        if (StringUtils.hasText(userUpdateRequest.getNickname())) {
            user.setNickname(userUpdateRequest.getNickname());
        }
        if (StringUtils.hasText(userUpdateRequest.getAvatar())) {
            user.setAvatar(userUpdateRequest.getAvatar());
        }
        if (userUpdateRequest.getGender() != null) {
            user.setGender(userUpdateRequest.getGender());
        }
        if (userUpdateRequest.getBirthday() != null) {
            user.setBirthday(userUpdateRequest.getBirthday());
        }
        
        // 设置更新人为当前用户
        user.setUpdateBy(userId);
        
        // 保存更新
        boolean updateResult = this.updateById(user);
        if (!updateResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新用户信息失败");
        }
        
        // 返回更新后的用户信息
        return getUserVO(user);
    }
    
    @Override
    public boolean changePassword(Long userId, ChangePasswordRequest changePasswordRequest) {
        // 校验参数
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        }
        if (changePasswordRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "修改密码参数不能为空");
        }
        
        String oldPassword = changePasswordRequest.getOldPassword();
        String newPassword = changePasswordRequest.getNewPassword();
        String confirmPassword = changePasswordRequest.getConfirmPassword();
        
        // 校验密码参数
        if (!StringUtils.hasText(oldPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "旧密码不能为空");
        }
        if (!StringUtils.hasText(newPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码不能为空");
        }
        if (!StringUtils.hasText(confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "确认密码不能为空");
        }
        
        // 校验新密码和确认密码是否一致
        if (!newPassword.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码和确认密码不一致");
        }
        
        // 校验新密码长度
        if (newPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码长度不能少于6位");
        }
        
        // 查询当前用户
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }
        
        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账户已被禁用或锁定");
        }
        
        // 验证旧密码
        if (!PasswordUtil.verifyPassword(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "旧密码错误");
        }
        
        // 检查新密码是否与旧密码相同
        if (PasswordUtil.verifyPassword(newPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码不能与旧密码相同");
        }
        
        // 加密新密码
        String encryptedNewPassword = PasswordUtil.encryptPassword(newPassword);
        
        // 更新密码
        user.setPassword(encryptedNewPassword);
        user.setUpdateBy(userId);
        
        // 保存更新
        boolean updateResult = this.updateById(user);
        if (!updateResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改密码失败");
        }
        
        return true;
    }
    
    /**
     * 将User实体转换为UserVO
     */
    private UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setEmail(user.getEmail());
        userVO.setPhone(user.getPhone());
        userVO.setNickname(user.getNickname());
        userVO.setAvatar(user.getAvatar());
        userVO.setGender(user.getGender());
        userVO.setBirthday(user.getBirthday());
        userVO.setStatus(user.getStatus());
        userVO.setLastLoginTime(user.getLastLoginTime());
        userVO.setLastLoginIp(user.getLastLoginIp());
        userVO.setLoginCount(user.getLoginCount());
        userVO.setRole(user.getRole());
        userVO.setCreateTime(user.getCreateTime());
        return userVO;
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor) && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp) && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}




