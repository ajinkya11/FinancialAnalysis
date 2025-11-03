package com.financialanalysis.model;

/**
 * Detailed Balance Sheet with comprehensive line items for airline analysis
 */
public class DetailedBalanceSheet {
    private int fiscalYear;

    // Current Assets
    private double cashAndCashEquivalents;
    private double shortTermInvestments;
    private double restrictedCash;
    private double accountsReceivable;
    private double allowanceForDoubtfulAccounts;
    private double prepaidExpenses;
    private double sparePartsAndSupplies;
    private double otherCurrentAssets;
    private double totalCurrentAssets;

    // Property, Plant & Equipment
    private double flightEquipment;
    private double groundEquipment;
    private double buildings;
    private double leaseholdImprovements;
    private double constructionInProgress;
    private double totalPPEAtCost;
    private double accumulatedDepreciation;
    private double netPPE;

    // Other Non-Current Assets
    private double operatingLeaseRightOfUseAssets;
    private double goodwill;
    private double intangibleAssets;
    private double routesAndSlots;
    private double accumulatedAmortization;
    private double netIntangibles;
    private double longTermInvestments;
    private double deferredTaxAssets;
    private double otherNonCurrentAssets;
    private double totalAssets;

    // Current Liabilities
    private double accountsPayable;
    private double accruedSalariesAndBenefits;
    private double airTrafficLiability;  // KEY for airlines - unearned ticket revenue
    private double currentDebt;
    private double currentOperatingLeaseLiabilities;
    private double currentFinanceLeaseLiabilities;
    private double otherCurrentLiabilities;
    private double totalCurrentLiabilities;

    // Non-Current Liabilities
    private double longTermDebt;
    private double longTermOperatingLeaseLiabilities;
    private double longTermFinanceLeaseLiabilities;
    private double pensionLiabilities;
    private double postRetirementBenefits;
    private double deferredTaxLiabilities;
    private double loyaltyProgramDeferredRevenue;
    private double otherLongTermLiabilities;
    private double totalLiabilities;

    // Stockholders' Equity
    private double preferredStock;
    private double preferredSharesOutstanding;
    private double commonStock;
    private double commonSharesAuthorized;
    private double commonSharesOutstanding;
    private double additionalPaidInCapital;
    private double treasuryStock;
    private double treasuryShares;
    private double retainedEarnings;
    private double accumulatedOtherComprehensiveIncome;
    private double totalStockholdersEquity;

    // Constructors
    public DetailedBalanceSheet() {}

    public DetailedBalanceSheet(int fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    // Getters and Setters
    public int getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(int fiscalYear) { this.fiscalYear = fiscalYear; }

    public double getCashAndCashEquivalents() { return cashAndCashEquivalents; }
    public void setCashAndCashEquivalents(double cashAndCashEquivalents) { this.cashAndCashEquivalents = cashAndCashEquivalents; }

    public double getShortTermInvestments() { return shortTermInvestments; }
    public void setShortTermInvestments(double shortTermInvestments) { this.shortTermInvestments = shortTermInvestments; }

    public double getRestrictedCash() { return restrictedCash; }
    public void setRestrictedCash(double restrictedCash) { this.restrictedCash = restrictedCash; }

    public double getAccountsReceivable() { return accountsReceivable; }
    public void setAccountsReceivable(double accountsReceivable) { this.accountsReceivable = accountsReceivable; }

    public double getAllowanceForDoubtfulAccounts() { return allowanceForDoubtfulAccounts; }
    public void setAllowanceForDoubtfulAccounts(double allowanceForDoubtfulAccounts) { this.allowanceForDoubtfulAccounts = allowanceForDoubtfulAccounts; }

    public double getPrepaidExpenses() { return prepaidExpenses; }
    public void setPrepaidExpenses(double prepaidExpenses) { this.prepaidExpenses = prepaidExpenses; }

    public double getSparePartsAndSupplies() { return sparePartsAndSupplies; }
    public void setSparePartsAndSupplies(double sparePartsAndSupplies) { this.sparePartsAndSupplies = sparePartsAndSupplies; }

    public double getOtherCurrentAssets() { return otherCurrentAssets; }
    public void setOtherCurrentAssets(double otherCurrentAssets) { this.otherCurrentAssets = otherCurrentAssets; }

    public double getTotalCurrentAssets() { return totalCurrentAssets; }
    public void setTotalCurrentAssets(double totalCurrentAssets) { this.totalCurrentAssets = totalCurrentAssets; }

    public double getFlightEquipment() { return flightEquipment; }
    public void setFlightEquipment(double flightEquipment) { this.flightEquipment = flightEquipment; }

    public double getGroundEquipment() { return groundEquipment; }
    public void setGroundEquipment(double groundEquipment) { this.groundEquipment = groundEquipment; }

    public double getBuildings() { return buildings; }
    public void setBuildings(double buildings) { this.buildings = buildings; }

    public double getLeaseholdImprovements() { return leaseholdImprovements; }
    public void setLeaseholdImprovements(double leaseholdImprovements) { this.leaseholdImprovements = leaseholdImprovements; }

    public double getConstructionInProgress() { return constructionInProgress; }
    public void setConstructionInProgress(double constructionInProgress) { this.constructionInProgress = constructionInProgress; }

    public double getTotalPPEAtCost() { return totalPPEAtCost; }
    public void setTotalPPEAtCost(double totalPPEAtCost) { this.totalPPEAtCost = totalPPEAtCost; }

    public double getAccumulatedDepreciation() { return accumulatedDepreciation; }
    public void setAccumulatedDepreciation(double accumulatedDepreciation) { this.accumulatedDepreciation = accumulatedDepreciation; }

    public double getNetPPE() { return netPPE; }
    public void setNetPPE(double netPPE) { this.netPPE = netPPE; }

    public double getOperatingLeaseRightOfUseAssets() { return operatingLeaseRightOfUseAssets; }
    public void setOperatingLeaseRightOfUseAssets(double operatingLeaseRightOfUseAssets) { this.operatingLeaseRightOfUseAssets = operatingLeaseRightOfUseAssets; }

    public double getGoodwill() { return goodwill; }
    public void setGoodwill(double goodwill) { this.goodwill = goodwill; }

    public double getIntangibleAssets() { return intangibleAssets; }
    public void setIntangibleAssets(double intangibleAssets) { this.intangibleAssets = intangibleAssets; }

    public double getRoutesAndSlots() { return routesAndSlots; }
    public void setRoutesAndSlots(double routesAndSlots) { this.routesAndSlots = routesAndSlots; }

    public double getAccumulatedAmortization() { return accumulatedAmortization; }
    public void setAccumulatedAmortization(double accumulatedAmortization) { this.accumulatedAmortization = accumulatedAmortization; }

    public double getNetIntangibles() { return netIntangibles; }
    public void setNetIntangibles(double netIntangibles) { this.netIntangibles = netIntangibles; }

    public double getLongTermInvestments() { return longTermInvestments; }
    public void setLongTermInvestments(double longTermInvestments) { this.longTermInvestments = longTermInvestments; }

    public double getDeferredTaxAssets() { return deferredTaxAssets; }
    public void setDeferredTaxAssets(double deferredTaxAssets) { this.deferredTaxAssets = deferredTaxAssets; }

    public double getOtherNonCurrentAssets() { return otherNonCurrentAssets; }
    public void setOtherNonCurrentAssets(double otherNonCurrentAssets) { this.otherNonCurrentAssets = otherNonCurrentAssets; }

    public double getTotalAssets() { return totalAssets; }
    public void setTotalAssets(double totalAssets) { this.totalAssets = totalAssets; }

    public double getAccountsPayable() { return accountsPayable; }
    public void setAccountsPayable(double accountsPayable) { this.accountsPayable = accountsPayable; }

    public double getAccruedSalariesAndBenefits() { return accruedSalariesAndBenefits; }
    public void setAccruedSalariesAndBenefits(double accruedSalariesAndBenefits) { this.accruedSalariesAndBenefits = accruedSalariesAndBenefits; }

    public double getAirTrafficLiability() { return airTrafficLiability; }
    public void setAirTrafficLiability(double airTrafficLiability) { this.airTrafficLiability = airTrafficLiability; }

    public double getCurrentDebt() { return currentDebt; }
    public void setCurrentDebt(double currentDebt) { this.currentDebt = currentDebt; }

    public double getCurrentOperatingLeaseLiabilities() { return currentOperatingLeaseLiabilities; }
    public void setCurrentOperatingLeaseLiabilities(double currentOperatingLeaseLiabilities) { this.currentOperatingLeaseLiabilities = currentOperatingLeaseLiabilities; }

    public double getCurrentFinanceLeaseLiabilities() { return currentFinanceLeaseLiabilities; }
    public void setCurrentFinanceLeaseLiabilities(double currentFinanceLeaseLiabilities) { this.currentFinanceLeaseLiabilities = currentFinanceLeaseLiabilities; }

    public double getOtherCurrentLiabilities() { return otherCurrentLiabilities; }
    public void setOtherCurrentLiabilities(double otherCurrentLiabilities) { this.otherCurrentLiabilities = otherCurrentLiabilities; }

    public double getTotalCurrentLiabilities() { return totalCurrentLiabilities; }
    public void setTotalCurrentLiabilities(double totalCurrentLiabilities) { this.totalCurrentLiabilities = totalCurrentLiabilities; }

    public double getLongTermDebt() { return longTermDebt; }
    public void setLongTermDebt(double longTermDebt) { this.longTermDebt = longTermDebt; }

    public double getLongTermOperatingLeaseLiabilities() { return longTermOperatingLeaseLiabilities; }
    public void setLongTermOperatingLeaseLiabilities(double longTermOperatingLeaseLiabilities) { this.longTermOperatingLeaseLiabilities = longTermOperatingLeaseLiabilities; }

    public double getLongTermFinanceLeaseLiabilities() { return longTermFinanceLeaseLiabilities; }
    public void setLongTermFinanceLeaseLiabilities(double longTermFinanceLeaseLiabilities) { this.longTermFinanceLeaseLiabilities = longTermFinanceLeaseLiabilities; }

    public double getPensionLiabilities() { return pensionLiabilities; }
    public void setPensionLiabilities(double pensionLiabilities) { this.pensionLiabilities = pensionLiabilities; }

    public double getPostRetirementBenefits() { return postRetirementBenefits; }
    public void setPostRetirementBenefits(double postRetirementBenefits) { this.postRetirementBenefits = postRetirementBenefits; }

    public double getDeferredTaxLiabilities() { return deferredTaxLiabilities; }
    public void setDeferredTaxLiabilities(double deferredTaxLiabilities) { this.deferredTaxLiabilities = deferredTaxLiabilities; }

    public double getLoyaltyProgramDeferredRevenue() { return loyaltyProgramDeferredRevenue; }
    public void setLoyaltyProgramDeferredRevenue(double loyaltyProgramDeferredRevenue) { this.loyaltyProgramDeferredRevenue = loyaltyProgramDeferredRevenue; }

    public double getOtherLongTermLiabilities() { return otherLongTermLiabilities; }
    public void setOtherLongTermLiabilities(double otherLongTermLiabilities) { this.otherLongTermLiabilities = otherLongTermLiabilities; }

    public double getTotalLiabilities() { return totalLiabilities; }
    public void setTotalLiabilities(double totalLiabilities) { this.totalLiabilities = totalLiabilities; }

    public double getPreferredStock() { return preferredStock; }
    public void setPreferredStock(double preferredStock) { this.preferredStock = preferredStock; }

    public double getPreferredSharesOutstanding() { return preferredSharesOutstanding; }
    public void setPreferredSharesOutstanding(double preferredSharesOutstanding) { this.preferredSharesOutstanding = preferredSharesOutstanding; }

    public double getCommonStock() { return commonStock; }
    public void setCommonStock(double commonStock) { this.commonStock = commonStock; }

    public double getCommonSharesAuthorized() { return commonSharesAuthorized; }
    public void setCommonSharesAuthorized(double commonSharesAuthorized) { this.commonSharesAuthorized = commonSharesAuthorized; }

    public double getCommonSharesOutstanding() { return commonSharesOutstanding; }
    public void setCommonSharesOutstanding(double commonSharesOutstanding) { this.commonSharesOutstanding = commonSharesOutstanding; }

    public double getAdditionalPaidInCapital() { return additionalPaidInCapital; }
    public void setAdditionalPaidInCapital(double additionalPaidInCapital) { this.additionalPaidInCapital = additionalPaidInCapital; }

    public double getTreasuryStock() { return treasuryStock; }
    public void setTreasuryStock(double treasuryStock) { this.treasuryStock = treasuryStock; }

    public double getTreasuryShares() { return treasuryShares; }
    public void setTreasuryShares(double treasuryShares) { this.treasuryShares = treasuryShares; }

    public double getRetainedEarnings() { return retainedEarnings; }
    public void setRetainedEarnings(double retainedEarnings) { this.retainedEarnings = retainedEarnings; }

    public double getAccumulatedOtherComprehensiveIncome() { return accumulatedOtherComprehensiveIncome; }
    public void setAccumulatedOtherComprehensiveIncome(double accumulatedOtherComprehensiveIncome) { this.accumulatedOtherComprehensiveIncome = accumulatedOtherComprehensiveIncome; }

    public double getTotalStockholdersEquity() { return totalStockholdersEquity; }
    public void setTotalStockholdersEquity(double totalStockholdersEquity) { this.totalStockholdersEquity = totalStockholdersEquity; }

    // Helper methods
    public double getTotalDebt() {
        return currentDebt + longTermDebt;
    }

    public double getTotalLeaseLiabilities() {
        return currentOperatingLeaseLiabilities + longTermOperatingLeaseLiabilities +
               currentFinanceLeaseLiabilities + longTermFinanceLeaseLiabilities;
    }

    public double getNetReceivables() {
        return accountsReceivable - allowanceForDoubtfulAccounts;
    }

    public double getWorkingCapital() {
        return totalCurrentAssets - totalCurrentLiabilities;
    }

    public double getBookValuePerShare() {
        return commonSharesOutstanding > 0 ? totalStockholdersEquity / commonSharesOutstanding : 0;
    }
}
