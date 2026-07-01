package com.ojbe.service.interfaces;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ojbe.model.dto.questionsolution.QuestionSolutionAddRequest;
import com.ojbe.model.dto.questionsolution.QuestionSolutionQueryRequest;
import com.ojbe.model.dto.questionsolution.QuestionSolutionUpdateRequest;
import com.ojbe.model.entity.QuestionSolution;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ojbe.model.vo.QuestionSolutionVO;
import com.ojbe.model.vo.SolutionStatsVO;

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
     * @param currentUserId 当前用户ID（可为null，未登录时传null）
     * @return
     */
    Page<QuestionSolutionVO> getQuestionSolutionVOPage(QuestionSolutionQueryRequest questionSolutionQueryRequest, Long currentUserId);

    /**
     * 根据ID查询题解详情
     * @param id
     * @param currentUserId 当前用户ID（可为null）
     * @return
     */
    QuestionSolutionVO getQuestionSolutionVOById(Long id, Long currentUserId);

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
     * @param userId 当前用户ID（可为null，未登录传null）
     * @return
     */
    Boolean viewQuestionSolution(Long id, Long userId);

    /**
     * 获取当前用户题解统计数据
     * @param userId
     * @return
     */
    SolutionStatsVO getUserSolutionStats(Long userId);

    SolutionStatsVO getTotalSolutionStats();
}
