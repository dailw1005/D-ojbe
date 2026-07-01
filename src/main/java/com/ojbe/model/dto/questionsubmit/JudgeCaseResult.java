package com.ojbe.model.dto.questionsubmit;

import lombok.Data;

/**
 * 判题用例详细结果
 */
@Data
public class JudgeCaseResult {
    /**
     * 测试用例 ID
     */
    private Long testCaseId;
    
    /**
     * 该用例判题状态
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
