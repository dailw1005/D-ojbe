package com.ojbe.controller;

import com.ojbe.annotation.AuthCheck;
import com.ojbe.common.BaseResponse;
import com.ojbe.common.DeleteRequest;
import com.ojbe.common.ErrorCode;
import com.ojbe.common.ResultUtils;
import com.ojbe.constant.UserConstant;
import com.ojbe.exception.BusinessException;
import com.ojbe.model.dto.questiontemplate.QuestionTemplateAddRequest;
import com.ojbe.model.dto.questiontemplate.QuestionTemplateUpdateRequest;
import com.ojbe.model.entity.QuestionTemplate;
import com.ojbe.service.interfaces.QuestionTemplateService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目代码模板接口
 */
@RestController
@RequestMapping("/question_template")
@Slf4j
public class QuestionTemplateController {

    @Resource
    private QuestionTemplateService questionTemplateService;

    /**
     * 创建代码模板
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addQuestionTemplate(@RequestBody QuestionTemplateAddRequest questionTemplateAddRequest) {
        if (questionTemplateAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long result = questionTemplateService.addQuestionTemplate(questionTemplateAddRequest);
        return ResultUtils.success(result);
    }

    /**
     * 删除代码模板
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestionTemplate(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean result = questionTemplateService.deleteQuestionTemplate(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 更新代码模板
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestionTemplate(@RequestBody QuestionTemplateUpdateRequest questionTemplateUpdateRequest) {
        if (questionTemplateUpdateRequest == null || questionTemplateUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean result = questionTemplateService.updateQuestionTemplate(questionTemplateUpdateRequest);
        return ResultUtils.success(result);
    }

    /**
     * 根据题目获取所有语言的模板列表
     */
    @GetMapping("/list")
    public BaseResponse<List<QuestionTemplate>> listQuestionTemplateByQuestionId(Long questionId) {
        if (questionId == null || questionId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<QuestionTemplate> templateList = questionTemplateService.getTemplatesByQuestionId(questionId);
        return ResultUtils.success(templateList);
    }

    /**
     * 根据题目和语言获取模板
     */
    @GetMapping("/get")
    public BaseResponse<QuestionTemplate> getQuestionTemplate(Long questionId, String language) {
        if (questionId == null || questionId <= 0 || language == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QuestionTemplate template = questionTemplateService.getTemplate(questionId, language);
        return ResultUtils.success(template);
    }
}
