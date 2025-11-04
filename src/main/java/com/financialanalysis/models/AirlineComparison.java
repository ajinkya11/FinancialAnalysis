package com.financialanalysis.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Airline-specific operational comparison
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirlineComparison {
    // Operational efficiency
    private String casmLeader;
    private String rasmLeader;
    private String loadFactorLeader;
    private String yieldLeader;

    // Cost structure
    private String lowerFuelCostPercentage;
    private String lowerLaborCostPercentage;

    // Fleet efficiency
    private String newerFleet;
    private String higherUtilization;

    // Summary
    private String operationalEfficiencyWinner;
    private String costEfficiencyWinner;
    private String revenueGenerationWinner;
}
