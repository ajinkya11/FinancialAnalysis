package com.financialanalysis.metrics;

import com.financialanalysis.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculator for comprehensive financial metrics and ratios
 */
@Slf4j
@Service
public class FinancialMetricsCalculator {

    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    /**
     * Calculate all financial metrics for a company
     */
    public FinancialMetrics calculateMetrics(IncomeStatement income, BalanceSheet balance,
                                            CashFlowStatement cashFlow, BalanceSheet previousBalance) {
        log.info("Calculating financial metrics for fiscal year: {}", income.getFiscalYear());

        return FinancialMetrics.builder()
                .fiscalYear(income.getFiscalYear())
                // Profitability ratios
                .returnOnAssets(calculateROA(income.getNetIncome(), balance.getTotalAssets()))
                .returnOnEquity(calculateROE(income.getNetIncome(), balance.getTotalEquity()))
                .returnOnInvestedCapital(calculateROIC(income, balance))
                .grossMargin(calculateMargin(income.getGrossProfit(), income.getTotalRevenue()))
                .operatingMargin(calculateMargin(income.getOperatingIncome(), income.getTotalRevenue()))
                .netMargin(calculateMargin(income.getNetIncome(), income.getTotalRevenue()))
                .ebitdaMargin(calculateMargin(income.getEbitda(), income.getTotalRevenue()))
                // Liquidity ratios
                .currentRatio(calculateRatio(balance.getCurrentAssets(), balance.getCurrentLiabilities()))
                .quickRatio(calculateQuickRatio(balance))
                .cashRatio(calculateRatio(balance.getCashAndEquivalents(), balance.getCurrentLiabilities()))
                .workingCapital(calculateWorkingCapital(balance))
                // Leverage ratios
                .debtToEquity(calculateRatio(balance.getLongTermDebt(), balance.getTotalEquity()))
                .debtToAssets(calculateRatio(balance.getTotalLiabilities(), balance.getTotalAssets()))
                .interestCoverage(calculateRatio(income.getEbit(), income.getInterestExpense()))
                // Efficiency ratios
                .assetTurnover(calculateRatio(income.getTotalRevenue(), balance.getTotalAssets()))
                // Cash flow metrics
                .freeCashFlow(calculateFreeCashFlow(cashFlow))
                .cashConversionRatio(calculateCashConversionRatio(cashFlow, income))
                .operatingCashFlow(cashFlow.getOperatingCashFlow())
                // Per share metrics
                .earningsPerShare(income.getDilutedEPS())
                .bookValuePerShare(calculateBookValuePerShare(balance))
                .cashFlowPerShare(calculateCashFlowPerShare(cashFlow, balance))
                // Additional metrics
                .liquidityPosition(calculateLiquidityPosition(balance))
                .totalDebt(calculateTotalDebt(balance))
                .netDebt(calculateNetDebt(balance))
                .build();
    }

    /**
     * Calculate Return on Assets (ROA)
     */
    private Double calculateROA(BigDecimal netIncome, BigDecimal totalAssets) {
        if (netIncome == null || totalAssets == null || totalAssets.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return netIncome.divide(totalAssets, SCALE, ROUNDING)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Calculate Return on Equity (ROE)
     */
    private Double calculateROE(BigDecimal netIncome, BigDecimal equity) {
        if (netIncome == null || equity == null || equity.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return netIncome.divide(equity, SCALE, ROUNDING)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Calculate Return on Invested Capital (ROIC)
     */
    private Double calculateROIC(IncomeStatement income, BalanceSheet balance) {
        BigDecimal nopat = calculateNOPAT(income);
        BigDecimal investedCapital = calculateInvestedCapital(balance);

        if (nopat == null || investedCapital == null || investedCapital.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return nopat.divide(investedCapital, SCALE, ROUNDING)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Calculate Net Operating Profit After Tax (NOPAT)
     */
    private BigDecimal calculateNOPAT(IncomeStatement income) {
        if (income.getOperatingIncome() == null) {
            return null;
        }

        BigDecimal taxRate = BigDecimal.valueOf(0.21); // Approximate corporate tax rate
        if (income.getIncomeTaxExpense() != null && income.getNetIncome() != null) {
            BigDecimal pretaxIncome = income.getNetIncome().add(income.getIncomeTaxExpense());
            if (pretaxIncome.compareTo(BigDecimal.ZERO) != 0) {
                taxRate = income.getIncomeTaxExpense().divide(pretaxIncome, SCALE, ROUNDING);
            }
        }

        return income.getOperatingIncome().multiply(BigDecimal.ONE.subtract(taxRate));
    }

    /**
     * Calculate Invested Capital
     */
    private BigDecimal calculateInvestedCapital(BalanceSheet balance) {
        if (balance.getTotalEquity() == null) {
            return null;
        }

        BigDecimal debt = balance.getLongTermDebt() != null ? balance.getLongTermDebt() : BigDecimal.ZERO;
        return balance.getTotalEquity().add(debt);
    }

    /**
     * Calculate margin (percentage)
     */
    private Double calculateMargin(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return numerator.divide(denominator, SCALE, ROUNDING)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Calculate ratio
     */
    private Double calculateRatio(BigDecimal numerator, BigDecimal denominator) {
        if (numerator == null || denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return numerator.divide(denominator, SCALE, ROUNDING).doubleValue();
    }

    /**
     * Calculate Quick Ratio
     */
    private Double calculateQuickRatio(BalanceSheet balance) {
        if (balance.getCurrentAssets() == null || balance.getCurrentLiabilities() == null ||
                balance.getCurrentLiabilities().compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        BigDecimal inventory = balance.getInventory() != null ? balance.getInventory() : BigDecimal.ZERO;
        BigDecimal quickAssets = balance.getCurrentAssets().subtract(inventory);

        return quickAssets.divide(balance.getCurrentLiabilities(), SCALE, ROUNDING).doubleValue();
    }

    /**
     * Calculate Working Capital
     */
    private BigDecimal calculateWorkingCapital(BalanceSheet balance) {
        if (balance.getCurrentAssets() == null || balance.getCurrentLiabilities() == null) {
            return null;
        }
        return balance.getCurrentAssets().subtract(balance.getCurrentLiabilities());
    }

    /**
     * Calculate Free Cash Flow
     */
    private BigDecimal calculateFreeCashFlow(CashFlowStatement cashFlow) {
        if (cashFlow.getOperatingCashFlow() == null) {
            return null;
        }

        BigDecimal capex = cashFlow.getCapitalExpenditures() != null ?
                cashFlow.getCapitalExpenditures() : BigDecimal.ZERO;

        return cashFlow.getOperatingCashFlow().subtract(capex.abs());
    }

    /**
     * Calculate Cash Conversion Ratio
     */
    private Double calculateCashConversionRatio(CashFlowStatement cashFlow, IncomeStatement income) {
        if (cashFlow.getOperatingCashFlow() == null || income.getNetIncome() == null ||
                income.getNetIncome().compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return cashFlow.getOperatingCashFlow()
                .divide(income.getNetIncome(), SCALE, ROUNDING)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Calculate Book Value Per Share
     */
    private BigDecimal calculateBookValuePerShare(BalanceSheet balance) {
        if (balance.getTotalEquity() == null || balance.getSharesOutstanding() == null ||
                balance.getSharesOutstanding() == 0) {
            return null;
        }

        return balance.getTotalEquity()
                .divide(BigDecimal.valueOf(balance.getSharesOutstanding()), SCALE, ROUNDING);
    }

    /**
     * Calculate Cash Flow Per Share
     */
    private BigDecimal calculateCashFlowPerShare(CashFlowStatement cashFlow, BalanceSheet balance) {
        if (cashFlow.getOperatingCashFlow() == null || balance.getSharesOutstanding() == null ||
                balance.getSharesOutstanding() == 0) {
            return null;
        }

        return cashFlow.getOperatingCashFlow()
                .divide(BigDecimal.valueOf(balance.getSharesOutstanding()), SCALE, ROUNDING);
    }

    /**
     * Calculate Liquidity Position (Cash + Short-term Investments)
     */
    private BigDecimal calculateLiquidityPosition(BalanceSheet balance) {
        BigDecimal cash = balance.getCashAndEquivalents() != null ?
                balance.getCashAndEquivalents() : BigDecimal.ZERO;
        BigDecimal investments = balance.getShortTermInvestments() != null ?
                balance.getShortTermInvestments() : BigDecimal.ZERO;

        return cash.add(investments);
    }

    /**
     * Calculate Total Debt
     */
    private BigDecimal calculateTotalDebt(BalanceSheet balance) {
        BigDecimal shortTerm = balance.getShortTermDebt() != null ?
                balance.getShortTermDebt() : BigDecimal.ZERO;
        BigDecimal longTerm = balance.getLongTermDebt() != null ?
                balance.getLongTermDebt() : BigDecimal.ZERO;

        return shortTerm.add(longTerm);
    }

    /**
     * Calculate Net Debt
     */
    private BigDecimal calculateNetDebt(BalanceSheet balance) {
        BigDecimal totalDebt = calculateTotalDebt(balance);
        BigDecimal cash = balance.getCashAndEquivalents() != null ?
                balance.getCashAndEquivalents() : BigDecimal.ZERO;

        return totalDebt.subtract(cash);
    }

    /**
     * Calculate growth rate between two values
     */
    public Double calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        if (current == null || previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return current.subtract(previous)
                .divide(previous, SCALE, ROUNDING)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Populate calculated fields in income statement
     */
    public void populateIncomeStatementCalculations(IncomeStatement income, IncomeStatement previousYear) {
        // Calculate margins
        income.setGrossMargin(calculateMargin(income.getGrossProfit(), income.getTotalRevenue()));
        income.setOperatingMargin(calculateMargin(income.getOperatingIncome(), income.getTotalRevenue()));
        income.setNetMargin(calculateMargin(income.getNetIncome(), income.getTotalRevenue()));
        income.setEbitdaMargin(calculateMargin(income.getEbitda(), income.getTotalRevenue()));

        // Calculate growth rates if previous year data available
        if (previousYear != null) {
            income.setRevenueGrowthRate(calculateGrowthRate(income.getTotalRevenue(), previousYear.getTotalRevenue()));
            income.setOperatingIncomeGrowthRate(calculateGrowthRate(income.getOperatingIncome(), previousYear.getOperatingIncome()));
            income.setNetIncomeGrowthRate(calculateGrowthRate(income.getNetIncome(), previousYear.getNetIncome()));
        }
    }

    /**
     * Populate calculated fields in balance sheet
     */
    public void populateBalanceSheetCalculations(BalanceSheet balance) {
        balance.setCurrentRatio(calculateRatio(balance.getCurrentAssets(), balance.getCurrentLiabilities()));
        balance.setQuickRatio(calculateQuickRatio(balance));
        balance.setDebtToEquityRatio(calculateRatio(balance.getLongTermDebt(), balance.getTotalEquity()));
        balance.setDebtToAssetsRatio(calculateRatio(balance.getTotalLiabilities(), balance.getTotalAssets()));
        balance.setWorkingCapital(calculateWorkingCapital(balance));
        balance.setNetDebt(calculateNetDebt(balance));
        balance.setBookValuePerShare(calculateBookValuePerShare(balance));
    }

    /**
     * Populate calculated fields in cash flow statement
     */
    public void populateCashFlowCalculations(CashFlowStatement cashFlow, IncomeStatement income) {
        cashFlow.setFreeCashFlow(calculateFreeCashFlow(cashFlow));
        cashFlow.setCashConversionRatio(calculateCashConversionRatio(cashFlow, income));
    }
}
