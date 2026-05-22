package com.dailw.judge.sandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 详细用例判题结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseResult {
    /**
     * 测试用例 ID / 序号
     */
    private Long testCaseId;
    
    /**
     * 判题状态
     */
    private String status;
    
    /**
     * 消耗时间（ms）
     */
    private Long timeUsed;
    
    /**
     * 消耗内存（字节）
     */
    private Long memoryUsed;
    
    /**
     * 实际输出
     */
    private String output;
    
    /**
     * 错误信息
     */
    private String message;
}
