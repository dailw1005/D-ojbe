package com.dailw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dailw.common.ErrorCode;
import com.dailw.exception.BusinessException;
import com.dailw.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.dailw.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.dailw.model.entity.Question;
import com.dailw.model.entity.QuestionSubmit;
import com.dailw.model.enums.QuestionSubmitLanguageEnum;
import com.dailw.model.enums.QuestionSubmitStatusEnum;
import com.dailw.model.vo.QuestionSubmitVO;
import com.dailw.service.interfaces.QuestionService;
import com.dailw.service.interfaces.QuestionSubmitService;
import com.dailw.mapper.QuestionSubmitMapper;
import com.dailw.mq.JudgeProducer;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author trave
* @description 针对表【question_submit(题目提交)】的数据库操作Service实现
* @createDate 2026-01-23 15:02:27
*/
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
    implements QuestionSubmitService{

    @Resource
    private QuestionService questionService;

    @Resource
    private JudgeProducer judgeProducer;

    @Override
    public Long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, Long userId) {
        // 校验编程语言是否合法
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
        }
        Long questionId = questionSubmitAddRequest.getQuestionId();
        // 判断实体是否存在，根据类别获取实体
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 是否已提交代码
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setLanguage(language);
        // 设置初始状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.PENDING.getValue());
        questionSubmit.setJudgeInfo("{}");
        boolean save = this.save(questionSubmit);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败");
        }
        Long questionSubmitId = questionSubmit.getId();
        
        // 发送消息到 Kafka，交由判题服务异步处理
        judgeProducer.sendMessage(String.valueOf(questionSubmitId));
        
        return questionSubmitId;
    }

    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(QuestionSubmitQueryRequest questionSubmitQueryRequest, Long userId) {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        
        LambdaQueryWrapper<QuestionSubmit> queryWrapper = Wrappers.lambdaQuery();
        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long submitUserId = questionSubmitQueryRequest.getUserId();

        queryWrapper.eq(StringUtils.isNotBlank(language), QuestionSubmit::getLanguage, language);
        queryWrapper.eq(status != null, QuestionSubmit::getStatus, status);
        queryWrapper.eq(questionId != null, QuestionSubmit::getQuestionId, questionId);
        queryWrapper.eq(submitUserId != null, QuestionSubmit::getUserId, submitUserId);
        queryWrapper.orderByDesc(QuestionSubmit::getCreateTime);

        Page<QuestionSubmit> questionSubmitPage = this.page(new Page<>(current, size), queryWrapper);
        
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitPage.getRecords().stream()
                .map(QuestionSubmitVO::objToVo)
                .collect(Collectors.toList());

        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }

    @Override
    public long getUserSubmitCount(Long userId) {
        LambdaQueryWrapper<QuestionSubmit> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(QuestionSubmit::getUserId, userId);
        return this.count(queryWrapper);
    }

    @Override
    public long getUserAcceptedCount(Long userId) {
        LambdaQueryWrapper<QuestionSubmit> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(QuestionSubmit::getUserId, userId);
        queryWrapper.eq(QuestionSubmit::getStatus, QuestionSubmitStatusEnum.ACCEPTED.getValue());
        return this.count(queryWrapper);
    }
}




