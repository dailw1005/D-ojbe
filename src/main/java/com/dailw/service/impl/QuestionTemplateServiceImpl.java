package com.dailw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dailw.common.ErrorCode;
import com.dailw.exception.BusinessException;
import com.dailw.model.dto.questiontemplate.QuestionTemplateAddRequest;
import com.dailw.model.dto.questiontemplate.QuestionTemplateUpdateRequest;
import com.dailw.model.entity.Question;
import com.dailw.model.entity.QuestionTemplate;
import com.dailw.service.interfaces.QuestionService;
import com.dailw.service.interfaces.QuestionTemplateService;
import com.dailw.mapper.QuestionTemplateMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author trave
* @description 针对表【question_template(题目代码模板)】的数据库操作Service实现
* @createDate 2026-01-23 15:02:44
*/
@Service
public class QuestionTemplateServiceImpl extends ServiceImpl<QuestionTemplateMapper, QuestionTemplate>
    implements QuestionTemplateService{

    @Resource
    private QuestionService questionService;

    @Override
    public Long addQuestionTemplate(QuestionTemplateAddRequest questionTemplateAddRequest) {
        Long questionId = questionTemplateAddRequest.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }

        // 检查该语言是否已经有模板
        LambdaQueryWrapper<QuestionTemplate> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(QuestionTemplate::getQuestionId, questionId);
        queryWrapper.eq(QuestionTemplate::getLanguage, questionTemplateAddRequest.getLanguage());
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该语言的模板已存在");
        }

        QuestionTemplate questionTemplate = new QuestionTemplate();
        BeanUtils.copyProperties(questionTemplateAddRequest, questionTemplate);
        
        boolean result = this.save(questionTemplate);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建模板失败");
        }
        return questionTemplate.getId();
    }

    @Override
    public Boolean updateQuestionTemplate(QuestionTemplateUpdateRequest questionTemplateUpdateRequest) {
        Long id = questionTemplateUpdateRequest.getId();
        QuestionTemplate oldTemplate = this.getById(id);
        if (oldTemplate == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "模板不存在");
        }

        QuestionTemplate questionTemplate = new QuestionTemplate();
        BeanUtils.copyProperties(questionTemplateUpdateRequest, questionTemplate);
        return this.updateById(questionTemplate);
    }

    @Override
    public Boolean deleteQuestionTemplate(Long id) {
        QuestionTemplate oldTemplate = this.getById(id);
        if (oldTemplate == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "模板不存在");
        }
        return this.removeById(id);
    }

    @Override
    public List<QuestionTemplate> getTemplatesByQuestionId(Long questionId) {
        LambdaQueryWrapper<QuestionTemplate> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(QuestionTemplate::getQuestionId, questionId);
        return this.list(queryWrapper);
    }

    @Override
    public QuestionTemplate getTemplate(Long questionId, String language) {
        LambdaQueryWrapper<QuestionTemplate> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(QuestionTemplate::getQuestionId, questionId);
        queryWrapper.eq(QuestionTemplate::getLanguage, language);
        return this.getOne(queryWrapper);
    }
}




