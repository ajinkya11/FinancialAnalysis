package com.financialanalysis.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents airline-specific operating metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirlineOperatingMetrics {
    private String fiscalYear;
    private String fiscalPeriod;

    // Capacity metrics
    private Long availableSeatMiles;  // ASM
    private Long revenuePassengerMiles;  // RPM
    private Long departures;
    private Long aircraftInService;
    private Integer averageStageLength;
    private Double averageDailyUtilization;

    // Performance metrics
    private Double loadFactor;  // RPM / ASM * 100
    private BigDecimal yield;  // Passenger Revenue / RPM (in cents)
    private BigDecimal rasm;  // Revenue per ASM (in cents)
    private BigDecimal prasm;  // Passenger RASM (in cents)
    private BigDecimal casm;  // Cost per ASM (in cents)
    private BigDecimal casmEx;  // CASM excluding fuel (in cents)
    private Double breakEvenLoadFactor;

    // Revenue metrics
    private BigDecimal passengerRevenue;
    private BigDecimal cargoRevenue;
    private BigDecimal ancillaryRevenue;

    // Cost metrics
    private BigDecimal fuelCost;
    private BigDecimal fuelCostPerGallon;
    private Long fuelConsumptionGallons;
    private BigDecimal laborCost;
    private BigDecimal maintenanceCost;

    // Fleet information
    private Integer ownedAircraft;
    private Integer leasedAircraft;
    private Integer totalFleetSize;
    private Double averageAircraftAge;

    // On-time performance (if available)
    private Double onTimePerformance;
    private Double completionFactor;

    // Employee metrics
    private Long fullTimeEmployees;
    private Long pilots;
    private Long flightAttendants;
}
