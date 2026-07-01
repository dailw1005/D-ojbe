package com.ojbe.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuestionInfoVO {

    private long total;

    private long easyCount;

    private long mediumCount;

    private long hardCount;

    private BigDecimal passRate;

    private long submitCount;
}
