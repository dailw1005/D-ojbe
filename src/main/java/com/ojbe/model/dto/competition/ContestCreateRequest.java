package com.ojbe.model.dto.competition;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class ContestCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String title;

    private String description;

    private String type;

    private Date startTime;

    private Date endTime;

    private String password;

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
