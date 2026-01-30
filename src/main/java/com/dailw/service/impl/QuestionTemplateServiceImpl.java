package com.dailw.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dailw.model.entity.QuestionTemplate;
import com.dailw.service.interfaces.QuestionTemplateService;
import com.dailw.mapper.QuestionTemplateMapper;
import org.springframework.stereotype.Service;

/**
* @author trave
* @description 针对表【question_template(题目代码模板)】的数据库操作Service实现
* @createDate 2026-01-23 15:02:48
*/
@Service
public class QuestionTemplateServiceImpl extends ServiceImpl<QuestionTemplateMapper, QuestionTemplate>
    implements QuestionTemplateService{

}




