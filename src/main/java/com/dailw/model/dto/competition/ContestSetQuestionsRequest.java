package com.dailw.model.dto.competition;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ContestSetQuestionsRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long contestId;

    private List<QuestionItem> questions;

    @Data
    public static class QuestionItem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private Long questionId;
        private Integer displayOrder;
        private Integer score;
    }
}
