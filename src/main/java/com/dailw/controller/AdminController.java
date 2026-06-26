package com.dailw.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dailw.annotation.AuthCheck;
import com.dailw.common.BaseResponse;
import com.dailw.common.ErrorCode;
import com.dailw.common.ResultUtils;
import com.dailw.exception.BusinessException;
import com.dailw.interceptor.JwtAuthenticationInterceptor;
import com.dailw.model.dto.admin.AdminUserQueryRequest;
import com.dailw.model.dto.admin.AdminUserUpdateRoleRequest;
import com.dailw.model.dto.admin.AdminUserUpdateStatusRequest;
import com.dailw.model.vo.UserVO;
import com.dailw.model.vo.admin.DashboardVO;
import com.dailw.service.interfaces.AdminService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Resource
    private AdminService adminService;

    @PostMapping("/user/list")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Page<UserVO>> listUsers(@RequestBody AdminUserQueryRequest request,
                                                 HttpServletRequest httpRequest) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long currentUserId = JwtAuthenticationInterceptor.getCurrentUserId(httpRequest);
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Page<UserVO> result = adminService.queryUserPage(request);
        return ResultUtils.success(result);
    }

    @PostMapping("/user/update-role")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> updateUserRole(@RequestBody AdminUserUpdateRoleRequest request,
                                                 HttpServletRequest httpRequest) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long currentUserId = JwtAuthenticationInterceptor.getCurrentUserId(httpRequest);
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Boolean result = adminService.updateUserRole(currentUserId, request);
        return ResultUtils.success(result);
    }

    @PostMapping("/user/update-status")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> updateUserStatus(@RequestBody AdminUserUpdateStatusRequest request,
                                                   HttpServletRequest httpRequest) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long currentUserId = JwtAuthenticationInterceptor.getCurrentUserId(httpRequest);
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Boolean result = adminService.updateUserStatus(currentUserId, request);
        return ResultUtils.success(result);
    }

    @GetMapping("/dashboard")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<DashboardVO> getDashboard(HttpServletRequest httpRequest) {
        Long currentUserId = JwtAuthenticationInterceptor.getCurrentUserId(httpRequest);
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        DashboardVO result = adminService.getDashboard();
        return ResultUtils.success(result);
    }
}
