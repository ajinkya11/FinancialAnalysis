package com.financialanalysis.model;

/**
 * Valuation metrics for market-based analysis
 * Requires stock price and market data
 */
public class ValuationMetrics {
    private int fiscalYear;
    private String ticker;

    // Market Data (as of fiscal year-end or current)
    private double stockPrice;
    private double marketCapitalization;
    private double enterpriseValue;
    private double week52High;
    private double week52Low;
    private long averageDailyVolume;

    // From Financial Statements (for calculation)
    private double totalRevenue;
    private double ebitda;
    private double ebit;
    private double netIncome;
    private double operatingCashFlow;
    private double freeCashFlow;
    private double totalDebt;
    private double cash;
    private double shareholderEquity;
    private double bookValuePerShare;
    private double earningsPerShare;
    private double sharesOutstanding;

    // Price Multiples
    private double priceToEarnings; // P/E
    private double priceToBook; // P/B
    private double priceToSales; // P/S
    private double priceToCashFlow; // P/CF

    // Enterprise Value Multiples
    private double evToRevenue; // EV/Revenue
    private double evToEBITDA; // EV/EBITDA
    private double evToEBIT; // EV/EBIT
    private double evToFreeCashFlow; // EV/FCF

    // Other Valuation Metrics
    private double pegRatio; // P/E to Growth
    private double dividendYield;
    private double payoutRatio;

    public ValuationMetrics() {}

    public ValuationMetrics(int fiscalYear, String ticker) {
        this.fiscalYear = fiscalYear;
        this.ticker = ticker;
    }

    // Getters and Setters
    public int getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(int fiscalYear) { this.fiscalYear = fiscalYear; }

    public String getTicker() { return ticker; }
    public void setTicker(String ticker) { this.ticker = ticker; }

    public double getStockPrice() { return stockPrice; }
    public void setStockPrice(double stockPrice) { this.stockPrice = stockPrice; }

    public double getMarketCapitalization() { return marketCapitalization; }
    public void setMarketCapitalization(double marketCapitalization) { this.marketCapitalization = marketCapitalization; }

    public double getEnterpriseValue() { return enterpriseValue; }
    public void setEnterpriseValue(double enterpriseValue) { this.enterpriseValue = enterpriseValue; }

    public double getWeek52High() { return week52High; }
    public void setWeek52High(double week52High) { this.week52High = week52High; }

    public double getWeek52Low() { return week52Low; }
    public void setWeek52Low(double week52Low) { this.week52Low = week52Low; }

    public long getAverageDailyVolume() { return averageDailyVolume; }
    public void setAverageDailyVolume(long averageDailyVolume) { this.averageDailyVolume = averageDailyVolume; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public double getEbitda() { return ebitda; }
    public void setEbitda(double ebitda) { this.ebitda = ebitda; }

    public double getEbit() { return ebit; }
    public void setEbit(double ebit) { this.ebit = ebit; }

    public double getNetIncome() { return netIncome; }
    public void setNetIncome(double netIncome) { this.netIncome = netIncome; }

    public double getOperatingCashFlow() { return operatingCashFlow; }
    public void setOperatingCashFlow(double operatingCashFlow) { this.operatingCashFlow = operatingCashFlow; }

    public double getFreeCashFlow() { return freeCashFlow; }
    public void setFreeCashFlow(double freeCashFlow) { this.freeCashFlow = freeCashFlow; }

    public double getTotalDebt() { return totalDebt; }
    public void setTotalDebt(double totalDebt) { this.totalDebt = totalDebt; }

    public double getCash() { return cash; }
    public void setCash(double cash) { this.cash = cash; }

    public double getShareholderEquity() { return shareholderEquity; }
    public void setShareholderEquity(double shareholderEquity) { this.shareholderEquity = shareholderEquity; }

    public double getBookValuePerShare() { return bookValuePerShare; }
    public void setBookValuePerShare(double bookValuePerShare) { this.bookValuePerShare = bookValuePerShare; }

    public double getEarningsPerShare() { return earningsPerShare; }
    public void setEarningsPerShare(double earningsPerShare) { this.earningsPerShare = earningsPerShare; }

    public double getSharesOutstanding() { return sharesOutstanding; }
    public void setSharesOutstanding(double sharesOutstanding) { this.sharesOutstanding = sharesOutstanding; }

    public double getPriceToEarnings() { return priceToEarnings; }
    public void setPriceToEarnings(double priceToEarnings) { this.priceToEarnings = priceToEarnings; }

    public double getPriceToBook() { return priceToBook; }
    public void setPriceToBook(double priceToBook) { this.priceToBook = priceToBook; }

    public double getPriceToSales() { return priceToSales; }
    public void setPriceToSales(double priceToSales) { this.priceToSales = priceToSales; }

    public double getPriceToCashFlow() { return priceToCashFlow; }
    public void setPriceToCashFlow(double priceToCashFlow) { this.priceToCashFlow = priceToCashFlow; }

    public double getEvToRevenue() { return evToRevenue; }
    public void setEvToRevenue(double evToRevenue) { this.evToRevenue = evToRevenue; }

    public double getEvToEBITDA() { return evToEBITDA; }
    public void setEvToEBITDA(double evToEBITDA) { this.evToEBITDA = evToEBITDA; }

    public double getEvToEBIT() { return evToEBIT; }
    public void setEvToEBIT(double evToEBIT) { this.evToEBIT = evToEBIT; }

    public double getEvToFreeCashFlow() { return evToFreeCashFlow; }
    public void setEvToFreeCashFlow(double evToFreeCashFlow) { this.evToFreeCashFlow = evToFreeCashFlow; }

    public double getPegRatio() { return pegRatio; }
    public void setPegRatio(double pegRatio) { this.pegRatio = pegRatio; }

    public double getDividendYield() { return dividendYield; }
    public void setDividendYield(double dividendYield) { this.dividendYield = dividendYield; }

    public double getPayoutRatio() { return payoutRatio; }
    public void setPayoutRatio(double payoutRatio) { this.payoutRatio = payoutRatio; }

    // Calculation methods
    public void calculateMarketCapitalization() {
        if (stockPrice > 0 && sharesOutstanding > 0) {
            this.marketCapitalization = stockPrice * sharesOutstanding;
        }
    }

    public void calculateEnterpriseValue() {
        if (marketCapitalization > 0) {
            this.enterpriseValue = marketCapitalization + totalDebt - cash;
        }
    }

    public void calculatePriceMultiples() {
        if (stockPrice > 0) {
            // P/E
            if (earningsPerShare > 0) {
                this.priceToEarnings = stockPrice / earningsPerShare;
            }
            // P/B
            if (bookValuePerShare > 0) {
                this.priceToBook = stockPrice / bookValuePerShare;
            }
        }

        if (marketCapitalization > 0) {
            // P/S
            if (totalRevenue > 0) {
                this.priceToSales = marketCapitalization / totalRevenue;
            }
            // P/CF
            if (operatingCashFlow > 0) {
                this.priceToCashFlow = marketCapitalization / operatingCashFlow;
            }
        }
    }

    public void calculateEVMultiples() {
        if (enterpriseValue > 0) {
            // EV/Revenue
            if (totalRevenue > 0) {
                this.evToRevenue = enterpriseValue / totalRevenue;
            }
            // EV/EBITDA
            if (ebitda > 0) {
                this.evToEBITDA = enterpriseValue / ebitda;
            }
            // EV/EBIT
            if (ebit > 0) {
                this.evToEBIT = enterpriseValue / ebit;
            }
            // EV/FCF
            if (freeCashFlow > 0) {
                this.evToFreeCashFlow = enterpriseValue / freeCashFlow;
            }
        }
    }

    public void calculateAllMetrics() {
        calculateMarketCapitalization();
        calculateEnterpriseValue();
        calculatePriceMultiples();
        calculateEVMultiples();
    }
}
