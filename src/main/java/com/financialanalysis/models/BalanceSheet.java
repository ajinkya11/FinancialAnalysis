package com.financialanalysis.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents a Balance Sheet with comprehensive asset, liability, and equity metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceSheet {
    private String fiscalYear;
    private String fiscalPeriod;

    // Assets
    private BigDecimal totalAssets;
    private BigDecimal currentAssets;
    private BigDecimal cashAndEquivalents;
    private BigDecimal shortTermInvestments;
    private BigDecimal accountsReceivable;
    private BigDecimal inventory;
    private BigDecimal prepaidExpenses;

    private BigDecimal nonCurrentAssets;
    private BigDecimal propertyPlantEquipment;
    private BigDecimal accumulatedDepreciation;
    private BigDecimal netPPE;
    private BigDecimal intangibleAssets;
    private BigDecimal goodwill;
    private BigDecimal longTermInvestments;

    // Liabilities
    private BigDecimal totalLiabilities;
    private BigDecimal currentLiabilities;
    private BigDecimal accountsPayable;
    private BigDecimal shortTermDebt;
    private BigDecimal currentPortionLongTermDebt;
    private BigDecimal accruedExpenses;
    private BigDecimal airTrafficLiability;

    private BigDecimal longTermLiabilities;
    private BigDecimal longTermDebt;
    private BigDecimal pensionLiabilities;
    private BigDecimal leaseLiabilities;
    private BigDecimal deferredTaxLiabilities;

    // Equity
    private BigDecimal totalEquity;
    private BigDecimal commonStock;
    private BigDecimal retainedEarnings;
    private BigDecimal treasuryStock;
    private BigDecimal accumulatedOtherIncome;

    // Shares outstanding
    private Long sharesOutstanding;

    // Calculated ratios
    private Double currentRatio;
    private Double quickRatio;
    private Double debtToEquityRatio;
    private Double debtToAssetsRatio;
    private BigDecimal workingCapital;
    private BigDecimal netDebt;
    private BigDecimal bookValuePerShare;
}
