package com.dailw.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dailw.annotation.AuthCheck;
import com.dailw.common.BaseResponse;
import com.dailw.common.DeleteRequest;
import com.dailw.common.ErrorCode;
import com.dailw.common.ResultUtils;
import com.dailw.constant.UserConstant;
import com.dailw.exception.BusinessException;
import com.dailw.interceptor.JwtAuthenticationInterceptor;
import com.dailw.model.dto.competition.*;
import com.dailw.model.vo.ContestDetailVO;
import com.dailw.model.vo.ContestRankingVO;
import com.dailw.model.vo.ContestVO;
import com.dailw.model.vo.QuestionSubmitVO;
import com.dailw.service.interfaces.CompetitionService;
import com.dailw.service.interfaces.QuestionSubmitService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contest")
@Slf4j
public class ContestController {

    @Resource
    private CompetitionService competitionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @PostMapping("/create")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> createContest(@RequestBody ContestCreateRequest request,
                                             HttpServletRequest httpRequest) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long userId = JwtAuthenticationInterceptor.getCurrentUserId(httpRequest);
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long result = competitionService.createContest(request, userId);
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateContest(@RequestBody ContestUpdateRequest request,
                                                HttpServletRequest httpRequest) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long userId = JwtAuthenticationInterceptor.getCurrentUserId(httpRequest);
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Boolean result = competitionService.updateContest(request, userId);
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteContest(@RequestBody DeleteRequest request,
                                                HttpServletRequest httpRequest) {
        if (request == null || request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long userId = JwtAuthenticationInterceptor.getCurrentUserId(httpRequest);
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Boolean result = competitionService.deleteContest(request.getId(), userId);
        return ResultUtils.success(result);
    }

    @PostMapping("/questions/set")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> setQuestions(@RequestBody ContestSetQuestionsRequest request,
                                               HttpServletRequest httpRequest) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long userId = JwtAuthenticationInterceptor.getCurrentUserId(httpRequest);
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Boolean result = competitionService.setQuestions(request, userId);
        return ResultUtils.success(result);
    }

    @PostMapping("/list")
    public BaseResponse<Page<ContestVO>> listContests(@RequestBody ContestQueryRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<ContestVO> result = competitionService.listContests(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/{id}")
    public BaseResponse<ContestDetailVO> getContestDetail(@PathVariable("id") Long contestId,
                                                            HttpServletRequest request) {
        Long userId = getOptionalUserId(request);
        ContestDetailVO result = competitionService.getContestDetail(contestId, userId);
        return ResultUtils.success(result);
    }

    @PostMapping("/{id}/register")
    public BaseResponse<Boolean> register(@PathVariable("id") Long contestId,
                                           @RequestBody ContestRegisterRequest request,
                                           HttpServletRequest httpRequest) {
        Long userId = JwtAuthenticationInterceptor.getCurrentUserId(httpRequest);
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Boolean result = competitionService.register(contestId, userId,
                request != null ? request.getPassword() : null);
        return ResultUtils.success(result);
    }

    @PostMapping("/{id}/submit")
    public BaseResponse<Long> submitCode(@PathVariable("id") Long contestId,
                                          @RequestBody ContestSubmitRequest request,
                                          HttpServletRequest httpRequest) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long userId = JwtAuthenticationInterceptor.getCurrentUserId(httpRequest);
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long result = competitionService.submitCode(contestId, userId, request);
        return ResultUtils.success(result);
    }

    @GetMapping("/{id}/my-submissions")
    public BaseResponse<List<QuestionSubmitVO>> getMySubmissions(@PathVariable("id") Long contestId,
                                                                    @RequestParam(required = false) Long questionId,
                                                                    HttpServletRequest httpRequest) {
        Long userId = JwtAuthenticationInterceptor.getCurrentUserId(httpRequest);
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // Query submissions for this user in this contest
        var queryRequest = new com.dailw.model.dto.questionsubmit.QuestionSubmitQueryRequest();
        queryRequest.setUserId(userId);
        queryRequest.setQuestionId(questionId);
        queryRequest.setCompetitionId(contestId);
        queryRequest.setCurrent(1);
        queryRequest.setPageSize(100);
        var page = questionSubmitService.getQuestionSubmitVOPage(queryRequest, userId);
        return ResultUtils.success(page.getRecords());
    }

    @GetMapping("/{id}/ranking")
    public BaseResponse<ContestRankingVO> getRanking(@PathVariable("id") Long contestId,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "50") int size,
                                                       HttpServletRequest request) {
        Long userId = getOptionalUserId(request);
        ContestRankingVO result = competitionService.getRanking(contestId, userId, page, size);
        return ResultUtils.success(result);
    }

    private Long getOptionalUserId(HttpServletRequest request) {
        try {
            return JwtAuthenticationInterceptor.getCurrentUserId(request);
        } catch (Exception e) {
            return null;
        }
    }
}
