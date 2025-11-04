package com.financialanalysis.data;

import com.financialanalysis.analysis.ComparativeAnalysisService;
import com.financialanalysis.metrics.AirlineMetricsCalculator;
import com.financialanalysis.metrics.FinancialMetricsCalculator;
import com.financialanalysis.models.*;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Orchestration service that coordinates data fetching, parsing, and calculations
 * Includes caching mechanism for downloaded filings
 */
@Slf4j
@Service
public class FinancialDataService {

    private final SECEdgarClient edgarClient;
    private final XBRLParser xbrlParser;
    private final FinancialMetricsCalculator metricsCalculator;
    private final AirlineMetricsCalculator airlineCalculator;
    private final ComparativeAnalysisService analysisService;

    // Cache for XBRL content (ticker -> XBRL content)
    private final Cache<String, String> xbrlCache;
    // Cache for parsed company data (ticker -> CompanyFinancialData)
    private final Cache<String, CompanyFinancialData> companyDataCache;

    public FinancialDataService(SECEdgarClient edgarClient,
                               XBRLParser xbrlParser,
                               FinancialMetricsCalculator metricsCalculator,
                               AirlineMetricsCalculator airlineCalculator,
                               ComparativeAnalysisService analysisService) {
        this.edgarClient = edgarClient;
        this.xbrlParser = xbrlParser;
        this.metricsCalculator = metricsCalculator;
        this.airlineCalculator = airlineCalculator;
        this.analysisService = analysisService;

        // Initialize caches
        this.xbrlCache = Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .build();

        this.companyDataCache = Caffeine.newBuilder()
                .maximumSize(50)
                .expireAfterWrite(12, TimeUnit.HOURS)
                .build();
    }

    /**
     * Fetch and analyze company financial data
     */
    public CompanyFinancialData getCompanyData(String ticker, int numberOfYears) {
        log.info("Fetching financial data for ticker: {}", ticker);

        // Check cache first
        String cacheKey = ticker + "_" + numberOfYears;
        CompanyFinancialData cachedData = companyDataCache.getIfPresent(cacheKey);
        if (cachedData != null) {
            log.info("Retrieved cached data for ticker: {}", ticker);
            return cachedData;
        }

        try {
            // Get CIK from ticker
            String cik = edgarClient.getCIKFromTicker(ticker);
            log.info("CIK for {}: {}", ticker, cik);

            // Get 10-K filings
            var filings = edgarClient.get10KFilings(cik, numberOfYears);
            if (filings.isEmpty()) {
                throw new RuntimeException("No 10-K filings found for ticker: " + ticker);
            }

            // Get the most recent filing
            SECEdgarClient.FilingInfo latestFiling = filings.get(0);
            log.info("Processing latest 10-K filing dated: {}", latestFiling.getFilingDate());

            // Download XBRL content (with caching)
            String xbrlContent = xbrlCache.get(ticker, key -> {
                log.info("Downloading XBRL file for ticker: {}", ticker);
                return edgarClient.downloadXBRLFile(latestFiling.getXbrlUrl());
            });

            // Parse XBRL
            CompanyFinancialData companyData = xbrlParser.parseXBRL(xbrlContent, ticker);
            companyData.setCik(cik);
            companyData.setFilingDate(latestFiling.getFilingDate());

            // Calculate metrics
            calculateAllMetrics(companyData);

            // Validate data
            validateCompanyData(companyData);

            // Cache the result
            companyDataCache.put(cacheKey, companyData);

            log.info("Successfully processed financial data for ticker: {}", ticker);
            return companyData;

        } catch (Exception e) {
            log.error("Error fetching data for ticker: {}", ticker, e);
            throw new RuntimeException("Failed to fetch data for ticker: " + ticker, e);
        }
    }

    /**
     * Perform comparative analysis between two companies
     */
    public ComparisonResult compareCompanies(String ticker1, String ticker2, int numberOfYears) {
        log.info("Performing comparative analysis: {} vs {}", ticker1, ticker2);

        // Fetch data for both companies
        CompanyFinancialData company1 = getCompanyData(ticker1, numberOfYears);
        CompanyFinancialData company2 = getCompanyData(ticker2, numberOfYears);

        // Perform comparative analysis
        return analysisService.compareCompanies(company1, company2);
    }

    /**
     * Calculate all metrics for a company
     */
    private void calculateAllMetrics(CompanyFinancialData companyData) {
        log.info("Calculating metrics for company: {}", companyData.getCompanyName());

        IncomeStatement income = companyData.getLatestIncomeStatement();
        BalanceSheet balance = companyData.getLatestBalanceSheet();
        CashFlowStatement cashFlow = companyData.getLatestCashFlowStatement();
        AirlineOperatingMetrics airlineMetrics = companyData.getLatestOperatingMetrics();

        // Calculate derived fields in financial statements
        metricsCalculator.populateIncomeStatementCalculations(income, null);
        metricsCalculator.populateBalanceSheetCalculations(balance);
        metricsCalculator.populateCashFlowCalculations(cashFlow, income);

        // Calculate airline-specific metrics
        if (airlineMetrics != null) {
            airlineCalculator.calculateAirlineMetrics(airlineMetrics, income);
        }

        // Calculate comprehensive financial metrics
        FinancialMetrics metrics = metricsCalculator.calculateMetrics(income, balance, cashFlow, null);

        // Add fuel and labor cost percentages
        if (airlineMetrics != null) {
            metrics.setFuelCostPercentage(
                    airlineCalculator.calculateFuelCostPercentage(
                            income.getFuelCosts(), income.getOperatingExpenses()));
            metrics.setLaborCostPercentage(
                    airlineCalculator.calculateLaborCostPercentage(
                            income.getLaborCosts(), income.getOperatingExpenses()));
        }

        companyData.setLatestFinancialMetrics(metrics);
    }

    /**
     * Validate company data for quality issues
     */
    private void validateCompanyData(CompanyFinancialData companyData) {
        log.info("Validating data for company: {}", companyData.getCompanyName());

        IncomeStatement income = companyData.getLatestIncomeStatement();

        // Validate revenue is positive
        if (income.getTotalRevenue() == null ||
                income.getTotalRevenue().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            log.warn("Invalid or missing revenue for {}: {}",
                    companyData.getCompanyName(), income.getTotalRevenue());
        }

        // Validate airline metrics if available
        AirlineOperatingMetrics metrics = companyData.getLatestOperatingMetrics();
        if (metrics != null) {
            boolean valid = airlineCalculator.validateMetrics(metrics);
            if (!valid) {
                log.warn("Airline metrics validation failed for {}", companyData.getCompanyName());
            }
        }

        // Validate balance sheet balances
        BalanceSheet balance = companyData.getLatestBalanceSheet();
        if (balance.getTotalAssets() != null && balance.getTotalLiabilities() != null &&
                balance.getTotalEquity() != null) {
            java.math.BigDecimal sum = balance.getTotalLiabilities().add(balance.getTotalEquity());
            java.math.BigDecimal diff = balance.getTotalAssets().subtract(sum).abs();
            java.math.BigDecimal threshold = balance.getTotalAssets()
                    .multiply(java.math.BigDecimal.valueOf(0.01)); // 1% threshold

            if (diff.compareTo(threshold) > 0) {
                log.warn("Balance sheet may not balance for {}: Assets={}, Liabilities+Equity={}",
                        companyData.getCompanyName(), balance.getTotalAssets(), sum);
            }
        }
    }

    /**
     * Clear all caches
     */
    public void clearCache() {
        log.info("Clearing all caches");
        xbrlCache.invalidateAll();
        companyDataCache.invalidateAll();
    }

    /**
     * Get cache statistics
     */
    public String getCacheStats() {
        return String.format("XBRL Cache: %d entries, Company Data Cache: %d entries",
                xbrlCache.estimatedSize(), companyDataCache.estimatedSize());
    }
}
