package com.dailw.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dailw.common.BaseResponse;
import com.dailw.common.ErrorCode;
import com.dailw.common.ResultUtils;
import com.dailw.exception.BusinessException;
import com.dailw.interceptor.JwtAuthenticationInterceptor;
import com.dailw.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.dailw.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.dailw.model.vo.QuestionSubmitVO;
import com.dailw.service.interfaces.QuestionSubmitService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 题目提交接口
 */
@RestController
@RequestMapping("/question_submit")
@Slf4j
public class QuestionSubmitController {

    @Resource
    private QuestionSubmitService questionSubmitService;

    /**
     * 提交代码
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return 提交记录的 id
     */
    @PostMapping("/do")
    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
                                               HttpServletRequest request) {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long currentUserId = JwtAuthenticationInterceptor.getCurrentUserId(request);
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        long questionSubmitId = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, currentUserId);
        return ResultUtils.success(questionSubmitId);
    }

    /**
     * 分页获取题目提交列表（只能获取到自己的提交记录）
     *
     * @param questionSubmitQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
                                                                         HttpServletRequest request) {
        if (questionSubmitQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long currentUserId = JwtAuthenticationInterceptor.getCurrentUserId(request);
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        
        // 只能获取到自己的提交记录
        questionSubmitQueryRequest.setUserId(currentUserId);
        
        // 返回分页数据
        Page<QuestionSubmitVO> questionSubmitVOPage = questionSubmitService.getQuestionSubmitVOPage(questionSubmitQueryRequest, currentUserId);
        
        // judge_info 字段去除 testCaseResults 部分
        if (questionSubmitVOPage != null && questionSubmitVOPage.getRecords() != null) {
            questionSubmitVOPage.getRecords().forEach(record -> {
                if (record.getJudgeInfo() != null) {
                    record.getJudgeInfo().setTestCaseResults(null);
                }
            });
        }
        
        return ResultUtils.success(questionSubmitVOPage);
    }

    /**
     * 获取当前登录用户的总提交数和总成功数
     *
     * @param request
     * @return 包含 submitCount 和 acceptedCount 的 Map
     */
    @GetMapping("/user/count")
    public BaseResponse<Map<String, Long>> getUserSubmitAndAcceptedCount(HttpServletRequest request) {
        Long currentUserId = JwtAuthenticationInterceptor.getCurrentUserId(request);
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        long submitCount = questionSubmitService.getUserSubmitCount(currentUserId);
        long acceptedCount = questionSubmitService.getUserAcceptedCount(currentUserId);

        Map<String, Long> countMap = new HashMap<>();
        countMap.put("submitCount", submitCount);
        countMap.put("acceptedCount", acceptedCount);

        return ResultUtils.success(countMap);
    }
}
