package com.financialanalysis.model;

/**
 * Detailed Income Statement with comprehensive line items for airline analysis
 */
public class DetailedIncomeStatement {
    private int fiscalYear;

    // Revenue Line Items
    private double passengerRevenue;
    private double cargoRevenue;
    private double otherOperatingRevenue;
    private double loyaltyProgramRevenue;
    private double baggageFees;
    private double changeFees;
    private double seatSelectionFees;
    private double totalOperatingRevenue;

    // Operating Expenses
    private double aircraftFuel;
    private double fuelGallonsConsumed;
    private double averageFuelPricePerGallon;
    private double salariesAndRelatedCosts;
    private int numberOfEmployees;
    private double regionalCapacityPurchase;
    private double landingFeesAndRent;
    private double aircraftMaintenance;
    private double depreciation;
    private double amortization;
    private double distributionExpenses;
    private double aircraftRent;
    private double specialCharges;
    private double otherOperatingExpenses;
    private double totalOperatingExpenses;

    // Operating Income
    private double operatingIncome;

    // Non-Operating Items
    private double interestExpense;
    private double interestIncome;
    private double otherIncomeExpense;
    private double pretaxIncome;
    private double incomeTaxExpense;
    private double effectiveTaxRate;

    // Bottom Line
    private double netIncome;
    private double netIncomeAvailableToCommon;

    // Per Share Data
    private double basicEPS;
    private double dilutedEPS;
    private double weightedAverageSharesBasic;
    private double weightedAverageSharesDiluted;
    private double shareCountYearEnd;

    // Calculated Metrics
    private double ebit;
    private double ebitda;

    // Constructors
    public DetailedIncomeStatement() {}

    public DetailedIncomeStatement(int fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    // Getters and Setters
    public int getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(int fiscalYear) { this.fiscalYear = fiscalYear; }

    public double getPassengerRevenue() { return passengerRevenue; }
    public void setPassengerRevenue(double passengerRevenue) { this.passengerRevenue = passengerRevenue; }

    public double getCargoRevenue() { return cargoRevenue; }
    public void setCargoRevenue(double cargoRevenue) { this.cargoRevenue = cargoRevenue; }

    public double getOtherOperatingRevenue() { return otherOperatingRevenue; }
    public void setOtherOperatingRevenue(double otherOperatingRevenue) { this.otherOperatingRevenue = otherOperatingRevenue; }

    public double getLoyaltyProgramRevenue() { return loyaltyProgramRevenue; }
    public void setLoyaltyProgramRevenue(double loyaltyProgramRevenue) { this.loyaltyProgramRevenue = loyaltyProgramRevenue; }

    public double getBaggageFees() { return baggageFees; }
    public void setBaggageFees(double baggageFees) { this.baggageFees = baggageFees; }

    public double getChangeFees() { return changeFees; }
    public void setChangeFees(double changeFees) { this.changeFees = changeFees; }

    public double getSeatSelectionFees() { return seatSelectionFees; }
    public void setSeatSelectionFees(double seatSelectionFees) { this.seatSelectionFees = seatSelectionFees; }

    public double getTotalOperatingRevenue() { return totalOperatingRevenue; }
    public void setTotalOperatingRevenue(double totalOperatingRevenue) { this.totalOperatingRevenue = totalOperatingRevenue; }

    public double getAircraftFuel() { return aircraftFuel; }
    public void setAircraftFuel(double aircraftFuel) { this.aircraftFuel = aircraftFuel; }

    public double getFuelGallonsConsumed() { return fuelGallonsConsumed; }
    public void setFuelGallonsConsumed(double fuelGallonsConsumed) { this.fuelGallonsConsumed = fuelGallonsConsumed; }

    public double getAverageFuelPricePerGallon() { return averageFuelPricePerGallon; }
    public void setAverageFuelPricePerGallon(double averageFuelPricePerGallon) { this.averageFuelPricePerGallon = averageFuelPricePerGallon; }

    public double getSalariesAndRelatedCosts() { return salariesAndRelatedCosts; }
    public void setSalariesAndRelatedCosts(double salariesAndRelatedCosts) { this.salariesAndRelatedCosts = salariesAndRelatedCosts; }

    public int getNumberOfEmployees() { return numberOfEmployees; }
    public void setNumberOfEmployees(int numberOfEmployees) { this.numberOfEmployees = numberOfEmployees; }

    public double getRegionalCapacityPurchase() { return regionalCapacityPurchase; }
    public void setRegionalCapacityPurchase(double regionalCapacityPurchase) { this.regionalCapacityPurchase = regionalCapacityPurchase; }

    public double getLandingFeesAndRent() { return landingFeesAndRent; }
    public void setLandingFeesAndRent(double landingFeesAndRent) { this.landingFeesAndRent = landingFeesAndRent; }

    public double getAircraftMaintenance() { return aircraftMaintenance; }
    public void setAircraftMaintenance(double aircraftMaintenance) { this.aircraftMaintenance = aircraftMaintenance; }

    public double getDepreciation() { return depreciation; }
    public void setDepreciation(double depreciation) { this.depreciation = depreciation; }

    public double getAmortization() { return amortization; }
    public void setAmortization(double amortization) { this.amortization = amortization; }

    public double getDistributionExpenses() { return distributionExpenses; }
    public void setDistributionExpenses(double distributionExpenses) { this.distributionExpenses = distributionExpenses; }

    public double getAircraftRent() { return aircraftRent; }
    public void setAircraftRent(double aircraftRent) { this.aircraftRent = aircraftRent; }

    public double getSpecialCharges() { return specialCharges; }
    public void setSpecialCharges(double specialCharges) { this.specialCharges = specialCharges; }

    public double getOtherOperatingExpenses() { return otherOperatingExpenses; }
    public void setOtherOperatingExpenses(double otherOperatingExpenses) { this.otherOperatingExpenses = otherOperatingExpenses; }

    public double getTotalOperatingExpenses() { return totalOperatingExpenses; }
    public void setTotalOperatingExpenses(double totalOperatingExpenses) { this.totalOperatingExpenses = totalOperatingExpenses; }

    public double getOperatingIncome() { return operatingIncome; }
    public void setOperatingIncome(double operatingIncome) { this.operatingIncome = operatingIncome; }

    public double getInterestExpense() { return interestExpense; }
    public void setInterestExpense(double interestExpense) { this.interestExpense = interestExpense; }

    public double getInterestIncome() { return interestIncome; }
    public void setInterestIncome(double interestIncome) { this.interestIncome = interestIncome; }

    public double getOtherIncomeExpense() { return otherIncomeExpense; }
    public void setOtherIncomeExpense(double otherIncomeExpense) { this.otherIncomeExpense = otherIncomeExpense; }

    public double getPretaxIncome() { return pretaxIncome; }
    public void setPretaxIncome(double pretaxIncome) { this.pretaxIncome = pretaxIncome; }

    public double getIncomeTaxExpense() { return incomeTaxExpense; }
    public void setIncomeTaxExpense(double incomeTaxExpense) { this.incomeTaxExpense = incomeTaxExpense; }

    public double getEffectiveTaxRate() { return effectiveTaxRate; }
    public void setEffectiveTaxRate(double effectiveTaxRate) { this.effectiveTaxRate = effectiveTaxRate; }

    public double getNetIncome() { return netIncome; }
    public void setNetIncome(double netIncome) { this.netIncome = netIncome; }

    public double getNetIncomeAvailableToCommon() { return netIncomeAvailableToCommon; }
    public void setNetIncomeAvailableToCommon(double netIncomeAvailableToCommon) { this.netIncomeAvailableToCommon = netIncomeAvailableToCommon; }

    public double getBasicEPS() { return basicEPS; }
    public void setBasicEPS(double basicEPS) { this.basicEPS = basicEPS; }

    public double getDilutedEPS() { return dilutedEPS; }
    public void setDilutedEPS(double dilutedEPS) { this.dilutedEPS = dilutedEPS; }

    public double getWeightedAverageSharesBasic() { return weightedAverageSharesBasic; }
    public void setWeightedAverageSharesBasic(double weightedAverageSharesBasic) { this.weightedAverageSharesBasic = weightedAverageSharesBasic; }

    public double getWeightedAverageSharesDiluted() { return weightedAverageSharesDiluted; }
    public void setWeightedAverageSharesDiluted(double weightedAverageSharesDiluted) { this.weightedAverageSharesDiluted = weightedAverageSharesDiluted; }

    public double getShareCountYearEnd() { return shareCountYearEnd; }
    public void setShareCountYearEnd(double shareCountYearEnd) { this.shareCountYearEnd = shareCountYearEnd; }

    public double getEbit() { return ebit; }
    public void setEbit(double ebit) { this.ebit = ebit; }

    public double getEbitda() { return ebitda; }
    public void setEbitda(double ebitda) { this.ebitda = ebitda; }

    // Helper methods
    public double getPassengerRevenuePercentage() {
        return totalOperatingRevenue > 0 ? (passengerRevenue / totalOperatingRevenue) * 100 : 0;
    }

    public double getCargoRevenuePercentage() {
        return totalOperatingRevenue > 0 ? (cargoRevenue / totalOperatingRevenue) * 100 : 0;
    }

    public double getFuelAsPercentOfOpex() {
        return totalOperatingExpenses > 0 ? (aircraftFuel / totalOperatingExpenses) * 100 : 0;
    }

    public double getSalariesAsPercentOfRevenue() {
        return totalOperatingRevenue > 0 ? (salariesAndRelatedCosts / totalOperatingRevenue) * 100 : 0;
    }

    public double getAverageCompensationPerEmployee() {
        return numberOfEmployees > 0 ? salariesAndRelatedCosts / numberOfEmployees : 0;
    }
}
