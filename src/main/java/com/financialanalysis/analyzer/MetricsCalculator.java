package com.financialanalysis.analyzer;

import com.financialanalysis.model.Company;
import com.financialanalysis.model.FinancialMetrics;
import com.financialanalysis.model.FinancialStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive financial metrics calculator for investment analysis
 */
public class MetricsCalculator {
    private static final Logger logger = LoggerFactory.getLogger(MetricsCalculator.class);
    private static final int DAYS_IN_YEAR = 365;

    /**
     * Calculates comprehensive metrics for all years of financial data
     */
    public List<FinancialMetrics> calculateMetrics(Company company) {
        logger.info("Calculating comprehensive metrics for company: {}", company.getName());

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
     * Calculates all metrics for a single year
     */
    public FinancialMetrics calculateMetrics(FinancialStatement current, FinancialStatement previous, String companyName) {
        FinancialMetrics metrics = new FinancialMetrics(current.getFiscalYear(), companyName);

        // Calculate each category of metrics
        calculateProfitabilityMetrics(metrics, current);
        calculateLiquidityMetrics(metrics, current);
        calculateSolvencyMetrics(metrics, current);
        calculateEfficiencyMetrics(metrics, current, previous);
        calculateGrowthMetrics(metrics, current, previous);
        calculateCashFlowMetrics(metrics, current);
        calculateBalanceSheetQualityMetrics(metrics, current);
        calculatePerShareMetrics(metrics, current);

        return metrics;
    }

    /**
     * PROFITABILITY METRICS
     */
    private void calculateProfitabilityMetrics(FinancialMetrics metrics, FinancialStatement stmt) {
        double revenue = stmt.getRevenue();

        // Margins
        metrics.setGrossMargin(calculateMargin(stmt.getGrossProfit(), revenue));
        metrics.setOperatingMargin(calculateMargin(stmt.getOperatingIncome(), revenue));
        metrics.setNetMargin(calculateMargin(stmt.getNetIncome(), revenue));
        metrics.setEbitdaMargin(calculateMargin(stmt.getEbitda(), revenue));

        // Return metrics
        metrics.setReturnOnAssets(calculateRatio(stmt.getNetIncome(), stmt.getTotalAssets()));
        metrics.setReturnOnEquity(calculateRatio(stmt.getNetIncome(), stmt.getShareholderEquity()));

        // ROIC = NOPAT / (Debt + Equity)
        // NOPAT ≈ Operating Income * (1 - tax rate), simplified as Operating Income for now
        double investedCapital = stmt.getTotalDebt() + stmt.getShareholderEquity();
        metrics.setReturnOnInvestedCapital(calculateRatio(stmt.getOperatingIncome(), investedCapital));

        // ROCE = EBIT / Capital Employed (Total Assets - Current Liabilities)
        double capitalEmployed = stmt.getTotalAssets() - stmt.getCurrentLiabilities();
        metrics.setReturnOnCapitalEmployed(calculateRatio(stmt.getEbit(), capitalEmployed));
    }

    /**
     * LIQUIDITY METRICS
     */
    private void calculateLiquidityMetrics(FinancialMetrics metrics, FinancialStatement stmt) {
        double currentLiabilities = stmt.getCurrentLiabilities();

        // Basic liquidity ratios
        metrics.setCurrentRatio(calculateRatio(stmt.getCurrentAssets(), currentLiabilities));

        double quickAssets = stmt.getCash() + stmt.getMarketableSecurities() + stmt.getAccountsReceivable();
        metrics.setQuickRatio(calculateRatio(quickAssets, currentLiabilities));

        metrics.setCashRatio(calculateRatio(stmt.getCash(), currentLiabilities));
        metrics.setOperatingCashFlowRatio(calculateRatio(stmt.getOperatingCashFlow(), currentLiabilities));

        // Working Capital Cycle
        // DSO = (Accounts Receivable / Revenue) * 365
        metrics.setDaysSalesOutstanding(
            calculateRatio(stmt.getAccountsReceivable(), stmt.getRevenue()) * DAYS_IN_YEAR
        );

        // DIO = (Inventory / COGS) * 365
        metrics.setDaysInventoryOutstanding(
            calculateRatio(stmt.getInventory(), stmt.getCostOfGoodsSold()) * DAYS_IN_YEAR
        );

        // DPO = (Accounts Payable / COGS) * 365
        // Note: We don't have accounts payable in the model, so this will be 0
        metrics.setDaysPayableOutstanding(0);  // TODO: Add accounts payable to FinancialStatement

        // CCC = DSO + DIO - DPO
        metrics.setCashConversionCycle(
            metrics.getDaysSalesOutstanding() +
            metrics.getDaysInventoryOutstanding() -
            metrics.getDaysPayableOutstanding()
        );
    }

    /**
     * SOLVENCY/LEVERAGE METRICS
     */
    private void calculateSolvencyMetrics(FinancialMetrics metrics, FinancialStatement stmt) {
        metrics.setDebtToEquity(calculateRatio(stmt.getTotalDebt(), stmt.getShareholderEquity()));
        metrics.setDebtToAssets(calculateRatio(stmt.getTotalDebt(), stmt.getTotalAssets()));

        // Interest Coverage
        if (stmt.getInterestExpense() > 0) {
            metrics.setInterestCoverage(calculateRatio(stmt.getEbit(), stmt.getInterestExpense()));
            metrics.setEbitdaCoverage(calculateRatio(stmt.getEbitda(), stmt.getInterestExpense()));
        }

        // Debt Service Coverage = Operating Cash Flow / Total Debt Service
        // Simplified as OCF / (Interest + Principal), assuming interest as proxy
        if (stmt.getInterestExpense() > 0) {
            metrics.setDebtServiceCoverage(
                calculateRatio(stmt.getOperatingCashFlow(), stmt.getInterestExpense())
            );
        }
    }

    /**
     * EFFICIENCY METRICS
     */
    private void calculateEfficiencyMetrics(FinancialMetrics metrics, FinancialStatement current, FinancialStatement previous) {
        double revenue = current.getRevenue();
        double cogs = current.getCostOfGoodsSold();

        // Asset turnover ratios
        metrics.setAssetTurnover(calculateRatio(revenue, current.getTotalAssets()));

        double fixedAssets = current.getTotalAssets() - current.getCurrentAssets();
        metrics.setFixedAssetTurnover(calculateRatio(revenue, fixedAssets));

        double workingCapital = current.getCurrentAssets() - current.getCurrentLiabilities();
        metrics.setWorkingCapitalTurnover(calculateRatio(revenue, workingCapital));

        // Turnover ratios (using current period values as proxy for average)
        // Ideally would use (Beginning + Ending) / 2
        metrics.setInventoryTurnover(calculateRatio(cogs, current.getInventory()));
        metrics.setReceivablesTurnover(calculateRatio(revenue, current.getAccountsReceivable()));
        // Payables turnover requires accounts payable
        metrics.setPayablesTurnover(0);  // TODO: Add accounts payable

        // Operating efficiency
        double operatingExpenses = revenue - current.getOperatingIncome();
        metrics.setOperatingExpenseRatio(calculateMargin(operatingExpenses, revenue));

        // SG&A - not directly available, using operating expenses as proxy
        metrics.setSgaToRevenue(metrics.getOperatingExpenseRatio());

        // CapEx metrics
        metrics.setCapexToRevenue(calculateMargin(Math.abs(current.getCapitalExpenditures()), revenue));

        // CapEx to Depreciation - would need depreciation in the model
        metrics.setCapexToDepreciation(0);  // TODO: Add depreciation to FinancialStatement
    }

    /**
     * GROWTH METRICS
     */
    private void calculateGrowthMetrics(FinancialMetrics metrics, FinancialStatement current, FinancialStatement previous) {
        if (previous == null) {
            return;  // Cannot calculate growth for first year
        }

        // Revenue growth
        metrics.setRevenueGrowthRate(
            calculateGrowthRate(current.getRevenue(), previous.getRevenue())
        );

        // Operating income growth
        metrics.setOperatingIncomeGrowthRate(
            calculateGrowthRate(current.getOperatingIncome(), previous.getOperatingIncome())
        );

        // Net income growth
        metrics.setNetIncomeGrowthRate(
            calculateGrowthRate(current.getNetIncome(), previous.getNetIncome())
        );

        // EBITDA growth
        metrics.setEbitdaGrowthRate(
            calculateGrowthRate(current.getEbitda(), previous.getEbitda())
        );

        // EPS growth
        metrics.setEpsGrowthRate(
            calculateGrowthRate(current.getEarningsPerShare(), previous.getEarningsPerShare())
        );

        // FCF growth
        metrics.setFreeCashFlowGrowthRate(
            calculateGrowthRate(current.getFreeCashFlow(), previous.getFreeCashFlow())
        );
    }

    /**
     * CASH FLOW METRICS
     */
    private void calculateCashFlowMetrics(FinancialMetrics metrics, FinancialStatement stmt) {
        double revenue = stmt.getRevenue();
        double operatingCF = stmt.getOperatingCashFlow();
        double netIncome = stmt.getNetIncome();

        // Margins
        metrics.setFreeCashFlowMargin(calculateMargin(stmt.getFreeCashFlow(), revenue));
        metrics.setOperatingCashFlowMargin(calculateMargin(operatingCF, revenue));

        // Quality of earnings
        metrics.setCashFlowToNetIncome(calculateRatio(operatingCF, netIncome));
        metrics.setFreeCashFlowToNetIncome(calculateRatio(stmt.getFreeCashFlow(), netIncome));

        // CapEx to OCF
        if (operatingCF > 0) {
            metrics.setCapexToOperatingCashFlow(
                calculateRatio(Math.abs(stmt.getCapitalExpenditures()), operatingCF)
            );
        }

        // Cash flow returns
        metrics.setCashFlowReturnOnAssets(calculateRatio(operatingCF, stmt.getTotalAssets()));
        metrics.setCashFlowReturnOnEquity(calculateRatio(operatingCF, stmt.getShareholderEquity()));

        // FCF yield requires market cap
        metrics.setFreeCashFlowYield(0);  // TODO: Add market data support
    }

    /**
     * BALANCE SHEET QUALITY METRICS
     */
    private void calculateBalanceSheetQualityMetrics(FinancialMetrics metrics, FinancialStatement stmt) {
        // Intangibles to total assets - would need goodwill/intangibles in model
        metrics.setIntangibleAssetsToTotalAssets(0);  // TODO: Add intangibles to FinancialStatement

        // Working capital ratio
        double workingCapital = stmt.getCurrentAssets() - stmt.getCurrentLiabilities();
        metrics.setWorkingCapitalRatio(calculateRatio(workingCapital, stmt.getTotalAssets()));

        // Equity multiplier (leverage indicator)
        metrics.setEquityMultiplier(calculateRatio(stmt.getTotalAssets(), stmt.getShareholderEquity()));
    }

    /**
     * PER-SHARE METRICS
     */
    private void calculatePerShareMetrics(FinancialMetrics metrics, FinancialStatement stmt) {
        long shares = stmt.getSharesOutstanding();

        if (shares > 0) {
            metrics.setEarningsPerShare(stmt.getNetIncome() / shares);
            metrics.setBookValuePerShare(stmt.getShareholderEquity() / shares);
            metrics.setFreeCashFlowPerShare(stmt.getFreeCashFlow() / shares);
            metrics.setRevenuePerShare(stmt.getRevenue() / shares);
        } else {
            // Use values from statement if shares are not available
            metrics.setEarningsPerShare(stmt.getEarningsPerShare());
            metrics.setBookValuePerShare(stmt.getBookValuePerShare());
        }
    }

    /**
     * Helper method to calculate margin (numerator / denominator)
     */
    private double calculateMargin(double numerator, double denominator) {
        if (denominator <= 0) {
            return 0;
        }
        return numerator / denominator;
    }

    /**
     * Helper method to calculate ratio (numerator / denominator)
     */
    private double calculateRatio(double numerator, double denominator) {
        if (denominator <= 0) {
            return 0;
        }
        return numerator / denominator;
    }

    /**
     * Helper method to calculate growth rate
     */
    private double calculateGrowthRate(double current, double previous) {
        if (previous <= 0) {
            return 0;
        }
        return (current - previous) / previous;
    }

    // Formatting helpers
    public static String formatPercentage(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "N/A";
        }
        return String.format("%.2f%%", value * 100);
    }

    public static String formatRatio(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "N/A";
        }
        return String.format("%.2f", value);
    }

    public static String formatDays(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value == 0) {
            return "N/A";
        }
        return String.format("%.0f days", value);
    }

    public static String formatCurrency(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "N/A";
        }
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
