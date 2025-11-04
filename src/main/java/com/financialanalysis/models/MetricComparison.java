package com.financialanalysis.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Comparison of a specific metric between two companies
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricComparison {
    private String metricName;
    private Object company1Value;
    private Object company2Value;
    private String winner;  // "company1", "company2", or "neutral"
    private Double differencePercentage;
    private String interpretation;
    private ComparisonRating rating;

    public enum ComparisonRating {
        SIGNIFICANTLY_BETTER,
        BETTER,
        SLIGHTLY_BETTER,
        NEUTRAL,
        SLIGHTLY_WORSE,
        WORSE,
        SIGNIFICANTLY_WORSE
    }
}
