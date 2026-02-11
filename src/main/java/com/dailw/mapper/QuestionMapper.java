package com.dailw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dailw.model.entity.Question;
import com.dailw.model.vo.QuestionInfoVO;

/**
* @author trave
* @description 针对表【question(题目)】的数据库操作Mapper
* @createDate 2026-01-23 14:52:44
* @Entity com.dailw.model.entity.Question
*/
public interface QuestionMapper extends BaseMapper<Question> {

    QuestionInfoVO selectQuestionInfo();
}




