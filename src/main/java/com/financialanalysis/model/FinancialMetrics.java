package com.financialanalysis.model;

/**
 * Represents calculated financial metrics for a company in a given year
 */
public class FinancialMetrics {
    private int fiscalYear;
    private String companyName;

    // Profitability Metrics
    private double revenueGrowthRate;
    private double grossMargin;
    private double operatingMargin;
    private double netMargin;
    private double ebitdaMargin;

    // Efficiency Metrics
    private double returnOnAssets;
    private double returnOnEquity;
    private double assetTurnover;

    // Liquidity Metrics
    private double currentRatio;
    private double quickRatio;

    // Solvency/Leverage Metrics
    private double debtToEquity;
    private double debtToAssets;
    private double interestCoverage;

    // Cash Flow Metrics
    private double freeCashFlowMargin;
    private double capexToRevenue;

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

    public double getRevenueGrowthRate() {
        return revenueGrowthRate;
    }

    public void setRevenueGrowthRate(double revenueGrowthRate) {
        this.revenueGrowthRate = revenueGrowthRate;
    }

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

    public double getAssetTurnover() {
        return assetTurnover;
    }

    public void setAssetTurnover(double assetTurnover) {
        this.assetTurnover = assetTurnover;
    }

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

    public double getFreeCashFlowMargin() {
        return freeCashFlowMargin;
    }

    public void setFreeCashFlowMargin(double freeCashFlowMargin) {
        this.freeCashFlowMargin = freeCashFlowMargin;
    }

    public double getCapexToRevenue() {
        return capexToRevenue;
    }

    public void setCapexToRevenue(double capexToRevenue) {
        this.capexToRevenue = capexToRevenue;
    }

    @Override
    public String toString() {
        return "FinancialMetrics{" +
                "fiscalYear=" + fiscalYear +
                ", companyName='" + companyName + '\'' +
                ", grossMargin=" + String.format("%.2f%%", grossMargin * 100) +
                ", netMargin=" + String.format("%.2f%%", netMargin * 100) +
                ", ROE=" + String.format("%.2f%%", returnOnEquity * 100) +
                '}';
    }
}
