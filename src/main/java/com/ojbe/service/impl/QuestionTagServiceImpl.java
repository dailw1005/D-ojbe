package com.ojbe.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ojbe.model.entity.QuestionTag;
import com.ojbe.service.interfaces.QuestionTagService;
import com.ojbe.mapper.QuestionTagMapper;
import org.springframework.stereotype.Service;

/**
* @author trave
* @description 针对表【question_tag(题目标签关联)】的数据库操作Service实现
* @createDate 2026-01-23 15:02:36
*/
@Service
public class QuestionTagServiceImpl extends ServiceImpl<QuestionTagMapper, QuestionTag>
    implements QuestionTagService{

}




