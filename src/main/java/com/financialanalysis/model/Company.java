package com.financialanalysis.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a company with its financial statements from multiple years
 * Enhanced with detailed financial data models
 */
public class Company {
    private String name;
    private String ticker;
    private String industry;

    // Basic financial statements (legacy, kept for backward compatibility)
    private List<FinancialStatement> financialStatements;

    // Detailed financial statements (new comprehensive models)
    private List<DetailedIncomeStatement> detailedIncomeStatements;
    private List<DetailedBalanceSheet> detailedBalanceSheets;
    private List<DetailedCashFlow> detailedCashFlows;

    // Airline-specific operating metrics
    private List<AirlineOperatingMetrics> operatingMetrics;

    // Fleet information (typically one per year, but stored as list for history)
    private List<FleetInformation> fleetInformation;

    // Segment information by year
    private List<SegmentInformation> segmentInformation;

    // Valuation metrics by year (requires market data)
    private List<ValuationMetrics> valuationMetrics;

    public Company() {
        this.financialStatements = new ArrayList<>();
        this.detailedIncomeStatements = new ArrayList<>();
        this.detailedBalanceSheets = new ArrayList<>();
        this.detailedCashFlows = new ArrayList<>();
        this.operatingMetrics = new ArrayList<>();
        this.fleetInformation = new ArrayList<>();
        this.segmentInformation = new ArrayList<>();
        this.valuationMetrics = new ArrayList<>();
    }

    public Company(String name, String ticker, String industry) {
        this.name = name;
        this.ticker = ticker;
        this.industry = industry;
        this.financialStatements = new ArrayList<>();
        this.detailedIncomeStatements = new ArrayList<>();
        this.detailedBalanceSheets = new ArrayList<>();
        this.detailedCashFlows = new ArrayList<>();
        this.operatingMetrics = new ArrayList<>();
        this.fleetInformation = new ArrayList<>();
        this.segmentInformation = new ArrayList<>();
        this.valuationMetrics = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public List<FinancialStatement> getFinancialStatements() {
        return financialStatements;
    }

    public void setFinancialStatements(List<FinancialStatement> financialStatements) {
        this.financialStatements = financialStatements;
    }

    public void addFinancialStatement(FinancialStatement statement) {
        this.financialStatements.add(statement);
    }

    // Detailed Income Statement methods
    public List<DetailedIncomeStatement> getDetailedIncomeStatements() {
        return detailedIncomeStatements;
    }

    public void setDetailedIncomeStatements(List<DetailedIncomeStatement> detailedIncomeStatements) {
        this.detailedIncomeStatements = detailedIncomeStatements;
    }

    public void addDetailedIncomeStatement(DetailedIncomeStatement statement) {
        this.detailedIncomeStatements.add(statement);
    }

    // Detailed Balance Sheet methods
    public List<DetailedBalanceSheet> getDetailedBalanceSheets() {
        return detailedBalanceSheets;
    }

    public void setDetailedBalanceSheets(List<DetailedBalanceSheet> detailedBalanceSheets) {
        this.detailedBalanceSheets = detailedBalanceSheets;
    }

    public void addDetailedBalanceSheet(DetailedBalanceSheet balanceSheet) {
        this.detailedBalanceSheets.add(balanceSheet);
    }

    // Detailed Cash Flow methods
    public List<DetailedCashFlow> getDetailedCashFlows() {
        return detailedCashFlows;
    }

    public void setDetailedCashFlows(List<DetailedCashFlow> detailedCashFlows) {
        this.detailedCashFlows = detailedCashFlows;
    }

    public void addDetailedCashFlow(DetailedCashFlow cashFlow) {
        this.detailedCashFlows.add(cashFlow);
    }

    // Operating Metrics methods
    public List<AirlineOperatingMetrics> getOperatingMetrics() {
        return operatingMetrics;
    }

    public void setOperatingMetrics(List<AirlineOperatingMetrics> operatingMetrics) {
        this.operatingMetrics = operatingMetrics;
    }

    public void addOperatingMetrics(AirlineOperatingMetrics metrics) {
        this.operatingMetrics.add(metrics);
    }

    // Fleet Information methods
    public List<FleetInformation> getFleetInformation() {
        return fleetInformation;
    }

    public void setFleetInformation(List<FleetInformation> fleetInformation) {
        this.fleetInformation = fleetInformation;
    }

    public void addFleetInformation(FleetInformation fleet) {
        this.fleetInformation.add(fleet);
    }

    // Segment Information methods
    public List<SegmentInformation> getSegmentInformation() {
        return segmentInformation;
    }

    public void setSegmentInformation(List<SegmentInformation> segmentInformation) {
        this.segmentInformation = segmentInformation;
    }

    public void addSegmentInformation(SegmentInformation segment) {
        this.segmentInformation.add(segment);
    }

    // Valuation Metrics methods
    public List<ValuationMetrics> getValuationMetrics() {
        return valuationMetrics;
    }

    public void setValuationMetrics(List<ValuationMetrics> valuationMetrics) {
        this.valuationMetrics = valuationMetrics;
    }

    public void addValuationMetrics(ValuationMetrics valuation) {
        this.valuationMetrics.add(valuation);
    }

    @Override
    public String toString() {
        return "Company{" +
                "name='" + name + '\'' +
                ", ticker='" + ticker + '\'' +
                ", industry='" + industry + '\'' +
                ", statements=" + financialStatements.size() +
                ", detailedStatements=" + detailedIncomeStatements.size() +
                '}';
    }
}
