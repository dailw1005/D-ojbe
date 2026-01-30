package com.dailw.service.interfaces;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dailw.common.DeleteRequest;
import com.dailw.model.dto.question.QuestionAddRequest;
import com.dailw.model.dto.question.QuestionQueryRequest;
import com.dailw.model.dto.question.QuestionUpdateRequest;
import com.dailw.model.entity.Question;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dailw.model.vo.QuestionVO;

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

}
