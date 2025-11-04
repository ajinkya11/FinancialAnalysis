package com.financialanalysis.reporting;

import com.financialanalysis.models.*;
import lombok.extern.slf4j.Slf4j;
import org.fusesource.jansi.Ansi;
import org.springframework.stereotype.Service;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Generate colored console reports for comparative analysis
 */
@Slf4j
@Service
public class ConsoleReportGenerator {

    private static final int COLUMN_WIDTH = 25;

    /**
     * Generate comprehensive console report
     */
    public void generateReport(ComparisonResult result) {
        printHeader("AIRLINE FINANCIAL ANALYSIS COMPARATIVE REPORT");
        printDivider();

        printExecutiveSummary(result);
        printDivider();

        printCompanyOverview(result);
        printDivider();

        printFinancialComparison(result);
        printDivider();

        printAirlineMetricsComparison(result);
        printDivider();

        printStrengthsAndWeaknesses(result);
        printDivider();

        printRecommendations(result);
        printDivider();
    }

    /**
     * Print executive summary
     */
    private void printExecutiveSummary(ComparisonResult result) {
        printHeader("EXECUTIVE SUMMARY");
        System.out.println(result.getExecutiveSummary());
        System.out.println();

        if (!result.getKeyHighlights().isEmpty()) {
            printSubHeader("Key Highlights:");
            for (String highlight : result.getKeyHighlights()) {
                System.out.println(success("  ✓ " + highlight));
            }
            System.out.println();
        }

        if (!result.getRedFlags().isEmpty()) {
            printSubHeader("Red Flags:");
            for (String flag : result.getRedFlags()) {
                System.out.println(error("  ⚠ " + flag));
            }
            System.out.println();
        }
    }

    /**
     * Print company overview
     */
    private void printCompanyOverview(ComparisonResult result) {
        printHeader("COMPANY OVERVIEW");

        CompanyFinancialData c1 = result.getCompany1();
        CompanyFinancialData c2 = result.getCompany2();

        printRow("", c1.getCompanyName() + " (" + c1.getTicker() + ")",
                c2.getCompanyName() + " (" + c2.getTicker() + ")");
        printRow("Fiscal Year", c1.getFiscalYearEnd(), c2.getFiscalYearEnd());
        printRow("Filing Date", c1.getFilingDate() != null ? c1.getFilingDate() : "N/A",
                c2.getFilingDate() != null ? c2.getFilingDate() : "N/A");
    }

    /**
     * Print financial comparison
     */
    private void printFinancialComparison(ComparisonResult result) {
        printHeader("FINANCIAL METRICS COMPARISON");

        printRow("Metric", result.getCompany1().getTicker(), result.getCompany2().getTicker());

        String[] financialMetrics = {
                "Revenue", "Operating Margin", "Net Margin", "ROA", "ROE",
                "Current Ratio", "Debt-to-Equity", "Free Cash Flow"
        };

        for (String metric : financialMetrics) {
            MetricComparison comp = result.getMetricComparisons().get(metric);
            if (comp != null) {
                printMetricRow(comp);
            }
        }
    }

    /**
     * Print airline metrics comparison
     */
    private void printAirlineMetricsComparison(ComparisonResult result) {
        printHeader("AIRLINE OPERATING METRICS COMPARISON");

        printRow("Metric", result.getCompany1().getTicker(), result.getCompany2().getTicker());

        String[] airlineMetrics = {
                "Load Factor", "RASM", "CASM", "CASM-ex", "Yield"
        };

        for (String metric : airlineMetrics) {
            MetricComparison comp = result.getMetricComparisons().get(metric);
            if (comp != null) {
                printMetricRow(comp);
            }
        }

        // Print airline comparison summary
        if (result.getAirlineComparison() != null) {
            System.out.println();
            printSubHeader("Operational Efficiency Leaders:");
            AirlineComparison ac = result.getAirlineComparison();
            System.out.println(String.format("  Cost Efficiency (CASM): %s", success(ac.getCasmLeader())));
            System.out.println(String.format("  Revenue Generation (RASM): %s", success(ac.getRasmLeader())));
            System.out.println(String.format("  Load Factor: %s", success(ac.getLoadFactorLeader())));
            System.out.println(String.format("  Passenger Yield: %s", success(ac.getYieldLeader())));
        }
    }

    /**
     * Print strengths and weaknesses
     */
    private void printStrengthsAndWeaknesses(ComparisonResult result) {
        printHeader("STRENGTHS & WEAKNESSES ANALYSIS");

        // Company 1
        printSubHeader(result.getCompany1().getCompanyName() + " (" + result.getCompany1().getTicker() + ")");
        System.out.println(bold("Strengths:"));
        if (result.getCompany1Strengths().isEmpty()) {
            System.out.println("  No significant strengths identified");
        } else {
            for (String strength : result.getCompany1Strengths()) {
                System.out.println(success("  + " + strength));
            }
        }

        System.out.println(bold("Weaknesses:"));
        if (result.getCompany1Weaknesses().isEmpty()) {
            System.out.println("  No significant weaknesses identified");
        } else {
            for (String weakness : result.getCompany1Weaknesses()) {
                System.out.println(error("  - " + weakness));
            }
        }

        System.out.println();

        // Company 2
        printSubHeader(result.getCompany2().getCompanyName() + " (" + result.getCompany2().getTicker() + ")");
        System.out.println(bold("Strengths:"));
        if (result.getCompany2Strengths().isEmpty()) {
            System.out.println("  No significant strengths identified");
        } else {
            for (String strength : result.getCompany2Strengths()) {
                System.out.println(success("  + " + strength));
            }
        }

        System.out.println(bold("Weaknesses:"));
        if (result.getCompany2Weaknesses().isEmpty()) {
            System.out.println("  No significant weaknesses identified");
        } else {
            for (String weakness : result.getCompany2Weaknesses()) {
                System.out.println(error("  - " + weakness));
            }
        }
    }

    /**
     * Print recommendations
     */
    private void printRecommendations(ComparisonResult result) {
        printHeader("INVESTMENT RECOMMENDATION");
        System.out.println(bold(result.getInvestmentRecommendation()));
    }

    /**
     * Print metric row with coloring
     */
    private void printMetricRow(MetricComparison comp) {
        String value1 = formatValue(comp.getCompany1Value());
        String value2 = formatValue(comp.getCompany2Value());

        // Add coloring based on winner
        if ("company1".equals(comp.getWinner())) {
            value1 = success(value1);
            value2 = neutral(value2);
        } else if ("company2".equals(comp.getWinner())) {
            value1 = neutral(value1);
            value2 = success(value2);
        }

        printRow(comp.getMetricName(), value1, value2);
    }

    /**
     * Format value for display
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "N/A";
        }
        if (value instanceof java.math.BigDecimal) {
            return String.format("$%.2fM", ((java.math.BigDecimal) value).divide(
                    java.math.BigDecimal.valueOf(1000000), 2, java.math.RoundingMode.HALF_UP).doubleValue());
        }
        if (value instanceof Double) {
            return String.format("%.2f%%", value);
        }
        return value.toString();
    }

    /**
     * Print row with three columns
     */
    private void printRow(String col1, String col2, String col3) {
        System.out.println(String.format("%-" + COLUMN_WIDTH + "s %-" + COLUMN_WIDTH + "s %-" + COLUMN_WIDTH + "s",
                col1, col2, col3));
    }

    /**
     * Print header
     */
    private void printHeader(String text) {
        System.out.println(ansi().bold().fg(Ansi.Color.CYAN).a("\n" + text).reset());
    }

    /**
     * Print subheader
     */
    private void printSubHeader(String text) {
        System.out.println(ansi().bold().a("\n" + text).reset());
    }

    /**
     * Print divider
     */
    private void printDivider() {
        System.out.println(ansi().fg(Ansi.Color.BLUE).a("=".repeat(80)).reset());
    }

    /**
     * Success text (green)
     */
    private String success(String text) {
        return ansi().fg(Ansi.Color.GREEN).a(text).reset().toString();
    }

    /**
     * Error text (red)
     */
    private String error(String text) {
        return ansi().fg(Ansi.Color.RED).a(text).reset().toString();
    }

    /**
     * Neutral text
     */
    private String neutral(String text) {
        return text;
    }

    /**
     * Bold text
     */
    private String bold(String text) {
        return ansi().bold().a(text).reset().toString();
    }
}
