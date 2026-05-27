package com.dailw.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dailw.common.BaseResponse;
import com.dailw.common.DeleteRequest;
import com.dailw.common.ErrorCode;
import com.dailw.common.ResultUtils;
import com.dailw.constant.UserConstant;
import com.dailw.exception.BusinessException;
import com.dailw.interceptor.JwtAuthenticationInterceptor;
import com.dailw.model.dto.questionsolution.QuestionSolutionAddRequest;
import com.dailw.model.dto.questionsolution.QuestionSolutionQueryRequest;
import com.dailw.model.dto.questionsolution.QuestionSolutionUpdateRequest;
import com.dailw.model.vo.QuestionSolutionVO;
import com.dailw.model.vo.SolutionStatsVO;
import com.dailw.model.vo.UserVO;
import com.dailw.service.interfaces.QuestionSolutionService;
import com.dailw.service.interfaces.UserService;
import com.dailw.utils.JwtUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 题解接口
 */
@RestController
@RequestMapping("/question_solution")
@Slf4j
public class QuestionSolutionController {

    @Resource
    private QuestionSolutionService questionSolutionService;

    @Resource
    private UserService userService;

    @Resource
    private JwtUtil jwtUtil;

    /**
     * 从请求中尝试提取用户ID（不强制要求登录），用于公开读接口的 hasLiked 判断
     */
    private Long getOptionalUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                if (jwtUtil.validateToken(token) && !jwtUtil.isTokenExpired(token)) {
                    return jwtUtil.getUserIdFromToken(token);
                }
            } catch (Exception e) {
                log.debug("Optional auth extraction failed: {}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 创建题解
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestionSolution(@RequestBody QuestionSolutionAddRequest questionSolutionAddRequest, HttpServletRequest request) {
        if (questionSolutionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long currentUserId = JwtAuthenticationInterceptor.getCurrentUserId(request);
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long result = questionSolutionService.addQuestionSolution(questionSolutionAddRequest, currentUserId);
        return ResultUtils.success(result);
    }

    /**
     * 删除题解
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestionSolution(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long currentUserId = JwtAuthenticationInterceptor.getCurrentUserId(request);
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        UserVO loginUser = userService.getLoginUser(currentUserId);
        boolean isAdmin = loginUser != null && UserConstant.ADMIN_ROLE.equals(loginUser.getRole());
        Boolean result = questionSolutionService.deleteQuestionSolution(deleteRequest.getId(), currentUserId, isAdmin);
        return ResultUtils.success(result);
    }

    /**
     * 更新题解
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateQuestionSolution(@RequestBody QuestionSolutionUpdateRequest questionSolutionUpdateRequest, HttpServletRequest request) {
        if (questionSolutionUpdateRequest == null || questionSolutionUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long currentUserId = JwtAuthenticationInterceptor.getCurrentUserId(request);
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        UserVO loginUser = userService.getLoginUser(currentUserId);
        boolean isAdmin = loginUser != null && UserConstant.ADMIN_ROLE.equals(loginUser.getRole());
        Boolean result = questionSolutionService.updateQuestionSolution(questionSolutionUpdateRequest, currentUserId, isAdmin);
        return ResultUtils.success(result);
    }

    /**
     * 分页获取题解列表
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<QuestionSolutionVO>> listQuestionSolutionByPage(@RequestBody QuestionSolutionQueryRequest questionSolutionQueryRequest, HttpServletRequest request) {
        if (questionSolutionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long currentUserId = getOptionalUserId(request);
        Page<QuestionSolutionVO> result = questionSolutionService.getQuestionSolutionVOPage(questionSolutionQueryRequest, currentUserId);
        return ResultUtils.success(result);
    }

    /**
     * 根据ID获取题解详情
     */
    @GetMapping("/get")
    public BaseResponse<QuestionSolutionVO> getQuestionSolutionById(Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long currentUserId = getOptionalUserId(request);
        QuestionSolutionVO result = questionSolutionService.getQuestionSolutionVOById(id, currentUserId);
        return ResultUtils.success(result);
    }

    /**
     * 点赞题解
     */
    @PostMapping("/thumb")
    public BaseResponse<Boolean> thumbQuestionSolution(Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long currentUserId = JwtAuthenticationInterceptor.getCurrentUserId(request);
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Boolean result = questionSolutionService.thumbQuestionSolution(id, currentUserId);
        return ResultUtils.success(result);
    }

    /**
     * 增加题解浏览量
     */
    @PostMapping("/view")
    public BaseResponse<Boolean> viewQuestionSolution(Long id, HttpServletRequest request) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long currentUserId = getOptionalUserId(request);
        Boolean result = questionSolutionService.viewQuestionSolution(id, currentUserId);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户的题解统计
     */
    @GetMapping("/user/count")
    public BaseResponse<SolutionStatsVO> getUserSolutionStats(HttpServletRequest request) {
        Long currentUserId = JwtAuthenticationInterceptor.getCurrentUserId(request);
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        SolutionStatsVO stats = questionSolutionService.getUserSolutionStats(currentUserId);
        return ResultUtils.success(stats);
    }

    @GetMapping("/total/count")
    public BaseResponse<SolutionStatsVO> getTotalSolutionStats() {
        SolutionStatsVO stats = questionSolutionService.getTotalSolutionStats();
        return ResultUtils.success(stats);
    }
}
