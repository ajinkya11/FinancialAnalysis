package com.financialanalysis.model;

/**
 * Represents financial data extracted from a single 10-K filing
 */
public class FinancialStatement {
    private int fiscalYear;
    private String filingDate;

    // Income Statement Items
    private double revenue;
    private double costOfGoodsSold;
    private double grossProfit;
    private double operatingIncome;
    private double netIncome;
    private double ebit;
    private double ebitda;
    private double interestExpense;

    // Balance Sheet Items
    private double totalAssets;
    private double currentAssets;
    private double cash;
    private double marketableSecurities;
    private double accountsReceivable;
    private double inventory;

    private double totalLiabilities;
    private double currentLiabilities;
    private double totalDebt;
    private double longTermDebt;
    private double shareholderEquity;

    // Cash Flow Items
    private double operatingCashFlow;
    private double capitalExpenditures;
    private double freeCashFlow;
    private double dividendsPaid;
    private double shareRepurchases;

    // Per-share data
    private double earningsPerShare;
    private double dilutedEPS;
    private double bookValuePerShare;
    private long sharesOutstanding;

    public FinancialStatement() {
    }

    public FinancialStatement(int fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    // Getters and Setters
    public int getFiscalYear() {
        return fiscalYear;
    }

    public void setFiscalYear(int fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    public String getFilingDate() {
        return filingDate;
    }

    public void setFilingDate(String filingDate) {
        this.filingDate = filingDate;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public double getCostOfGoodsSold() {
        return costOfGoodsSold;
    }

    public void setCostOfGoodsSold(double costOfGoodsSold) {
        this.costOfGoodsSold = costOfGoodsSold;
    }

    public double getGrossProfit() {
        return grossProfit;
    }

    public void setGrossProfit(double grossProfit) {
        this.grossProfit = grossProfit;
    }

    public double getOperatingIncome() {
        return operatingIncome;
    }

    public void setOperatingIncome(double operatingIncome) {
        this.operatingIncome = operatingIncome;
    }

    public double getNetIncome() {
        return netIncome;
    }

    public void setNetIncome(double netIncome) {
        this.netIncome = netIncome;
    }

    public double getEbit() {
        return ebit;
    }

    public void setEbit(double ebit) {
        this.ebit = ebit;
    }

    public double getEbitda() {
        return ebitda;
    }

    public void setEbitda(double ebitda) {
        this.ebitda = ebitda;
    }

    public double getInterestExpense() {
        return interestExpense;
    }

    public void setInterestExpense(double interestExpense) {
        this.interestExpense = interestExpense;
    }

    public double getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(double totalAssets) {
        this.totalAssets = totalAssets;
    }

    public double getCurrentAssets() {
        return currentAssets;
    }

    public void setCurrentAssets(double currentAssets) {
        this.currentAssets = currentAssets;
    }

    public double getCash() {
        return cash;
    }

    public void setCash(double cash) {
        this.cash = cash;
    }

    public double getMarketableSecurities() {
        return marketableSecurities;
    }

    public void setMarketableSecurities(double marketableSecurities) {
        this.marketableSecurities = marketableSecurities;
    }

    public double getAccountsReceivable() {
        return accountsReceivable;
    }

    public void setAccountsReceivable(double accountsReceivable) {
        this.accountsReceivable = accountsReceivable;
    }

    public double getInventory() {
        return inventory;
    }

    public void setInventory(double inventory) {
        this.inventory = inventory;
    }

    public double getTotalLiabilities() {
        return totalLiabilities;
    }

    public void setTotalLiabilities(double totalLiabilities) {
        this.totalLiabilities = totalLiabilities;
    }

    public double getCurrentLiabilities() {
        return currentLiabilities;
    }

    public void setCurrentLiabilities(double currentLiabilities) {
        this.currentLiabilities = currentLiabilities;
    }

    public double getTotalDebt() {
        return totalDebt;
    }

    public void setTotalDebt(double totalDebt) {
        this.totalDebt = totalDebt;
    }

    public double getLongTermDebt() {
        return longTermDebt;
    }

    public void setLongTermDebt(double longTermDebt) {
        this.longTermDebt = longTermDebt;
    }

    public double getShareholderEquity() {
        return shareholderEquity;
    }

    public void setShareholderEquity(double shareholderEquity) {
        this.shareholderEquity = shareholderEquity;
    }

    public double getOperatingCashFlow() {
        return operatingCashFlow;
    }

    public void setOperatingCashFlow(double operatingCashFlow) {
        this.operatingCashFlow = operatingCashFlow;
    }

    public double getCapitalExpenditures() {
        return capitalExpenditures;
    }

    public void setCapitalExpenditures(double capitalExpenditures) {
        this.capitalExpenditures = capitalExpenditures;
    }

    public double getFreeCashFlow() {
        return freeCashFlow;
    }

    public void setFreeCashFlow(double freeCashFlow) {
        this.freeCashFlow = freeCashFlow;
    }

    public double getDividendsPaid() {
        return dividendsPaid;
    }

    public void setDividendsPaid(double dividendsPaid) {
        this.dividendsPaid = dividendsPaid;
    }

    public double getShareRepurchases() {
        return shareRepurchases;
    }

    public void setShareRepurchases(double shareRepurchases) {
        this.shareRepurchases = shareRepurchases;
    }

    public double getEarningsPerShare() {
        return earningsPerShare;
    }

    public void setEarningsPerShare(double earningsPerShare) {
        this.earningsPerShare = earningsPerShare;
    }

    public double getDilutedEPS() {
        return dilutedEPS;
    }

    public void setDilutedEPS(double dilutedEPS) {
        this.dilutedEPS = dilutedEPS;
    }

    public double getBookValuePerShare() {
        return bookValuePerShare;
    }

    public void setBookValuePerShare(double bookValuePerShare) {
        this.bookValuePerShare = bookValuePerShare;
    }

    public long getSharesOutstanding() {
        return sharesOutstanding;
    }

    public void setSharesOutstanding(long sharesOutstanding) {
        this.sharesOutstanding = sharesOutstanding;
    }

    @Override
    public String toString() {
        return "FinancialStatement{" +
                "fiscalYear=" + fiscalYear +
                ", revenue=" + revenue +
                ", netIncome=" + netIncome +
                ", totalAssets=" + totalAssets +
                '}';
    }
}
