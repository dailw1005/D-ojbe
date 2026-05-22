package com.dailw.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dailw.common.ErrorCode;
import com.dailw.exception.BusinessException;
import com.dailw.model.dto.questionsolution.QuestionSolutionAddRequest;
import com.dailw.model.dto.questionsolution.QuestionSolutionQueryRequest;
import com.dailw.model.dto.questionsolution.QuestionSolutionUpdateRequest;
import com.dailw.model.entity.Question;
import com.dailw.model.entity.QuestionSolution;
import com.dailw.model.entity.User;
import com.dailw.model.vo.QuestionSolutionVO;
import com.dailw.model.vo.UserSimpleVO;
import com.dailw.service.interfaces.QuestionService;
import com.dailw.service.interfaces.QuestionSolutionService;
import com.dailw.mapper.QuestionSolutionMapper;
import com.dailw.service.interfaces.UserService;
import com.dailw.utils.StaticJsonUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author trave
* @description 针对表【question_solution(题解)】的数据库操作Service实现
* @createDate 2026-01-23 15:02:12
*/
@Service
public class QuestionSolutionServiceImpl extends ServiceImpl<QuestionSolutionMapper, QuestionSolution>
    implements QuestionSolutionService{

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Override
    public Long addQuestionSolution(QuestionSolutionAddRequest questionSolutionAddRequest, Long userId) {
        QuestionSolution questionSolution = new QuestionSolution();
        BeanUtils.copyProperties(questionSolutionAddRequest, questionSolution);
        
        List<String> tags = questionSolutionAddRequest.getTags();
        if (tags != null) {
            questionSolution.setTags(StaticJsonUtil.toJsonStr(tags));
        }
        questionSolution.setUserId(userId);
        questionSolution.setViewNum(0);

        Long questionId = questionSolutionAddRequest.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }

        boolean result = this.save(questionSolution);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建题解失败");
        }
        return questionSolution.getId();
    }

    @Override
    public Boolean updateQuestionSolution(QuestionSolutionUpdateRequest questionSolutionUpdateRequest, Long userId, boolean isAdmin) {
        Long id = questionSolutionUpdateRequest.getId();
        QuestionSolution oldSolution = this.getById(id);
        if (oldSolution == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题解不存在");
        }

        if (!oldSolution.getUserId().equals(userId) && !isAdmin) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权修改");
        }

        QuestionSolution questionSolution = new QuestionSolution();
        BeanUtils.copyProperties(questionSolutionUpdateRequest, questionSolution);
        
        List<String> tags = questionSolutionUpdateRequest.getTags();
        if (tags != null) {
            questionSolution.setTags(StaticJsonUtil.toJsonStr(tags));
        }

        return this.updateById(questionSolution);
    }

    @Override
    public Boolean deleteQuestionSolution(Long id, Long userId, boolean isAdmin) {
        QuestionSolution oldSolution = this.getById(id);
        if (oldSolution == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题解不存在");
        }

        if (!oldSolution.getUserId().equals(userId) && !isAdmin) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权删除");
        }

        return this.removeById(id);
    }

    @Override
    public Page<QuestionSolutionVO> getQuestionSolutionVOPage(QuestionSolutionQueryRequest questionSolutionQueryRequest) {
        long current = questionSolutionQueryRequest.getCurrent();
        long size = questionSolutionQueryRequest.getPageSize();
        
        LambdaQueryWrapper<QuestionSolution> queryWrapper = Wrappers.lambdaQuery();
        Long id = questionSolutionQueryRequest.getId();
        Long questionId = questionSolutionQueryRequest.getQuestionId();
        Long userId = questionSolutionQueryRequest.getUserId();
        String title = questionSolutionQueryRequest.getTitle();
        List<String> tags = questionSolutionQueryRequest.getTags();

        String sortField = questionSolutionQueryRequest.getSortField();
        String sortOrder = questionSolutionQueryRequest.getSortOrder();

        queryWrapper.eq(id != null, QuestionSolution::getId, id);
        queryWrapper.eq(questionId != null, QuestionSolution::getQuestionId, questionId);
        queryWrapper.eq(userId != null, QuestionSolution::getUserId, userId);
        queryWrapper.like(StringUtils.isNotBlank(title), QuestionSolution::getTitle, title);
        
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like(QuestionSolution::getTags, "\"" + tag + "\"");
            }
        }
        
        // 排序逻辑：支持按最新（时间）、最热（点赞最多）、最多浏览排序
        if (StringUtils.isNotBlank(sortField) && StringUtils.isNotBlank(sortOrder)) {
            boolean isAsc = com.dailw.constant.CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
            if ("thumbNum".equals(sortField)) {
                queryWrapper.orderBy(true, isAsc, QuestionSolution::getThumbNum);
            } else if ("viewNum".equals(sortField)) {
                queryWrapper.orderBy(true, isAsc, QuestionSolution::getViewNum);
            } else if ("createTime".equals(sortField)) {
                queryWrapper.orderBy(true, isAsc, QuestionSolution::getCreateTime);
            } else {
                queryWrapper.orderByDesc(QuestionSolution::getCreateTime);
            }
        } else {
            // 默认按创建时间降序排序（最新）
            queryWrapper.orderByDesc(QuestionSolution::getCreateTime);
        }

        Page<QuestionSolution> questionSolutionPage = this.page(new Page<>(current, size), queryWrapper);
        
        List<QuestionSolutionVO> questionSolutionVOList = questionSolutionPage.getRecords().stream().map(questionSolution -> {
            QuestionSolutionVO questionSolutionVO = QuestionSolutionVO.objToVo(questionSolution);
            User user = userService.getById(questionSolution.getUserId());
            if (user != null) {
                UserSimpleVO userVO = new UserSimpleVO();
                userVO.setAvatar(user.getAvatar());
                userVO.setNickname(user.getNickname());
                userVO.setGender(user.getGender());
                userVO.setBirthday(user.getBirthday());
                questionSolutionVO.setUserVO(userVO);
            }
            return questionSolutionVO;
        }).collect(Collectors.toList());

        Page<QuestionSolutionVO> questionSolutionVOPage = new Page<>(questionSolutionPage.getCurrent(), questionSolutionPage.getSize(), questionSolutionPage.getTotal());
        questionSolutionVOPage.setRecords(questionSolutionVOList);
        
        return questionSolutionVOPage;
    }

    @Override
    public Boolean thumbQuestionSolution(Long id, Long userId) {
        // 这里可以使用 Redis 实现防重复点赞，这里简化处理直接加 1
        QuestionSolution questionSolution = this.getById(id);
        if (questionSolution == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题解不存在");
        }
        
        return this.update()
                .setSql("thumb_num = thumb_num + 1")
                .eq("id", id)
                .update();
    }

    @Override
    public Boolean viewQuestionSolution(Long id) {
        QuestionSolution questionSolution = this.getById(id);
        if (questionSolution == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题解不存在");
        }
        return this.update()
                .setSql("view_num = view_num + 1")
                .eq("id", id)
                .update();
    }

    @Override
    public Map<String, Long> getUserSolutionStats(Long userId) {
        LambdaQueryWrapper<QuestionSolution> countQueryWrapper = Wrappers.lambdaQuery();
        countQueryWrapper.eq(QuestionSolution::getUserId, userId);
        long solutionCount = this.count(countQueryWrapper);

        QueryWrapper<QuestionSolution> sumQueryWrapper = new QueryWrapper<>();
        sumQueryWrapper.select("COALESCE(SUM(thumb_num), 0) AS thumbCount", "COALESCE(SUM(view_num), 0) AS viewCount");
        sumQueryWrapper.eq("user_id", userId);
        Map<String, Object> sumMap = this.getMap(sumQueryWrapper);

        long thumbCount = 0L;
        long viewCount = 0L;
        if (sumMap != null) {
            Object thumbCountObj = sumMap.get("thumbCount");
            Object viewCountObj = sumMap.get("viewCount");
            if (thumbCountObj instanceof Number) {
                thumbCount = ((Number) thumbCountObj).longValue();
            }
            if (viewCountObj instanceof Number) {
                viewCount = ((Number) viewCountObj).longValue();
            }
        }
        return Map.of(
                "solutionCount", solutionCount,
                "thumbCount", thumbCount,
                "viewCount", viewCount
        );
    }

    @Override
    public Map<String, Long> getTotalSolutionStats() {
        long solutionCount = this.count();
        QueryWrapper<QuestionSolution> sumQueryWrapper = new QueryWrapper<>();
        sumQueryWrapper.select("COALESCE(SUM(thumb_num), 0) AS thumbCount", "COALESCE(SUM(view_num), 0) AS viewCount");
        Map<String, Object> sumMap = this.getMap(sumQueryWrapper);
        long thumbCount = 0L;
        long viewCount = 0L;
        if (sumMap != null) {
            Object thumbCountObj = sumMap.get("thumbCount");
            Object viewCountObj = sumMap.get("viewCount");
            if (thumbCountObj instanceof Number) {
                thumbCount = ((Number) thumbCountObj).longValue();
            }
            if (viewCountObj instanceof Number) {
                viewCount = ((Number) viewCountObj).longValue();
            }
        }
        return Map.of(
                "solutionCount", solutionCount,
                "thumbCount", thumbCount,
                "viewCount", viewCount
        );
    }
}




