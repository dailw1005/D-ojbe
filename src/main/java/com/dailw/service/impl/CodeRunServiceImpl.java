package com.dailw.service.impl;

import com.dailw.common.ErrorCode;
import com.dailw.exception.BusinessException;
import com.dailw.judge.sandbox.CodeSandbox;
import com.dailw.judge.sandbox.model.ExecuteCodeRequest;
import com.dailw.judge.sandbox.model.ExecuteCodeResponse;
import com.dailw.judge.sandbox.model.JudgeTestCase;
import com.dailw.judge.sandbox.model.TestCaseResult;
import com.dailw.model.dto.coderun.CodeRunRequest;
import com.dailw.model.dto.question.JudgeConfig;
import com.dailw.model.entity.Question;
import com.dailw.model.enums.JudgeInfoMessageEnum;
import com.dailw.model.enums.QuestionSubmitLanguageEnum;
import com.dailw.model.vo.CodeRunVO;
import com.dailw.service.interfaces.CodeRunService;
import com.dailw.service.interfaces.QuestionService;
import com.dailw.utils.StaticJsonUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
@Slf4j
public class CodeRunServiceImpl implements CodeRunService {

    private static final long DEFAULT_TIME_LIMIT_MS = 10_000L;
    private static final long DEFAULT_MEMORY_LIMIT_BYTES = 256L * 1024 * 1024;

    @Resource
    private QuestionService questionService;

    @Resource
    private CodeSandbox codeSandbox;

    @Override
    public CodeRunVO doRun(CodeRunRequest request) {
        String language = request.getLanguage();
        String sourceCode = request.getSourceCode();
        String inputCase = request.getInputCase();

        if (QuestionSubmitLanguageEnum.getEnumByValue(language) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的编程语言: " + language);
        }

        long timeLimit = DEFAULT_TIME_LIMIT_MS;
        long memoryLimit = DEFAULT_MEMORY_LIMIT_BYTES;

        Long questionId = request.getQuestionId();
        if (questionId != null) {
            Question question = questionService.getById(questionId);
            if (question != null) {
                String judgeConfigStr = question.getJudgeConfig();
                if (judgeConfigStr != null && !judgeConfigStr.isBlank()) {
                    try {
                        JudgeConfig judgeConfig = StaticJsonUtil.toObj(judgeConfigStr, JudgeConfig.class);
                        if (judgeConfig != null) {
                            if (judgeConfig.getTimeLimit() != null) {
                                timeLimit = judgeConfig.getTimeLimit();
                            }
                            if (judgeConfig.getMemoryLimit() != null) {
                                memoryLimit = judgeConfig.getMemoryLimit() * 1024;
                            }
                        }
                    } catch (Exception e) {
                        log.warn("解析题目 judgeConfig 失败, questionId={}", questionId, e);
                    }
                }
            }
        }

        JudgeTestCase testCase = new JudgeTestCase();
        testCase.setInput(inputCase != null ? inputCase : "");
        testCase.setExpectedOutput(null);

        ExecuteCodeRequest executeRequest = ExecuteCodeRequest.builder()
                .id("run-" + UUID.randomUUID())
                .language(language)
                .sourceCode(sourceCode)
                .testCases(Collections.singletonList(testCase))
                .timeLimit(timeLimit)
                .memoryLimit(memoryLimit)
                .build();

        ExecuteCodeResponse response;
        try {
            response = codeSandbox.executeCode(executeRequest);
        } catch (Exception e) {
            log.error("调用代码沙箱失败", e);
            return CodeRunVO.builder()
                    .status(JudgeInfoMessageEnum.SYSTEM_ERROR.getValue())
                    .message("判题服务暂时不可用，请稍后重试")
                    .build();
        }

        if (response == null) {
            return CodeRunVO.builder()
                    .status(JudgeInfoMessageEnum.SYSTEM_ERROR.getValue())
                    .message("沙箱返回结果为空")
                    .build();
        }

        // 无测试用例结果时（如编译错误），使用顶层 status 和 message
        if (response.getTestCaseResults() == null || response.getTestCaseResults().isEmpty()) {
            String topStatus = response.getStatus();
            String topMessage = response.getMessage();
            return CodeRunVO.builder()
                    .status(topStatus != null ? topStatus : JudgeInfoMessageEnum.SYSTEM_ERROR.getValue())
                    .message(topMessage)
                    .timeUsed(response.getTimeUsed())
                    .memoryUsed(response.getMemoryUsed())
                    .build();
        }

        TestCaseResult result = response.getTestCaseResults().get(0);
        String status = result.getStatus();
        String output = result.getOutput();
        String message = result.getMessage();
        Long timeUsed = result.getTimeUsed();
        Long memoryUsed = result.getMemoryUsed();

        if (timeUsed == null) timeUsed = response.getTimeUsed();
        if (memoryUsed == null) memoryUsed = response.getMemoryUsed();

        CodeRunVO.CodeRunVOBuilder builder = CodeRunVO.builder()
                .timeUsed(timeUsed)
                .memoryUsed(memoryUsed);

        if (JudgeInfoMessageEnum.COMPILE_ERROR.getValue().equals(status)) {
            String errMsg = (response.getMessage() != null ? response.getMessage() + "\n" : "")
                    + (message != null ? message : "");
            return builder.status(JudgeInfoMessageEnum.COMPILE_ERROR.getValue())
                    .message(errMsg.trim())
                    .build();
        }

        if (JudgeInfoMessageEnum.SYSTEM_ERROR.getValue().equals(status)) {
            return builder.status(status)
                    .message(message != null ? message : response.getMessage())
                    .build();
        }

        if (JudgeInfoMessageEnum.RUNTIME_ERROR.getValue().equals(status)
                || JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED.getValue().equals(status)
                || JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED.getValue().equals(status)) {
            return builder.status(status)
                    .output(output)
                    .message(message)
                    .build();
        }

        return builder.status(JudgeInfoMessageEnum.ACCEPTED.getValue())
                .output(output)
                .message(message)
                .build();
    }
}
