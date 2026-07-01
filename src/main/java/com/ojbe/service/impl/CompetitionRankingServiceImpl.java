package com.ojbe.service.impl;

import com.ojbe.event.JudgeCompletedEvent;
import com.ojbe.mapper.CompetitionMapper;
import com.ojbe.mapper.CompetitionQuestionMapper;
import com.ojbe.model.entity.*;
import com.ojbe.model.enums.QuestionSubmitStatusEnum;
import com.ojbe.service.interfaces.CompetitionRankingService;
import com.ojbe.service.interfaces.RedisService;
import com.ojbe.utils.RedisDistributedLock;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class CompetitionRankingServiceImpl implements CompetitionRankingService {

    @Resource
    private CompetitionMapper competitionMapper;

    @Resource
    private CompetitionQuestionMapper competitionQuestionMapper;

    @Resource
    private com.ojbe.mapper.QuestionSubmitMapper questionSubmitMapper;

    @Resource
    private com.ojbe.mapper.CompetitionRegistrationMapper competitionRegistrationMapper;

    @Resource
    private RedisService redisService;

    @Resource
    private RedisDistributedLock redisDistributedLock;

    private static final long ACM_MULTIPLIER = 1_000_000_000_000L;
    private static final long OI_MULTIPLIER = 1_000_000_000_000L;

    private static final Set<Integer> WRONG_ANSWER_STATUSES = Set.of(
            QuestionSubmitStatusEnum.WRONG_ANSWER.getValue(),
            QuestionSubmitStatusEnum.TIME_LIMIT_EXCEEDED.getValue(),
            QuestionSubmitStatusEnum.MEMORY_LIMIT_EXCEEDED.getValue(),
            QuestionSubmitStatusEnum.RUNTIME_ERROR.getValue());

    @EventListener
    public void onJudgeCompleted(JudgeCompletedEvent event) {
        updateRankingOnJudgeComplete(event.getQuestionSubmit());
    }

    @Override
    public void updateRankingOnJudgeComplete(QuestionSubmit submit) {
        Long contestId = submit.getCompetitionId();
        if (contestId == null) return;

        Competition contest = competitionMapper.selectById(contestId);
        if (contest == null) return;

        Long userId = submit.getUserId();
        Long questionId = submit.getQuestionId();

        String lockKey = "contest:rank:" + contestId + ":" + userId;
        RedisDistributedLock.LockInfo lockInfo = redisDistributedLock.tryLock(lockKey, 10);
        if (lockInfo == null) {
            log.warn("获取排名更新锁失败: contest={}, user={}", contestId, userId);
            return;
        }

        try {
            if ("ACM".equals(contest.getType())) {
                updateAcmRanking(contest, submit);
            } else {
                updateOiRanking(contest, submit);
            }
        } catch (Exception e) {
            log.error("更新排名失败: contest={}, user={}, question={}", contestId, userId, questionId, e);
        } finally {
            redisDistributedLock.unlock(lockInfo);
        }
    }

    private void updateAcmRanking(Competition contest, QuestionSubmit submit) {
        Long contestId = contest.getId();
        Long userId = submit.getUserId();
        Long questionId = submit.getQuestionId();

        String statusKey = "contest:" + contestId + ":user:" + userId
                + ":problem:" + questionId + ":status";
        Object alreadySolved = redisService.get(statusKey);

        if ("solved".equals(alreadySolved)) {
            return;
        }

        String failKey = "contest:" + contestId + ":user:" + userId
                + ":problem:" + questionId + ":fail_count";

        if (QuestionSubmitStatusEnum.ACCEPTED.getValue().equals(submit.getStatus())) {
            redisService.set(statusKey, "solved");
        } else if (WRONG_ANSWER_STATUSES.contains(submit.getStatus())) {
            Object failObj = redisService.get(failKey);
            int currentFail = failObj != null ? Integer.parseInt(failObj.toString()) : 0;
            redisService.set(failKey, String.valueOf(currentFail + 1));
            return;
        } else {
            return;
        }

        recomputeAcmScore(contest, userId);
    }

    private void recomputeAcmScore(Competition contest, Long userId) {
        Long contestId = contest.getId();
        long contestStartMs = contest.getStartTime().getTime();

        int solvedCount = 0;
        long totalPenaltySeconds = 0;

        var cqList = competitionQuestionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CompetitionQuestion>()
                        .eq(CompetitionQuestion::getCompetitionId, contestId));

        for (CompetitionQuestion cq : cqList) {
            String statusKey = "contest:" + contestId + ":user:" + userId
                    + ":problem:" + cq.getQuestionId() + ":status";
            Object statusObj = redisService.get(statusKey);

            if (!"solved".equals(statusObj)) continue;

            solvedCount++;
            String failKey = "contest:" + contestId + ":user:" + userId
                    + ":problem:" + cq.getQuestionId() + ":fail_count";
            Object failObj = redisService.get(failKey);
            int failCount = failObj != null ? Integer.parseInt(failObj.toString()) : 0;

            long solveTimeMs = 0;
            var acceptedWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<QuestionSubmit>()
                    .eq(QuestionSubmit::getCompetitionId, contestId)
                    .eq(QuestionSubmit::getUserId, userId)
                    .eq(QuestionSubmit::getQuestionId, cq.getQuestionId())
                    .eq(QuestionSubmit::getStatus, QuestionSubmitStatusEnum.ACCEPTED.getValue())
                    .orderByAsc(QuestionSubmit::getCreateTime)
                    .last("LIMIT 1");
            List<QuestionSubmit> acceptedList = questionSubmitMapper.selectList(acceptedWrapper);
            if (!acceptedList.isEmpty()) {
                solveTimeMs = acceptedList.get(0).getCreateTime().getTime() - contestStartMs;
            }

            long penalty = Math.max(0, solveTimeMs) / 1000 + failCount * 20L * 60;
            totalPenaltySeconds += penalty;
        }

        long score = -(solvedCount * ACM_MULTIPLIER + totalPenaltySeconds);
        String lbKey = "contest:" + contestId + ":leaderboard";
        redisService.zAdd(lbKey, userId.toString(), (double) score);

        log.debug("ACM排名更新: contest={}, user={}, solved={}, penalty={}s",
                contestId, userId, solvedCount, totalPenaltySeconds);
    }

    private void updateOiRanking(Competition contest, QuestionSubmit submit) {
        Long contestId = contest.getId();
        Long userId = submit.getUserId();
        Long questionId = submit.getQuestionId();

        CompetitionQuestion cq = competitionQuestionMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CompetitionQuestion>()
                        .eq(CompetitionQuestion::getCompetitionId, contestId)
                        .eq(CompetitionQuestion::getQuestionId, questionId));
        int maxScore = cq != null && cq.getScore() != null ? cq.getScore() : 100;

        String bestKey = "contest:" + contestId + ":user:" + userId
                + ":problem:" + questionId + ":best_score";
        Object bestObj = redisService.get(bestKey);
        int currentBest = bestObj != null ? Integer.parseInt(bestObj.toString()) : 0;

        String tryKey = "contest:" + contestId + ":user:" + userId
                + ":problem:" + questionId + ":try_count";
        Object tryObj = redisService.get(tryKey);
        int tryCount = tryObj != null ? Integer.parseInt(tryObj.toString()) : 0;
        redisService.set(tryKey, String.valueOf(tryCount + 1));

        int submissionScore = calculateOiSubmissionScore(submit, maxScore);

        if (submissionScore <= currentBest) return;

        redisService.set(bestKey, String.valueOf(submissionScore));
        String lastTimeKey = "contest:" + contestId + ":user:" + userId
                + ":last_submit_time";
        if (submit.getCreateTime() != null) {
            redisService.set(lastTimeKey, String.valueOf(
                    submit.getCreateTime().getTime() / 1000));
        }
        recomputeOiScore(contest, userId);
    }

    private int calculateOiSubmissionScore(QuestionSubmit submit, int maxScore) {
        Integer status = submit.getStatus();
        if (status == null) return 0;

        if (QuestionSubmitStatusEnum.ACCEPTED.getValue().equals(status)) {
            return maxScore;
        }

        if (QuestionSubmitStatusEnum.COMPILATION_ERROR.getValue().equals(status)
                || QuestionSubmitStatusEnum.SYSTEM_ERROR.getValue().equals(status)
                || QuestionSubmitStatusEnum.PENDING.getValue().equals(status)
                || QuestionSubmitStatusEnum.JUDGING.getValue().equals(status)) {
            return 0;
        }

        try {
            String judgeInfoStr = submit.getJudgeInfo();
            if (judgeInfoStr == null || judgeInfoStr.isEmpty() || "{}".equals(judgeInfoStr)) {
                return 0;
            }
            com.ojbe.model.dto.questionsubmit.JudgeInfo judgeInfo =
                    com.ojbe.utils.StaticJsonUtil.toObj(judgeInfoStr,
                            com.ojbe.model.dto.questionsubmit.JudgeInfo.class);
            if (judgeInfo == null || judgeInfo.getTestCaseResults() == null
                    || judgeInfo.getTestCaseResults().isEmpty()) {
                return 0;
            }

            var results = judgeInfo.getTestCaseResults();
            int total = results.size();
            long passed = results.stream()
                    .filter(r -> com.ojbe.model.enums.JudgeInfoMessageEnum.ACCEPTED.getValue()
                            .equals(r.getStatus()))
                    .count();

            return (int) ((long) maxScore * passed / total);
        } catch (Exception e) {
            log.warn("解析OI判题结果失败: submitId={}", submit.getId(), e);
            return 0;
        }
    }

    private void recomputeOiScore(Competition contest, Long userId) {
        Long contestId = contest.getId();

        int totalScore = 0;

        var cqList = competitionQuestionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CompetitionQuestion>()
                        .eq(CompetitionQuestion::getCompetitionId, contestId));

        for (CompetitionQuestion cq : cqList) {
            String bestKey = "contest:" + contestId + ":user:" + userId
                    + ":problem:" + cq.getQuestionId() + ":best_score";
            Object bestObj = redisService.get(bestKey);
            totalScore += bestObj != null ? Integer.parseInt(bestObj.toString()) : 0;
        }

        String lastTimeKey = "contest:" + contestId + ":user:" + userId
                + ":last_submit_time";
        Object lastTimeObj = redisService.get(lastTimeKey);
        long lastSubmitSecs = lastTimeObj != null ? Long.parseLong(lastTimeObj.toString()) : 0;
        // Score encoding: -totalScore for ranking (higher score = more negative = better rank)
        // Tiebreaker: earlier submission (smaller timestamp) → more negative → better rank
        long score = -(totalScore * OI_MULTIPLIER) + lastSubmitSecs;
        String lbKey = "contest:" + contestId + ":leaderboard";
        redisService.zAdd(lbKey, userId.toString(), (double) score);

        log.debug("OI排名更新: contest={}, user={}, totalScore={}, lastSubmit={}",
                contestId, userId, totalScore, lastSubmitSecs);
    }

    @Override
    public void rebuildRankings(Long contestId) {
        Competition contest = competitionMapper.selectById(contestId);
        if (contest == null) {
            log.warn("rebuildRankings: 比赛不存在, contestId={}", contestId);
            return;
        }

        String lockKey = "contest:rank:rebuild:" + contestId;
        com.ojbe.utils.RedisDistributedLock.LockInfo lockInfo =
                redisDistributedLock.tryLock(lockKey, 30);
        if (lockInfo == null) {
            log.warn("rebuildRankings: 获取重建锁失败, contestId={}", contestId);
            return;
        }

        try {
            String participantsKey = "contest:" + contestId + ":participants";
            String lbKey = "contest:" + contestId + ":leaderboard";

            // Clear existing Redis data
            Set<Object> existingMembers = redisService.sMembers(participantsKey);
            if (existingMembers != null) {
                for (Object member : existingMembers) {
                    redisService.zRemove(lbKey, member);
                }
            }

            // Rebuild participants from MySQL and initialize leaderboard
            var registrations = competitionRegistrationMapper.selectList(
                    com.baomidou.mybatisplus.core.toolkit.Wrappers.<CompetitionRegistration>lambdaQuery()
                            .eq(CompetitionRegistration::getCompetitionId, contestId));
            for (CompetitionRegistration reg : registrations) {
                redisService.sAdd(participantsKey, reg.getUserId().toString());
                redisService.zAdd(lbKey, reg.getUserId().toString(), 0);
            }

            // Rebuild scores from all submissions in chronological order
            var submissions = questionSubmitMapper.selectList(
                    com.baomidou.mybatisplus.core.toolkit.Wrappers.<QuestionSubmit>lambdaQuery()
                            .eq(QuestionSubmit::getCompetitionId, contestId)
                            .orderByAsc(QuestionSubmit::getCreateTime));
            for (QuestionSubmit submit : submissions) {
                updateRankingOnJudgeComplete(submit);
            }

            log.info("排名重建完成: contestId={}, participants={}, submissions={}",
                    contestId, registrations.size(), submissions.size());
        } catch (Exception e) {
            log.error("rebuildRankings失败: contestId={}", contestId, e);
        } finally {
            redisDistributedLock.unlock(lockInfo);
        }
    }
}
