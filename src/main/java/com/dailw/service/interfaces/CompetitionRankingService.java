package com.dailw.service.interfaces;

import com.dailw.model.entity.QuestionSubmit;

public interface CompetitionRankingService {

    void updateRankingOnJudgeComplete(QuestionSubmit submit);

    void rebuildRankings(Long contestId);
}
