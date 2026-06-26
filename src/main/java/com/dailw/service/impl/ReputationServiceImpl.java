package com.dailw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dailw.constant.LevelConstants;
import com.dailw.event.JudgeCompletedEvent;
import com.dailw.event.ReputationChangeEvent;
import com.dailw.mapper.CompetitionRegistrationMapper;
import com.dailw.mapper.QuestionSubmitMapper;
import com.dailw.mapper.UserMapper;
import com.dailw.model.entity.CompetitionRegistration;
import com.dailw.model.entity.QuestionSubmit;
import com.dailw.model.entity.User;
import com.dailw.model.enums.QuestionSubmitStatusEnum;
import com.dailw.service.interfaces.RedisService;
import com.dailw.service.interfaces.ReputationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Slf4j
public class ReputationServiceImpl implements ReputationService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private QuestionSubmitMapper questionSubmitMapper;

    @Resource
    private CompetitionRegistrationMapper competitionRegistrationMapper;

    @Resource
    private RedisService redisService;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Lazy
    @Resource
    private ReputationService self;

    @EventListener
    public void onJudgeCompleted(JudgeCompletedEvent event) {
        QuestionSubmit submit = event.getQuestionSubmit();
        log.info("判题完成事件: submitId={}, status={}, userId={}, questionId={}, competitionId={}",
                submit.getId(), submit.getStatus(), submit.getUserId(),
                submit.getQuestionId(), submit.getCompetitionId());
        if (!QuestionSubmitStatusEnum.ACCEPTED.getValue().equals(submit.getStatus())) {
            return;
        }
        self.onFirstAccepted(submit.getUserId(), submit.getQuestionId());
    }

    @Override
    @Transactional
    @CacheEvict(value = "userInfo", key = "#userId")
    public int addReputation(Long userId, int amount, String reason) {
        if (amount == 0) return getCurrentLevel(userId);

        User user = userMapper.selectById(userId);
        if (user == null) return 0;

        int oldReputation = user.getReputation() != null ? user.getReputation() : 0;
        int newReputation = Math.max(0, oldReputation + amount);
        int oldLevel = user.getLevel() != null ? user.getLevel() : 0;
        int newLevel = LevelConstants.computeLevel(newReputation);

        user.setReputation(newReputation);
        user.setLevel(newLevel);
        userMapper.updateById(user);

        log.info("声望变动: userId={}, amount={}, {}→{} (Lv{}→Lv{}), 原因={}",
                userId, amount, oldReputation, newReputation, oldLevel, newLevel, reason);

        eventPublisher.publishEvent(new ReputationChangeEvent(this, userId, amount, reason));
        return newLevel;
    }

    @Override
    @Transactional
    public void onFirstAccepted(Long userId, Long questionId) {
        long acCount = questionSubmitMapper.selectCount(
                Wrappers.<QuestionSubmit>lambdaQuery()
                        .eq(QuestionSubmit::getUserId, userId)
                        .eq(QuestionSubmit::getQuestionId, questionId)
                        .eq(QuestionSubmit::getStatus, QuestionSubmitStatusEnum.ACCEPTED.getValue()));
        log.info("首次AC检查: userId={}, questionId={}, acCount={}", userId, questionId, acCount);
        if (acCount == 1) {
            self.addReputation(userId, LevelConstants.FIRST_AC_REPUTATION,
                    "首次AC题目#" + questionId);
        }
    }

    @Override
    public void onContestEnded(Long contestId) {
        String rewardKey = "contest:" + contestId + ":rewards_given";
        if (redisService.get(rewardKey) != null) {
            return;
        }

        // All participants get participation reward
        var registrations = competitionRegistrationMapper.selectList(
                Wrappers.<CompetitionRegistration>lambdaQuery()
                        .eq(CompetitionRegistration::getCompetitionId, contestId));
        for (CompetitionRegistration reg : registrations) {
            // Check if user actually submitted (participation requires at least one submission)
            long submitCount = questionSubmitMapper.selectCount(
                    Wrappers.<QuestionSubmit>lambdaQuery()
                            .eq(QuestionSubmit::getCompetitionId, contestId)
                            .eq(QuestionSubmit::getUserId, reg.getUserId()));
            if (submitCount > 0) {
                self.addReputation(reg.getUserId(), LevelConstants.CONTEST_PARTICIPATION_REPUTATION,
                        "参加比赛#" + contestId);
            }
        }

        // Ranking rewards for top positions
        String lbKey = "contest:" + contestId + ":leaderboard";
        Set<Object> rankedMembers = redisService.zRange(lbKey, 0, -1);
        if (rankedMembers != null && !rankedMembers.isEmpty()) {
            int total = rankedMembers.size();
            int rank = 1;
            for (Object member : rankedMembers) {
                Long userId = Long.valueOf(member.toString());
                int reward = 0;
                String reason = "";
                if (rank == 1) {
                    reward = LevelConstants.CONTEST_RANK_1_REPUTATION;
                    reason = "比赛#" + contestId + " 冠军";
                } else if (rank == 2) {
                    reward = LevelConstants.CONTEST_RANK_2_REPUTATION;
                    reason = "比赛#" + contestId + " 亚军";
                } else if (rank == 3) {
                    reward = LevelConstants.CONTEST_RANK_3_REPUTATION;
                    reason = "比赛#" + contestId + " 季军";
                } else if (rank <= Math.ceil(total * 0.1)) {
                    reward = LevelConstants.CONTEST_TOP_10_REPUTATION;
                    reason = "比赛#" + contestId + " 排名前10%";
                } else if (rank <= Math.ceil(total * 0.5)) {
                    reward = LevelConstants.CONTEST_TOP_50_REPUTATION;
                    reason = "比赛#" + contestId + " 排名前50%";
                }
                if (reward > 0) {
                    self.addReputation(userId, reward, reason);
                }
                rank++;
            }
        }

        redisService.set(rewardKey, "1", 30, java.util.concurrent.TimeUnit.DAYS);
        log.info("比赛声望奖励已发放: contestId={}, participants={}", contestId, registrations.size());
    }

    private int getCurrentLevel(Long userId) {
        User user = userMapper.selectById(userId);
        return user != null && user.getLevel() != null ? user.getLevel() : 0;
    }
}
