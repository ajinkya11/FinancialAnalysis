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
}
