package com.dailw.model.dto.questionsubmit;

import lombok.Data;
import java.util.List;

/**
 * 判题详细信息
 */
@Data
public class JudgeInfo {

    /**
     * 程序执行信息 (错误信息等)
     */
    private String message;

    /**
     * 消耗时间（ms）
     */
    private Long timeUsed;

    /**
     * 消耗内存（字节）
     */
    private Long memoryUsed;

    /**
     * 判题状态
     */
    private String status;

    /**
     * 详细用例结果
     */
    private List<JudgeCaseResult> testCaseResults;
}
