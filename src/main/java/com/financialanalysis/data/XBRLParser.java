package com.financialanalysis.data;

import com.financialanalysis.exceptions.ParsingException;
import com.financialanalysis.models.*;
import lombok.extern.slf4j.Slf4j;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.*;

/**
 * Parser for XBRL financial documents
 * CRITICAL: Extracts aggregate revenue concepts to avoid double-counting
 */
@Slf4j
@Service
public class XBRLParser {

    private static final String US_GAAP_NS = "http://fasb.org/us-gaap/2023";
    private static final List<String> REVENUE_TAGS = Arrays.asList(
            "Revenues",
            "RevenueFromContractWithCustomerIncludingAssessedTax",
            "RevenueFromContractWithCustomerExcludingAssessedTax",
            "OperatingRevenue",
            "SalesRevenueNet"
    );

    /**
     * Parse XBRL content and extract financial data
     */
    public CompanyFinancialData parseXBRL(String xbrlContent, String ticker) {
        log.info("Parsing XBRL data for ticker: {}", ticker);

        try {
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(new StringReader(xbrlContent));
            Element root = document.getRootElement();

            // Get all namespaces
            List<Namespace> namespaces = root.getAdditionalNamespaces();
            Namespace usGaapNs = findNamespace(namespaces, "us-gaap");
            Namespace contextNs = root.getNamespace();

            // Extract company information
            String companyName = extractCompanyName(root, contextNs);
            String fiscalYear = extractFiscalYear(root, contextNs);

            // Extract financial statements
            IncomeStatement incomeStatement = extractIncomeStatement(root, usGaapNs, fiscalYear);
            BalanceSheet balanceSheet = extractBalanceSheet(root, usGaapNs, fiscalYear);
            CashFlowStatement cashFlow = extractCashFlowStatement(root, usGaapNs, fiscalYear);
            AirlineOperatingMetrics operatingMetrics = extractAirlineMetrics(root, usGaapNs, fiscalYear);

            return CompanyFinancialData.builder()
                    .ticker(ticker)
                    .companyName(companyName)
                    .fiscalYearEnd(fiscalYear)
                    .latestIncomeStatement(incomeStatement)
                    .latestBalanceSheet(balanceSheet)
                    .latestCashFlowStatement(cashFlow)
                    .latestOperatingMetrics(operatingMetrics)
                    .incomeStatements(Collections.singletonList(incomeStatement))
                    .balanceSheets(Collections.singletonList(balanceSheet))
                    .cashFlowStatements(Collections.singletonList(cashFlow))
                    .operatingMetrics(Collections.singletonList(operatingMetrics))
                    .build();

        } catch (Exception e) {
            log.error("Error parsing XBRL for ticker: {}", ticker, e);
            throw new ParsingException("Failed to parse XBRL data", e);
        }
    }

    /**
     * Extract Income Statement with critical revenue validation
     */
    private IncomeStatement extractIncomeStatement(Element root, Namespace ns, String fiscalYear) {
        log.info("Extracting income statement data");

        // Extract aggregate revenue (avoid double-counting)
        BigDecimal revenue = extractAggregateRevenue(root, ns);

        // Validate revenue
        if (revenue != null && revenue.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid revenue detected: {}. This may indicate data quality issues.", revenue);
        }

        return IncomeStatement.builder()
                .fiscalYear(fiscalYear)
                .fiscalPeriod("FY")
                .totalRevenue(revenue)
                .passengerRevenue(extractValue(root, ns, "PassengerRevenue"))
                .cargoRevenue(extractValue(root, ns, "CargoRevenue"))
                .operatingExpenses(extractValue(root, ns, "OperatingExpenses", "OperatingCostsAndExpenses"))
                .fuelCosts(extractValue(root, ns, "FuelCosts", "AircraftFuelAndRelatedTaxes"))
                .laborCosts(extractValue(root, ns, "LaborAndRelatedExpense", "SalariesAndWages"))
                .operatingIncome(extractValue(root, ns, "OperatingIncomeLoss"))
                .ebit(extractValue(root, ns, "OperatingIncomeLoss"))
                .interestExpense(extractValue(root, ns, "InterestExpense"))
                .incomeTaxExpense(extractValue(root, ns, "IncomeTaxExpenseBenefit"))
                .netIncome(extractValue(root, ns, "NetIncomeLoss", "ProfitLoss"))
                .basicEPS(extractValue(root, ns, "EarningsPerShareBasic"))
                .dilutedEPS(extractValue(root, ns, "EarningsPerShareDiluted"))
                .depreciationAmortization(extractValue(root, ns, "DepreciationAndAmortization"))
                .build();
    }

    /**
     * CRITICAL: Extract aggregate revenue concept to avoid double-counting
     * DO NOT sum up individual revenue line items
     */
    private BigDecimal extractAggregateRevenue(Element root, Namespace ns) {
        for (String tag : REVENUE_TAGS) {
            BigDecimal value = extractValue(root, ns, tag);
            if (value != null && value.compareTo(BigDecimal.ZERO) > 0) {
                log.info("Found revenue using tag: {} = {}", tag, value);
                return value;
            }
        }

        log.warn("Could not find aggregate revenue using standard tags");
        return null;
    }

    /**
     * Extract Balance Sheet data
     */
    private BalanceSheet extractBalanceSheet(Element root, Namespace ns, String fiscalYear) {
        log.info("Extracting balance sheet data");

        return BalanceSheet.builder()
                .fiscalYear(fiscalYear)
                .fiscalPeriod("FY")
                .totalAssets(extractValue(root, ns, "Assets"))
                .currentAssets(extractValue(root, ns, "AssetsCurrent"))
                .cashAndEquivalents(extractValue(root, ns, "CashAndCashEquivalentsAtCarryingValue", "Cash"))
                .accountsReceivable(extractValue(root, ns, "AccountsReceivableNetCurrent"))
                .inventory(extractValue(root, ns, "InventoryNet"))
                .propertyPlantEquipment(extractValue(root, ns, "PropertyPlantAndEquipmentGross"))
                .accumulatedDepreciation(extractValue(root, ns, "AccumulatedDepreciationDepletionAndAmortizationPropertyPlantAndEquipment"))
                .totalLiabilities(extractValue(root, ns, "Liabilities"))
                .currentLiabilities(extractValue(root, ns, "LiabilitiesCurrent"))
                .accountsPayable(extractValue(root, ns, "AccountsPayableCurrent"))
                .shortTermDebt(extractValue(root, ns, "ShortTermBorrowings", "DebtCurrent"))
                .longTermDebt(extractValue(root, ns, "LongTermDebtNoncurrent", "LongTermDebt"))
                .airTrafficLiability(extractValue(root, ns, "AirTrafficLiability", "DeferredRevenue"))
                .totalEquity(extractValue(root, ns, "StockholdersEquity", "Equity"))
                .retainedEarnings(extractValue(root, ns, "RetainedEarningsAccumulatedDeficit"))
                .build();
    }

    /**
     * Extract Cash Flow Statement data
     */
    private CashFlowStatement extractCashFlowStatement(Element root, Namespace ns, String fiscalYear) {
        log.info("Extracting cash flow statement data");

        return CashFlowStatement.builder()
                .fiscalYear(fiscalYear)
                .fiscalPeriod("FY")
                .operatingCashFlow(extractValue(root, ns, "NetCashProvidedByUsedInOperatingActivities"))
                .netIncomeStartingLine(extractValue(root, ns, "NetIncomeLoss"))
                .depreciationAmortization(extractValue(root, ns, "DepreciationDepletionAndAmortization"))
                .changeInWorkingCapital(extractValue(root, ns, "IncreaseDecreaseInOperatingCapital"))
                .cashFromInvesting(extractValue(root, ns, "NetCashProvidedByUsedInInvestingActivities"))
                .capitalExpenditures(extractValue(root, ns, "PaymentsToAcquirePropertyPlantAndEquipment"))
                .cashFromFinancing(extractValue(root, ns, "NetCashProvidedByUsedInFinancingActivities"))
                .debtIssuance(extractValue(root, ns, "ProceedsFromIssuanceOfDebt"))
                .debtRepayment(extractValue(root, ns, "RepaymentsOfDebt"))
                .netChangeInCash(extractValue(root, ns, "CashCashEquivalentsRestrictedCashAndRestrictedCashEquivalentsPeriodIncreaseDecreaseIncludingExchangeRateEffect"))
                .build();
    }

    /**
     * Extract airline-specific operating metrics
     */
    private AirlineOperatingMetrics extractAirlineMetrics(Element root, Namespace ns, String fiscalYear) {
        log.info("Extracting airline operating metrics");

        return AirlineOperatingMetrics.builder()
                .fiscalYear(fiscalYear)
                .fiscalPeriod("FY")
                .availableSeatMiles(extractLongValue(root, ns, "AvailableSeatMiles"))
                .revenuePassengerMiles(extractLongValue(root, ns, "RevenuePassengerMiles"))
                .passengerRevenue(extractValue(root, ns, "PassengerRevenue"))
                .cargoRevenue(extractValue(root, ns, "CargoRevenue"))
                .fuelCost(extractValue(root, ns, "FuelCosts", "AircraftFuelAndRelatedTaxes"))
                .laborCost(extractValue(root, ns, "LaborAndRelatedExpense"))
                .totalFleetSize(extractIntegerValue(root, ns, "NumberOfAircraftInFleet"))
                .fullTimeEmployees(extractLongValue(root, ns, "NumberOfEmployees"))
                .build();
    }

    /**
     * Extract a value from XBRL, trying multiple possible tag names
     */
    private BigDecimal extractValue(Element root, Namespace ns, String... tagNames) {
        for (String tagName : tagNames) {
            List<Element> elements = root.getChildren(tagName, ns);
            if (elements != null && !elements.isEmpty()) {
                // Get the most recent value (typically the last one)
                Element element = elements.get(elements.size() - 1);
                String value = element.getTextTrim();
                try {
                    return new BigDecimal(value);
                } catch (NumberFormatException e) {
                    log.debug("Could not parse value for tag {}: {}", tagName, value);
                }
            }
        }
        return null;
    }

    /**
     * Extract Long value
     */
    private Long extractLongValue(Element root, Namespace ns, String... tagNames) {
        BigDecimal value = extractValue(root, ns, tagNames);
        return value != null ? value.longValue() : null;
    }

    /**
     * Extract Integer value
     */
    private Integer extractIntegerValue(Element root, Namespace ns, String... tagNames) {
        BigDecimal value = extractValue(root, ns, tagNames);
        return value != null ? value.intValue() : null;
    }

    /**
     * Extract company name
     */
    private String extractCompanyName(Element root, Namespace ns) {
        Element nameElement = root.getChild("EntityRegistrantName", ns);
        return nameElement != null ? nameElement.getTextTrim() : "Unknown";
    }

    /**
     * Extract fiscal year
     */
    private String extractFiscalYear(Element root, Namespace ns) {
        Element yearElement = root.getChild("DocumentFiscalYearFocus", ns);
        return yearElement != null ? yearElement.getTextTrim() : "Unknown";
    }

    /**
     * Find namespace by prefix
     */
    private Namespace findNamespace(List<Namespace> namespaces, String prefix) {
        for (Namespace ns : namespaces) {
            if (prefix.equals(ns.getPrefix())) {
                return ns;
            }
        }
        return null;
    }
}
