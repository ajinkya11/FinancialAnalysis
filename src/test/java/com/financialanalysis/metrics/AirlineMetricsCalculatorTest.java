package com.financialanalysis.metrics;

import com.financialanalysis.models.AirlineOperatingMetrics;
import com.financialanalysis.models.IncomeStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AirlineMetricsCalculator
 */
class AirlineMetricsCalculatorTest {

    private AirlineMetricsCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new AirlineMetricsCalculator();
    }

    @Test
    void testCalculateLoadFactor() {
        AirlineOperatingMetrics metrics = AirlineOperatingMetrics.builder()
                .revenuePassengerMiles(80000000L)
                .availableSeatMiles(100000000L)
                .build();

        IncomeStatement income = IncomeStatement.builder()
                .totalRevenue(BigDecimal.valueOf(1000000000))
                .operatingExpenses(BigDecimal.valueOf(900000000))
                .build();

        calculator.calculateAirlineMetrics(metrics, income);

        assertNotNull(metrics.getLoadFactor());
        assertEquals(80.0, metrics.getLoadFactor(), 0.01);
    }

    @Test
    void testCalculateRASM() {
        AirlineOperatingMetrics metrics = AirlineOperatingMetrics.builder()
                .availableSeatMiles(100000000L)
                .revenuePassengerMiles(80000000L)
                .build();

        IncomeStatement income = IncomeStatement.builder()
                .totalRevenue(BigDecimal.valueOf(1000000000))
                .operatingExpenses(BigDecimal.valueOf(900000000))
                .build();

        calculator.calculateAirlineMetrics(metrics, income);

        assertNotNull(metrics.getRasm());
        // Revenue = $1B, ASM = 100M
        // RASM = ($1B * 100) / 100M = 10 cents
        assertEquals(1.0, metrics.getRasm().doubleValue(), 0.01);
    }

    @Test
    void testCalculateCASM() {
        AirlineOperatingMetrics metrics = AirlineOperatingMetrics.builder()
                .availableSeatMiles(100000000L)
                .revenuePassengerMiles(80000000L)
                .build();

        IncomeStatement income = IncomeStatement.builder()
                .totalRevenue(BigDecimal.valueOf(1000000000))
                .operatingExpenses(BigDecimal.valueOf(900000000))
                .build();

        calculator.calculateAirlineMetrics(metrics, income);

        assertNotNull(metrics.getCasm());
        // Operating expenses = $900M, ASM = 100M
        // CASM = ($900M * 100) / 100M = 9 cents
        assertEquals(0.9, metrics.getCasm().doubleValue(), 0.01);
    }

    @Test
    void testCalculateYield() {
        AirlineOperatingMetrics metrics = AirlineOperatingMetrics.builder()
                .revenuePassengerMiles(80000000L)
                .availableSeatMiles(100000000L)
                .passengerRevenue(BigDecimal.valueOf(800000000))
                .build();

        IncomeStatement income = IncomeStatement.builder()
                .totalRevenue(BigDecimal.valueOf(1000000000))
                .operatingExpenses(BigDecimal.valueOf(900000000))
                .build();

        calculator.calculateAirlineMetrics(metrics, income);

        assertNotNull(metrics.getYield());
        // Passenger Revenue = $800M, RPM = 80M
        // Yield = ($800M * 100) / 80M = 10 cents
        assertEquals(1.0, metrics.getYield().doubleValue(), 0.01);
    }

    @Test
    void testCalculateCASMEx() {
        AirlineOperatingMetrics metrics = AirlineOperatingMetrics.builder()
                .availableSeatMiles(100000000L)
                .revenuePassengerMiles(80000000L)
                .fuelCost(BigDecimal.valueOf(300000000))
                .build();

        IncomeStatement income = IncomeStatement.builder()
                .totalRevenue(BigDecimal.valueOf(1000000000))
                .operatingExpenses(BigDecimal.valueOf(900000000))
                .fuelCosts(BigDecimal.valueOf(300000000))
                .build();

        calculator.calculateAirlineMetrics(metrics, income);

        assertNotNull(metrics.getCasmEx());
        // Non-fuel expenses = $900M - $300M = $600M, ASM = 100M
        // CASM-ex = ($600M * 100) / 100M = 6 cents
        assertEquals(0.6, metrics.getCasmEx().doubleValue(), 0.01);
    }

    @Test
    void testCalculateFuelCostPercentage() {
        BigDecimal fuelCost = BigDecimal.valueOf(300000000);
        BigDecimal operatingExpenses = BigDecimal.valueOf(900000000);

        Double percentage = calculator.calculateFuelCostPercentage(fuelCost, operatingExpenses);

        assertNotNull(percentage);
        assertEquals(33.33, percentage, 0.01);
    }

    @Test
    void testValidateMetrics() {
        AirlineOperatingMetrics metrics = AirlineOperatingMetrics.builder()
                .loadFactor(80.0)
                .rasm(BigDecimal.valueOf(12.5))
                .casm(BigDecimal.valueOf(10.0))
                .build();

        boolean valid = calculator.validateMetrics(metrics);

        assertTrue(valid);
    }

    @Test
    void testValidateMetricsInvalidLoadFactor() {
        AirlineOperatingMetrics metrics = AirlineOperatingMetrics.builder()
                .loadFactor(120.0)  // Invalid: > 100%
                .rasm(BigDecimal.valueOf(12.5))
                .casm(BigDecimal.valueOf(10.0))
                .build();

        boolean valid = calculator.validateMetrics(metrics);

        assertFalse(valid);
    }

    @Test
    void testHandleNullValues() {
        AirlineOperatingMetrics metrics = AirlineOperatingMetrics.builder()
                .revenuePassengerMiles(null)
                .availableSeatMiles(null)
                .build();

        IncomeStatement income = IncomeStatement.builder()
                .totalRevenue(BigDecimal.valueOf(1000000000))
                .build();

        calculator.calculateAirlineMetrics(metrics, income);

        assertNull(metrics.getLoadFactor());
        assertNull(metrics.getRasm());
    }
}
