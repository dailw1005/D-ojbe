package com.ojbe.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ojbe.common.BaseResponse;
import com.ojbe.common.ErrorCode;
import com.ojbe.common.ResultUtils;
import com.ojbe.exception.BusinessException;
import com.ojbe.interceptor.JwtAuthenticationInterceptor;
import com.ojbe.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.ojbe.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.ojbe.model.entity.QuestionSubmit;
import com.ojbe.model.enums.QuestionSubmitStatusEnum;
import com.ojbe.model.vo.QuestionSubmitVO;
import com.ojbe.mq.JudgeProducer;
import com.ojbe.service.interfaces.QuestionSubmitService;
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

    @Resource
    private JudgeProducer judgeProducer;

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

    /**
     * 根据提交 ID 获取提交详情（仅本人）
     *
     * @param id      提交 ID
     * @param request HTTP 请求
     * @return 提交视图（包含完整判题信息，含用例结果）
     */
    @GetMapping("/get/vo/byId")
    public BaseResponse<QuestionSubmitVO> getQuestionSubmitVoById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long currentUserId = JwtAuthenticationInterceptor.getCurrentUserId(request);
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        QuestionSubmit questionSubmit = questionSubmitService.getById(id);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交不存在");
        }
        if (!currentUserId.equals(questionSubmit.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权查看他人提交");
        }
        QuestionSubmitVO vo = QuestionSubmitVO.objToVo(questionSubmit);
        return ResultUtils.success(vo);
    }

    /**
     * 重新触发判题（仅本人）
     *
     * @param id      提交 ID
     * @param request HTTP 请求
     * @return 是否成功发送判题消息
     */
    @PostMapping("/rejudge")
    public BaseResponse<Boolean> rejudge(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long currentUserId = JwtAuthenticationInterceptor.getCurrentUserId(request);
        if (currentUserId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        QuestionSubmit questionSubmit = questionSubmitService.getById(id);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交不存在");
        }
        if (!currentUserId.equals(questionSubmit.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权重新判题他人提交");
        }
        // 重置为等待中并清空判题信息，再发送 Kafka 消息触发判题
        QuestionSubmit update = new QuestionSubmit();
        update.setId(id);
        update.setStatus(QuestionSubmitStatusEnum.PENDING.getValue());
        update.setJudgeInfo("{}");
        boolean ok = questionSubmitService.updateById(update);
        if (!ok) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "重置提交状态失败");
        }
        judgeProducer.sendMessage(String.valueOf(id));
        return ResultUtils.success(true);
    }
}
