package com.ojbe.service.interfaces;

import com.ojbe.model.dto.questiontemplate.QuestionTemplateAddRequest;
import com.ojbe.model.dto.questiontemplate.QuestionTemplateUpdateRequest;
import com.ojbe.model.entity.QuestionTemplate;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author trave
* @description 针对表【question_template(题目代码模板)】的数据库操作Service
* @createDate 2026-01-23 15:02:44
*/
public interface QuestionTemplateService extends IService<QuestionTemplate> {

    /**
     * 创建题目模板
     */
    Long addQuestionTemplate(QuestionTemplateAddRequest questionTemplateAddRequest);

    /**
     * 更新题目模板
     */
    Boolean updateQuestionTemplate(QuestionTemplateUpdateRequest questionTemplateUpdateRequest);

    /**
     * 删除题目模板
     */
    Boolean deleteQuestionTemplate(Long id);

    /**
     * 根据题目获取所有语言的模板
     */
    List<QuestionTemplate> getTemplatesByQuestionId(Long questionId);

    /**
     * 根据题目和语言获取模板
     */
    QuestionTemplate getTemplate(Long questionId, String language);
}
