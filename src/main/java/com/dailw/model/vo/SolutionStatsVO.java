package com.dailw.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 题解统计数据 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolutionStatsVO implements Serializable {

    private long solutionCount;
    private long thumbCount;
    private long viewCount;

    private static final long serialVersionUID = 1L;
}
