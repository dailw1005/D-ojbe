package com.ojbe.service.interfaces;

import com.ojbe.model.entity.QuestionSubmit;

public interface CompetitionRankingService {

    void updateRankingOnJudgeComplete(QuestionSubmit submit);

    void rebuildRankings(Long contestId);
}
