package com.dailw.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ContestDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private String description;

    private String type;

    private String startTime;

    private String endTime;

    private String status;

    private Boolean hasPassword;

    private Integer participantCount;

    private Boolean isRegistered;

    private List<QuestionItem> questions;

    private UserVO creator;

    @Data
    public static class QuestionItem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private Long questionId;
        private String title;
        private String difficulty;
        private Integer displayOrder;
        private Integer score;
        private String status;
    }
}
