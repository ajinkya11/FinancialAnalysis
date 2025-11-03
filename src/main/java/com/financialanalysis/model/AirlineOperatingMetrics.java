package com.financialanalysis.model;

/**
 * Airline-specific operating metrics (ASMs, RPMs, PRASM, CASM, etc.)
 * These are typically found in the MD&A section or Operating Statistics section
 */
public class AirlineOperatingMetrics {
    private int fiscalYear;
    private String period; // "Annual", "Q1", "Q2", etc.

    // Capacity Metrics (in millions)
    private double availableSeatMiles; // ASMs - total
    private double domesticASMs;
    private double internationalASMs;
    private double atlanticASMs;
    private double pacificASMs;
    private double latinAmericaASMs;
    private double mainlineASMs;
    private double regionalASMs;

    // Traffic Metrics (in millions)
    private double revenuePassengerMiles; // RPMs - total
    private double domesticRPMs;
    private double internationalRPMs;

    // Operations
    private int departures;
    private double blockHours;
    private int averageAircraftInFleet;
    private int aircraftAtPeriodEnd;

    // Load Factor (%)
    private double loadFactor;
    private double domesticLoadFactor;
    private double internationalLoadFactor;

    // Passengers
    private double passengersCarried; // in millions
    private double averageTripLength; // in miles

    // Unit Revenue Metrics (in cents)
    private double passengerRevenuePerASM; // PRASM
    private double domesticPRASM;
    private double internationalPRASM;
    private double totalRevenuePerASM; // RASM
    private double domesticRASM;
    private double internationalRASM;
    private double cargoRevenuePerASM;
    private double otherRevenuePerASM;
    private double yield; // passenger revenue per RPM

    // Unit Cost Metrics (in cents) - CRITICAL
    private double operatingCostPerASM; // CASM
    private double domesticCASM;
    private double internationalCASM;
    private double casmExcludingFuel; // CASM-ex - KEY EFFICIENCY METRIC
    private double domesticCASMEx;
    private double internationalCASMEx;
    private double fuelCostPerGallon;
    private double fuelGallonsConsumed; // in millions
    private double fuelExpensePerASM;

    // Employee Metrics
    private int fullTimeEmployees;
    private int pilots;
    private int flightAttendants;
    private int technicians;
    private int groundService;
    private double asmsPerEmployee;
    private double employeesPerAircraft;

    // Other Operational
    private double averageAircraftDailyUtilization; // block hours per day
    private double completionFactor; // % of scheduled flights completed
    private double onTimePerformance; // % within 14 minutes
    private double controllableCompletionFactor; // excludes weather
    private double mishandledBagsPer1000; // baggage performance

    // Constructors
    public AirlineOperatingMetrics() {}

    public AirlineOperatingMetrics(int fiscalYear, String period) {
        this.fiscalYear = fiscalYear;
        this.period = period;
    }

    // Getters and Setters
    public int getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(int fiscalYear) { this.fiscalYear = fiscalYear; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public double getAvailableSeatMiles() { return availableSeatMiles; }
    public void setAvailableSeatMiles(double availableSeatMiles) { this.availableSeatMiles = availableSeatMiles; }

    public double getDomesticASMs() { return domesticASMs; }
    public void setDomesticASMs(double domesticASMs) { this.domesticASMs = domesticASMs; }

    public double getInternationalASMs() { return internationalASMs; }
    public void setInternationalASMs(double internationalASMs) { this.internationalASMs = internationalASMs; }

    public double getAtlanticASMs() { return atlanticASMs; }
    public void setAtlanticASMs(double atlanticASMs) { this.atlanticASMs = atlanticASMs; }

    public double getPacificASMs() { return pacificASMs; }
    public void setPacificASMs(double pacificASMs) { this.pacificASMs = pacificASMs; }

    public double getLatinAmericaASMs() { return latinAmericaASMs; }
    public void setLatinAmericaASMs(double latinAmericaASMs) { this.latinAmericaASMs = latinAmericaASMs; }

    public double getMainlineASMs() { return mainlineASMs; }
    public void setMainlineASMs(double mainlineASMs) { this.mainlineASMs = mainlineASMs; }

    public double getRegionalASMs() { return regionalASMs; }
    public void setRegionalASMs(double regionalASMs) { this.regionalASMs = regionalASMs; }

    public double getRevenuePassengerMiles() { return revenuePassengerMiles; }
    public void setRevenuePassengerMiles(double revenuePassengerMiles) { this.revenuePassengerMiles = revenuePassengerMiles; }

    public double getDomesticRPMs() { return domesticRPMs; }
    public void setDomesticRPMs(double domesticRPMs) { this.domesticRPMs = domesticRPMs; }

    public double getInternationalRPMs() { return internationalRPMs; }
    public void setInternationalRPMs(double internationalRPMs) { this.internationalRPMs = internationalRPMs; }

    public int getDepartures() { return departures; }
    public void setDepartures(int departures) { this.departures = departures; }

    public double getBlockHours() { return blockHours; }
    public void setBlockHours(double blockHours) { this.blockHours = blockHours; }

    public int getAverageAircraftInFleet() { return averageAircraftInFleet; }
    public void setAverageAircraftInFleet(int averageAircraftInFleet) { this.averageAircraftInFleet = averageAircraftInFleet; }

    public int getAircraftAtPeriodEnd() { return aircraftAtPeriodEnd; }
    public void setAircraftAtPeriodEnd(int aircraftAtPeriodEnd) { this.aircraftAtPeriodEnd = aircraftAtPeriodEnd; }

    public double getLoadFactor() { return loadFactor; }
    public void setLoadFactor(double loadFactor) { this.loadFactor = loadFactor; }

    public double getDomesticLoadFactor() { return domesticLoadFactor; }
    public void setDomesticLoadFactor(double domesticLoadFactor) { this.domesticLoadFactor = domesticLoadFactor; }

    public double getInternationalLoadFactor() { return internationalLoadFactor; }
    public void setInternationalLoadFactor(double internationalLoadFactor) { this.internationalLoadFactor = internationalLoadFactor; }

    public double getPassengersCarried() { return passengersCarried; }
    public void setPassengersCarried(double passengersCarried) { this.passengersCarried = passengersCarried; }

    public double getAverageTripLength() { return averageTripLength; }
    public void setAverageTripLength(double averageTripLength) { this.averageTripLength = averageTripLength; }

    public double getPassengerRevenuePerASM() { return passengerRevenuePerASM; }
    public void setPassengerRevenuePerASM(double passengerRevenuePerASM) { this.passengerRevenuePerASM = passengerRevenuePerASM; }

    public double getDomesticPRASM() { return domesticPRASM; }
    public void setDomesticPRASM(double domesticPRASM) { this.domesticPRASM = domesticPRASM; }

    public double getInternationalPRASM() { return internationalPRASM; }
    public void setInternationalPRASM(double internationalPRASM) { this.internationalPRASM = internationalPRASM; }

    public double getTotalRevenuePerASM() { return totalRevenuePerASM; }
    public void setTotalRevenuePerASM(double totalRevenuePerASM) { this.totalRevenuePerASM = totalRevenuePerASM; }

    public double getDomesticRASM() { return domesticRASM; }
    public void setDomesticRASM(double domesticRASM) { this.domesticRASM = domesticRASM; }

    public double getInternationalRASM() { return internationalRASM; }
    public void setInternationalRASM(double internationalRASM) { this.internationalRASM = internationalRASM; }

    public double getCargoRevenuePerASM() { return cargoRevenuePerASM; }
    public void setCargoRevenuePerASM(double cargoRevenuePerASM) { this.cargoRevenuePerASM = cargoRevenuePerASM; }

    public double getOtherRevenuePerASM() { return otherRevenuePerASM; }
    public void setOtherRevenuePerASM(double otherRevenuePerASM) { this.otherRevenuePerASM = otherRevenuePerASM; }

    public double getYield() { return yield; }
    public void setYield(double yield) { this.yield = yield; }

    public double getOperatingCostPerASM() { return operatingCostPerASM; }
    public void setOperatingCostPerASM(double operatingCostPerASM) { this.operatingCostPerASM = operatingCostPerASM; }

    public double getDomesticCASM() { return domesticCASM; }
    public void setDomesticCASM(double domesticCASM) { this.domesticCASM = domesticCASM; }

    public double getInternationalCASM() { return internationalCASM; }
    public void setInternationalCASM(double internationalCASM) { this.internationalCASM = internationalCASM; }

    public double getCasmExcludingFuel() { return casmExcludingFuel; }
    public void setCasmExcludingFuel(double casmExcludingFuel) { this.casmExcludingFuel = casmExcludingFuel; }

    public double getDomesticCASMEx() { return domesticCASMEx; }
    public void setDomesticCASMEx(double domesticCASMEx) { this.domesticCASMEx = domesticCASMEx; }

    public double getInternationalCASMEx() { return internationalCASMEx; }
    public void setInternationalCASMEx(double internationalCASMEx) { this.internationalCASMEx = internationalCASMEx; }

    public double getFuelCostPerGallon() { return fuelCostPerGallon; }
    public void setFuelCostPerGallon(double fuelCostPerGallon) { this.fuelCostPerGallon = fuelCostPerGallon; }

    public double getFuelGallonsConsumed() { return fuelGallonsConsumed; }
    public void setFuelGallonsConsumed(double fuelGallonsConsumed) { this.fuelGallonsConsumed = fuelGallonsConsumed; }

    public double getFuelExpensePerASM() { return fuelExpensePerASM; }
    public void setFuelExpensePerASM(double fuelExpensePerASM) { this.fuelExpensePerASM = fuelExpensePerASM; }

    public int getFullTimeEmployees() { return fullTimeEmployees; }
    public void setFullTimeEmployees(int fullTimeEmployees) { this.fullTimeEmployees = fullTimeEmployees; }

    public int getPilots() { return pilots; }
    public void setPilots(int pilots) { this.pilots = pilots; }

    public int getFlightAttendants() { return flightAttendants; }
    public void setFlightAttendants(int flightAttendants) { this.flightAttendants = flightAttendants; }

    public int getTechnicians() { return technicians; }
    public void setTechnicians(int technicians) { this.technicians = technicians; }

    public int getGroundService() { return groundService; }
    public void setGroundService(int groundService) { this.groundService = groundService; }

    public double getAsmsPerEmployee() { return asmsPerEmployee; }
    public void setAsmsPerEmployee(double asmsPerEmployee) { this.asmsPerEmployee = asmsPerEmployee; }

    public double getEmployeesPerAircraft() { return employeesPerAircraft; }
    public void setEmployeesPerAircraft(double employeesPerAircraft) { this.employeesPerAircraft = employeesPerAircraft; }

    public double getAverageAircraftDailyUtilization() { return averageAircraftDailyUtilization; }
    public void setAverageAircraftDailyUtilization(double averageAircraftDailyUtilization) { this.averageAircraftDailyUtilization = averageAircraftDailyUtilization; }

    public double getCompletionFactor() { return completionFactor; }
    public void setCompletionFactor(double completionFactor) { this.completionFactor = completionFactor; }

    public double getOnTimePerformance() { return onTimePerformance; }
    public void setOnTimePerformance(double onTimePerformance) { this.onTimePerformance = onTimePerformance; }

    public double getControllableCompletionFactor() { return controllableCompletionFactor; }
    public void setControllableCompletionFactor(double controllableCompletionFactor) { this.controllableCompletionFactor = controllableCompletionFactor; }

    public double getMishandledBagsPer1000() { return mishandledBagsPer1000; }
    public void setMishandledBagsPer1000(double mishandledBagsPer1000) { this.mishandledBagsPer1000 = mishandledBagsPer1000; }

    // Helper methods to calculate derived metrics
    public void calculateLoadFactor() {
        if (availableSeatMiles > 0) {
            this.loadFactor = (revenuePassengerMiles / availableSeatMiles) * 100;
        }
    }

    public void calculateYield() {
        if (revenuePassengerMiles > 0) {
            // Yield is calculated from passenger revenue and RPMs
            // This would need passenger revenue passed in or stored
        }
    }

    public void calculateEmployeeProductivity() {
        if (fullTimeEmployees > 0) {
            this.asmsPerEmployee = availableSeatMiles / fullTimeEmployees;
        }
        if (averageAircraftInFleet > 0) {
            this.employeesPerAircraft = (double) fullTimeEmployees / averageAircraftInFleet;
        }
    }
}
