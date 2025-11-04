package com.financialanalysis.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents a Cash Flow Statement
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowStatement {
    private String fiscalYear;
    private String fiscalPeriod;

    // Operating Activities
    private BigDecimal operatingCashFlow;
    private BigDecimal netIncomeStartingLine;
    private BigDecimal depreciationAmortization;
    private BigDecimal deferredTaxes;
    private BigDecimal stockBasedCompensation;
    private BigDecimal changeInWorkingCapital;
    private BigDecimal changeInAccountsReceivable;
    private BigDecimal changeInInventory;
    private BigDecimal changeInAccountsPayable;

    // Investing Activities
    private BigDecimal cashFromInvesting;
    private BigDecimal capitalExpenditures;
    private BigDecimal acquisitionsOfAssets;
    private BigDecimal purchaseOfInvestments;
    private BigDecimal saleOfInvestments;

    // Financing Activities
    private BigDecimal cashFromFinancing;
    private BigDecimal debtIssuance;
    private BigDecimal debtRepayment;
    private BigDecimal equityIssuance;
    private BigDecimal dividendsPaid;
    private BigDecimal stockRepurchase;

    // Summary
    private BigDecimal netChangeInCash;
    private BigDecimal beginningCashBalance;
    private BigDecimal endingCashBalance;

    // Calculated metrics
    private BigDecimal freeCashFlow;
    private Double cashConversionRatio;
}
