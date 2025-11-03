package com.financialanalysis.model;

/**
 * Detailed Cash Flow Statement for airline analysis
 */
public class DetailedCashFlow {
    private int fiscalYear;

    // Operating Activities
    private double netIncome;
    private double depreciation;
    private double amortization;
    private double deferredIncomeTaxes;
    private double stockBasedCompensation;
    private double impairmentCharges;
    private double gainsLossesOnAssetSales;
    private double gainsLossesOnDebtExtinguishment;
    private double pensionExpense;

    // Changes in Working Capital
    private double changeInReceivables;
    private double changeInPrepaidExpenses;
    private double changeInAccountsPayable;
    private double changeInAirTrafficLiability;  // KEY for airlines
    private double changeInAccruedLiabilities;
    private double otherWorkingCapitalChanges;

    private double netCashFromOperating;

    // Investing Activities
    private double capitalExpenditures;
    private double aircraftPurchases;
    private double preDeliveryDeposits;
    private double groundEquipmentPurchases;
    private double technologyInvestments;
    private double facilitiesInvestments;
    private double proceedsFromAssetSales;
    private double purchasesOfInvestments;
    private double salesOfInvestments;
    private double otherInvestingActivities;
    private double netCashFromInvesting;

    // Financing Activities
    private double proceedsFromDebtIssuance;
    private double debtRepayments;
    private double proceedsFromSaleLeasebacks;
    private double financeLeasePayments;
    private double proceedsFromStockIssuance;
    private double stockRepurchases;
    private double dividendsPaid;
    private double debtIssuanceCosts;
    private double otherFinancingActivities;
    private double netCashFromFinancing;

    // Summary
    private double netChangeInCash;
    private double cashAtBeginning;
    private double cashAtEnd;

    // Supplemental Information
    private double cashPaidForInterest;
    private double cashPaidForTaxes;

    // Calculated
    private double freeCashFlow;

    // Constructors
    public DetailedCashFlow() {}

    public DetailedCashFlow(int fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    // Getters and Setters
    public int getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(int fiscalYear) { this.fiscalYear = fiscalYear; }

    public double getNetIncome() { return netIncome; }
    public void setNetIncome(double netIncome) { this.netIncome = netIncome; }

    public double getDepreciation() { return depreciation; }
    public void setDepreciation(double depreciation) { this.depreciation = depreciation; }

    public double getAmortization() { return amortization; }
    public void setAmortization(double amortization) { this.amortization = amortization; }

    public double getDeferredIncomeTaxes() { return deferredIncomeTaxes; }
    public void setDeferredIncomeTaxes(double deferredIncomeTaxes) { this.deferredIncomeTaxes = deferredIncomeTaxes; }

    public double getStockBasedCompensation() { return stockBasedCompensation; }
    public void setStockBasedCompensation(double stockBasedCompensation) { this.stockBasedCompensation = stockBasedCompensation; }

    public double getImpairmentCharges() { return impairmentCharges; }
    public void setImpairmentCharges(double impairmentCharges) { this.impairmentCharges = impairmentCharges; }

    public double getGainsLossesOnAssetSales() { return gainsLossesOnAssetSales; }
    public void setGainsLossesOnAssetSales(double gainsLossesOnAssetSales) { this.gainsLossesOnAssetSales = gainsLossesOnAssetSales; }

    public double getGainsLossesOnDebtExtinguishment() { return gainsLossesOnDebtExtinguishment; }
    public void setGainsLossesOnDebtExtinguishment(double gainsLossesOnDebtExtinguishment) { this.gainsLossesOnDebtExtinguishment = gainsLossesOnDebtExtinguishment; }

    public double getPensionExpense() { return pensionExpense; }
    public void setPensionExpense(double pensionExpense) { this.pensionExpense = pensionExpense; }

    public double getChangeInReceivables() { return changeInReceivables; }
    public void setChangeInReceivables(double changeInReceivables) { this.changeInReceivables = changeInReceivables; }

    public double getChangeInPrepaidExpenses() { return changeInPrepaidExpenses; }
    public void setChangeInPrepaidExpenses(double changeInPrepaidExpenses) { this.changeInPrepaidExpenses = changeInPrepaidExpenses; }

    public double getChangeInAccountsPayable() { return changeInAccountsPayable; }
    public void setChangeInAccountsPayable(double changeInAccountsPayable) { this.changeInAccountsPayable = changeInAccountsPayable; }

    public double getChangeInAirTrafficLiability() { return changeInAirTrafficLiability; }
    public void setChangeInAirTrafficLiability(double changeInAirTrafficLiability) { this.changeInAirTrafficLiability = changeInAirTrafficLiability; }

    public double getChangeInAccruedLiabilities() { return changeInAccruedLiabilities; }
    public void setChangeInAccruedLiabilities(double changeInAccruedLiabilities) { this.changeInAccruedLiabilities = changeInAccruedLiabilities; }

    public double getOtherWorkingCapitalChanges() { return otherWorkingCapitalChanges; }
    public void setOtherWorkingCapitalChanges(double otherWorkingCapitalChanges) { this.otherWorkingCapitalChanges = otherWorkingCapitalChanges; }

    public double getNetCashFromOperating() { return netCashFromOperating; }
    public void setNetCashFromOperating(double netCashFromOperating) { this.netCashFromOperating = netCashFromOperating; }

    public double getCapitalExpenditures() { return capitalExpenditures; }
    public void setCapitalExpenditures(double capitalExpenditures) { this.capitalExpenditures = capitalExpenditures; }

    public double getAircraftPurchases() { return aircraftPurchases; }
    public void setAircraftPurchases(double aircraftPurchases) { this.aircraftPurchases = aircraftPurchases; }

    public double getPreDeliveryDeposits() { return preDeliveryDeposits; }
    public void setPreDeliveryDeposits(double preDeliveryDeposits) { this.preDeliveryDeposits = preDeliveryDeposits; }

    public double getGroundEquipmentPurchases() { return groundEquipmentPurchases; }
    public void setGroundEquipmentPurchases(double groundEquipmentPurchases) { this.groundEquipmentPurchases = groundEquipmentPurchases; }

    public double getTechnologyInvestments() { return technologyInvestments; }
    public void setTechnologyInvestments(double technologyInvestments) { this.technologyInvestments = technologyInvestments; }

    public double getFacilitiesInvestments() { return facilitiesInvestments; }
    public void setFacilitiesInvestments(double facilitiesInvestments) { this.facilitiesInvestments = facilitiesInvestments; }

    public double getProceedsFromAssetSales() { return proceedsFromAssetSales; }
    public void setProceedsFromAssetSales(double proceedsFromAssetSales) { this.proceedsFromAssetSales = proceedsFromAssetSales; }

    public double getPurchasesOfInvestments() { return purchasesOfInvestments; }
    public void setPurchasesOfInvestments(double purchasesOfInvestments) { this.purchasesOfInvestments = purchasesOfInvestments; }

    public double getSalesOfInvestments() { return salesOfInvestments; }
    public void setSalesOfInvestments(double salesOfInvestments) { this.salesOfInvestments = salesOfInvestments; }

    public double getOtherInvestingActivities() { return otherInvestingActivities; }
    public void setOtherInvestingActivities(double otherInvestingActivities) { this.otherInvestingActivities = otherInvestingActivities; }

    public double getNetCashFromInvesting() { return netCashFromInvesting; }
    public void setNetCashFromInvesting(double netCashFromInvesting) { this.netCashFromInvesting = netCashFromInvesting; }

    public double getProceedsFromDebtIssuance() { return proceedsFromDebtIssuance; }
    public void setProceedsFromDebtIssuance(double proceedsFromDebtIssuance) { this.proceedsFromDebtIssuance = proceedsFromDebtIssuance; }

    public double getDebtRepayments() { return debtRepayments; }
    public void setDebtRepayments(double debtRepayments) { this.debtRepayments = debtRepayments; }

    public double getProceedsFromSaleLeasebacks() { return proceedsFromSaleLeasebacks; }
    public void setProceedsFromSaleLeasebacks(double proceedsFromSaleLeasebacks) { this.proceedsFromSaleLeasebacks = proceedsFromSaleLeasebacks; }

    public double getFinanceLeasePayments() { return financeLeasePayments; }
    public void setFinanceLeasePayments(double financeLeasePayments) { this.financeLeasePayments = financeLeasePayments; }

    public double getProceedsFromStockIssuance() { return proceedsFromStockIssuance; }
    public void setProceedsFromStockIssuance(double proceedsFromStockIssuance) { this.proceedsFromStockIssuance = proceedsFromStockIssuance; }

    public double getStockRepurchases() { return stockRepurchases; }
    public void setStockRepurchases(double stockRepurchases) { this.stockRepurchases = stockRepurchases; }

    public double getDividendsPaid() { return dividendsPaid; }
    public void setDividendsPaid(double dividendsPaid) { this.dividendsPaid = dividendsPaid; }

    public double getDebtIssuanceCosts() { return debtIssuanceCosts; }
    public void setDebtIssuanceCosts(double debtIssuanceCosts) { this.debtIssuanceCosts = debtIssuanceCosts; }

    public double getOtherFinancingActivities() { return otherFinancingActivities; }
    public void setOtherFinancingActivities(double otherFinancingActivities) { this.otherFinancingActivities = otherFinancingActivities; }

    public double getNetCashFromFinancing() { return netCashFromFinancing; }
    public void setNetCashFromFinancing(double netCashFromFinancing) { this.netCashFromFinancing = netCashFromFinancing; }

    public double getNetChangeInCash() { return netChangeInCash; }
    public void setNetChangeInCash(double netChangeInCash) { this.netChangeInCash = netChangeInCash; }

    public double getCashAtBeginning() { return cashAtBeginning; }
    public void setCashAtBeginning(double cashAtBeginning) { this.cashAtBeginning = cashAtBeginning; }

    public double getCashAtEnd() { return cashAtEnd; }
    public void setCashAtEnd(double cashAtEnd) { this.cashAtEnd = cashAtEnd; }

    public double getCashPaidForInterest() { return cashPaidForInterest; }
    public void setCashPaidForInterest(double cashPaidForInterest) { this.cashPaidForInterest = cashPaidForInterest; }

    public double getCashPaidForTaxes() { return cashPaidForTaxes; }
    public void setCashPaidForTaxes(double cashPaidForTaxes) { this.cashPaidForTaxes = cashPaidForTaxes; }

    public double getFreeCashFlow() { return freeCashFlow; }
    public void setFreeCashFlow(double freeCashFlow) { this.freeCashFlow = freeCashFlow; }

    // Helper methods
    public void calculateFreeCashFlow() {
        // CapEx should be stored as positive (use of cash), so we subtract it
        // If it's stored as negative, take absolute value before subtracting
        double capexAbsolute = Math.abs(capitalExpenditures);
        this.freeCashFlow = netCashFromOperating - capexAbsolute;
    }
}
