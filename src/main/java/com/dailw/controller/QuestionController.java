package com.dailw.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dailw.annotation.AuthCheck;
import com.dailw.common.BaseResponse;
import com.dailw.common.DeleteRequest;
import com.dailw.common.ErrorCode;
import com.dailw.common.ResultUtils;
import com.dailw.constant.UserConstant;
import com.dailw.exception.BusinessException;
import com.dailw.exception.ThrowUtils;
import com.dailw.interceptor.JwtAuthenticationInterceptor;
import com.dailw.model.dto.question.QuestionAddRequest;
import com.dailw.model.dto.question.QuestionQueryRequest;
import com.dailw.model.dto.question.QuestionUpdateRequest;
import com.dailw.model.vo.QuestionVO;
import com.dailw.service.interfaces.QuestionService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @PostMapping("/add")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        if (questionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 从JWT拦截器中获取当前用户ID
        Long currentUserId = JwtAuthenticationInterceptor.getCurrentUserId(request);

        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        Long result = questionService.add(currentUserId, questionAddRequest);
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest, HttpServletRequest request) {
        if (questionUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 从JWT拦截器中获取当前用户ID
        Long currentUserId = JwtAuthenticationInterceptor.getCurrentUserId(request);

        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        Boolean result = questionService.update(currentUserId, questionUpdateRequest);
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 从JWT拦截器中获取当前用户ID
        Long currentUserId = JwtAuthenticationInterceptor.getCurrentUserId(request);

        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }

        Long questionId = deleteRequest.getId();
        Boolean result = questionService.delete(currentUserId, questionId);
        return ResultUtils.success(result);
    }

    @PostMapping("/query")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionVO>> queryQuestion(@RequestBody QuestionQueryRequest questionQueryRequest, HttpServletRequest request) {

        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        Page<QuestionVO> result = questionService.queryByPage(current, size, questionQueryRequest);
        return ResultUtils.success(result);
    }
}
