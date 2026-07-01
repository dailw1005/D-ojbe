package com.ojbe.service.interfaces;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ojbe.model.dto.question.QuestionAddRequest;
import com.ojbe.model.dto.question.QuestionQueryRequest;
import com.ojbe.model.dto.question.QuestionUpdateRequest;
import com.ojbe.model.entity.Question;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ojbe.model.vo.QuestionInfoVO;
import com.ojbe.model.vo.QuestionVO;

/**
* @author trave
* @description 针对表【question(题目)】的数据库操作Service
* @createDate 2026-01-23 14:52:44
*/
public interface QuestionService extends IService<Question> {

    Long add(Long currentUserId, QuestionAddRequest questionAddRequest);

    Boolean update(Long currentUserId, QuestionUpdateRequest questionUpdateRequest);

    Boolean delete(Long currentUserId, Long questionId);

    Page<QuestionVO> queryByPage(Long current, Long size, QuestionQueryRequest questionQueryRequest);

    QuestionVO queryQuestionVoById(Long questionId);

    QuestionInfoVO getQuestionInfo();

}
