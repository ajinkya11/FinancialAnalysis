package com.financialanalysis.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Results of comparative analysis between two companies
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonResult {
    private CompanyFinancialData company1;
    private CompanyFinancialData company2;

    // Executive summary
    private String executiveSummary;
    private List<String> keyHighlights;
    private List<String> redFlags;

    // Comparative metrics
    private Map<String, MetricComparison> metricComparisons;

    // Strengths and weaknesses
    private List<String> company1Strengths;
    private List<String> company1Weaknesses;
    private List<String> company2Strengths;
    private List<String> company2Weaknesses;

    // Airline-specific comparison
    private AirlineComparison airlineComparison;

    // Recommendations
    private String investmentRecommendation;
    private List<String> recommendations;

    // Trend analysis
    private TrendAnalysis trendAnalysis;
}
