package com.financialanalysis.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Complete financial data for a company including multiple years
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyFinancialData {
    private String ticker;
    private String companyName;
    private String cik;
    private String industry;

    // Multi-year data
    private List<IncomeStatement> incomeStatements;
    private List<BalanceSheet> balanceSheets;
    private List<CashFlowStatement> cashFlowStatements;
    private List<AirlineOperatingMetrics> operatingMetrics;
    private List<FinancialMetrics> financialMetrics;

    // Latest year data for quick access
    private IncomeStatement latestIncomeStatement;
    private BalanceSheet latestBalanceSheet;
    private CashFlowStatement latestCashFlowStatement;
    private AirlineOperatingMetrics latestOperatingMetrics;
    private FinancialMetrics latestFinancialMetrics;

    // Filing information
    private String filingDate;
    private String fiscalYearEnd;
}
