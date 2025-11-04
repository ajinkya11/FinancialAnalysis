package com.financialanalysis.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Trend analysis over multiple years
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendAnalysis {
    private Map<String, TrendData> company1Trends;
    private Map<String, TrendData> company2Trends;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        private String metricName;
        private Double[] values;  // Values over years
        private String[] years;
        private Double averageGrowth;
        private String trend;  // "improving", "declining", "stable", "volatile"
        private Double volatility;
    }
}
