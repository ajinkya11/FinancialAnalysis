package com.financialanalysis.analyzer;

import com.financialanalysis.model.Company;
import com.financialanalysis.model.FinancialMetrics;
import com.financialanalysis.model.FinancialStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculates financial metrics from financial statements
 */
public class MetricsCalculator {
    private static final Logger logger = LoggerFactory.getLogger(MetricsCalculator.class);

    /**
     * Calculates metrics for all years of financial data for a company
     *
     * @param company the company with financial statements
     * @return list of calculated metrics for each year
     */
    public List<FinancialMetrics> calculateMetrics(Company company) {
        logger.info("Calculating metrics for company: {}", company.getName());

        List<FinancialStatement> statements = company.getFinancialStatements();
        if (statements == null || statements.isEmpty()) {
            logger.warn("No financial statements found for {}", company.getName());
            return new ArrayList<>();
        }

        // Sort statements by fiscal year
        statements.sort((a, b) -> Integer.compare(a.getFiscalYear(), b.getFiscalYear()));

        List<FinancialMetrics> metricsList = new ArrayList<>();

        for (int i = 0; i < statements.size(); i++) {
            FinancialStatement current = statements.get(i);
            FinancialStatement previous = (i > 0) ? statements.get(i - 1) : null;

            FinancialMetrics metrics = calculateMetrics(current, previous, company.getName());
            metricsList.add(metrics);
        }

        logger.info("Calculated metrics for {} years", metricsList.size());
        return metricsList;
    }

    /**
     * Calculates metrics for a single year
     *
     * @param current the current year's financial statement
     * @param previous the previous year's financial statement (for growth calculations)
     * @param companyName the company name
     * @return calculated financial metrics
     */
    public FinancialMetrics calculateMetrics(FinancialStatement current, FinancialStatement previous, String companyName) {
        FinancialMetrics metrics = new FinancialMetrics(current.getFiscalYear(), companyName);

        // Profitability Metrics
        metrics.setGrossMargin(calculateMargin(current.getGrossProfit(), current.getRevenue()));
        metrics.setOperatingMargin(calculateMargin(current.getOperatingIncome(), current.getRevenue()));
        metrics.setNetMargin(calculateMargin(current.getNetIncome(), current.getRevenue()));
        metrics.setEbitdaMargin(calculateMargin(current.getEbitda(), current.getRevenue()));

        // Revenue Growth Rate (requires previous year)
        if (previous != null && previous.getRevenue() > 0) {
            double growthRate = (current.getRevenue() - previous.getRevenue()) / previous.getRevenue();
            metrics.setRevenueGrowthRate(growthRate);
        }

        // Efficiency Metrics
        metrics.setReturnOnAssets(calculateRatio(current.getNetIncome(), current.getTotalAssets()));
        metrics.setReturnOnEquity(calculateRatio(current.getNetIncome(), current.getShareholderEquity()));
        metrics.setAssetTurnover(calculateRatio(current.getRevenue(), current.getTotalAssets()));

        // Liquidity Metrics
        metrics.setCurrentRatio(calculateRatio(current.getCurrentAssets(), current.getCurrentLiabilities()));

        double quickAssets = current.getCash() + current.getMarketableSecurities() + current.getAccountsReceivable();
        metrics.setQuickRatio(calculateRatio(quickAssets, current.getCurrentLiabilities()));

        // Solvency/Leverage Metrics
        metrics.setDebtToEquity(calculateRatio(current.getTotalDebt(), current.getShareholderEquity()));
        metrics.setDebtToAssets(calculateRatio(current.getTotalDebt(), current.getTotalAssets()));

        if (current.getInterestExpense() > 0) {
            metrics.setInterestCoverage(calculateRatio(current.getEbit(), current.getInterestExpense()));
        }

        // Cash Flow Metrics
        metrics.setFreeCashFlowMargin(calculateMargin(current.getFreeCashFlow(), current.getRevenue()));
        metrics.setCapexToRevenue(calculateMargin(Math.abs(current.getCapitalExpenditures()), current.getRevenue()));

        return metrics;
    }

    /**
     * Helper method to calculate a margin (numerator / denominator)
     * Returns 0 if denominator is 0 or negative
     */
    private double calculateMargin(double numerator, double denominator) {
        if (denominator <= 0) {
            return 0;
        }
        return numerator / denominator;
    }

    /**
     * Helper method to calculate a ratio (numerator / denominator)
     * Returns 0 if denominator is 0 or negative
     */
    private double calculateRatio(double numerator, double denominator) {
        if (denominator <= 0) {
            return 0;
        }
        return numerator / denominator;
    }

    /**
     * Formats a percentage value
     */
    public static String formatPercentage(double value) {
        return String.format("%.2f%%", value * 100);
    }

    /**
     * Formats a ratio value
     */
    public static String formatRatio(double value) {
        return String.format("%.2f", value);
    }

    /**
     * Formats a currency value
     */
    public static String formatCurrency(double value) {
        if (value >= 1_000_000_000) {
            return String.format("$%.2fB", value / 1_000_000_000);
        } else if (value >= 1_000_000) {
            return String.format("$%.2fM", value / 1_000_000);
        } else if (value >= 1_000) {
            return String.format("$%.2fK", value / 1_000);
        } else {
            return String.format("$%.2f", value);
        }
    }
}
