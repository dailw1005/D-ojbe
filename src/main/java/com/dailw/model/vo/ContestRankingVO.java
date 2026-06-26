package com.dailw.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ContestRankingVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long totalParticipants;

    private List<RankItem> rankings;

    private RankItem myRank;

    @Data
    public static class RankItem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private Integer rank;
        private Long userId;
        private String username;
        private String avatar;
        private Integer solvedCount;
        private Long penalty;
        private Integer totalScore;
        private List<ProblemResult> problemResults;
    }

    @Data
    public static class ProblemResult implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private Long questionId;
        private Integer tryCount;
        private Boolean solved;
        private Long penaltyTime;
        private Integer score;
    }
}
