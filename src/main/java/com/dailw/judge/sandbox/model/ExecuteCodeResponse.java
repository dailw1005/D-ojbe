package com.dailw.judge.sandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 远程代码沙箱执行响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeResponse {
    /**
     * 提交标识
     */
    private String submissionId;

    /**
     * 沙箱返回状态 (ACCEPTED, WRONG_ANSWER, TIME_LIMIT_EXCEEDED 等)
     */
    private String status;

    /**
     * 总体消耗时间（ms）
     */
    private Long timeUsed;

    /**
     * 总体消耗内存（字节）
     */
    private Long memoryUsed;

    /**
     * 总体错误信息 (编译错误等)
     */
    private String message;

    /**
     * 每个测试用例的详细执行结果
     */
    private List<TestCaseResult> testCaseResults;
}
