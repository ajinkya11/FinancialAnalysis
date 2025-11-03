package com.financialanalysis.model;

/**
 * Comprehensive financial metrics for investment analysis
 * Organized by category for detailed comparative analysis
 */
public class FinancialMetrics {
    private int fiscalYear;
    private String companyName;

    // ========== PROFITABILITY METRICS ==========

    // Margins
    private double grossMargin;              // (Revenue - COGS) / Revenue
    private double operatingMargin;          // Operating Income / Revenue
    private double netMargin;                // Net Income / Revenue
    private double ebitdaMargin;             // EBITDA / Revenue

    // Return Metrics
    private double returnOnAssets;           // ROA = Net Income / Total Assets
    private double returnOnEquity;           // ROE = Net Income / Shareholder Equity
    private double returnOnInvestedCapital;  // ROIC = NOPAT / (Debt + Equity)
    private double returnOnCapitalEmployed;  // ROCE = EBIT / Capital Employed

    // ========== LIQUIDITY METRICS ==========

    private double currentRatio;             // Current Assets / Current Liabilities
    private double quickRatio;               // (Current Assets - Inventory) / Current Liabilities
    private double cashRatio;                // Cash / Current Liabilities
    private double operatingCashFlowRatio;   // Operating Cash Flow / Current Liabilities

    // Working Capital Cycle
    private double daysSalesOutstanding;     // DSO = (Accounts Receivable / Revenue) * 365
    private double daysInventoryOutstanding;  // DIO = (Inventory / COGS) * 365
    private double daysPayableOutstanding;    // DPO = (Accounts Payable / COGS) * 365
    private double cashConversionCycle;       // CCC = DSO + DIO - DPO

    // ========== SOLVENCY/LEVERAGE METRICS ==========

    private double debtToEquity;             // Total Debt / Shareholder Equity
    private double debtToAssets;             // Total Debt / Total Assets
    private double interestCoverage;         // EBIT / Interest Expense
    private double ebitdaCoverage;           // EBITDA / Interest Expense
    private double debtServiceCoverage;      // Operating Cash Flow / Total Debt Service

    // ========== EFFICIENCY METRICS ==========

    private double assetTurnover;            // Revenue / Total Assets
    private double fixedAssetTurnover;       // Revenue / Net Fixed Assets
    private double workingCapitalTurnover;   // Revenue / Working Capital
    private double inventoryTurnover;        // COGS / Average Inventory
    private double receivablesTurnover;      // Revenue / Average Receivables
    private double payablesTurnover;         // COGS / Average Payables

    // Operating Efficiency
    private double operatingExpenseRatio;    // Operating Expenses / Revenue
    private double sgaToRevenue;             // SG&A / Revenue
    private double capexToRevenue;           // CapEx / Revenue
    private double capexToDepreciation;      // CapEx / Depreciation

    // ========== GROWTH METRICS ==========

    private double revenueGrowthRate;        // (Current Revenue - Prior Revenue) / Prior Revenue
    private double operatingIncomeGrowthRate;
    private double netIncomeGrowthRate;
    private double ebitdaGrowthRate;
    private double epsGrowthRate;
    private double freeCashFlowGrowthRate;

    // ========== CASH FLOW METRICS ==========

    private double freeCashFlowMargin;       // FCF / Revenue
    private double operatingCashFlowMargin;  // Operating CF / Revenue
    private double cashFlowToNetIncome;      // Operating CF / Net Income (quality of earnings)
    private double freeCashFlowToNetIncome;
    private double capexToOperatingCashFlow; // CapEx / Operating CF

    // Cash Flow Returns
    private double cashFlowReturnOnAssets;   // Operating CF / Total Assets
    private double cashFlowReturnOnEquity;   // Operating CF / Shareholder Equity
    private double freeCashFlowYield;        // FCF / Market Cap (if market data available)

    // ========== VALUATION METRICS ==========
    // Note: These require market data (stock price)

    private double priceToEarnings;          // P/E = Market Price / EPS
    private double priceToBook;              // P/B = Market Price / Book Value per Share
    private double priceToSales;             // P/S = Market Cap / Revenue
    private double evToEbitda;               // EV/EBITDA = Enterprise Value / EBITDA
    private double evToRevenue;              // EV/Revenue
    private double evToFreeCashFlow;         // EV/FCF
    private double pegRatio;                 // PEG = (P/E) / EPS Growth Rate

    // ========== BALANCE SHEET QUALITY METRICS ==========

    private double intangibleAssetsToTotalAssets;
    private double workingCapitalRatio;      // Working Capital / Total Assets
    private double equityMultiplier;         // Total Assets / Shareholder Equity (leverage)

    // ========== PER-SHARE METRICS ==========

    private double earningsPerShare;
    private double bookValuePerShare;
    private double freeCashFlowPerShare;
    private double revenuePerShare;

    // Constructors
    public FinancialMetrics() {
    }

    public FinancialMetrics(int fiscalYear, String companyName) {
        this.fiscalYear = fiscalYear;
        this.companyName = companyName;
    }

    // Getters and Setters

    public int getFiscalYear() {
        return fiscalYear;
    }

    public void setFiscalYear(int fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    // Profitability Getters/Setters

    public double getGrossMargin() {
        return grossMargin;
    }

    public void setGrossMargin(double grossMargin) {
        this.grossMargin = grossMargin;
    }

    public double getOperatingMargin() {
        return operatingMargin;
    }

    public void setOperatingMargin(double operatingMargin) {
        this.operatingMargin = operatingMargin;
    }

    public double getNetMargin() {
        return netMargin;
    }

    public void setNetMargin(double netMargin) {
        this.netMargin = netMargin;
    }

    public double getEbitdaMargin() {
        return ebitdaMargin;
    }

    public void setEbitdaMargin(double ebitdaMargin) {
        this.ebitdaMargin = ebitdaMargin;
    }

    public double getReturnOnAssets() {
        return returnOnAssets;
    }

    public void setReturnOnAssets(double returnOnAssets) {
        this.returnOnAssets = returnOnAssets;
    }

    public double getReturnOnEquity() {
        return returnOnEquity;
    }

    public void setReturnOnEquity(double returnOnEquity) {
        this.returnOnEquity = returnOnEquity;
    }

    public double getReturnOnInvestedCapital() {
        return returnOnInvestedCapital;
    }

    public void setReturnOnInvestedCapital(double returnOnInvestedCapital) {
        this.returnOnInvestedCapital = returnOnInvestedCapital;
    }

    public double getReturnOnCapitalEmployed() {
        return returnOnCapitalEmployed;
    }

    public void setReturnOnCapitalEmployed(double returnOnCapitalEmployed) {
        this.returnOnCapitalEmployed = returnOnCapitalEmployed;
    }

    // Liquidity Getters/Setters

    public double getCurrentRatio() {
        return currentRatio;
    }

    public void setCurrentRatio(double currentRatio) {
        this.currentRatio = currentRatio;
    }

    public double getQuickRatio() {
        return quickRatio;
    }

    public void setQuickRatio(double quickRatio) {
        this.quickRatio = quickRatio;
    }

    public double getCashRatio() {
        return cashRatio;
    }

    public void setCashRatio(double cashRatio) {
        this.cashRatio = cashRatio;
    }

    public double getOperatingCashFlowRatio() {
        return operatingCashFlowRatio;
    }

    public void setOperatingCashFlowRatio(double operatingCashFlowRatio) {
        this.operatingCashFlowRatio = operatingCashFlowRatio;
    }

    public double getDaysSalesOutstanding() {
        return daysSalesOutstanding;
    }

    public void setDaysSalesOutstanding(double daysSalesOutstanding) {
        this.daysSalesOutstanding = daysSalesOutstanding;
    }

    public double getDaysInventoryOutstanding() {
        return daysInventoryOutstanding;
    }

    public void setDaysInventoryOutstanding(double daysInventoryOutstanding) {
        this.daysInventoryOutstanding = daysInventoryOutstanding;
    }

    public double getDaysPayableOutstanding() {
        return daysPayableOutstanding;
    }

    public void setDaysPayableOutstanding(double daysPayableOutstanding) {
        this.daysPayableOutstanding = daysPayableOutstanding;
    }

    public double getCashConversionCycle() {
        return cashConversionCycle;
    }

    public void setCashConversionCycle(double cashConversionCycle) {
        this.cashConversionCycle = cashConversionCycle;
    }

    // Solvency Getters/Setters

    public double getDebtToEquity() {
        return debtToEquity;
    }

    public void setDebtToEquity(double debtToEquity) {
        this.debtToEquity = debtToEquity;
    }

    public double getDebtToAssets() {
        return debtToAssets;
    }

    public void setDebtToAssets(double debtToAssets) {
        this.debtToAssets = debtToAssets;
    }

    public double getInterestCoverage() {
        return interestCoverage;
    }

    public void setInterestCoverage(double interestCoverage) {
        this.interestCoverage = interestCoverage;
    }

    public double getEbitdaCoverage() {
        return ebitdaCoverage;
    }

    public void setEbitdaCoverage(double ebitdaCoverage) {
        this.ebitdaCoverage = ebitdaCoverage;
    }

    public double getDebtServiceCoverage() {
        return debtServiceCoverage;
    }

    public void setDebtServiceCoverage(double debtServiceCoverage) {
        this.debtServiceCoverage = debtServiceCoverage;
    }

    // Efficiency Getters/Setters

    public double getAssetTurnover() {
        return assetTurnover;
    }

    public void setAssetTurnover(double assetTurnover) {
        this.assetTurnover = assetTurnover;
    }

    public double getFixedAssetTurnover() {
        return fixedAssetTurnover;
    }

    public void setFixedAssetTurnover(double fixedAssetTurnover) {
        this.fixedAssetTurnover = fixedAssetTurnover;
    }

    public double getWorkingCapitalTurnover() {
        return workingCapitalTurnover;
    }

    public void setWorkingCapitalTurnover(double workingCapitalTurnover) {
        this.workingCapitalTurnover = workingCapitalTurnover;
    }

    public double getInventoryTurnover() {
        return inventoryTurnover;
    }

    public void setInventoryTurnover(double inventoryTurnover) {
        this.inventoryTurnover = inventoryTurnover;
    }

    public double getReceivablesTurnover() {
        return receivablesTurnover;
    }

    public void setReceivablesTurnover(double receivablesTurnover) {
        this.receivablesTurnover = receivablesTurnover;
    }

    public double getPayablesTurnover() {
        return payablesTurnover;
    }

    public void setPayablesTurnover(double payablesTurnover) {
        this.payablesTurnover = payablesTurnover;
    }

    public double getOperatingExpenseRatio() {
        return operatingExpenseRatio;
    }

    public void setOperatingExpenseRatio(double operatingExpenseRatio) {
        this.operatingExpenseRatio = operatingExpenseRatio;
    }

    public double getSgaToRevenue() {
        return sgaToRevenue;
    }

    public void setSgaToRevenue(double sgaToRevenue) {
        this.sgaToRevenue = sgaToRevenue;
    }

    public double getCapexToRevenue() {
        return capexToRevenue;
    }

    public void setCapexToRevenue(double capexToRevenue) {
        this.capexToRevenue = capexToRevenue;
    }

    public double getCapexToDepreciation() {
        return capexToDepreciation;
    }

    public void setCapexToDepreciation(double capexToDepreciation) {
        this.capexToDepreciation = capexToDepreciation;
    }

    // Growth Getters/Setters

    public double getRevenueGrowthRate() {
        return revenueGrowthRate;
    }

    public void setRevenueGrowthRate(double revenueGrowthRate) {
        this.revenueGrowthRate = revenueGrowthRate;
    }

    public double getOperatingIncomeGrowthRate() {
        return operatingIncomeGrowthRate;
    }

    public void setOperatingIncomeGrowthRate(double operatingIncomeGrowthRate) {
        this.operatingIncomeGrowthRate = operatingIncomeGrowthRate;
    }

    public double getNetIncomeGrowthRate() {
        return netIncomeGrowthRate;
    }

    public void setNetIncomeGrowthRate(double netIncomeGrowthRate) {
        this.netIncomeGrowthRate = netIncomeGrowthRate;
    }

    public double getEbitdaGrowthRate() {
        return ebitdaGrowthRate;
    }

    public void setEbitdaGrowthRate(double ebitdaGrowthRate) {
        this.ebitdaGrowthRate = ebitdaGrowthRate;
    }

    public double getEpsGrowthRate() {
        return epsGrowthRate;
    }

    public void setEpsGrowthRate(double epsGrowthRate) {
        this.epsGrowthRate = epsGrowthRate;
    }

    public double getFreeCashFlowGrowthRate() {
        return freeCashFlowGrowthRate;
    }

    public void setFreeCashFlowGrowthRate(double freeCashFlowGrowthRate) {
        this.freeCashFlowGrowthRate = freeCashFlowGrowthRate;
    }

    // Cash Flow Getters/Setters

    public double getFreeCashFlowMargin() {
        return freeCashFlowMargin;
    }

    public void setFreeCashFlowMargin(double freeCashFlowMargin) {
        this.freeCashFlowMargin = freeCashFlowMargin;
    }

    public double getOperatingCashFlowMargin() {
        return operatingCashFlowMargin;
    }

    public void setOperatingCashFlowMargin(double operatingCashFlowMargin) {
        this.operatingCashFlowMargin = operatingCashFlowMargin;
    }

    public double getCashFlowToNetIncome() {
        return cashFlowToNetIncome;
    }

    public void setCashFlowToNetIncome(double cashFlowToNetIncome) {
        this.cashFlowToNetIncome = cashFlowToNetIncome;
    }

    public double getFreeCashFlowToNetIncome() {
        return freeCashFlowToNetIncome;
    }

    public void setFreeCashFlowToNetIncome(double freeCashFlowToNetIncome) {
        this.freeCashFlowToNetIncome = freeCashFlowToNetIncome;
    }

    public double getCapexToOperatingCashFlow() {
        return capexToOperatingCashFlow;
    }

    public void setCapexToOperatingCashFlow(double capexToOperatingCashFlow) {
        this.capexToOperatingCashFlow = capexToOperatingCashFlow;
    }

    public double getCashFlowReturnOnAssets() {
        return cashFlowReturnOnAssets;
    }

    public void setCashFlowReturnOnAssets(double cashFlowReturnOnAssets) {
        this.cashFlowReturnOnAssets = cashFlowReturnOnAssets;
    }

    public double getCashFlowReturnOnEquity() {
        return cashFlowReturnOnEquity;
    }

    public void setCashFlowReturnOnEquity(double cashFlowReturnOnEquity) {
        this.cashFlowReturnOnEquity = cashFlowReturnOnEquity;
    }

    public double getFreeCashFlowYield() {
        return freeCashFlowYield;
    }

    public void setFreeCashFlowYield(double freeCashFlowYield) {
        this.freeCashFlowYield = freeCashFlowYield;
    }

    // Valuation Getters/Setters

    public double getPriceToEarnings() {
        return priceToEarnings;
    }

    public void setPriceToEarnings(double priceToEarnings) {
        this.priceToEarnings = priceToEarnings;
    }

    public double getPriceToBook() {
        return priceToBook;
    }

    public void setPriceToBook(double priceToBook) {
        this.priceToBook = priceToBook;
    }

    public double getPriceToSales() {
        return priceToSales;
    }

    public void setPriceToSales(double priceToSales) {
        this.priceToSales = priceToSales;
    }

    public double getEvToEbitda() {
        return evToEbitda;
    }

    public void setEvToEbitda(double evToEbitda) {
        this.evToEbitda = evToEbitda;
    }

    public double getEvToRevenue() {
        return evToRevenue;
    }

    public void setEvToRevenue(double evToRevenue) {
        this.evToRevenue = evToRevenue;
    }

    public double getEvToFreeCashFlow() {
        return evToFreeCashFlow;
    }

    public void setEvToFreeCashFlow(double evToFreeCashFlow) {
        this.evToFreeCashFlow = evToFreeCashFlow;
    }

    public double getPegRatio() {
        return pegRatio;
    }

    public void setPegRatio(double pegRatio) {
        this.pegRatio = pegRatio;
    }

    // Balance Sheet Quality Getters/Setters

    public double getIntangibleAssetsToTotalAssets() {
        return intangibleAssetsToTotalAssets;
    }

    public void setIntangibleAssetsToTotalAssets(double intangibleAssetsToTotalAssets) {
        this.intangibleAssetsToTotalAssets = intangibleAssetsToTotalAssets;
    }

    public double getWorkingCapitalRatio() {
        return workingCapitalRatio;
    }

    public void setWorkingCapitalRatio(double workingCapitalRatio) {
        this.workingCapitalRatio = workingCapitalRatio;
    }

    public double getEquityMultiplier() {
        return equityMultiplier;
    }

    public void setEquityMultiplier(double equityMultiplier) {
        this.equityMultiplier = equityMultiplier;
    }

    // Per-Share Getters/Setters

    public double getEarningsPerShare() {
        return earningsPerShare;
    }

    public void setEarningsPerShare(double earningsPerShare) {
        this.earningsPerShare = earningsPerShare;
    }

    public double getBookValuePerShare() {
        return bookValuePerShare;
    }

    public void setBookValuePerShare(double bookValuePerShare) {
        this.bookValuePerShare = bookValuePerShare;
    }

    public double getFreeCashFlowPerShare() {
        return freeCashFlowPerShare;
    }

    public void setFreeCashFlowPerShare(double freeCashFlowPerShare) {
        this.freeCashFlowPerShare = freeCashFlowPerShare;
    }

    public double getRevenuePerShare() {
        return revenuePerShare;
    }

    public void setRevenuePerShare(double revenuePerShare) {
        this.revenuePerShare = revenuePerShare;
    }

    @Override
    public String toString() {
        return "FinancialMetrics{" +
                "fiscalYear=" + fiscalYear +
                ", companyName='" + companyName + '\'' +
                ", grossMargin=" + String.format("%.2f%%", grossMargin * 100) +
                ", netMargin=" + String.format("%.2f%%", netMargin * 100) +
                ", ROE=" + String.format("%.2f%%", returnOnEquity * 100) +
                ", ROIC=" + String.format("%.2f%%", returnOnInvestedCapital * 100) +
                '}';
    }
}
