package com.ojbe.judge.sandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 远程代码沙箱执行请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeRequest {
    /**
     * 提交标识
     */
    private String id;

    /**
     * 编程语言 (c, cpp, java, python, go)
     */
    private String language;

    /**
     * 源代码
     */
    private String sourceCode;

    /**
     * 测试用例
     */
    private List<JudgeTestCase> testCases;

    /**
     * 时间限制 (ms)
     */
    private Long timeLimit;

    /**
     * 内存限制 (bytes)
     */
    private Long memoryLimit;
}
