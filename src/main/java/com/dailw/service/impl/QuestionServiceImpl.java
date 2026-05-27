package com.dailw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dailw.common.ErrorCode;
import com.dailw.constant.CommonConstant;
import com.dailw.exception.BusinessException;
import com.dailw.exception.ThrowUtils;
import com.dailw.model.dto.question.*;
import com.dailw.model.entity.Question;
import com.dailw.model.vo.QuestionInfoVO;
import com.dailw.model.vo.QuestionVO;
import com.dailw.service.interfaces.QuestionService;
import com.dailw.mapper.QuestionMapper;
import com.dailw.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author trave
* @description 针对表【question(题目)】的数据库操作Service实现
* @createDate 2026-01-23 14:52:44
*/
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
    implements QuestionService{

    @Resource
    private JsonUtil jsonUtil;

    @Resource
    private QuestionMapper questionMapper;

    @Override
    @CacheEvict(value = {"questionPage", "questionInfo"}, allEntries = true)
    public Long add(Long currentUserId, QuestionAddRequest questionAddRequest) {

        if (questionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Question question = new Question();

        question.setTitle(questionAddRequest.getTitle());
        question.setContent(questionAddRequest.getContent());
        question.setAnswer(questionAddRequest.getAnswer());
        question.setDifficulty(questionAddRequest.getDifficulty());
        question.setUserId(currentUserId);

        JudgeConfig judgeConfig = questionAddRequest.getJudgeConfig();
        List<String> tags = questionAddRequest.getTags();
        List<JudgeCase> judgeCase = questionAddRequest.getJudgeCase();

        question.setJudgeConfig(jsonUtil.toJson(judgeConfig));
        question.setTags(jsonUtil.toJson(tags));
        question.setJudgeCase(jsonUtil.toJson(judgeCase));

        this.validQuestion(question, true);
        boolean result = this.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        return question.getId();
    }

    @Override
    @CacheEvict(value = {"question", "questionPage", "questionInfo"}, allEntries = true)
    public Boolean update(Long currentUserId, QuestionUpdateRequest questionUpdateRequest) {
        if (questionUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Question question = new Question();

        question.setTitle(questionUpdateRequest.getTitle());
        question.setContent(questionUpdateRequest.getContent());
        question.setAnswer(questionUpdateRequest.getAnswer());
        question.setDifficulty(questionUpdateRequest.getDifficulty());
        question.setUserId(currentUserId);

        JudgeConfig judgeConfig = questionUpdateRequest.getJudgeConfig();
        List<String> tags = questionUpdateRequest.getTags();
        List<JudgeCase> judgeCase = questionUpdateRequest.getJudgeCase();

        question.setJudgeConfig(jsonUtil.toJson(judgeConfig));
        question.setTags(jsonUtil.toJson(tags));
        question.setJudgeCase(jsonUtil.toJson(judgeCase));

        this.validQuestion(question, true);
        boolean result = this.updateById(question);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return true;
    }

    @Override
    @CacheEvict(value = {"question", "questionPage", "questionInfo"}, allEntries = true)
    public Boolean delete(Long currentUserId, Long questionId) {

        if (questionId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 判断是否存在
        Question oldQuestion = this.getById(questionId);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);

        boolean result = this.removeById(questionId);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return true;
    }

    @Override
    @Cacheable(value = "questionPage", key = "#current + '_' + #size + '_' + #questionQueryRequest.hashCode()")
    public Page<QuestionVO> queryByPage(Long current, Long size, QuestionQueryRequest questionQueryRequest) {
        if (questionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        LambdaQueryWrapper<Question> queryWrapper = Wrappers.lambdaQuery();
        Long id = questionQueryRequest.getId();
        String title = questionQueryRequest.getTitle();
        String content = questionQueryRequest.getContent();
        List<String> tags = questionQueryRequest.getTags();
        String difficulty = questionQueryRequest.getDifficulty();

        queryWrapper.eq(id != null, Question::getId, id);
        queryWrapper.eq(StringUtils.isNotBlank(difficulty), Question::getDifficulty, difficulty);
        queryWrapper.like(StringUtils.isNotBlank(title), Question::getTitle, title);
        queryWrapper.like(StringUtils.isNotBlank(content), Question::getContent, content);

        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                if (StringUtils.isNotBlank(tag)) {
                    queryWrapper.like(Question::getTags, tag);
                }
            }
        }

        // 白名单排序：只允许对指定字段排序，使用 MyBatis-Plus lambda 方法引用
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();
        if (StringUtils.isNotBlank(sortField) && StringUtils.isNotBlank(sortOrder)) {
            boolean isAsc = CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
            switch (sortField) {
                case "submitNum":
                    queryWrapper.orderBy(true, isAsc, Question::getSubmitNum);
                    break;
                case "acceptedNum":
                    queryWrapper.orderBy(true, isAsc, Question::getAcceptedNum);
                    break;
                case "createTime":
                    queryWrapper.orderBy(true, isAsc, Question::getCreateTime);
                    break;
                case "difficulty":
                    queryWrapper.orderBy(true, isAsc, Question::getDifficulty);
                    break;
                case "title":
                    queryWrapper.orderBy(true, isAsc, Question::getTitle);
                    break;
                default:
                    queryWrapper.orderByDesc(Question::getId);
            }
        } else {
            queryWrapper.orderByDesc(Question::getId);
        }

        Page<Question> page = this.page(new Page<>(current, size), queryWrapper);
        List<QuestionVO> records = page.getRecords().stream().map(question -> {
            QuestionVO questionVO = new QuestionVO();
            questionVO.setId(question.getId());
            questionVO.setTitle(question.getTitle());
            questionVO.setContent(question.getContent());
            questionVO.setTags(jsonUtil.fromJson(question.getTags(), new TypeReference<List<String>>() {}));
            questionVO.setDifficulty(question.getDifficulty());
            questionVO.setSubmitNum(question.getSubmitNum());
            questionVO.setAcceptedNum(question.getAcceptedNum());
            questionVO.setJudgeConfig(jsonUtil.fromJson(question.getJudgeConfig(), JudgeConfig.class));
            questionVO.setThumbNum(question.getThumbNum());
            questionVO.setFavourNum(question.getFavourNum());
            return questionVO;
        }).collect(Collectors.toList());

        Page<QuestionVO> result = new Page<>(current, size, page.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    @Cacheable(value = "question", key = "#questionId")
    public QuestionVO queryQuestionVoById(Long questionId) {
        Question question = this.getById(questionId);
        QuestionVO questionVO = new QuestionVO();
        questionVO.setId(question.getId());
        questionVO.setTitle(question.getTitle());
        questionVO.setContent(question.getContent());
        questionVO.setTags(jsonUtil.fromJson(question.getTags(), new TypeReference<List<String>>() {}));
        questionVO.setDifficulty(question.getDifficulty());
        questionVO.setSubmitNum(question.getSubmitNum());
        questionVO.setAcceptedNum(question.getAcceptedNum());
        questionVO.setJudgeConfig(jsonUtil.fromJson(question.getJudgeConfig(), JudgeConfig.class));
        questionVO.setThumbNum(question.getThumbNum());
        questionVO.setFavourNum(question.getFavourNum());
        return questionVO;
    }

    @Override
    @Cacheable(value = "questionInfo", key = "'overview'")
    public QuestionInfoVO getQuestionInfo() {
        return questionMapper.selectQuestionInfo();
    }

    public void validQuestion(Question question, boolean add) {
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = question.getTitle();
        String content = question.getContent();
        String tags = question.getTags();
        String answer = question.getAnswer();
        String judgeCase = question.getJudgeCase();
        String judgeConfig = question.getJudgeConfig();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content, tags), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
        if (StringUtils.isNotBlank(answer) && answer.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "答案过长");
        }
        if (StringUtils.isNotBlank(judgeCase) && judgeCase.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "判题用例过长");
        }
        if (StringUtils.isNotBlank(judgeConfig) && judgeConfig.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "判题配置过长");
        }
    }

}




