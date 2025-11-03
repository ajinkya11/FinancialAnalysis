package com.financialanalysis.parser;

import com.financialanalysis.model.*;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * Parses XBRL (eXtensible Business Reporting Language) files to extract financial data
 * XBRL is the standard format for company financial statements filed with the SEC
 * Enhanced to extract comprehensive detailed financial statements
 */
public class XBRLParser {
    private static final Logger logger = LoggerFactory.getLogger(XBRLParser.class);

    /**
     * Parses an XBRL file and extracts financial data
     */
    public FinancialStatement parse(File xbrlFile) throws Exception {
        logger.info("Parsing XBRL file: {}", xbrlFile.getName());

        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = saxBuilder.build(xbrlFile);
        Element rootElement = document.getRootElement();

        Namespace usGaapNs = findNamespace(rootElement, "us-gaap");

        int fiscalYear = extractFiscalYear(xbrlFile.getName(), rootElement);
        FinancialStatement statement = new FinancialStatement(fiscalYear);

        logger.info("Extracting data for fiscal year: {}", fiscalYear);

        // Extract financial data using US-GAAP tags
        extractIncomeStatementData(statement, rootElement, usGaapNs);
        extractBalanceSheetData(statement, rootElement, usGaapNs);
        extractCashFlowData(statement, rootElement, usGaapNs);
        extractPerShareData(statement, rootElement, usGaapNs);

        // Calculate derived values
        calculateDerivedValues(statement, rootElement, usGaapNs);

        logger.info("Successfully parsed XBRL file - Revenue: {}, Net Income: {}, Total Assets: {}",
                statement.getRevenue(), statement.getNetIncome(), statement.getTotalAssets());

        return statement;
    }

    /**
     * Extracts income statement data
     */
    private void extractIncomeStatementData(FinancialStatement statement, Element root, Namespace ns) {
        // Revenue tags (multiple variations)
        statement.setRevenue(extractValue(root, ns,
                "Revenues",
                "RevenueFromContractWithCustomerExcludingAssessedTax",
                "SalesRevenueNet",
                "RevenueFromContractWithCustomer",
                "RevenueFromContractWithCustomerIncludingAssessedTax"
        ));

        // Cost of Goods Sold (may be 0 for service companies like airlines)
        statement.setCostOfGoodsSold(extractValue(root, ns,
                "CostOfRevenue",
                "CostOfGoodsAndServicesSold",
                "CostOfSales",
                "CostOfGoodsSold"
        ));

        // Gross Profit
        statement.setGrossProfit(extractValue(root, ns,
                "GrossProfit"
        ));

        // Operating Income
        statement.setOperatingIncome(extractValue(root, ns,
                "OperatingIncomeLoss",
                "IncomeLossFromContinuingOperationsBeforeIncomeTaxesExtraordinaryItemsNoncontrollingInterest",
                "OperatingIncome"
        ));

        // Net Income
        statement.setNetIncome(extractValue(root, ns,
                "NetIncomeLoss",
                "ProfitLoss",
                "NetIncomeLossAvailableToCommonStockholdersBasic",
                "NetIncomeLossAttributableToParent"
        ));

        // EBIT (Earnings Before Interest and Taxes)
        statement.setEbit(extractValue(root, ns,
                "IncomeLossFromContinuingOperationsBeforeIncomeTaxesExtraordinaryItemsNoncontrollingInterest",
                "IncomeLossFromContinuingOperationsBeforeIncomeTaxesMinorityInterestAndIncomeLossFromEquityMethodInvestments"
        ));

        // Interest Expense
        statement.setInterestExpense(extractValue(root, ns,
                "InterestExpense",
                "InterestExpenseDebt",
                "InterestPaidNet",
                "InterestExpenseDebtExcludingAmortization"
        ));
    }

    /**
     * Extracts balance sheet data
     */
    private void extractBalanceSheetData(FinancialStatement statement, Element root, Namespace ns) {
        // Assets
        statement.setTotalAssets(extractValue(root, ns,
                "Assets"
        ));

        statement.setCurrentAssets(extractValue(root, ns,
                "AssetsCurrent"
        ));

        statement.setCash(extractValue(root, ns,
                "CashAndCashEquivalentsAtCarryingValue",
                "Cash",
                "CashCashEquivalentsAndShortTermInvestments"
        ));

        statement.setMarketableSecurities(extractValue(root, ns,
                "MarketableSecurities",
                "AvailableForSaleSecuritiesCurrent",
                "ShortTermInvestments",
                "MarketableSecuritiesCurrent"
        ));

        statement.setAccountsReceivable(extractValue(root, ns,
                "AccountsReceivableNetCurrent",
                "ReceivablesNetCurrent",
                "AccountsReceivableNet"
        ));

        statement.setInventory(extractValue(root, ns,
                "InventoryNet",
                "Inventory"
        ));

        // Liabilities - FIXED: Removed "LiabilitiesAndStockholdersEquity" which was returning total assets!
        statement.setTotalLiabilities(extractValue(root, ns,
                "Liabilities"
        ));

        statement.setCurrentLiabilities(extractValue(root, ns,
                "LiabilitiesCurrent"
        ));

        // Long-term debt
        statement.setLongTermDebt(extractValue(root, ns,
                "LongTermDebt",
                "LongTermDebtNoncurrent",
                "LongTermDebtAndCapitalLeaseObligations",
                "DebtLongtermAndShorttermCombinedAmount"
        ));

        // Short-term debt
        double shortTermDebt = extractValue(root, ns,
                "DebtCurrent",
                "ShortTermBorrowings",
                "ShortTermDebtAndCurrentPortionOfLongTermDebt"
        );

        // Total debt = short-term + long-term
        statement.setTotalDebt(shortTermDebt + statement.getLongTermDebt());

        // Equity
        statement.setShareholderEquity(extractValue(root, ns,
                "StockholdersEquity",
                "Equity",
                "StockholdersEquityIncludingPortionAttributableToNoncontrollingInterest"
        ));
    }

    /**
     * Extracts cash flow statement data
     */
    private void extractCashFlowData(FinancialStatement statement, Element root, Namespace ns) {
        statement.setOperatingCashFlow(extractValue(root, ns,
                "NetCashProvidedByUsedInOperatingActivities",
                "CashFlowFromOperatingActivities",
                "NetCashProvidedByUsedInOperatingActivitiesContinuingOperations"
        ));

        // Capital Expenditures - FIXED: Added more tag variations
        double capex = extractValue(root, ns,
                "PaymentsToAcquirePropertyPlantAndEquipment",
                "CapitalExpendituresIncurredButNotYetPaid",
                "PaymentsForCapitalImprovements",
                "PaymentsToAcquireProductiveAssets"
        );

        // CapEx is usually negative in cash flow statement
        statement.setCapitalExpenditures(-Math.abs(capex));

        // Calculate free cash flow
        double fcf = statement.getOperatingCashFlow() - Math.abs(statement.getCapitalExpenditures());
        statement.setFreeCashFlow(fcf);

        statement.setDividendsPaid(extractValue(root, ns,
                "PaymentsOfDividends",
                "DividendsPaid",
                "PaymentsOfDividendsCommonStock"
        ));

        statement.setShareRepurchases(extractValue(root, ns,
                "PaymentsForRepurchaseOfCommonStock",
                "StockRepurchasedDuringPeriodValue",
                "PaymentsForRepurchaseOfEquity"
        ));
    }

    /**
     * Extracts per-share data
     */
    private void extractPerShareData(FinancialStatement statement, Element root, Namespace ns) {
        statement.setEarningsPerShare(extractValue(root, ns,
                "EarningsPerShareBasic"
        ));

        statement.setDilutedEPS(extractValue(root, ns,
                "EarningsPerShareDiluted"
        ));

        long shares = (long) extractValue(root, ns,
                "CommonStockSharesOutstanding",
                "WeightedAverageNumberOfSharesOutstandingBasic",
                "CommonStockSharesIssued"
        );
        statement.setSharesOutstanding(shares);

        // Calculate book value per share if we have the data
        if (shares > 0 && statement.getShareholderEquity() > 0) {
            statement.setBookValuePerShare(statement.getShareholderEquity() / shares);
        }
    }

    /**
     * Calculates derived values not directly in XBRL
     */
    private void calculateDerivedValues(FinancialStatement statement, Element root, Namespace ns) {
        // Calculate gross profit if not found (Revenue - COGS)
        if (statement.getGrossProfit() == 0 && statement.getRevenue() > 0 && statement.getCostOfGoodsSold() > 0) {
            statement.setGrossProfit(statement.getRevenue() - statement.getCostOfGoodsSold());
        }

        // Extract Depreciation and Amortization
        double depreciation = extractValue(root, ns,
                "DepreciationDepletionAndAmortization",
                "Depreciation",
                "DepreciationAndAmortization"
        );

        // Calculate EBITDA if not found
        // EBITDA = EBIT + Depreciation + Amortization
        // OR EBITDA = Net Income + Interest + Taxes + Depreciation + Amortization
        if (statement.getEbitda() == 0) {
            if (statement.getEbit() != 0 && depreciation > 0) {
                statement.setEbitda(statement.getEbit() + depreciation);
            } else if (statement.getOperatingIncome() > 0 && depreciation > 0) {
                // Approximate: Operating Income + D&A
                statement.setEbitda(statement.getOperatingIncome() + depreciation);
            }
        }

        // Validate and fix total liabilities if it equals total assets (accounting equation check)
        if (statement.getTotalLiabilities() > 0 && statement.getTotalAssets() > 0 &&
            statement.getShareholderEquity() > 0) {

            // Check if liabilities was incorrectly set to total assets
            if (Math.abs(statement.getTotalLiabilities() - statement.getTotalAssets()) < 1000) {
                // Calculate correct liabilities: Assets = Liabilities + Equity
                double correctLiabilities = statement.getTotalAssets() - statement.getShareholderEquity();
                logger.warn("Correcting total liabilities from {} to {} using accounting equation",
                        statement.getTotalLiabilities(), correctLiabilities);
                statement.setTotalLiabilities(correctLiabilities);
            }
        }

        // If EBIT is 0 but we have Net Income and Interest, approximate it
        if (statement.getEbit() == 0 && statement.getNetIncome() != 0) {
            // EBIT ≈ Net Income + Interest + Taxes
            // We don't have taxes directly, so use Net Income + Interest as approximation
            statement.setEbit(statement.getNetIncome() + statement.getInterestExpense());
        }
    }

    /**
     * Extracts a numeric value from XBRL using multiple possible tag names
     */
    private double extractValue(Element root, Namespace ns, String... tagNames) {
        for (String tagName : tagNames) {
            try {
                Element element = findElementRecursive(root, tagName, ns);
                if (element != null && element.getText() != null && !element.getText().trim().isEmpty()) {
                    String text = element.getText().trim();
                    double value = Double.parseDouble(text);
                    logger.debug("Extracted {} = {}", tagName, value);
                    return value;
                }
            } catch (NumberFormatException e) {
                logger.debug("Could not parse value for tag {}: {}", tagName, e.getMessage());
            }
        }

        logger.debug("Could not find value for tags: {}", String.join(", ", tagNames));
        return 0;
    }

    /**
     * Recursively searches for an element by name
     */
    private Element findElementRecursive(Element parent, String name, Namespace ns) {
        // Check current element
        if (parent.getName().equals(name)) {
            return parent;
        }

        // Check children with namespace
        if (ns != null) {
            Element child = parent.getChild(name, ns);
            if (child != null) {
                return child;
            }
        }

        // Check children without namespace
        Element child = parent.getChild(name);
        if (child != null) {
            return child;
        }

        // Recursively check all children
        for (Element childElement : parent.getChildren()) {
            Element found = findElementRecursive(childElement, name, ns);
            if (found != null) {
                return found;
            }
        }

        return null;
    }

    /**
     * Finds the US-GAAP namespace in the document
     */
    private Namespace findNamespace(Element root, String prefix) {
        for (Namespace ns : root.getNamespacesInScope()) {
            if (ns.getPrefix().equals(prefix) || ns.getURI().contains(prefix)) {
                return ns;
            }
        }
        return null;
    }

    /**
     * Extracts fiscal year from filename or document
     */
    private int extractFiscalYear(String filename, Element root) {
        // Try to extract from filename (e.g., "ual-20231231.xml" -> 2023)
        String pattern = "\\d{8}";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(filename);
        if (m.find()) {
            String dateStr = m.group(0);
            return Integer.parseInt(dateStr.substring(0, 4));
        }

        // Try to extract from document
        try {
            Element periodEnd = findElementRecursive(root, "DocumentPeriodEndDate", null);
            if (periodEnd != null) {
                String dateText = periodEnd.getText();
                if (dateText.length() >= 4) {
                    return Integer.parseInt(dateText.substring(0, 4));
                }
            }
        } catch (Exception e) {
            logger.warn("Could not extract fiscal year from document", e);
        }

        return 0;
    }

    /**
     * Parses XBRL file and extracts DETAILED financial statements
     * This is the enhanced method that populates all detailed models
     */
    public DetailedFinancialData parseDetailed(File xbrlFile) throws Exception {
        logger.info("Parsing XBRL file for detailed extraction: {}", xbrlFile.getName());

        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = saxBuilder.build(xbrlFile);
        Element rootElement = document.getRootElement();

        Namespace usGaapNs = findNamespace(rootElement, "us-gaap");
        int fiscalYear = extractFiscalYear(xbrlFile.getName(), rootElement);

        DetailedFinancialData data = new DetailedFinancialData();
        data.setFiscalYear(fiscalYear);

        // Extract all detailed statements
        data.setIncomeStatement(parseDetailedIncomeStatement(rootElement, usGaapNs, fiscalYear));
        data.setBalanceSheet(parseDetailedBalanceSheet(rootElement, usGaapNs, fiscalYear));
        data.setCashFlow(parseDetailedCashFlow(rootElement, usGaapNs, fiscalYear));

        logger.info("Successfully parsed detailed financial data for year {}", fiscalYear);
        return data;
    }

    /**
     * Extracts detailed income statement
     */
    private DetailedIncomeStatement parseDetailedIncomeStatement(Element root, Namespace ns, int fiscalYear) {
        DetailedIncomeStatement stmt = new DetailedIncomeStatement(fiscalYear);

        // Revenue breakdown
        stmt.setTotalOperatingRevenue(extractValue(root, ns,
                "Revenues",
                "RevenueFromContractWithCustomerExcludingAssessedTax",
                "SalesRevenueNet"
        ));

        stmt.setPassengerRevenue(extractValue(root, ns,
                "PassengerRevenue",
                "TransportationRevenue",
                "AirTransportationRevenue"
        ));

        stmt.setCargoRevenue(extractValue(root, ns,
                "CargoRevenue",
                "FreightRevenue"
        ));

        // Other revenue = Total - Passenger - Cargo
        stmt.setOtherOperatingRevenue(stmt.getTotalOperatingRevenue() - stmt.getPassengerRevenue() - stmt.getCargoRevenue());

        // Loyalty program revenue
        stmt.setLoyaltyProgramRevenue(extractValue(root, ns,
                "LoyaltyProgramRevenue",
                "MileageCreditRevenue",
                "FrequentFlyerMileageCreditRevenue"
        ));

        stmt.setBaggageFees(extractValue(root, ns,
                "BaggageFeesRevenue",
                "PassengerBaggageRevenue"
        ));

        // Operating Expenses
        stmt.setAircraftFuel(extractValue(root, ns,
                "FuelAndFuelRelatedExpense",
                "AircraftFuelExpense",
                "FuelExpense"
        ));

        stmt.setSalariesAndRelatedCosts(extractValue(root, ns,
                "LaborAndRelatedExpense",
                "SalariesAndWages",
                "EmployeeBenefitsAndShareBasedCompensation"
        ));

        stmt.setRegionalCapacityPurchase(extractValue(root, ns,
                "RegionalCapacityPurchaseExpense",
                "ContractualAgreementsExpense"
        ));

        stmt.setLandingFeesAndRent(extractValue(root, ns,
                "LandingFeesAndOtherRentalsCosts",
                "AirportFeesAndRent",
                "LandingFees"
        ));

        stmt.setAircraftMaintenance(extractValue(root, ns,
                "MaintenanceMaterialsAndRepairs",
                "AircraftMaintenanceExpense",
                "MaintenanceExpense"
        ));

        stmt.setDepreciation(extractValue(root, ns,
                "Depreciation",
                "DepreciationNonproduction",
                "DepreciationAndAmortizationDepreciationComponent"
        ));

        stmt.setAmortization(extractValue(root, ns,
                "AmortizationOfIntangibleAssets",
                "DepreciationAndAmortizationAmortizationComponent"
        ));

        stmt.setDistributionExpenses(extractValue(root, ns,
                "SellingAndMarketingExpense",
                "DistributionExpense",
                "SalesAndMarketingExpense"
        ));

        stmt.setAircraftRent(extractValue(root, ns,
                "AircraftRentExpense",
                "OperatingLeaseExpense",
                "OperatingLeaseCost"
        ));

        stmt.setSpecialCharges(extractValue(root, ns,
                "RestructuringCharges",
                "AssetImpairmentCharges",
                "SpecialCharges"
        ));

        stmt.setTotalOperatingExpenses(extractValue(root, ns,
                "OperatingExpenses",
                "CostsAndExpenses",
                "OperatingCostsAndExpenses"
        ));

        // Operating Income
        stmt.setOperatingIncome(extractValue(root, ns,
                "OperatingIncomeLoss",
                "OperatingIncome"
        ));

        // Non-operating items
        stmt.setInterestExpense(extractValue(root, ns,
                "InterestExpense",
                "InterestExpenseDebt"
        ));

        stmt.setInterestIncome(extractValue(root, ns,
                "InterestIncome",
                "InterestAndDividendIncomeOperating"
        ));

        stmt.setOtherIncomeExpense(extractValue(root, ns,
                "OtherNonoperatingIncomeExpense",
                "NonoperatingIncomeExpense",
                "OtherIncomeAndExpenses"
        ));

        // Taxes
        stmt.setIncomeTaxExpense(extractValue(root, ns,
                "IncomeTaxExpenseBenefit",
                "IncomeTaxExpense"
        ));

        stmt.setPretaxIncome(extractValue(root, ns,
                "IncomeLossFromContinuingOperationsBeforeIncomeTaxesExtraordinaryItemsNoncontrollingInterest",
                "IncomeLossFromContinuingOperationsBeforeIncomeTaxesMinorityInterestAndIncomeLossFromEquityMethodInvestments"
        ));

        // Net Income
        stmt.setNetIncome(extractValue(root, ns,
                "NetIncomeLoss",
                "ProfitLoss"
        ));

        stmt.setNetIncomeAvailableToCommon(extractValue(root, ns,
                "NetIncomeLossAvailableToCommonStockholdersBasic",
                "NetIncomeLoss"
        ));

        // Per Share Data
        stmt.setBasicEPS(extractValue(root, ns,
                "EarningsPerShareBasic"
        ));

        stmt.setDilutedEPS(extractValue(root, ns,
                "EarningsPerShareDiluted"
        ));

        stmt.setWeightedAverageSharesBasic(extractValue(root, ns,
                "WeightedAverageNumberOfSharesOutstandingBasic"
        ));

        stmt.setWeightedAverageSharesDiluted(extractValue(root, ns,
                "WeightedAverageNumberOfDilutedSharesOutstanding"
        ));

        stmt.setShareCountYearEnd(extractValue(root, ns,
                "CommonStockSharesOutstanding",
                "CommonStockSharesIssued"
        ));

        // Calculate derived values
        if (stmt.getPretaxIncome() == 0 && stmt.getNetIncome() != 0) {
            stmt.setPretaxIncome(stmt.getNetIncome() + stmt.getIncomeTaxExpense());
        }

        // EBIT = Operating Income (typically)
        if (stmt.getOperatingIncome() != 0) {
            stmt.setEbit(stmt.getOperatingIncome());
        }

        // EBITDA = EBIT + D&A
        double totalDA = stmt.getDepreciation() + stmt.getAmortization();
        if (stmt.getEbit() != 0 && totalDA > 0) {
            stmt.setEbitda(stmt.getEbit() + totalDA);
        }

        // Calculate effective tax rate
        if (stmt.getPretaxIncome() > 0) {
            stmt.setEffectiveTaxRate((stmt.getIncomeTaxExpense() / stmt.getPretaxIncome()) * 100);
        }

        // Estimate employee count if available
        int employeeCount = (int) extractValue(root, ns,
                "NumberOfEmployees",
                "EmployeeRelatedLiabilitiesCurrentNumberOfEmployees"
        );
        stmt.setNumberOfEmployees(employeeCount);

        // Calculate fuel gallons and price if possible
        if (stmt.getAircraftFuel() > 0) {
            double gallons = extractValue(root, ns,
                    "FuelConsumedGallons",
                    "FuelGallonsConsumed"
            );
            stmt.setFuelGallonsConsumed(gallons);
            if (gallons > 0) {
                stmt.setAverageFuelPricePerGallon(stmt.getAircraftFuel() / gallons);
            }
        }

        return stmt;
    }

    /**
     * Extracts detailed balance sheet
     */
    private DetailedBalanceSheet parseDetailedBalanceSheet(Element root, Namespace ns, int fiscalYear) {
        DetailedBalanceSheet bs = new DetailedBalanceSheet(fiscalYear);

        // Current Assets
        bs.setCashAndCashEquivalents(extractValue(root, ns,
                "CashAndCashEquivalentsAtCarryingValue",
                "Cash",
                "CashEquivalents"
        ));

        bs.setShortTermInvestments(extractValue(root, ns,
                "ShortTermInvestments",
                "MarketableSecuritiesCurrent",
                "AvailableForSaleSecuritiesCurrent"
        ));

        bs.setRestrictedCash(extractValue(root, ns,
                "RestrictedCashCurrent",
                "RestrictedCash"
        ));

        bs.setAccountsReceivable(extractValue(root, ns,
                "AccountsReceivableNetCurrent",
                "ReceivablesNetCurrent",
                "AccountsReceivableNet"
        ));

        bs.setAllowanceForDoubtfulAccounts(extractValue(root, ns,
                "AllowanceForDoubtfulAccountsReceivableCurrent",
                "AllowanceForDoubtfulAccounts"
        ));

        bs.setPrepaidExpenses(extractValue(root, ns,
                "PrepaidExpenseCurrent",
                "PrepaidExpenseAndOtherAssetsCurrent"
        ));

        bs.setSparePartsAndSupplies(extractValue(root, ns,
                "SparePartsSuppliesAndFuel",
                "InventorySparePartsSuppliesAndFuel",
                "MaterialsSuppliesAndFuel"
        ));

        bs.setTotalCurrentAssets(extractValue(root, ns,
                "AssetsCurrent"
        ));

        // Property, Plant & Equipment
        bs.setFlightEquipment(extractValue(root, ns,
                "FlightEquipmentGross",
                "AircraftAndFlightEquipmentGross",
                "PropertyPlantAndEquipmentAircraft"
        ));

        bs.setGroundEquipment(extractValue(root, ns,
                "PropertyPlantAndEquipmentOther",
                "GroundPropertyAndEquipmentGross"
        ));

        bs.setBuildings(extractValue(root, ns,
                "PropertyPlantAndEquipmentBuildings",
                "BuildingsAndImprovementsGross"
        ));

        bs.setConstructionInProgress(extractValue(root, ns,
                "ConstructionInProgressGross",
                "PropertyPlantAndEquipmentConstructionInProgress"
        ));

        bs.setTotalPPEAtCost(extractValue(root, ns,
                "PropertyPlantAndEquipmentGross"
        ));

        bs.setAccumulatedDepreciation(extractValue(root, ns,
                "AccumulatedDepreciationDepletionAndAmortizationPropertyPlantAndEquipment",
                "AccumulatedDepreciation"
        ));

        bs.setNetPPE(extractValue(root, ns,
                "PropertyPlantAndEquipmentNet"
        ));

        // Operating Lease ROU Assets (ASC 842)
        bs.setOperatingLeaseRightOfUseAssets(extractValue(root, ns,
                "OperatingLeaseRightOfUseAsset",
                "OperatingLeaseRightOfUseAssetNoncurrent"
        ));

        // Intangibles
        bs.setGoodwill(extractValue(root, ns,
                "Goodwill"
        ));

        bs.setIntangibleAssets(extractValue(root, ns,
                "IntangibleAssetsNetExcludingGoodwill",
                "FiniteLivedIntangibleAssetsNet"
        ));

        bs.setRoutesAndSlots(extractValue(root, ns,
                "RouteAuthoritiesIntangibleAsset",
                "LandingAndTakeoffSlotsIntangibleAsset",
                "AirlineRoutesIntangibleAsset"
        ));

        bs.setLongTermInvestments(extractValue(root, ns,
                "LongTermInvestments",
                "InvestmentsNoncurrent"
        ));

        bs.setDeferredTaxAssets(extractValue(root, ns,
                "DeferredTaxAssetsNetNoncurrent",
                "DeferredIncomeTaxAssetsNet"
        ));

        bs.setTotalAssets(extractValue(root, ns,
                "Assets"
        ));

        // Current Liabilities
        bs.setAccountsPayable(extractValue(root, ns,
                "AccountsPayableCurrent"
        ));

        bs.setAccruedSalariesAndBenefits(extractValue(root, ns,
                "EmployeeRelatedLiabilitiesCurrent",
                "AccruedSalariesCurrent"
        ));

        // Air Traffic Liability - KEY for airlines!
        bs.setAirTrafficLiability(extractValue(root, ns,
                "AirTrafficLiabilityCurrent",
                "CustomerAdvancesAndDeposits",
                "DeferredRevenueAndCustomerAdvances",
                "ContractWithCustomerLiabilityCurrent"
        ));

        bs.setCurrentDebt(extractValue(root, ns,
                "DebtCurrent",
                "ShortTermBorrowings",
                "LongTermDebtCurrent"
        ));

        bs.setCurrentOperatingLeaseLiabilities(extractValue(root, ns,
                "OperatingLeaseLiabilityCurrent"
        ));

        bs.setCurrentFinanceLeaseLiabilities(extractValue(root, ns,
                "FinanceLeaseLiabilityCurrent",
                "CapitalLeaseObligationsCurrent"
        ));

        bs.setTotalCurrentLiabilities(extractValue(root, ns,
                "LiabilitiesCurrent"
        ));

        // Long-term Liabilities
        bs.setLongTermDebt(extractValue(root, ns,
                "LongTermDebtNoncurrent",
                "LongTermDebt"
        ));

        bs.setLongTermOperatingLeaseLiabilities(extractValue(root, ns,
                "OperatingLeaseLiabilityNoncurrent"
        ));

        bs.setLongTermFinanceLeaseLiabilities(extractValue(root, ns,
                "FinanceLeaseLiabilityNoncurrent",
                "CapitalLeaseObligationsNoncurrent"
        ));

        bs.setPensionLiabilities(extractValue(root, ns,
                "DefinedBenefitPensionPlanLiabilitiesNoncurrent",
                "PensionAndOtherPostretirementDefinedBenefitPlansLiabilitiesNoncurrent"
        ));

        bs.setPostRetirementBenefits(extractValue(root, ns,
                "OtherPostretirementDefinedBenefitPlanLiabilitiesNoncurrent"
        ));

        bs.setDeferredTaxLiabilities(extractValue(root, ns,
                "DeferredIncomeTaxLiabilitiesNet",
                "DeferredTaxLiabilitiesNoncurrent"
        ));

        bs.setLoyaltyProgramDeferredRevenue(extractValue(root, ns,
                "LoyaltyProgramDeferredRevenue",
                "DeferredRevenueNoncurrent"
        ));

        bs.setTotalLiabilities(extractValue(root, ns,
                "Liabilities"
        ));

        // Stockholders' Equity
        bs.setCommonStock(extractValue(root, ns,
                "CommonStockValue"
        ));

        bs.setCommonSharesOutstanding(extractValue(root, ns,
                "CommonStockSharesOutstanding",
                "CommonStockSharesIssued"
        ));

        bs.setAdditionalPaidInCapital(extractValue(root, ns,
                "AdditionalPaidInCapital",
                "AdditionalPaidInCapitalCommonStock"
        ));

        bs.setTreasuryStock(extractValue(root, ns,
                "TreasuryStockValue"
        ));

        bs.setRetainedEarnings(extractValue(root, ns,
                "RetainedEarningsAccumulatedDeficit"
        ));

        bs.setAccumulatedOtherComprehensiveIncome(extractValue(root, ns,
                "AccumulatedOtherComprehensiveIncomeLossNetOfTax",
                "AccumulatedOtherComprehensiveIncomeLossNetOfTaxTotal"
        ));

        bs.setTotalStockholdersEquity(extractValue(root, ns,
                "StockholdersEquity",
                "Equity"
        ));

        // Validate accounting equation
        if (bs.getTotalLiabilities() > 0 && bs.getTotalAssets() > 0 && bs.getTotalStockholdersEquity() > 0) {
            if (Math.abs(bs.getTotalLiabilities() - bs.getTotalAssets()) < 1000) {
                double correctLiabilities = bs.getTotalAssets() - bs.getTotalStockholdersEquity();
                logger.warn("Correcting liabilities using accounting equation");
                bs.setTotalLiabilities(correctLiabilities);
            }
        }

        return bs;
    }

    /**
     * Extracts detailed cash flow statement
     */
    private DetailedCashFlow parseDetailedCashFlow(Element root, Namespace ns, int fiscalYear) {
        DetailedCashFlow cf = new DetailedCashFlow(fiscalYear);

        // Operating Activities
        cf.setNetIncome(extractValue(root, ns,
                "NetIncomeLoss",
                "ProfitLoss"
        ));

        cf.setDepreciation(extractValue(root, ns,
                "Depreciation",
                "DepreciationNonproduction"
        ));

        cf.setAmortization(extractValue(root, ns,
                "AmortizationOfIntangibleAssets"
        ));

        cf.setDeferredIncomeTaxes(extractValue(root, ns,
                "DeferredIncomeTaxExpenseBenefit",
                "IncreaseDecreaseInDeferredIncomeTaxes"
        ));

        cf.setStockBasedCompensation(extractValue(root, ns,
                "ShareBasedCompensation",
                "AllocatedShareBasedCompensationExpense"
        ));

        cf.setImpairmentCharges(extractValue(root, ns,
                "AssetImpairmentCharges",
                "ImpairmentOfLongLivedAssetsHeldForUse"
        ));

        cf.setGainsLossesOnAssetSales(extractValue(root, ns,
                "GainLossOnSaleOfPropertyPlantEquipment",
                "GainLossOnDispositionOfAssets"
        ));

        // Working Capital Changes
        cf.setChangeInReceivables(extractValue(root, ns,
                "IncreaseDecreaseInAccountsReceivable"
        ));

        cf.setChangeInPrepaidExpenses(extractValue(root, ns,
                "IncreaseDecreaseInPrepaidExpense",
                "IncreaseDecreaseInPrepaidDeferredExpenseAndOtherAssets"
        ));

        cf.setChangeInAccountsPayable(extractValue(root, ns,
                "IncreaseDecreaseInAccountsPayable"
        ));

        cf.setChangeInAirTrafficLiability(extractValue(root, ns,
                "IncreaseDecreaseInAirTrafficLiability",
                "IncreaseDecreaseInCustomerDeposits",
                "IncreaseDecreaseInContractWithCustomerLiability"
        ));

        cf.setChangeInAccruedLiabilities(extractValue(root, ns,
                "IncreaseDecreaseInAccruedLiabilities"
        ));

        cf.setNetCashFromOperating(extractValue(root, ns,
                "NetCashProvidedByUsedInOperatingActivities"
        ));

        // Investing Activities
        double capex = extractValue(root, ns,
                "PaymentsToAcquirePropertyPlantAndEquipment",
                "CapitalExpendituresIncurredButNotYetPaid"
        );
        cf.setCapitalExpenditures(-Math.abs(capex));

        cf.setAircraftPurchases(extractValue(root, ns,
                "PaymentsToAcquireAircraft",
                "PaymentsToAcquireFlightEquipment"
        ));

        cf.setPreDeliveryDeposits(extractValue(root, ns,
                "PaymentsForAircraftPredeliveryDeposits",
                "IncreaseDecreaseInPrepaidDepositOnAircraft"
        ));

        cf.setProceedsFromAssetSales(extractValue(root, ns,
                "ProceedsFromSaleOfPropertyPlantAndEquipment"
        ));

        cf.setPurchasesOfInvestments(extractValue(root, ns,
                "PaymentsToAcquireInvestments",
                "PaymentsToAcquireMarketableSecurities"
        ));

        cf.setSalesOfInvestments(extractValue(root, ns,
                "ProceedsFromSaleOfAvailableForSaleSecurities",
                "ProceedsFromSaleAndMaturityOfMarketableSecurities"
        ));

        cf.setNetCashFromInvesting(extractValue(root, ns,
                "NetCashProvidedByUsedInInvestingActivities"
        ));

        // Financing Activities
        cf.setProceedsFromDebtIssuance(extractValue(root, ns,
                "ProceedsFromIssuanceOfLongTermDebt",
                "ProceedsFromDebtNetOfIssuanceCosts"
        ));

        cf.setDebtRepayments(extractValue(root, ns,
                "RepaymentsOfLongTermDebt",
                "RepaymentsOfDebt"
        ));

        cf.setProceedsFromSaleLeasebacks(extractValue(root, ns,
                "ProceedsFromSaleAndLeasebackTransactions"
        ));

        cf.setFinanceLeasePayments(extractValue(root, ns,
                "FinanceLeasePrincipalPayments",
                "RepaymentsOfLongTermCapitalLeaseObligations"
        ));

        cf.setProceedsFromStockIssuance(extractValue(root, ns,
                "ProceedsFromIssuanceOfCommonStock"
        ));

        cf.setStockRepurchases(extractValue(root, ns,
                "PaymentsForRepurchaseOfCommonStock"
        ));

        cf.setDividendsPaid(extractValue(root, ns,
                "PaymentsOfDividends"
        ));

        cf.setNetCashFromFinancing(extractValue(root, ns,
                "NetCashProvidedByUsedInFinancingActivities"
        ));

        // Summary
        cf.setNetChangeInCash(extractValue(root, ns,
                "CashCashEquivalentsRestrictedCashAndRestrictedCashEquivalentsPeriodIncreaseDecreaseIncludingExchangeRateEffect",
                "CashAndCashEquivalentsPeriodIncreaseDecrease"
        ));

        cf.setCashAtEnd(extractValue(root, ns,
                "CashAndCashEquivalentsAtCarryingValue",
                "Cash"
        ));

        // Supplemental
        cf.setCashPaidForInterest(extractValue(root, ns,
                "InterestPaidNet",
                "InterestPaid"
        ));

        cf.setCashPaidForTaxes(extractValue(root, ns,
                "IncomeTaxesPaidNet",
                "IncomeTaxesPaid"
        ));

        // Calculate free cash flow
        cf.calculateFreeCashFlow();

        return cf;
    }

    /**
     * Container class for detailed financial data
     */
    public static class DetailedFinancialData {
        private int fiscalYear;
        private DetailedIncomeStatement incomeStatement;
        private DetailedBalanceSheet balanceSheet;
        private DetailedCashFlow cashFlow;

        public int getFiscalYear() { return fiscalYear; }
        public void setFiscalYear(int fiscalYear) { this.fiscalYear = fiscalYear; }

        public DetailedIncomeStatement getIncomeStatement() { return incomeStatement; }
        public void setIncomeStatement(DetailedIncomeStatement incomeStatement) { this.incomeStatement = incomeStatement; }

        public DetailedBalanceSheet getBalanceSheet() { return balanceSheet; }
        public void setBalanceSheet(DetailedBalanceSheet balanceSheet) { this.balanceSheet = balanceSheet; }

        public DetailedCashFlow getCashFlow() { return cashFlow; }
        public void setCashFlow(DetailedCashFlow cashFlow) { this.cashFlow = cashFlow; }
    }
}
