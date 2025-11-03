package com.financialanalysis.parser;

import com.financialanalysis.model.FinancialStatement;
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
 */
public class XBRLParser {
    private static final Logger logger = LoggerFactory.getLogger(XBRLParser.class);

    // Common US-GAAP namespace prefixes used in XBRL files
    private static final String[] US_GAAP_PREFIXES = {"us-gaap", "dei", "ifrs-full"};

    /**
     * Parses an XBRL file and extracts financial data
     *
     * @param xbrlFile the XBRL XML file
     * @return FinancialStatement with extracted data
     * @throws Exception if parsing fails
     */
    public FinancialStatement parse(File xbrlFile) throws Exception {
        logger.info("Parsing XBRL file: {}", xbrlFile.getName());

        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = saxBuilder.build(xbrlFile);
        Element rootElement = document.getRootElement();

        // Get all namespaces from the document
        List<Namespace> namespaces = rootElement.getAdditionalNamespaces();
        Namespace usGaapNs = findNamespace(rootElement, "us-gaap");

        // Extract fiscal year from filename or document
        int fiscalYear = extractFiscalYear(xbrlFile.getName(), rootElement);
        FinancialStatement statement = new FinancialStatement(fiscalYear);

        logger.info("Extracting data for fiscal year: {}", fiscalYear);

        // Extract financial data using US-GAAP tags
        extractIncomeStatementData(statement, rootElement, usGaapNs);
        extractBalanceSheetData(statement, rootElement, usGaapNs);
        extractCashFlowData(statement, rootElement, usGaapNs);
        extractPerShareData(statement, rootElement, usGaapNs);

        logger.info("Successfully parsed XBRL file");
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
                "RevenueFromContractWithCustomer"
        ));

        // Cost of Goods Sold
        statement.setCostOfGoodsSold(extractValue(root, ns,
                "CostOfRevenue",
                "CostOfGoodsAndServicesSold",
                "CostOfSales"
        ));

        // Gross Profit
        statement.setGrossProfit(extractValue(root, ns,
                "GrossProfit"
        ));

        // Operating Income
        statement.setOperatingIncome(extractValue(root, ns,
                "OperatingIncomeLoss",
                "IncomeLossFromContinuingOperationsBeforeIncomeTaxesExtraordinaryItemsNoncontrollingInterest"
        ));

        // Net Income
        statement.setNetIncome(extractValue(root, ns,
                "NetIncomeLoss",
                "ProfitLoss",
                "NetIncomeLossAvailableToCommonStockholdersBasic"
        ));

        // EBIT
        statement.setEbit(extractValue(root, ns,
                "IncomeLossFromContinuingOperationsBeforeIncomeTaxesExtraordinaryItemsNoncontrollingInterest"
        ));

        // EBITDA (often calculated)
        statement.setEbitda(extractValue(root, ns,
                "EarningsBeforeInterestTaxesDepreciationAndAmortization"
        ));

        // Interest Expense
        statement.setInterestExpense(extractValue(root, ns,
                "InterestExpense",
                "InterestExpenseDebt",
                "InterestIncomeExpenseNet"
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
                "ShortTermInvestments"
        ));

        statement.setAccountsReceivable(extractValue(root, ns,
                "AccountsReceivableNetCurrent",
                "ReceivablesNetCurrent"
        ));

        statement.setInventory(extractValue(root, ns,
                "InventoryNet",
                "Inventory"
        ));

        // Liabilities
        statement.setTotalLiabilities(extractValue(root, ns,
                "Liabilities",
                "LiabilitiesAndStockholdersEquity"
        ));

        statement.setCurrentLiabilities(extractValue(root, ns,
                "LiabilitiesCurrent"
        ));

        statement.setLongTermDebt(extractValue(root, ns,
                "LongTermDebt",
                "LongTermDebtNoncurrent",
                "DebtLongtermAndShorttermCombinedAmount"
        ));

        statement.setTotalDebt(extractValue(root, ns,
                "DebtCurrent",
                "ShortTermBorrowings"
        ) + statement.getLongTermDebt());

        // Equity
        statement.setShareholderEquity(extractValue(root, ns,
                "StockholdersEquity",
                "Equity"
        ));
    }

    /**
     * Extracts cash flow statement data
     */
    private void extractCashFlowData(FinancialStatement statement, Element root, Namespace ns) {
        statement.setOperatingCashFlow(extractValue(root, ns,
                "NetCashProvidedByUsedInOperatingActivities",
                "CashFlowFromOperatingActivities"
        ));

        statement.setCapitalExpenditures(extractValue(root, ns,
                "PaymentsToAcquirePropertyPlantAndEquipment",
                "CapitalExpenditures"
        ));

        // Calculate free cash flow
        double fcf = statement.getOperatingCashFlow() - Math.abs(statement.getCapitalExpenditures());
        statement.setFreeCashFlow(fcf);

        statement.setDividendsPaid(extractValue(root, ns,
                "PaymentsOfDividends",
                "DividendsPaid"
        ));

        statement.setShareRepurchases(extractValue(root, ns,
                "PaymentsForRepurchaseOfCommonStock",
                "StockRepurchasedDuringPeriodValue"
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
                "WeightedAverageNumberOfSharesOutstandingBasic"
        );
        statement.setSharesOutstanding(shares);

        // Calculate book value per share if we have the data
        if (shares > 0 && statement.getShareholderEquity() > 0) {
            statement.setBookValuePerShare(statement.getShareholderEquity() / shares);
        }
    }

    /**
     * Extracts a numeric value from XBRL using multiple possible tag names
     *
     * @param root the root element
     * @param ns the namespace
     * @param tagNames possible tag names to search for
     * @return the extracted value, or 0 if not found
     */
    private double extractValue(Element root, Namespace ns, String... tagNames) {
        for (String tagName : tagNames) {
            try {
                // Try to find the element with the given tag name
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
}
