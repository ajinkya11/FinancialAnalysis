package com.financialanalysis.metrics;

import com.financialanalysis.models.AirlineOperatingMetrics;
import com.financialanalysis.models.IncomeStatement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculator for airline-specific operating metrics
 * Calculates RASM, CASM, Load Factor, Yield, etc.
 */
@Slf4j
@Service
public class AirlineMetricsCalculator {

    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    private static final BigDecimal CENTS_MULTIPLIER = BigDecimal.valueOf(100);

    /**
     * Calculate and populate all airline metrics
     */
    public void calculateAirlineMetrics(AirlineOperatingMetrics metrics, IncomeStatement income) {
        log.info("Calculating airline metrics for fiscal year: {}", metrics.getFiscalYear());

        // Calculate load factor
        metrics.setLoadFactor(calculateLoadFactor(metrics.getRevenuePassengerMiles(),
                metrics.getAvailableSeatMiles()));

        // Calculate yield (cents per RPM)
        metrics.setYield(calculateYield(metrics.getPassengerRevenue(),
                metrics.getRevenuePassengerMiles()));

        // Calculate RASM (Revenue per Available Seat Mile)
        metrics.setRasm(calculateRASM(income.getTotalRevenue(),
                metrics.getAvailableSeatMiles()));

        // Calculate PRASM (Passenger Revenue per Available Seat Mile)
        metrics.setPrasm(calculateRASM(metrics.getPassengerRevenue(),
                metrics.getAvailableSeatMiles()));

        // Calculate CASM (Cost per Available Seat Mile)
        metrics.setCasm(calculateCASM(income.getOperatingExpenses(),
                metrics.getAvailableSeatMiles()));

        // Calculate CASM-ex (CASM excluding fuel)
        BigDecimal nonFuelOperatingExpenses = calculateNonFuelExpenses(income.getOperatingExpenses(),
                metrics.getFuelCost());
        metrics.setCasmEx(calculateCASM(nonFuelOperatingExpenses,
                metrics.getAvailableSeatMiles()));

        // Calculate break-even load factor
        metrics.setBreakEvenLoadFactor(calculateBreakEvenLoadFactor(metrics));

        log.info("Calculated metrics - Load Factor: {}%, RASM: {} cents, CASM: {} cents",
                metrics.getLoadFactor(), metrics.getRasm(), metrics.getCasm());
    }

    /**
     * Calculate Load Factor: (RPM / ASM) * 100
     */
    private Double calculateLoadFactor(Long rpm, Long asm) {
        if (rpm == null || asm == null || asm == 0) {
            log.warn("Cannot calculate load factor: RPM={}, ASM={}", rpm, asm);
            return null;
        }

        double loadFactor = (double) rpm / asm * 100.0;

        // Validate load factor
        if (loadFactor < 0 || loadFactor > 100) {
            log.warn("Invalid load factor calculated: {}%. This may indicate data quality issues.", loadFactor);
        }

        return loadFactor;
    }

    /**
     * Calculate Yield: Passenger Revenue / RPM (in cents)
     */
    private BigDecimal calculateYield(BigDecimal passengerRevenue, Long rpm) {
        if (passengerRevenue == null || rpm == null || rpm == 0) {
            log.warn("Cannot calculate yield: Revenue={}, RPM={}", passengerRevenue, rpm);
            return null;
        }

        // Convert to cents per RPM
        BigDecimal yieldValue = passengerRevenue
                .multiply(CENTS_MULTIPLIER)
                .divide(BigDecimal.valueOf(rpm), SCALE, ROUNDING);

        // Validate yield (typically between 5 and 20 cents)
        if (yieldValue.compareTo(BigDecimal.valueOf(2)) < 0 ||
                yieldValue.compareTo(BigDecimal.valueOf(50)) > 0) {
            log.warn("Unusual yield calculated: {} cents. This may indicate data quality issues.", yieldValue);
        }

        return yieldValue;
    }

    /**
     * Calculate RASM: Revenue / ASM (in cents)
     */
    private BigDecimal calculateRASM(BigDecimal revenue, Long asm) {
        if (revenue == null || asm == null || asm == 0) {
            log.warn("Cannot calculate RASM: Revenue={}, ASM={}", revenue, asm);
            return null;
        }

        // Convert to cents per ASM
        BigDecimal rasm = revenue
                .multiply(CENTS_MULTIPLIER)
                .divide(BigDecimal.valueOf(asm), SCALE, ROUNDING);

        // Validate RASM (typically between 8 and 20 cents)
        if (rasm.compareTo(BigDecimal.valueOf(5)) < 0 ||
                rasm.compareTo(BigDecimal.valueOf(30)) > 0) {
            log.warn("Unusual RASM calculated: {} cents. This may indicate data quality issues.", rasm);
        }

        return rasm;
    }

    /**
     * Calculate CASM: Operating Expenses / ASM (in cents)
     */
    private BigDecimal calculateCASM(BigDecimal operatingExpenses, Long asm) {
        if (operatingExpenses == null || asm == null || asm == 0) {
            log.warn("Cannot calculate CASM: Expenses={}, ASM={}", operatingExpenses, asm);
            return null;
        }

        // Convert to cents per ASM
        BigDecimal casm = operatingExpenses
                .multiply(CENTS_MULTIPLIER)
                .divide(BigDecimal.valueOf(asm), SCALE, ROUNDING);

        // Validate CASM (typically between 8 and 20 cents)
        if (casm.compareTo(BigDecimal.valueOf(5)) < 0 ||
                casm.compareTo(BigDecimal.valueOf(30)) > 0) {
            log.warn("Unusual CASM calculated: {} cents. This may indicate data quality issues.", casm);
        }

        return casm;
    }

    /**
     * Calculate non-fuel operating expenses
     */
    private BigDecimal calculateNonFuelExpenses(BigDecimal operatingExpenses, BigDecimal fuelCost) {
        if (operatingExpenses == null) {
            return null;
        }

        BigDecimal fuel = fuelCost != null ? fuelCost : BigDecimal.ZERO;
        return operatingExpenses.subtract(fuel);
    }

    /**
     * Calculate Break-Even Load Factor
     * Formula: (CASM / (CASM + Yield)) * 100
     */
    private Double calculateBreakEvenLoadFactor(AirlineOperatingMetrics metrics) {
        if (metrics.getCasm() == null || metrics.getYield() == null) {
            log.warn("Cannot calculate break-even load factor: CASM={}, Yield={}",
                    metrics.getCasm(), metrics.getYield());
            return null;
        }

        if (metrics.getCasm().add(metrics.getYield()).compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        double breakEven = metrics.getCasm()
                .divide(metrics.getCasm().add(metrics.getYield()), SCALE, ROUNDING)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();

        // Validate break-even load factor
        if (breakEven < 0 || breakEven > 100) {
            log.warn("Invalid break-even load factor: {}%. This may indicate data quality issues.", breakEven);
        }

        return breakEven;
    }

    /**
     * Calculate fuel cost as percentage of operating expenses
     */
    public Double calculateFuelCostPercentage(BigDecimal fuelCost, BigDecimal operatingExpenses) {
        if (fuelCost == null || operatingExpenses == null ||
                operatingExpenses.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return fuelCost.divide(operatingExpenses, SCALE, ROUNDING)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Calculate labor cost as percentage of operating expenses
     */
    public Double calculateLaborCostPercentage(BigDecimal laborCost, BigDecimal operatingExpenses) {
        if (laborCost == null || operatingExpenses == null ||
                operatingExpenses.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return laborCost.divide(operatingExpenses, SCALE, ROUNDING)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Calculate revenue per employee
     */
    public BigDecimal calculateRevenuePerEmployee(BigDecimal revenue, Long employees) {
        if (revenue == null || employees == null || employees == 0) {
            return null;
        }

        return revenue.divide(BigDecimal.valueOf(employees), SCALE, ROUNDING);
    }

    /**
     * Calculate operating expense per ASM growth
     */
    public Double calculateCASMGrowth(BigDecimal currentCASM, BigDecimal previousCASM) {
        if (currentCASM == null || previousCASM == null ||
                previousCASM.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return currentCASM.subtract(previousCASM)
                .divide(previousCASM, SCALE, ROUNDING)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Validate airline metrics for data quality
     */
    public boolean validateMetrics(AirlineOperatingMetrics metrics) {
        boolean valid = true;

        // Validate load factor
        if (metrics.getLoadFactor() != null &&
                (metrics.getLoadFactor() < 0 || metrics.getLoadFactor() > 100)) {
            log.error("Invalid load factor: {}%", metrics.getLoadFactor());
            valid = false;
        }

        // Validate RASM is positive and reasonable
        if (metrics.getRasm() != null &&
                (metrics.getRasm().compareTo(BigDecimal.ZERO) <= 0 ||
                        metrics.getRasm().compareTo(BigDecimal.valueOf(50)) > 0)) {
            log.error("Invalid RASM: {} cents", metrics.getRasm());
            valid = false;
        }

        // Validate CASM is positive and reasonable
        if (metrics.getCasm() != null &&
                (metrics.getCasm().compareTo(BigDecimal.ZERO) <= 0 ||
                        metrics.getCasm().compareTo(BigDecimal.valueOf(50)) > 0)) {
            log.error("Invalid CASM: {} cents", metrics.getCasm());
            valid = false;
        }

        // RASM should generally be higher than CASM for profitable operations
        if (metrics.getRasm() != null && metrics.getCasm() != null &&
                metrics.getRasm().compareTo(metrics.getCasm()) < 0) {
            log.warn("RASM ({}) is lower than CASM ({}), indicating unprofitable operations",
                    metrics.getRasm(), metrics.getCasm());
        }

        return valid;
    }
}
