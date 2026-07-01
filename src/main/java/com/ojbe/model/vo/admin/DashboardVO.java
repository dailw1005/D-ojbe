package com.ojbe.model.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class DashboardVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Overview overview;

    private List<TrendPoint> submissionTrend;

    private Map<String, Long> submissionStatusDist;

    private List<TrendPoint> userTrend;

    private Map<String, Long> userRoleDist;

    @Data
    public static class Overview implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private long totalUsers;
        private long totalQuestions;
        private long totalSubmissions;
        private long totalSolutions;
        private long todayNewUsers;
        private long todaySubmissions;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TrendPoint implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String date;
        private Long count;
    }
}
