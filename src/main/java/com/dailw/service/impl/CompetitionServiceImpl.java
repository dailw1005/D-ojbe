package com.dailw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dailw.common.ErrorCode;
import com.dailw.constant.CommonConstant;
import com.dailw.exception.BusinessException;
import com.dailw.exception.ThrowUtils;
import com.dailw.mapper.*;
import com.dailw.model.dto.competition.*;
import com.dailw.model.entity.*;
import com.dailw.model.enums.QuestionSubmitLanguageEnum;
import com.dailw.model.enums.QuestionSubmitStatusEnum;
import com.dailw.model.vo.*;
import com.dailw.mq.JudgeProducer;
import com.dailw.service.interfaces.CompetitionRankingService;
import com.dailw.service.interfaces.CompetitionService;
import com.dailw.service.interfaces.ReputationService;
import com.dailw.service.interfaces.QuestionService;
import com.dailw.service.interfaces.QuestionSubmitService;
import com.dailw.service.interfaces.RedisService;
import com.dailw.service.interfaces.UserService;
import com.dailw.utils.TimeFormatUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CompetitionServiceImpl extends ServiceImpl<CompetitionMapper, Competition>
        implements CompetitionService {

    @Resource
    private CompetitionMapper competitionMapper;

    @Resource
    private CompetitionQuestionMapper competitionQuestionMapper;

    @Resource
    private CompetitionRegistrationMapper competitionRegistrationMapper;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private QuestionSubmitMapper questionSubmitMapper;

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private UserService userService;

    @Resource
    private RedisService redisService;

    @Resource
    private JudgeProducer judgeProducer;

    @Resource
    private CompetitionRankingService competitionRankingService;

    @Resource
    private ReputationService reputationService;

    private static final Set<String> VALID_TYPES = Set.of("ACM", "OI");
    private static final Set<Integer> WRONG_STATUSES = Set.of(
            QuestionSubmitStatusEnum.WRONG_ANSWER.getValue(),
            QuestionSubmitStatusEnum.TIME_LIMIT_EXCEEDED.getValue(),
            QuestionSubmitStatusEnum.MEMORY_LIMIT_EXCEEDED.getValue(),
            QuestionSubmitStatusEnum.RUNTIME_ERROR.getValue(),
            QuestionSubmitStatusEnum.COMPILATION_ERROR.getValue());

    @Override
    @Transactional
    public Long createContest(ContestCreateRequest request, Long userId) {
        // Level check (admin bypass)
        User creator = userService.getById(userId);
        if (creator != null && !"admin".equals(creator.getRole())) {
            int level = creator.getLevel() != null ? creator.getLevel() : 0;
            if (level < com.dailw.constant.LevelConstants.CREATE_PRIVATE_LEVEL) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR,
                        "声望不足，Lv1 初出茅庐以上可创建比赛（当前声望: " +
                        (creator.getReputation() != null ? creator.getReputation() : 0) + "）");
            }
            if (level < com.dailw.constant.LevelConstants.CREATE_PUBLIC_LEVEL
                    && StringUtils.isBlank(request.getPassword())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR,
                        "声望不足，Lv2 小有所成以上可创建公开比赛，请设置参赛密码");
            }
        }

        validateContestParams(request.getTitle(), request.getType(),
                request.getStartTime(), request.getEndTime(), true);

        Competition contest = new Competition();
        contest.setTitle(request.getTitle());
        contest.setDescription(request.getDescription());
        contest.setType(request.getType());
        contest.setStartTime(request.getStartTime());
        contest.setEndTime(request.getEndTime());
        if (StringUtils.isNotBlank(request.getPassword())) {
            contest.setPassword(com.dailw.utils.PasswordUtil.encryptPassword(request.getPassword()));
        }
        contest.setUserId(userId);

        save(contest);

        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            saveContestQuestions(contest.getId(), request.getQuestions());
        }

        log.info("比赛创建成功: id={}, title={}, type={}", contest.getId(), contest.getTitle(), contest.getType());
        return contest.getId();
    }

    @Override
    @Transactional
    public Boolean updateContest(ContestUpdateRequest request, Long userId) {
        if (request.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Competition contest = getById(request.getId());
        if (contest == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "比赛不存在");
        }
        if (!contest.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只能修改自己创建的比赛");
        }

        if (StringUtils.isNotBlank(request.getTitle())) {
            contest.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            contest.setDescription(request.getDescription());
        }
        if (StringUtils.isNotBlank(request.getType())) {
            if (!VALID_TYPES.contains(request.getType())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "比赛类型必须为 ACM 或 OI");
            }
            if (!request.getType().equals(contest.getType())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "比赛类型创建后不可修改");
            }
        }
        if (request.getStartTime() != null) {
            contest.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            contest.setEndTime(request.getEndTime());
        }
        if (request.getPassword() != null) {
            if (StringUtils.isNotBlank(request.getPassword())) {
                contest.setPassword(com.dailw.utils.PasswordUtil.encryptPassword(request.getPassword()));
            } else {
                contest.setPassword(null);
            }
        }
        if (contest.getStartTime() != null && contest.getEndTime() != null
                && !contest.getEndTime().after(contest.getStartTime())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "结束时间必须晚于开始时间");
        }

        updateById(contest);
        return true;
    }

    @Override
    @Transactional
    public Boolean deleteContest(Long contestId, Long userId) {
        ContestDetailVO detail = getContestDetail(contestId, userId);
        if (!detail.getCreator().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只能删除自己创建的比赛");
        }

        // 1. Physical delete competition-question associations
        competitionQuestionMapper.delete(
                Wrappers.<CompetitionQuestion>lambdaQuery()
                        .eq(CompetitionQuestion::getCompetitionId, contestId));

        // 2. Physical delete competition registrations
        competitionRegistrationMapper.delete(
                Wrappers.<CompetitionRegistration>lambdaQuery()
                        .eq(CompetitionRegistration::getCompetitionId, contestId));

        // 3. Soft delete the contest
        removeById(contestId);

        // 4. Clean up Redis ranking keys
        Set<Object> participants = redisService.sMembers(
                "contest:" + contestId + ":participants");
        if (participants != null) {
            for (Object member : participants) {
                long uid = Long.parseLong(member.toString());
                var cqList = competitionQuestionMapper.selectList(
                        Wrappers.<CompetitionQuestion>lambdaQuery()
                                .eq(CompetitionQuestion::getCompetitionId, contestId));
                for (CompetitionQuestion cq : cqList) {
                    String prefix = "contest:" + contestId + ":user:" + uid
                            + ":problem:" + cq.getQuestionId();
                    redisService.delete(prefix + ":status");
                    redisService.delete(prefix + ":fail_count");
                    redisService.delete(prefix + ":best_score");
                    redisService.delete(prefix + ":try_count");
                    redisService.delete(prefix + ":last_submit_time");
                }
                redisService.delete("contest:" + contestId + ":user:" + uid
                        + ":last_submit_time");
            }
        }
        redisService.delete("contest:" + contestId + ":leaderboard");
        redisService.delete("contest:" + contestId + ":participants");
        redisService.delete("contest:" + contestId + ":ttl_set");

        log.info("比赛及其关联数据已清理: contestId={}", contestId);
        return true;
    }

    @Override
    @Transactional
    public Boolean setQuestions(ContestSetQuestionsRequest request, Long userId) {
        Long contestId = request.getContestId();
        Competition contest = getById(contestId);
        if (contest == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "比赛不存在");
        }
        if (!contest.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        LambdaQueryWrapper<CompetitionQuestion> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CompetitionQuestion::getCompetitionId, contestId);
        competitionQuestionMapper.delete(wrapper);

        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            saveContestQuestions(contestId, request.getQuestions());
        }
        return true;
    }

    @Override
    public Page<ContestVO> listContests(ContestQueryRequest request) {
        long current = request.getCurrent();
        long size = request.getPageSize();

        LambdaQueryWrapper<Competition> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(request.getType())) {
            wrapper.eq(Competition::getType, request.getType());
        }

        if (StringUtils.isNotBlank(request.getStatus())) {
            Date now = new Date();
            switch (request.getStatus()) {
                case "PENDING":
                    wrapper.gt(Competition::getStartTime, now);
                    break;
                case "RUNNING":
                    wrapper.le(Competition::getStartTime, now).ge(Competition::getEndTime, now);
                    break;
                case "ENDED":
                    wrapper.lt(Competition::getEndTime, now);
                    break;
            }
        }

        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        if (StringUtils.isNotBlank(sortField)) {
            boolean isAsc = CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
            switch (sortField) {
                case "id":
                    wrapper.orderBy(true, isAsc, Competition::getId);
                    break;
                case "startTime":
                    wrapper.orderBy(true, isAsc, Competition::getStartTime);
                    break;
                case "createTime":
                    wrapper.orderBy(true, isAsc, Competition::getCreateTime);
                    break;
                default:
                    wrapper.orderByDesc(Competition::getId);
            }
        } else {
            wrapper.orderByDesc(Competition::getId);
        }

        Page<Competition> page = page(new Page<>(current, size), wrapper);
        List<ContestVO> records = page.getRecords().stream()
                .map(this::toContestVO)
                .collect(Collectors.toList());

        Page<ContestVO> result = new Page<>(current, size, page.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public ContestDetailVO getContestDetail(Long contestId, Long userId) {
        Competition contest = getById(contestId);
        if (contest == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "比赛不存在");
        }

        ContestDetailVO vo = new ContestDetailVO();
        vo.setId(contest.getId());
        vo.setTitle(contest.getTitle());
        vo.setDescription(contest.getDescription());
        vo.setType(contest.getType());
        vo.setStartTime(TimeFormatUtil.toIsoOffsetString(contest.getStartTime()));
        vo.setEndTime(TimeFormatUtil.toIsoOffsetString(contest.getEndTime()));
        vo.setStatus(computeStatus(contest));
        vo.setHasPassword(StringUtils.isNotBlank(contest.getPassword()));

        long participantCount = competitionRegistrationMapper.selectCount(
                Wrappers.<CompetitionRegistration>lambdaQuery()
                        .eq(CompetitionRegistration::getCompetitionId, contestId));
        vo.setParticipantCount((int) participantCount);

        if (userId != null) {
            long regCount = competitionRegistrationMapper.selectCount(
                    Wrappers.<CompetitionRegistration>lambdaQuery()
                            .eq(CompetitionRegistration::getCompetitionId, contestId)
                            .eq(CompetitionRegistration::getUserId, userId));
            vo.setIsRegistered(regCount > 0);
        }

        User creator = userService.getById(contest.getUserId());
        if (creator != null) {
            UserVO creatorVO = new UserVO();
            creatorVO.setId(creator.getId());
            creatorVO.setUsername(creator.getUsername());
            creatorVO.setAvatar(creator.getAvatar());
            vo.setCreator(creatorVO);
        }

        String status = vo.getStatus();
        if ("RUNNING".equals(status) || "ENDED".equals(status)) {
            List<CompetitionQuestion> cqList = competitionQuestionMapper.selectList(
                    Wrappers.<CompetitionQuestion>lambdaQuery()
                            .eq(CompetitionQuestion::getCompetitionId, contestId)
                            .orderByAsc(CompetitionQuestion::getDisplayOrder));

            List<ContestDetailVO.QuestionItem> questionItems = new ArrayList<>();
            for (CompetitionQuestion cq : cqList) {
                Question question = questionService.getById(cq.getQuestionId());
                if (question == null) continue;

                ContestDetailVO.QuestionItem item = new ContestDetailVO.QuestionItem();
                item.setQuestionId(question.getId());
                item.setTitle(question.getTitle());
                item.setDifficulty(question.getDifficulty());
                item.setDisplayOrder(cq.getDisplayOrder());
                item.setScore(cq.getScore());

                if (userId != null && "RUNNING".equals(status)) {
                    long acceptedCount = questionSubmitMapper.selectCount(
                            Wrappers.<QuestionSubmit>lambdaQuery()
                                    .eq(QuestionSubmit::getCompetitionId, contestId)
                                    .eq(QuestionSubmit::getUserId, userId)
                                    .eq(QuestionSubmit::getQuestionId, question.getId())
                                    .eq(QuestionSubmit::getStatus, QuestionSubmitStatusEnum.ACCEPTED.getValue()));
                    item.setStatus(acceptedCount > 0 ? "ACCEPTED" : null);
                }

                questionItems.add(item);
            }
            vo.setQuestions(questionItems);
        }

        return vo;
    }

    @Override
    public Boolean register(Long contestId, Long userId, String password) {
        Competition contest = getById(contestId);
        if (contest == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "比赛不存在");
        }

        Date now = new Date();
        if (now.after(contest.getEndTime())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "比赛已结束，无法报名");
        }

        if (StringUtils.isNotBlank(contest.getPassword())) {
            if (StringUtils.isBlank(password) ||
                    !com.dailw.utils.PasswordUtil.verifyPassword(password, contest.getPassword())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "比赛密码错误");
            }
        }

        long count = competitionRegistrationMapper.selectCount(
                Wrappers.<CompetitionRegistration>lambdaQuery()
                        .eq(CompetitionRegistration::getCompetitionId, contestId)
                        .eq(CompetitionRegistration::getUserId, userId));
        if (count > 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "您已报名该比赛");
        }

        CompetitionRegistration reg = new CompetitionRegistration();
        reg.setCompetitionId(contestId);
        reg.setUserId(userId);
        competitionRegistrationMapper.insert(reg);

        String participantsKey = "contest:" + contestId + ":participants";
        redisService.sAdd(participantsKey, userId.toString());

        String lbKey = "contest:" + contestId + ":leaderboard";
        redisService.zAdd(lbKey, userId.toString(), 0);

        return true;
    }

    @Override
    public Long submitCode(Long contestId, Long userId, ContestSubmitRequest request) {
        Competition contest = getById(contestId);
        if (contest == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "比赛不存在");
        }

        Date now = new Date();
        if (now.before(contest.getStartTime())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "比赛尚未开始");
        }
        if (now.after(contest.getEndTime())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "比赛已结束");
        }

        long regCount = competitionRegistrationMapper.selectCount(
                Wrappers.<CompetitionRegistration>lambdaQuery()
                        .eq(CompetitionRegistration::getCompetitionId, contestId)
                        .eq(CompetitionRegistration::getUserId, userId));
        if (regCount == 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "请先报名比赛");
        }

        Question question = questionService.getById(request.getQuestionId());
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }

        long cqCount = competitionQuestionMapper.selectCount(
                Wrappers.<CompetitionQuestion>lambdaQuery()
                        .eq(CompetitionQuestion::getCompetitionId, contestId)
                        .eq(CompetitionQuestion::getQuestionId, request.getQuestionId()));
        if (cqCount == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该题目不属于此比赛");
        }

        if (!QuestionSubmitLanguageEnum.getValues().contains(request.getLanguage())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的编程语言");
        }

        QuestionSubmit submit = new QuestionSubmit();
        submit.setLanguage(request.getLanguage());
        submit.setCode(request.getCode());
        submit.setQuestionId(request.getQuestionId());
        submit.setUserId(userId);
        submit.setCompetitionId(contestId);
        submit.setStatus(QuestionSubmitStatusEnum.PENDING.getValue());
        submit.setJudgeInfo("{}");

        questionSubmitService.save(submit);
        judgeProducer.sendMessage(String.valueOf(submit.getId()));

        log.info("比赛提交: contestId={}, userId={}, submitId={}", contestId, userId, submit.getId());
        return submit.getId();
    }

    @Override
    public ContestRankingVO getRanking(Long contestId, Long userId, int page, int size) {
        Competition contest = getById(contestId);
        if (contest == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "比赛不存在");
        }

        String lbKey = "contest:" + contestId + ":leaderboard";
        String participantsKey = "contest:" + contestId + ":participants";

        Long totalParticipants = redisService.sSize(participantsKey);
        if (totalParticipants == null) totalParticipants = 0L;

        // Fallback: if Redis is empty but MySQL has registrations, rebuild rankings
        if (totalParticipants == 0L) {
            long dbRegCount = competitionRegistrationMapper.selectCount(
                    Wrappers.<CompetitionRegistration>lambdaQuery()
                            .eq(CompetitionRegistration::getCompetitionId, contestId));
            if (dbRegCount > 0) {
                log.info("Redis排名数据为空，从MySQL重建: contestId={}", contestId);
                competitionRankingService.rebuildRankings(contestId);
                totalParticipants = redisService.sSize(participantsKey);
                if (totalParticipants == null) totalParticipants = 0L;
            }
        }

        // After contest ends, set 7-day TTL on core Redis keys (once)
        if ("ENDED".equals(computeStatus(contest))) {
            String ttlFlagKey = "contest:" + contestId + ":ttl_set";
            if (redisService.get(ttlFlagKey) == null) {
                redisService.set(ttlFlagKey, "1", 7, java.util.concurrent.TimeUnit.DAYS);
                redisService.expire(lbKey, 7, java.util.concurrent.TimeUnit.DAYS);
                redisService.expire(participantsKey, 7, java.util.concurrent.TimeUnit.DAYS);
                log.info("比赛Redis数据TTL已设置(7天): contestId={}", contestId);
                // Distribute reputation rewards
                reputationService.onContestEnded(contestId);
            }
        }

        int start = (page - 1) * size;
        int end = start + size - 1;

        Set<Object> rankedMembers = redisService.zRange(lbKey, start, end);
        ContestRankingVO vo = new ContestRankingVO();
        vo.setTotalParticipants(totalParticipants);

        List<ContestRankingVO.RankItem> rankings = new ArrayList<>();
        if (rankedMembers != null) {
            int rank = start + 1;
            for (Object member : rankedMembers) {
                Long memberId = Long.valueOf(member.toString());
                ContestRankingVO.RankItem item = buildRankItem(contest, memberId, rank);
                rankings.add(item);
                rank++;
            }
        }
        vo.setRankings(rankings);

        if (userId != null) {
            boolean isRegistered = redisService.sIsMember(participantsKey, userId.toString()) != null
                    && redisService.sIsMember(participantsKey, userId.toString());
            // Fallback: check MySQL if Redis doesn't have the participant record
            if (!isRegistered) {
                long dbRegCount = competitionRegistrationMapper.selectCount(
                        Wrappers.<CompetitionRegistration>lambdaQuery()
                                .eq(CompetitionRegistration::getCompetitionId, contestId)
                                .eq(CompetitionRegistration::getUserId, userId));
                isRegistered = dbRegCount > 0;
            }
            if (isRegistered) {
                Long myRank = getRankPosition(lbKey, userId);
                if (myRank == null) {
                    // User is registered but not in Redis leaderboard — rebuild rankings from MySQL
                    log.info("用户已报名但不在Redis排行榜中，触发重建: contestId={}, userId={}", contestId, userId);
                    competitionRankingService.rebuildRankings(contestId);
                    myRank = getRankPosition(lbKey, userId);
                }
                if (myRank != null) {
                    vo.setMyRank(buildRankItem(contest, userId, myRank.intValue()));
                }
            }
        }

        return vo;
    }

    private ContestRankingVO.RankItem buildRankItem(Competition contest, Long userId, int rank) {
        ContestRankingVO.RankItem item = new ContestRankingVO.RankItem();
        item.setRank(rank);
        item.setUserId(userId);

        User user = userService.getById(userId);
        if (user != null) {
            item.setUsername(user.getUsername());
            item.setAvatar(user.getAvatar());
        }

        if ("ACM".equals(contest.getType())) {
            fillAcmRankItem(contest.getId(), userId, item);
        } else {
            fillOiRankItem(contest.getId(), userId, item);
        }
        return item;
    }

    private void fillAcmRankItem(Long contestId, Long userId, ContestRankingVO.RankItem item) {
        int solvedCount = 0;
        long totalPenalty = 0;
        List<CompetitionQuestion> cqList = competitionQuestionMapper.selectList(
                Wrappers.<CompetitionQuestion>lambdaQuery()
                        .eq(CompetitionQuestion::getCompetitionId, contestId)
                        .orderByAsc(CompetitionQuestion::getDisplayOrder));

        List<ContestRankingVO.ProblemResult> problemResults = new ArrayList<>();
        for (CompetitionQuestion cq : cqList) {
            ContestRankingVO.ProblemResult pr = new ContestRankingVO.ProblemResult();
            pr.setQuestionId(cq.getQuestionId());

            String statusKey = "contest:" + contestId + ":user:" + userId
                    + ":problem:" + cq.getQuestionId() + ":status";
            Object statusObj = redisService.get(statusKey);
            boolean solved = "solved".equals(statusObj);
            pr.setSolved(solved);

            String failKey = "contest:" + contestId + ":user:" + userId
                    + ":problem:" + cq.getQuestionId() + ":fail_count";
            Object failObj = redisService.get(failKey);
            int failCount = failObj != null ? Integer.parseInt(failObj.toString()) : 0;
            int tryCount = failCount;
            if (solved) tryCount++;
            pr.setTryCount(tryCount);

            if (solved) {
                solvedCount++;
                Competition contest = getById(contestId);
                List<QuestionSubmit> acceptedList = questionSubmitMapper.selectList(
                        Wrappers.<QuestionSubmit>lambdaQuery()
                                .eq(QuestionSubmit::getCompetitionId, contestId)
                                .eq(QuestionSubmit::getUserId, userId)
                                .eq(QuestionSubmit::getQuestionId, cq.getQuestionId())
                                .eq(QuestionSubmit::getStatus, QuestionSubmitStatusEnum.ACCEPTED.getValue())
                                .orderByAsc(QuestionSubmit::getCreateTime)
                                .last("LIMIT 1"));
                if (!acceptedList.isEmpty()) {
                    long solveTimeMs = acceptedList.get(0).getCreateTime().getTime()
                            - contest.getStartTime().getTime();
                    long penalty = solveTimeMs / 1000 + failCount * 20L * 60;
                    pr.setPenaltyTime(penalty);
                    totalPenalty += penalty;
                }
            }
            problemResults.add(pr);
        }

        item.setSolvedCount(solvedCount);
        item.setPenalty(totalPenalty);
        item.setProblemResults(problemResults);
    }

    private void fillOiRankItem(Long contestId, Long userId, ContestRankingVO.RankItem item) {
        int totalScore = 0;
        List<CompetitionQuestion> cqList = competitionQuestionMapper.selectList(
                Wrappers.<CompetitionQuestion>lambdaQuery()
                        .eq(CompetitionQuestion::getCompetitionId, contestId)
                        .orderByAsc(CompetitionQuestion::getDisplayOrder));

        List<ContestRankingVO.ProblemResult> problemResults = new ArrayList<>();
        for (CompetitionQuestion cq : cqList) {
            ContestRankingVO.ProblemResult pr = new ContestRankingVO.ProblemResult();
            pr.setQuestionId(cq.getQuestionId());

            String bestKey = "contest:" + contestId + ":user:" + userId
                    + ":problem:" + cq.getQuestionId() + ":best_score";
            Object bestObj = redisService.get(bestKey);
            int bestScore = bestObj != null ? Integer.parseInt(bestObj.toString()) : 0;
            pr.setScore(bestScore);
            totalScore += bestScore;

            String tryKey = "contest:" + contestId + ":user:" + userId
                    + ":problem:" + cq.getQuestionId() + ":try_count";
            Object tryObj = redisService.get(tryKey);
            pr.setTryCount(tryObj != null ? Integer.parseInt(tryObj.toString()) : 0);

            pr.setSolved(bestScore > 0);
            problemResults.add(pr);
        }

        item.setTotalScore(totalScore);
        item.setProblemResults(problemResults);
    }

    private Long getRankPosition(String lbKey, Long userId) {
        Long rank = redisService.zRank(lbKey, userId.toString());
        return rank != null ? rank + 1 : null;
    }

    private String computeStatus(Competition contest) {
        Date now = new Date();
        if (now.before(contest.getStartTime())) return "PENDING";
        if (now.after(contest.getEndTime())) return "ENDED";
        return "RUNNING";
    }

    private ContestVO toContestVO(Competition contest) {
        ContestVO vo = new ContestVO();
        vo.setId(contest.getId());
        vo.setTitle(contest.getTitle());
        vo.setDescription(contest.getDescription());
        vo.setType(contest.getType());
        vo.setStartTime(TimeFormatUtil.toIsoOffsetString(contest.getStartTime()));
        vo.setEndTime(TimeFormatUtil.toIsoOffsetString(contest.getEndTime()));
        vo.setHasPassword(StringUtils.isNotBlank(contest.getPassword()));
        vo.setStatus(computeStatus(contest));
        vo.setUserId(contest.getUserId());
        vo.setCreateTime(TimeFormatUtil.toIsoOffsetString(contest.getCreateTime()));

        User creator = userService.getById(contest.getUserId());
        if (creator != null) {
            UserVO creatorVO = new UserVO();
            creatorVO.setId(creator.getId());
            creatorVO.setUsername(creator.getUsername());
            creatorVO.setAvatar(creator.getAvatar());
            vo.setCreator(creatorVO);
        }

        long participantCount = competitionRegistrationMapper.selectCount(
                Wrappers.<CompetitionRegistration>lambdaQuery()
                        .eq(CompetitionRegistration::getCompetitionId, contest.getId()));
        vo.setParticipantCount((int) participantCount);

        long questionCount = competitionQuestionMapper.selectCount(
                Wrappers.<CompetitionQuestion>lambdaQuery()
                        .eq(CompetitionQuestion::getCompetitionId, contest.getId()));
        vo.setQuestionCount((int) questionCount);

        return vo;
    }

    private void saveContestQuestions(Long contestId, List<?> questionItems) {
        for (Object item : questionItems) {
            Long questionId;
            Integer displayOrder;
            Integer score;
            if (item instanceof ContestCreateRequest.QuestionItem qi) {
                questionId = qi.getQuestionId();
                displayOrder = qi.getDisplayOrder();
                score = qi.getScore();
            } else if (item instanceof ContestSetQuestionsRequest.QuestionItem qi) {
                questionId = qi.getQuestionId();
                displayOrder = qi.getDisplayOrder();
                score = qi.getScore();
            } else {
                continue;
            }

            if (questionService.getById(questionId) == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,
                        "题目不存在: id=" + questionId);
            }

            CompetitionQuestion cq = new CompetitionQuestion();
            cq.setCompetitionId(contestId);
            cq.setQuestionId(questionId);
            cq.setDisplayOrder(displayOrder != null ? displayOrder : 0);
            cq.setScore(score);
            competitionQuestionMapper.insert(cq);
        }
    }

    private void validateContestParams(String title, String type, Date startTime,
                                       Date endTime, boolean isCreate) {
        if (isCreate) {
            if (StringUtils.isBlank(title)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "比赛标题不能为空");
            }
            if (StringUtils.isBlank(type)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "比赛类型不能为空");
            }
            if (startTime == null || endTime == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "比赛时间不能为空");
            }
        }
        if (type != null && !VALID_TYPES.contains(type)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "比赛类型必须为 ACM 或 OI");
        }
        if (startTime != null && endTime != null && !endTime.after(startTime)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "结束时间必须晚于开始时间");
        }
    }
}
