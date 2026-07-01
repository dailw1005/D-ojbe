package com.ojbe.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ojbe.annotation.AuthCheck;
import com.ojbe.common.BaseResponse;
import com.ojbe.common.DeleteRequest;
import com.ojbe.common.ErrorCode;
import com.ojbe.common.ResultUtils;
import com.ojbe.constant.UserConstant;
import com.ojbe.exception.BusinessException;
import com.ojbe.exception.ThrowUtils;
import com.ojbe.interceptor.JwtAuthenticationInterceptor;
import com.ojbe.model.dto.question.QuestionAddRequest;
import com.ojbe.model.dto.question.QuestionQueryRequest;
import com.ojbe.model.dto.question.QuestionUpdateRequest;
import com.ojbe.model.entity.Question;
import com.ojbe.model.vo.QuestionInfoVO;
import com.ojbe.model.vo.QuestionVO;
import com.ojbe.service.interfaces.QuestionService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
    public BaseResponse<Page<QuestionVO>> queryQuestion(@RequestBody QuestionQueryRequest questionQueryRequest, HttpServletRequest request) {

        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        Page<QuestionVO> result = questionService.queryByPage(current, size, questionQueryRequest);
        return ResultUtils.success(result);
    }

    @GetMapping("/query/vo/byId")
    public BaseResponse<QuestionVO> queryQuestionVoById(long id, HttpServletRequest request) {

        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QuestionVO questionVo = questionService.queryQuestionVoById(id);
        ThrowUtils.throwIf(questionVo == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(questionVo);
    }

    @GetMapping("/query/byId")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Question> queryQuestionById(long id, HttpServletRequest request) {

        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = questionService.getById(id);
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(question);
    }

    @GetMapping("/get/questionInfo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<QuestionInfoVO> getQuestionInfo(HttpServletRequest request) {

        QuestionInfoVO questionInfo = questionService.getQuestionInfo();
        ThrowUtils.throwIf(questionInfo == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(questionInfo);
    }
}
