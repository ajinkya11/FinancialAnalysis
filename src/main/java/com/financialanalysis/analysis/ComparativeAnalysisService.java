package com.financialanalysis.analysis;

import com.financialanalysis.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Service for performing comparative analysis between two companies
 */
@Slf4j
@Service
public class ComparativeAnalysisService {

    private static final int SCALE = 2;

    /**
     * Perform comprehensive comparative analysis
     */
    public ComparisonResult compareCompanies(CompanyFinancialData company1, CompanyFinancialData company2) {
        log.info("Comparing {} vs {}", company1.getCompanyName(), company2.getCompanyName());

        // Create metric comparisons
        Map<String, MetricComparison> comparisons = new LinkedHashMap<>();
        addFinancialComparisons(comparisons, company1, company2);
        addAirlineComparisons(comparisons, company1, company2);

        // Analyze strengths and weaknesses
        List<String> company1Strengths = identifyStrengths(company1, company2, comparisons);
        List<String> company1Weaknesses = identifyWeaknesses(company1, company2, comparisons);
        List<String> company2Strengths = identifyStrengths(company2, company1, comparisons);
        List<String> company2Weaknesses = identifyWeaknesses(company2, company1, comparisons);

        // Airline-specific comparison
        AirlineComparison airlineComp = compareAirlineMetrics(company1, company2);

        // Generate executive summary
        String execSummary = generateExecutiveSummary(company1, company2, comparisons);
        List<String> highlights = generateKeyHighlights(comparisons);
        List<String> redFlags = identifyRedFlags(company1, company2);

        // Generate recommendations
        String recommendation = generateRecommendation(company1, company2, comparisons);

        return ComparisonResult.builder()
                .company1(company1)
                .company2(company2)
                .metricComparisons(comparisons)
                .company1Strengths(company1Strengths)
                .company1Weaknesses(company1Weaknesses)
                .company2Strengths(company2Strengths)
                .company2Weaknesses(company2Weaknesses)
                .airlineComparison(airlineComp)
                .executiveSummary(execSummary)
                .keyHighlights(highlights)
                .redFlags(redFlags)
                .investmentRecommendation(recommendation)
                .build();
    }

    /**
     * Add financial metric comparisons
     */
    private void addFinancialComparisons(Map<String, MetricComparison> comparisons,
                                        CompanyFinancialData c1, CompanyFinancialData c2) {
        // Revenue comparison
        comparisons.put("Revenue", createComparison(
                "Total Revenue",
                c1.getLatestIncomeStatement().getTotalRevenue(),
                c2.getLatestIncomeStatement().getTotalRevenue(),
                true
        ));

        // Profitability comparisons
        comparisons.put("Operating Margin", createComparison(
                "Operating Margin",
                c1.getLatestFinancialMetrics().getOperatingMargin(),
                c2.getLatestFinancialMetrics().getOperatingMargin(),
                true
        ));

        comparisons.put("Net Margin", createComparison(
                "Net Margin",
                c1.getLatestFinancialMetrics().getNetMargin(),
                c2.getLatestFinancialMetrics().getNetMargin(),
                true
        ));

        comparisons.put("ROA", createComparison(
                "Return on Assets",
                c1.getLatestFinancialMetrics().getReturnOnAssets(),
                c2.getLatestFinancialMetrics().getReturnOnAssets(),
                true
        ));

        comparisons.put("ROE", createComparison(
                "Return on Equity",
                c1.getLatestFinancialMetrics().getReturnOnEquity(),
                c2.getLatestFinancialMetrics().getReturnOnEquity(),
                true
        ));

        // Liquidity comparisons
        comparisons.put("Current Ratio", createComparison(
                "Current Ratio",
                c1.getLatestFinancialMetrics().getCurrentRatio(),
                c2.getLatestFinancialMetrics().getCurrentRatio(),
                true
        ));

        comparisons.put("Free Cash Flow", createComparison(
                "Free Cash Flow",
                c1.getLatestFinancialMetrics().getFreeCashFlow(),
                c2.getLatestFinancialMetrics().getFreeCashFlow(),
                true
        ));

        // Leverage comparisons
        comparisons.put("Debt-to-Equity", createComparison(
                "Debt to Equity Ratio",
                c1.getLatestFinancialMetrics().getDebtToEquity(),
                c2.getLatestFinancialMetrics().getDebtToEquity(),
                false  // Lower is better
        ));
    }

    /**
     * Add airline-specific metric comparisons
     */
    private void addAirlineComparisons(Map<String, MetricComparison> comparisons,
                                      CompanyFinancialData c1, CompanyFinancialData c2) {
        AirlineOperatingMetrics m1 = c1.getLatestOperatingMetrics();
        AirlineOperatingMetrics m2 = c2.getLatestOperatingMetrics();

        if (m1 == null || m2 == null) {
            log.warn("Missing airline operating metrics for comparison");
            return;
        }

        comparisons.put("Load Factor", createComparison(
                "Load Factor",
                m1.getLoadFactor(),
                m2.getLoadFactor(),
                true
        ));

        comparisons.put("RASM", createComparison(
                "Revenue per ASM",
                m1.getRasm(),
                m2.getRasm(),
                true
        ));

        comparisons.put("CASM", createComparison(
                "Cost per ASM",
                m1.getCasm(),
                m2.getCasm(),
                false  // Lower is better
        ));

        comparisons.put("CASM-ex", createComparison(
                "CASM excluding fuel",
                m1.getCasmEx(),
                m2.getCasmEx(),
                false  // Lower is better
        ));

        comparisons.put("Yield", createComparison(
                "Passenger Yield",
                m1.getYield(),
                m2.getYield(),
                true
        ));
    }

    /**
     * Create a metric comparison
     */
    private MetricComparison createComparison(String name, Object value1, Object value2, boolean higherIsBetter) {
        if (value1 == null || value2 == null) {
            return MetricComparison.builder()
                    .metricName(name)
                    .company1Value(value1)
                    .company2Value(value2)
                    .winner("neutral")
                    .rating(MetricComparison.ComparisonRating.NEUTRAL)
                    .build();
        }

        double num1 = getNumericValue(value1);
        double num2 = getNumericValue(value2);

        double diff = Math.abs(num1 - num2);
        double diffPercentage = num2 != 0 ? (diff / Math.abs(num2)) * 100 : 0;

        String winner;
        MetricComparison.ComparisonRating rating;

        if (higherIsBetter) {
            if (num1 > num2) {
                winner = "company1";
                rating = getRating(diffPercentage);
            } else if (num2 > num1) {
                winner = "company2";
                rating = getRating(diffPercentage);
            } else {
                winner = "neutral";
                rating = MetricComparison.ComparisonRating.NEUTRAL;
            }
        } else {
            // Lower is better
            if (num1 < num2) {
                winner = "company1";
                rating = getRating(diffPercentage);
            } else if (num2 < num1) {
                winner = "company2";
                rating = getRating(diffPercentage);
            } else {
                winner = "neutral";
                rating = MetricComparison.ComparisonRating.NEUTRAL;
            }
        }

        return MetricComparison.builder()
                .metricName(name)
                .company1Value(value1)
                .company2Value(value2)
                .winner(winner)
                .differencePercentage(diffPercentage)
                .rating(rating)
                .build();
    }

    /**
     * Get numeric value from object
     */
    private double getNumericValue(Object value) {
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).doubleValue();
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0;
    }

    /**
     * Get rating based on percentage difference
     */
    private MetricComparison.ComparisonRating getRating(double diffPercentage) {
        if (diffPercentage >= 30) {
            return MetricComparison.ComparisonRating.SIGNIFICANTLY_BETTER;
        } else if (diffPercentage >= 15) {
            return MetricComparison.ComparisonRating.BETTER;
        } else if (diffPercentage >= 5) {
            return MetricComparison.ComparisonRating.SLIGHTLY_BETTER;
        } else {
            return MetricComparison.ComparisonRating.NEUTRAL;
        }
    }

    /**
     * Identify company strengths
     */
    private List<String> identifyStrengths(CompanyFinancialData company, CompanyFinancialData competitor,
                                          Map<String, MetricComparison> comparisons) {
        List<String> strengths = new ArrayList<>();

        for (Map.Entry<String, MetricComparison> entry : comparisons.entrySet()) {
            MetricComparison comp = entry.getValue();
            if ("company1".equals(comp.getWinner()) &&
                    (comp.getRating() == MetricComparison.ComparisonRating.BETTER ||
                            comp.getRating() == MetricComparison.ComparisonRating.SIGNIFICANTLY_BETTER)) {
                strengths.add(String.format("Superior %s: %.2f%% better than competitor",
                        comp.getMetricName(), comp.getDifferencePercentage()));
            }
        }

        return strengths;
    }

    /**
     * Identify company weaknesses
     */
    private List<String> identifyWeaknesses(CompanyFinancialData company, CompanyFinancialData competitor,
                                           Map<String, MetricComparison> comparisons) {
        List<String> weaknesses = new ArrayList<>();

        for (Map.Entry<String, MetricComparison> entry : comparisons.entrySet()) {
            MetricComparison comp = entry.getValue();
            if ("company2".equals(comp.getWinner()) &&
                    (comp.getRating() == MetricComparison.ComparisonRating.BETTER ||
                            comp.getRating() == MetricComparison.ComparisonRating.SIGNIFICANTLY_BETTER)) {
                weaknesses.add(String.format("Lower %s: %.2f%% behind competitor",
                        comp.getMetricName(), comp.getDifferencePercentage()));
            }
        }

        return weaknesses;
    }

    /**
     * Compare airline-specific metrics
     */
    private AirlineComparison compareAirlineMetrics(CompanyFinancialData c1, CompanyFinancialData c2) {
        AirlineOperatingMetrics m1 = c1.getLatestOperatingMetrics();
        AirlineOperatingMetrics m2 = c2.getLatestOperatingMetrics();

        if (m1 == null || m2 == null) {
            return null;
        }

        return AirlineComparison.builder()
                .casmLeader(compareBigDecimal(m1.getCasm(), m2.getCasm(), false) ?
                        c1.getTicker() : c2.getTicker())
                .rasmLeader(compareBigDecimal(m1.getRasm(), m2.getRasm(), true) ?
                        c1.getTicker() : c2.getTicker())
                .loadFactorLeader(compareDouble(m1.getLoadFactor(), m2.getLoadFactor()) ?
                        c1.getTicker() : c2.getTicker())
                .yieldLeader(compareBigDecimal(m1.getYield(), m2.getYield(), true) ?
                        c1.getTicker() : c2.getTicker())
                .build();
    }

    /**
     * Compare BigDecimal values
     */
    private boolean compareBigDecimal(BigDecimal v1, BigDecimal v2, boolean higherIsBetter) {
        if (v1 == null || v2 == null) return false;
        return higherIsBetter ? v1.compareTo(v2) > 0 : v1.compareTo(v2) < 0;
    }

    /**
     * Compare Double values
     */
    private boolean compareDouble(Double v1, Double v2) {
        if (v1 == null || v2 == null) return false;
        return v1 > v2;
    }

    /**
     * Generate executive summary
     */
    private String generateExecutiveSummary(CompanyFinancialData c1, CompanyFinancialData c2,
                                           Map<String, MetricComparison> comparisons) {
        int c1Wins = 0;
        int c2Wins = 0;

        for (MetricComparison comp : comparisons.values()) {
            if ("company1".equals(comp.getWinner())) c1Wins++;
            if ("company2".equals(comp.getWinner())) c2Wins++;
        }

        String leader = c1Wins > c2Wins ? c1.getCompanyName() : c2.getCompanyName();
        String laggard = c1Wins > c2Wins ? c2.getCompanyName() : c1.getCompanyName();

        return String.format("%s demonstrates stronger overall financial performance compared to %s, " +
                        "leading in %d out of %d key metrics analyzed.",
                leader, laggard, Math.max(c1Wins, c2Wins), comparisons.size());
    }

    /**
     * Generate key highlights
     */
    private List<String> generateKeyHighlights(Map<String, MetricComparison> comparisons) {
        List<String> highlights = new ArrayList<>();

        for (Map.Entry<String, MetricComparison> entry : comparisons.entrySet()) {
            MetricComparison comp = entry.getValue();
            if (comp.getRating() == MetricComparison.ComparisonRating.SIGNIFICANTLY_BETTER) {
                highlights.add(String.format("%s shows significantly better %s (%.2f%% difference)",
                        comp.getWinner().equals("company1") ? "Company 1" : "Company 2",
                        comp.getMetricName(),
                        comp.getDifferencePercentage()));
            }
        }

        return highlights;
    }

    /**
     * Identify red flags
     */
    private List<String> identifyRedFlags(CompanyFinancialData c1, CompanyFinancialData c2) {
        List<String> flags = new ArrayList<>();

        // Check negative net income
        if (c1.getLatestIncomeStatement().getNetIncome().compareTo(BigDecimal.ZERO) < 0) {
            flags.add(c1.getCompanyName() + " has negative net income");
        }
        if (c2.getLatestIncomeStatement().getNetIncome().compareTo(BigDecimal.ZERO) < 0) {
            flags.add(c2.getCompanyName() + " has negative net income");
        }

        // Check high debt levels
        if (c1.getLatestFinancialMetrics().getDebtToEquity() != null &&
                c1.getLatestFinancialMetrics().getDebtToEquity() > 2.0) {
            flags.add(c1.getCompanyName() + " has high debt-to-equity ratio");
        }
        if (c2.getLatestFinancialMetrics().getDebtToEquity() != null &&
                c2.getLatestFinancialMetrics().getDebtToEquity() > 2.0) {
            flags.add(c2.getCompanyName() + " has high debt-to-equity ratio");
        }

        return flags;
    }

    /**
     * Generate investment recommendation
     */
    private String generateRecommendation(CompanyFinancialData c1, CompanyFinancialData c2,
                                         Map<String, MetricComparison> comparisons) {
        int c1Wins = 0;
        int c2Wins = 0;

        for (MetricComparison comp : comparisons.values()) {
            if ("company1".equals(comp.getWinner())) c1Wins++;
            if ("company2".equals(comp.getWinner())) c2Wins++;
        }

        if (Math.abs(c1Wins - c2Wins) <= 2) {
            return "Both companies show comparable financial performance. Investment decision should " +
                    "consider additional factors such as growth strategy, market position, and management quality.";
        } else if (c1Wins > c2Wins) {
            return String.format("%s appears to be the stronger investment candidate based on superior " +
                    "financial metrics and operational efficiency.", c1.getCompanyName());
        } else {
            return String.format("%s appears to be the stronger investment candidate based on superior " +
                    "financial metrics and operational efficiency.", c2.getCompanyName());
        }
    }
}
