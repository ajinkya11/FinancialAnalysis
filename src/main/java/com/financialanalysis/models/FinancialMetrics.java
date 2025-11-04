package com.financialanalysis.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Comprehensive financial metrics and ratios
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialMetrics {
    private String fiscalYear;

    // Profitability Ratios
    private Double returnOnAssets;  // ROA = Net Income / Total Assets
    private Double returnOnEquity;  // ROE = Net Income / Shareholders' Equity
    private Double returnOnInvestedCapital;  // ROIC = NOPAT / Invested Capital
    private Double grossMargin;
    private Double operatingMargin;
    private Double netMargin;
    private Double ebitdaMargin;

    // Liquidity Ratios
    private Double currentRatio;
    private Double quickRatio;
    private Double cashRatio;
    private BigDecimal workingCapital;

    // Leverage Ratios
    private Double debtToEquity;
    private Double debtToAssets;
    private Double equityMultiplier;
    private Double interestCoverage;  // EBIT / Interest Expense
    private Double debtServiceCoverage;

    // Efficiency Ratios
    private Double assetTurnover;  // Revenue / Average Total Assets
    private Double receivablesTurnover;
    private Double inventoryTurnover;
    private Integer daysReceivablesOutstanding;
    private Integer daysInventoryOutstanding;

    // Per Share Metrics
    private BigDecimal earningsPerShare;
    private BigDecimal bookValuePerShare;
    private BigDecimal cashFlowPerShare;

    // Cash Flow Metrics
    private BigDecimal freeCashFlow;
    private Double cashConversionRatio;
    private BigDecimal operatingCashFlow;

    // Growth Metrics
    private Double revenueGrowthYoY;
    private Double netIncomeGrowthYoY;
    private Double epsGrowthYoY;
    private Double assetGrowthYoY;

    // Airline-Specific Financial Health
    private BigDecimal liquidityPosition;
    private Double fuelCostPercentage;
    private Double laborCostPercentage;
    private BigDecimal totalDebt;
    private BigDecimal netDebt;
}
