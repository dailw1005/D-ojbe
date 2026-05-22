package com.dailw.service.interfaces;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dailw.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.dailw.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.dailw.model.entity.QuestionSubmit;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dailw.model.vo.QuestionSubmitVO;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author trave
* @description 针对表【question_submit(题目提交)】的数据库操作Service
* @createDate 2026-01-23 15:02:27
*/
public interface QuestionSubmitService extends IService<QuestionSubmit> {

    /**
     * 题目提交
     *
     * @param questionSubmitAddRequest
     * @param userId
     * @return 提交记录的 id
     */
    Long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, Long userId);

    /**
     * 分页获取题目提交列表（只能获取到自己的提交记录）
     *
     * @param questionSubmitQueryRequest
     * @param userId
     * @return
     */
    Page<QuestionSubmitVO> getQuestionSubmitVOPage(QuestionSubmitQueryRequest questionSubmitQueryRequest, Long userId);

    /**
     * 获取用户提交总数
     *
     * @param userId 用户 ID
     * @return 提交总数
     */
    long getUserSubmitCount(Long userId);

    /**
     * 获取用户通过总数
     *
     * @param userId 用户 ID
     * @return 通过总数
     */
    long getUserAcceptedCount(Long userId);
}
