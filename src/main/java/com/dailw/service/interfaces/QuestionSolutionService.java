package com.dailw.service.interfaces;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dailw.model.dto.questionsolution.QuestionSolutionAddRequest;
import com.dailw.model.dto.questionsolution.QuestionSolutionQueryRequest;
import com.dailw.model.dto.questionsolution.QuestionSolutionUpdateRequest;
import com.dailw.model.entity.QuestionSolution;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dailw.model.vo.QuestionSolutionVO;
import java.util.Map;

/**
* @author trave
* @description 针对表【question_solution(题解)】的数据库操作Service
* @createDate 2026-01-23 15:02:12
*/
public interface QuestionSolutionService extends IService<QuestionSolution> {

    /**
     * 创建题解
     * @param questionSolutionAddRequest
     * @param userId
     * @return
     */
    Long addQuestionSolution(QuestionSolutionAddRequest questionSolutionAddRequest, Long userId);

    /**
     * 更新题解
     * @param questionSolutionUpdateRequest
     * @param userId
     * @param isAdmin
     * @return
     */
    Boolean updateQuestionSolution(QuestionSolutionUpdateRequest questionSolutionUpdateRequest, Long userId, boolean isAdmin);

    /**
     * 删除题解
     * @param id
     * @param userId
     * @param isAdmin
     * @return
     */
    Boolean deleteQuestionSolution(Long id, Long userId, boolean isAdmin);

    /**
     * 分页查询题解
     * @param questionSolutionQueryRequest
     * @return
     */
    Page<QuestionSolutionVO> getQuestionSolutionVOPage(QuestionSolutionQueryRequest questionSolutionQueryRequest);

    /**
     * 题解点赞
     * @param id
     * @param userId
     * @return
     */
    Boolean thumbQuestionSolution(Long id, Long userId);

    /**
     * 题解浏览
     * @param id
     * @return
     */
    Boolean viewQuestionSolution(Long id);

    /**
     * 获取当前用户题解统计数据
     * @param userId
     * @return
     */
    Map<String, Long> getUserSolutionStats(Long userId);

    Map<String, Long> getTotalSolutionStats();
}
