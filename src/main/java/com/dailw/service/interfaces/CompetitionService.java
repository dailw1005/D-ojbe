package com.dailw.service.interfaces;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dailw.model.dto.competition.*;
import com.dailw.model.vo.ContestDetailVO;
import com.dailw.model.vo.ContestRankingVO;
import com.dailw.model.vo.ContestVO;

public interface CompetitionService {

    Long createContest(ContestCreateRequest request, Long userId);

    Boolean updateContest(ContestUpdateRequest request, Long userId);

    Boolean deleteContest(Long contestId, Long userId);

    Boolean setQuestions(ContestSetQuestionsRequest request, Long userId);

    Page<ContestVO> listContests(ContestQueryRequest request);

    ContestDetailVO getContestDetail(Long contestId, Long userId);

    Boolean register(Long contestId, Long userId, String password);

    Long submitCode(Long contestId, Long userId, ContestSubmitRequest request);

    ContestRankingVO getRanking(Long contestId, Long userId, int page, int size);
}
