package com.dailw.judge.sandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 判题用例
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JudgeTestCase {
    /**
     * 输入用例
     */
    private String input;

    /**
     * 预期输出
     */
    private String expectedOutput;
}
