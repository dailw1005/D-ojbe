package com.ojbe.service.interfaces;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ojbe.model.dto.competition.*;
import com.ojbe.model.vo.ContestDetailVO;
import com.ojbe.model.vo.ContestRankingVO;
import com.ojbe.model.vo.ContestVO;

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
