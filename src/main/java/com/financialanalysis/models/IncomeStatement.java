package com.financialanalysis.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents an Income Statement with comprehensive metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomeStatement {
    private String fiscalYear;
    private String fiscalPeriod;

    // Revenue metrics
    private BigDecimal totalRevenue;
    private BigDecimal passengerRevenue;
    private BigDecimal cargoRevenue;
    private BigDecimal otherRevenue;

    // Cost metrics
    private BigDecimal costOfRevenue;
    private BigDecimal operatingExpenses;
    private BigDecimal fuelCosts;
    private BigDecimal laborCosts;
    private BigDecimal maintenanceCosts;
    private BigDecimal aircraftRentExpense;
    private BigDecimal landingFees;
    private BigDecimal depreciationAmortization;

    // Profit metrics
    private BigDecimal grossProfit;
    private BigDecimal operatingIncome;
    private BigDecimal ebitda;
    private BigDecimal ebit;
    private BigDecimal interestExpense;
    private BigDecimal incomeTaxExpense;
    private BigDecimal netIncome;

    // Per share metrics
    private BigDecimal basicEPS;
    private BigDecimal dilutedEPS;
    private Long weightedAverageSharesBasic;
    private Long weightedAverageSharesDiluted;

    // Calculated margins (percentages)
    private Double grossMargin;
    private Double operatingMargin;
    private Double ebitdaMargin;
    private Double netMargin;

    // Growth rates (year-over-year)
    private Double revenueGrowthRate;
    private Double operatingIncomeGrowthRate;
    private Double netIncomeGrowthRate;
}
