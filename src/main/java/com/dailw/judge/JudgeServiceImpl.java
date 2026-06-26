package com.dailw.judge;

import com.fasterxml.jackson.core.type.TypeReference;
import com.dailw.common.ErrorCode;
import com.dailw.event.JudgeCompletedEvent;
import com.dailw.exception.BusinessException;
import com.dailw.judge.sandbox.CodeSandbox;
import com.dailw.judge.sandbox.model.ExecuteCodeRequest;
import com.dailw.judge.sandbox.model.ExecuteCodeResponse;
import com.dailw.judge.sandbox.model.JudgeTestCase;
import com.dailw.model.dto.question.JudgeCase;
import com.dailw.model.dto.question.JudgeConfig;
import com.dailw.model.dto.questionsubmit.JudgeInfo;
import com.dailw.model.entity.Question;
import com.dailw.model.entity.QuestionSubmit;
import com.dailw.model.enums.JudgeInfoMessageEnum;
import com.dailw.model.enums.QuestionSubmitStatusEnum;
import com.dailw.service.interfaces.QuestionService;
import com.dailw.service.interfaces.QuestionSubmitService;
import com.dailw.utils.StaticJsonUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private CodeSandbox codeSandbox;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        // 1. 根据传入的提交 id，获取到对应的题目、提交信息（包含代码、编程语言等）
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }

        // 2. 如果不为等待状态，就不用重复执行了
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.PENDING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中或已完成判题");
        }

        // 3. 更改判题状态为“判题中”，防止重复执行（此处可考虑引入乐观锁更新）
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.JUDGING.getValue());
        boolean update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }

        // 4. 调用沙箱，获取到执行结果
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();
        // 获取输入用例
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = StaticJsonUtil.toObj(judgeCaseStr, new TypeReference<List<JudgeCase>>() {
        });
        if (judgeCaseList == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目测试用例解析失败");
        }
        
        // 构造沙箱所需的测试用例格式
        List<JudgeTestCase> judgeTestCases = judgeCaseList.stream().map(judgeCase -> {
            JudgeTestCase judgeTestCase = new JudgeTestCase();
            judgeTestCase.setInput(judgeCase.getInput());
            judgeTestCase.setExpectedOutput(judgeCase.getOutput());
            return judgeTestCase;
        }).collect(Collectors.toList());

        // 获取题目限制并转换单位
        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig judgeConfig = StaticJsonUtil.toObj(judgeConfigStr, JudgeConfig.class);
        if (judgeConfig == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目配置解析失败");
        }
        Long timeLimit = judgeConfig.getTimeLimit(); // ms
        Long memoryLimitKB = judgeConfig.getMemoryLimit(); // KB
        Long memoryLimitBytes = memoryLimitKB != null ? memoryLimitKB * 1024 : 268435456L; // 默认 256MB

        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .id(String.valueOf(questionSubmitId))
                .language(language)
                .sourceCode(code)
                .testCases(judgeTestCases)
                .timeLimit(timeLimit)
                .memoryLimit(memoryLimitBytes)
                .build();

        ExecuteCodeResponse executeCodeResponse;
        try {
            executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        } catch (Exception e) {
            log.error("调用沙箱失败", e);
            // 处理沙箱调用异常，将状态更新为失败
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SYSTEM_ERROR.getValue());
            JudgeInfo errorJudgeInfo = new JudgeInfo();
            errorJudgeInfo.setMessage("判题服务暂时不可用，请稍后重试");
            questionSubmitUpdate.setJudgeInfo(StaticJsonUtil.toJsonStr(errorJudgeInfo));
            questionSubmitService.updateById(questionSubmitUpdate);
            QuestionSubmit finalSubmit = questionSubmitService.getById(questionSubmitId);
            eventPublisher.publishEvent(new JudgeCompletedEvent(this, finalSubmit));
            return finalSubmit;
        }

        // 5. 根据沙箱的执行结果，设置题目的判题状态和信息
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(executeCodeResponse.getMessage());
        judgeInfo.setTimeUsed(executeCodeResponse.getTimeUsed());
        judgeInfo.setMemoryUsed(executeCodeResponse.getMemoryUsed());
        judgeInfo.setStatus(executeCodeResponse.getStatus());
        
        // 映射用例结果
        if (executeCodeResponse.getTestCaseResults() != null) {
            List<com.dailw.model.dto.questionsubmit.JudgeCaseResult> resultList = executeCodeResponse.getTestCaseResults().stream().map(r -> {
                com.dailw.model.dto.questionsubmit.JudgeCaseResult res = new com.dailw.model.dto.questionsubmit.JudgeCaseResult();
                BeanUtils.copyProperties(r, res);
                return res;
            }).collect(Collectors.toList());
            judgeInfo.setTestCaseResults(resultList);
        }

        // 根据状态判断成功与否
        Integer finalStatus;
        if (JudgeInfoMessageEnum.ACCEPTED.getValue().equals(executeCodeResponse.getStatus())) {
            finalStatus = QuestionSubmitStatusEnum.ACCEPTED.getValue();
        } else if (JudgeInfoMessageEnum.WRONG_ANSWER.getValue().equals(executeCodeResponse.getStatus())) {
            finalStatus = QuestionSubmitStatusEnum.WRONG_ANSWER.getValue();
        } else if (JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED.getValue().equals(executeCodeResponse.getStatus())) {
            finalStatus = QuestionSubmitStatusEnum.TIME_LIMIT_EXCEEDED.getValue();
        } else if (JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED.getValue().equals(executeCodeResponse.getStatus())) {
            finalStatus = QuestionSubmitStatusEnum.MEMORY_LIMIT_EXCEEDED.getValue();
        } else if (JudgeInfoMessageEnum.RUNTIME_ERROR.getValue().equals(executeCodeResponse.getStatus())) {
            finalStatus = QuestionSubmitStatusEnum.RUNTIME_ERROR.getValue();
        } else if (JudgeInfoMessageEnum.COMPILE_ERROR.getValue().equals(executeCodeResponse.getStatus())) {
            finalStatus = QuestionSubmitStatusEnum.COMPILATION_ERROR.getValue();
        } else if (JudgeInfoMessageEnum.SYSTEM_ERROR.getValue().equals(executeCodeResponse.getStatus())) {
            finalStatus = QuestionSubmitStatusEnum.SYSTEM_ERROR.getValue();
        } else {
            // 默认兜底异常
            finalStatus = QuestionSubmitStatusEnum.SYSTEM_ERROR.getValue();
        }

        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(finalStatus);
        questionSubmitUpdate.setJudgeInfo(StaticJsonUtil.toJsonStr(judgeInfo));
        update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }

        // 6. 更新题目通过数和提交数
        // 注意：如果是通过，则通过数 +1；无论是否通过，提交数都需要在提交时或此时 +1
        // 为了简化，我们在判题结束时更新
        Question updateQuestion = new Question();
        updateQuestion.setId(questionId);
        // 这里使用简单的读后写，高并发下推荐使用 SQL：update question set submit_num = submit_num + 1 where id = ?
        questionService.update()
                .setSql("submit_num = submit_num + 1")
                .setSql(finalStatus.equals(QuestionSubmitStatusEnum.ACCEPTED.getValue()) ? "accepted_num = accepted_num + 1" : "accepted_num = accepted_num")
                .eq("id", questionId)
                .update();

        QuestionSubmit finalSubmit = questionSubmitService.getById(questionSubmitId);
        eventPublisher.publishEvent(new JudgeCompletedEvent(this, finalSubmit));
        return finalSubmit;
    }
}
