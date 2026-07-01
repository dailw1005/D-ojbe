package com.ojbe.service.interfaces;

public interface ReputationService {

    /**
     * 给用户增加声望并自动升级
     * @return 新等级（如果升级了则 > 旧等级，否则 = 旧等级）
     */
    int addReputation(Long userId, int amount, String reason);

    /**
     * 用户首次 AC 题目时 +10 声望
     */
    void onFirstAccepted(Long userId, Long questionId);

    /**
     * 比赛结束时批量计算并发放声望奖励
     */
    void onContestEnded(Long contestId);
}
