package com.financialanalysis.metrics;

import com.financialanalysis.models.BalanceSheet;
import com.financialanalysis.models.CashFlowStatement;
import com.financialanalysis.models.IncomeStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FinancialMetricsCalculator
 */
class FinancialMetricsCalculatorTest {

    private FinancialMetricsCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new FinancialMetricsCalculator();
    }

    @Test
    void testCalculateROA() {
        IncomeStatement income = IncomeStatement.builder()
                .netIncome(BigDecimal.valueOf(1000000))
                .build();

        BalanceSheet balance = BalanceSheet.builder()
                .totalAssets(BigDecimal.valueOf(10000000))
                .build();

        CashFlowStatement cashFlow = CashFlowStatement.builder()
                .operatingCashFlow(BigDecimal.valueOf(1500000))
                .build();

        var metrics = calculator.calculateMetrics(income, balance, cashFlow, null);

        assertNotNull(metrics);
        assertEquals(10.0, metrics.getReturnOnAssets(), 0.01);
    }

    @Test
    void testCalculateROE() {
        IncomeStatement income = IncomeStatement.builder()
                .netIncome(BigDecimal.valueOf(1000000))
                .build();

        BalanceSheet balance = BalanceSheet.builder()
                .totalAssets(BigDecimal.valueOf(10000000))
                .totalEquity(BigDecimal.valueOf(5000000))
                .build();

        CashFlowStatement cashFlow = CashFlowStatement.builder()
                .operatingCashFlow(BigDecimal.valueOf(1500000))
                .build();

        var metrics = calculator.calculateMetrics(income, balance, cashFlow, null);

        assertNotNull(metrics);
        assertEquals(20.0, metrics.getReturnOnEquity(), 0.01);
    }

    @Test
    void testCalculateCurrentRatio() {
        BalanceSheet balance = BalanceSheet.builder()
                .currentAssets(BigDecimal.valueOf(5000000))
                .currentLiabilities(BigDecimal.valueOf(2500000))
                .build();

        calculator.populateBalanceSheetCalculations(balance);

        assertNotNull(balance.getCurrentRatio());
        assertEquals(2.0, balance.getCurrentRatio(), 0.01);
    }

    @Test
    void testCalculateWorkingCapital() {
        BalanceSheet balance = BalanceSheet.builder()
                .currentAssets(BigDecimal.valueOf(5000000))
                .currentLiabilities(BigDecimal.valueOf(2500000))
                .build();

        calculator.populateBalanceSheetCalculations(balance);

        assertNotNull(balance.getWorkingCapital());
        assertEquals(2500000, balance.getWorkingCapital().doubleValue(), 0.01);
    }

    @Test
    void testCalculateGrowthRate() {
        BigDecimal current = BigDecimal.valueOf(1200000);
        BigDecimal previous = BigDecimal.valueOf(1000000);

        Double growthRate = calculator.calculateGrowthRate(current, previous);

        assertNotNull(growthRate);
        assertEquals(20.0, growthRate, 0.01);
    }

    @Test
    void testCalculateGrowthRateWithNegativeChange() {
        BigDecimal current = BigDecimal.valueOf(800000);
        BigDecimal previous = BigDecimal.valueOf(1000000);

        Double growthRate = calculator.calculateGrowthRate(current, previous);

        assertNotNull(growthRate);
        assertEquals(-20.0, growthRate, 0.01);
    }

    @Test
    void testCalculateMargins() {
        IncomeStatement income = IncomeStatement.builder()
                .totalRevenue(BigDecimal.valueOf(10000000))
                .grossProfit(BigDecimal.valueOf(4000000))
                .operatingIncome(BigDecimal.valueOf(2000000))
                .netIncome(BigDecimal.valueOf(1000000))
                .build();

        calculator.populateIncomeStatementCalculations(income, null);

        assertEquals(40.0, income.getGrossMargin(), 0.01);
        assertEquals(20.0, income.getOperatingMargin(), 0.01);
        assertEquals(10.0, income.getNetMargin(), 0.01);
    }

    @Test
    void testHandleNullValues() {
        IncomeStatement income = IncomeStatement.builder()
                .netIncome(null)
                .build();

        BalanceSheet balance = BalanceSheet.builder()
                .totalAssets(null)
                .build();

        CashFlowStatement cashFlow = CashFlowStatement.builder()
                .operatingCashFlow(null)
                .build();

        var metrics = calculator.calculateMetrics(income, balance, cashFlow, null);

        assertNotNull(metrics);
        assertNull(metrics.getReturnOnAssets());
        assertNull(metrics.getReturnOnEquity());
    }
}
