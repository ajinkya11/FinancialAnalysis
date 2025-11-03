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

        // Validate and calculate Total Liabilities using accounting equation if needed
        // Assets = Liabilities + Equity, so Liabilities = Assets - Equity
        if (statement.getTotalAssets() > 0 && statement.getShareholderEquity() > 0) {
            if (statement.getTotalLiabilities() == 0) {
                // Liabilities not found in XBRL, calculate from accounting equation
                double calculatedLiabilities = statement.getTotalAssets() - statement.getShareholderEquity();
                logger.info("Calculating total liabilities from accounting equation: {} - {} = {}",
                        statement.getTotalAssets(), statement.getShareholderEquity(), calculatedLiabilities);
                statement.setTotalLiabilities(calculatedLiabilities);
            } else if (Math.abs(statement.getTotalLiabilities() - statement.getTotalAssets()) < 1000) {
                // Liabilities was incorrectly set to total assets, correct it
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
     * Extracts a numeric value from XBRL searching both US-GAAP and company-specific namespaces
     * Tries US-GAAP first, then falls back to company namespace
     */
    private double extractValue(Element root, Namespace usGaapNs, Namespace companyNs, String... tagNames) {
        // First try US-GAAP namespace
        double value = extractValue(root, usGaapNs, tagNames);
        if (value != 0) {
            return value;
        }

        // Fall back to company-specific namespace if available
        if (companyNs != null) {
            value = extractValue(root, companyNs, tagNames);
            if (value != 0) {
                logger.debug("Found value in company namespace: {}", companyNs.getPrefix());
                return value;
            }
        }

        return 0;
    }

    /**
     * Finds all context IDs that match the given fiscal year for consolidated data
     * Inline XBRL files contain multiple years, each with different contexts
     */
    private Set<String> findContextsForYear(Element root, int fiscalYear) {
        Set<String> contexts = new HashSet<>();
        String yearStr = String.valueOf(fiscalYear);

        // Find the xbrli namespace
        Namespace xbrliNs = root.getNamespace("xbrli");
        if (xbrliNs == null) {
            // Try without namespace prefix
            List<Element> contextElements = root.getDescendants(new org.jdom2.filter.ElementFilter("context")).toList();
            for (Element ctx : contextElements) {
                String contextId = ctx.getAttributeValue("id");
                if (contextId != null && isContextForYear(ctx, fiscalYear)) {
                    contexts.add(contextId);
                }
            }
        } else {
            List<Element> contextElements = root.getDescendants(new org.jdom2.filter.ElementFilter("context", xbrliNs)).toList();
            for (Element ctx : contextElements) {
                String contextId = ctx.getAttributeValue("id");
                if (contextId != null && isContextForYear(ctx, fiscalYear)) {
                    contexts.add(contextId);
                }
            }
        }

        logger.debug("Found {} contexts for year {}: {}", contexts.size(), fiscalYear, contexts);
        return contexts;
    }

    /**
     * Checks if a context element is for the given fiscal year
     */
    private boolean isContextForYear(Element contextElement, int fiscalYear) {
        String yearStr = String.valueOf(fiscalYear);

        // Look for period with matching year
        Element period = contextElement.getChild("period", contextElement.getNamespace());
        if (period != null) {
            Element startDate = period.getChild("startDate", period.getNamespace());
            Element endDate = period.getChild("endDate", period.getNamespace());
            Element instant = period.getChild("instant", period.getNamespace());

            if (endDate != null && endDate.getText().contains(yearStr)) {
                // Check it's an annual period (starts with year-01-01)
                if (startDate != null && startDate.getText().contains(yearStr + "-01-01")) {
                    return true;
                }
            }

            if (instant != null && instant.getText().contains(yearStr + "-12-31")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Extracts value from inline XBRL format (ix:nonFraction elements) with context filtering
     * Inline XBRL files use contextRef attributes to differentiate between years
     */
    private double extractValueFromInlineXBRL(Element root, Set<String> validContexts, Namespace ns, Namespace companyNs, String... tagNames) {
        // Find ix namespace
        Namespace ixNs = root.getNamespace("ix");
        if (ixNs == null) {
            // Try finding it in document
            for (Namespace declaredNs : root.getAdditionalNamespaces()) {
                if (declaredNs.getPrefix().equals("ix")) {
                    ixNs = declaredNs;
                    break;
                }
            }
        }

        if (ixNs == null || validContexts.isEmpty()) {
            logger.debug("No ix namespace or valid contexts found for inline XBRL extraction");
            return 0;
        }

        // Search for ix:nonFraction elements
        List<Element> nonFractionElements = root.getDescendants(new org.jdom2.filter.ElementFilter("nonFraction", ixNs)).toList();

        for (String tagName : tagNames) {
            // Try both us-gaap and company namespace
            String[] possibleNames = {
                "us-gaap:" + tagName,
                ns != null ? ns.getPrefix() + ":" + tagName : null,
                companyNs != null ? companyNs.getPrefix() + ":" + tagName : null
            };

            for (Element elem : nonFractionElements) {
                String nameAttr = elem.getAttributeValue("name");
                String contextRef = elem.getAttributeValue("contextRef");

                if (nameAttr != null && contextRef != null && validContexts.contains(contextRef)) {
                    for (String possibleName : possibleNames) {
                        if (possibleName != null && nameAttr.equals(possibleName)) {
                            try {
                                String text = elem.getText().trim().replace(",", "").replace("−", "-");
                                double value = Double.parseDouble(text);

                                // Handle scale attribute (scale="6" means multiply by 10^6)
                                String scaleAttr = elem.getAttributeValue("scale");
                                if (scaleAttr != null) {
                                    int scale = Integer.parseInt(scaleAttr);
                                    value = value * Math.pow(10, scale);
                                }

                                logger.debug("Extracted {} = {} from inline XBRL (context: {})", nameAttr, value, contextRef);
                                return value;
                            } catch (NumberFormatException e) {
                                logger.debug("Could not parse value for {}: {}", nameAttr, e.getMessage());
                            }
                        }
                    }
                }
            }
        }

        return 0;
    }

    /**
     * Extracts value from inline XBRL with segment filtering
     * Used for extracting revenue breakdown by product/service type
     */
    private double extractValueFromInlineXBRLWithSegment(Element root, int fiscalYear, String segmentMember, Namespace ns, Namespace companyNs, String... tagNames) {
        // Find contexts that match both the year AND the segment
        Set<String> validContexts = new HashSet<>();
        String yearStr = String.valueOf(fiscalYear);

        Namespace xbrliNs = root.getNamespace("xbrli");
        if (xbrliNs != null) {
            List<Element> contextElements = root.getDescendants(new org.jdom2.filter.ElementFilter("context", xbrliNs)).toList();
            for (Element ctx : contextElements) {
                String contextId = ctx.getAttributeValue("id");
                if (contextId != null && isContextForYear(ctx, fiscalYear)) {
                    // Check if this context has the required segment
                    Element segment = ctx.getDescendants(new org.jdom2.filter.ElementFilter("segment")).next();
                    if (segment != null) {
                        Element explicitMember = segment.getChild("explicitMember", segment.getNamespace());
                        if (explicitMember != null && explicitMember.getText().contains(segmentMember)) {
                            validContexts.add(contextId);
                        }
                    }
                }
            }
        }

        if (validContexts.isEmpty()) {
            logger.debug("No contexts found for year {} with segment {}", fiscalYear, segmentMember);
            return 0;
        }

        return extractValueFromInlineXBRL(root, validContexts, ns, companyNs, tagNames);
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
     * Finds the company-specific namespace (e.g., ual, jblu, etc.)
     * Company namespaces typically have short prefixes that aren't standard (us-gaap, dei, xbrl, etc.)
     */
    private Namespace findCompanyNamespace(Element root) {
        List<String> standardPrefixes = List.of("", "us-gaap", "dei", "xbrl", "xlink", "link",
                "xsi", "iso4217", "xbrldi", "srt", "ecd", "cyd", "xml");

        for (Namespace ns : root.getNamespacesInScope()) {
            String prefix = ns.getPrefix();
            // Look for non-standard prefixes that aren't empty
            if (!prefix.isEmpty() && !standardPrefixes.contains(prefix)) {
                logger.debug("Found potential company namespace: {}:{}", prefix, ns.getURI());
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
        Namespace companyNs = findCompanyNamespace(rootElement);
        int fiscalYear = extractFiscalYear(xbrlFile.getName(), rootElement);

        if (companyNs != null) {
            logger.info("Found company-specific namespace: {}:{}", companyNs.getPrefix(), companyNs.getURI());
        }

        DetailedFinancialData data = new DetailedFinancialData();
        data.setFiscalYear(fiscalYear);

        // Extract all detailed statements (pass both namespaces)
        data.setIncomeStatement(parseDetailedIncomeStatement(rootElement, usGaapNs, companyNs, fiscalYear));
        data.setBalanceSheet(parseDetailedBalanceSheet(rootElement, usGaapNs, companyNs, fiscalYear));
        data.setCashFlow(parseDetailedCashFlow(rootElement, usGaapNs, companyNs, fiscalYear));

        logger.info("Successfully parsed detailed financial data for year {}", fiscalYear);
        return data;
    }

    /**
     * Extracts detailed income statement
     */
    private DetailedIncomeStatement parseDetailedIncomeStatement(Element root, Namespace ns, Namespace companyNs, int fiscalYear) {
        DetailedIncomeStatement stmt = new DetailedIncomeStatement(fiscalYear);

        // Detect if this is inline XBRL format (check for ix namespace)
        boolean isInlineXBRL = root.getNamespace("ix") != null ||
                               root.getAdditionalNamespaces().stream().anyMatch(n -> "ix".equals(n.getPrefix()));

        double totalRevenue, passengerRevenue, cargoRevenue, otherRevenue;

        if (isInlineXBRL) {
            logger.info("Detected inline XBRL format - using context-aware extraction for year {}", fiscalYear);

            // Find contexts for this year (consolidated, no segments)
            Set<String> consolidatedContexts = findContextsForYear(root, fiscalYear);

            // Extract revenue using inline XBRL method
            totalRevenue = extractValueFromInlineXBRL(root, consolidatedContexts, ns, companyNs,
                    "Revenues",
                    "OperatingRevenue",
                    "OperatingRevenues",
                    "TotalOperatingRevenue",
                    "TotalOperatingRevenues",
                    "RevenueFromContractWithCustomerExcludingAssessedTax",
                    "SalesRevenueNet",
                    "RevenueFromContractWithCustomer"
            );

            // Extract segment revenue (passenger, cargo, other)
            passengerRevenue = extractValueFromInlineXBRLWithSegment(root, fiscalYear, "PassengerMember", ns, companyNs,
                    "PassengerRevenue",
                    "PassengerRevenues",
                    "PassengerRevenueGross",
                    "TransportationRevenue",
                    "AirTransportationRevenue",
                    "RevenuePassenger",
                    "ScheduledServiceRevenue",
                    "AirlinePassengerRevenue",
                    "RevenueFromContractWithCustomerExcludingAssessedTax"
            );

            cargoRevenue = extractValueFromInlineXBRLWithSegment(root, fiscalYear, "CargoAndFreightMember", ns, companyNs,
                    "CargoRevenue",
                    "CargoRevenues",
                    "FreightRevenue",
                    "CargoAndFreightRevenue",
                    "RevenueCargo",
                    "MailRevenue",
                    "RevenueFromContractWithCustomerExcludingAssessedTax"
            );

            otherRevenue = extractValueFromInlineXBRLWithSegment(root, fiscalYear, "ProductAndServiceOtherMember", ns, companyNs,
                    "OtherOperatingIncome",
                    "OtherOperatingRevenue",
                    "OtherOperatingRevenues",
                    "AncillaryRevenue",
                    "LoyaltyProgramRevenue",
                    "MiscellaneousOperatingRevenue",
                    "RevenueFromContractWithCustomerExcludingAssessedTax"
            );
        } else {
            // Traditional XBRL extraction (for standalone XML files)
            logger.info("Using traditional XBRL extraction for year {}", fiscalYear);

            totalRevenue = extractValue(root, ns, companyNs,
                    "Revenues",
                    "OperatingRevenue",
                    "OperatingRevenues",
                    "TotalOperatingRevenue",
                    "TotalOperatingRevenues",
                    "RevenueFromContractWithCustomerExcludingAssessedTax",
                    "SalesRevenueNet",
                    "RevenueFromContractWithCustomer"
            );

            passengerRevenue = extractValue(root, ns, companyNs,
                    "PassengerRevenue",
                    "PassengerRevenues",
                    "PassengerRevenueGross",
                    "TransportationRevenue",
                    "AirTransportationRevenue",
                    "RevenuePassenger",
                    "ScheduledServiceRevenue",
                    "AirlinePassengerRevenue"
            );

            cargoRevenue = extractValue(root, ns, companyNs,
                    "CargoRevenue",
                    "CargoRevenues",
                    "FreightRevenue",
                    "CargoAndFreightRevenue",
                    "RevenueCargo",
                    "MailRevenue"
            );

            otherRevenue = extractValue(root, ns, companyNs,
                    "OtherOperatingIncome",
                    "OtherOperatingRevenue",
                    "OtherOperatingRevenues",
                    "AncillaryRevenue",
                    "LoyaltyProgramRevenue",
                    "MiscellaneousOperatingRevenue"
            );
        }

        // Log extracted values for debugging
        logger.info("Revenue extraction for year {}: Total=${}, Passenger=${}, Cargo=${}, Other=${}",
            fiscalYear,
            totalRevenue / 1_000_000,
            passengerRevenue / 1_000_000,
            cargoRevenue / 1_000_000,
            otherRevenue / 1_000_000);

        // Revenue calculation logic - ensure consistency
        if (totalRevenue > 0 && passengerRevenue > 0 && cargoRevenue >= 0) {
            // We have total and components - calculate other if missing
            if (otherRevenue == 0) {
                otherRevenue = totalRevenue - passengerRevenue - cargoRevenue;
                logger.debug("Calculated other revenue from total: ${}", otherRevenue / 1_000_000);
            }

            // Check if the calculation makes sense (other shouldn't be negative)
            if (otherRevenue < 0) {
                // This means "totalRevenue" is actually wrong - it's probably just "other revenue"
                // The real total should be the sum of components
                logger.warn("Detected revenue misclassification for year {}: extracted total (${}) is less than passenger+cargo (${})",
                    fiscalYear,
                    totalRevenue / 1_000_000,
                    (passengerRevenue + cargoRevenue) / 1_000_000);
                logger.info("Treating extracted 'total' as 'other revenue' and recalculating");

                // The "totalRevenue" we extracted is actually "otherRevenue"
                otherRevenue = totalRevenue;
                totalRevenue = passengerRevenue + cargoRevenue + otherRevenue;

                logger.info("Corrected revenue for year {}: Total=${} (Pax: ${}, Cargo: ${}, Other: ${})",
                    fiscalYear,
                    totalRevenue / 1_000_000,
                    passengerRevenue / 1_000_000,
                    cargoRevenue / 1_000_000,
                    otherRevenue / 1_000_000);
            } else {
                // Verify total matches sum (within rounding)
                double calculatedTotal = passengerRevenue + cargoRevenue + otherRevenue;
                if (Math.abs(calculatedTotal - totalRevenue) > 1000) { // Allow $1k rounding error
                    logger.warn("Revenue mismatch for year {}: Total={}, Sum={}. Using calculated sum.",
                        fiscalYear, totalRevenue / 1_000_000, calculatedTotal / 1_000_000);
                    totalRevenue = calculatedTotal;
                }
            }
        } else if (passengerRevenue > 0) {
            // We have components but not total - sum them
            totalRevenue = passengerRevenue + cargoRevenue + otherRevenue;
            logger.info("Calculated total revenue from components for year {}: ${} (Pax: ${}, Cargo: ${}, Other: ${})",
                fiscalYear,
                totalRevenue / 1_000_000,
                passengerRevenue / 1_000_000,
                cargoRevenue / 1_000_000,
                otherRevenue / 1_000_000);
        } else if (totalRevenue > 0) {
            // We have total but not components - issue a warning
            logger.warn("Have total revenue (${}) but missing revenue breakdown for year {}",
                totalRevenue / 1_000_000, fiscalYear);
        }

        // Set all values
        stmt.setTotalOperatingRevenue(totalRevenue);
        stmt.setPassengerRevenue(passengerRevenue);
        stmt.setCargoRevenue(cargoRevenue);
        stmt.setOtherOperatingRevenue(otherRevenue);

        // Loyalty program revenue (subset of other revenue)
        stmt.setLoyaltyProgramRevenue(extractValue(root, ns, companyNs,
                "LoyaltyProgramRevenue",
                "MileageCreditRevenue",
                "FrequentFlyerMileageCreditRevenue",
                "MileagePlusRevenue",
                "TrueBluePointsRevenue"
        ));

        stmt.setBaggageFees(extractValue(root, ns, companyNs,
                "BaggageFeesRevenue",
                "PassengerBaggageRevenue",
                "BaggageFees"
        ));

        // Operating Expenses
        stmt.setAircraftFuel(extractValue(root, ns, companyNs,
                "AircraftFuelExpense",
                "FuelAndFuelRelatedExpense",
                "FuelExpense",
                "AircraftFuelAndRelatedTaxes",
                "FuelCosts",
                "CostOfGoodsAndServicesSoldFuel"
        ));

        stmt.setSalariesAndRelatedCosts(extractValue(root, ns, companyNs,
                "LaborAndRelatedExpense",
                "SalariesAndWages",
                "EmployeeBenefitsAndShareBasedCompensation",
                "SalariesWagesAndOfficersCompensation",
                "LaborRelatedExpense",
                "WagesAndSalaries"
        ));

        stmt.setRegionalCapacityPurchase(extractValue(root, ns, companyNs,
                "RegionalCapacityPurchaseExpense",
                "ContractualAgreementsExpense",
                "RegionalAffiliateExpense",
                "PurchasedServicesExpense",
                "CapacityPurchaseAgreementExpense"
        ));

        stmt.setLandingFeesAndRent(extractValue(root, ns, companyNs,
                "LandingFeesAndOtherRentalsCosts",
                "LandingFeesAndOtherRentals",
                "AirportFeesAndRent",
                "LandingFees",
                "AirportAndAirwayFees",
                "LandingAndOtherFees"
        ));

        stmt.setAircraftMaintenance(extractValue(root, ns, companyNs,
                "AircraftMaintenanceExpense",
                "AircraftMaintenanceMaterialsAndRepairs",
                "MaintenanceMaterialsAndRepairs",
                "MaintenanceExpense",
                "MaintenanceAndRepairs",
                "AircraftMaintenanceCost"
        ));

        // Depreciation - try multiple sources
        double depreciation = extractValue(root, ns, companyNs,
                "DepreciationAndAmortization",  // Try combined first
                "DepreciationDepletionAndAmortization",
                "Depreciation",
                "DepreciationExpense",
                "DepreciationAndAmortizationDepreciationComponent",
                "DepreciationOfPropertyPlantAndEquipment",
                "DepreciationNonproduction"
        );

        double amortization = extractValue(root, ns, companyNs,
                "AmortizationOfIntangibleAssets",
                "DepreciationAndAmortizationAmortizationComponent",
                "AmortizationExpense"
        );

        // If we got combined D&A but no separate amortization, use the combined value for depreciation
        // and try to extract separate components
        if (depreciation > 0 && amortization == 0) {
            // We might have combined D&A in depreciation field
            // Try to find separate amortization to split it out
            double separateAmort = extractValue(root, ns, companyNs,
                    "AmortizationOfIntangibleAssets",
                    "AmortizationExpense",
                    "Amortization"
            );
            if (separateAmort > 0 && separateAmort < depreciation) {
                amortization = separateAmort;
                depreciation = depreciation - separateAmort;
            }
        }

        stmt.setDepreciation(depreciation);
        stmt.setAmortization(amortization);

        stmt.setDistributionExpenses(extractValue(root, ns, companyNs,
                "SellingAndMarketingExpense",
                "DistributionExpense",
                "SalesAndMarketingExpense"
        ));

        stmt.setAircraftRent(extractValue(root, ns, companyNs,
                "AircraftRentExpense",
                "OperatingLeaseExpense",
                "OperatingLeaseCost"
        ));

        stmt.setSpecialCharges(extractValue(root, ns, companyNs,
                "RestructuringCharges",
                "AssetImpairmentCharges",
                "SpecialCharges",
                "SpecialItemsAndOtherCharges"
        ));

        stmt.setOtherOperatingExpenses(extractValue(root, ns, companyNs,
                "OtherOperatingExpenses",
                "OtherCostAndExpenseOperating",
                "OtherOperatingCostAndExpense",
                "OtherExpenses"
        ));

        stmt.setTotalOperatingExpenses(extractValue(root, ns, companyNs,
                "OperatingExpenses",
                "CostsAndExpenses",
                "OperatingCostsAndExpenses",
                "CostOfRevenueAndOperatingExpenses"
        ));

        // Operating Income
        stmt.setOperatingIncome(extractValue(root, ns, companyNs,
                "OperatingIncomeLoss",
                "OperatingIncome"
        ));

        // Non-operating items
        stmt.setInterestExpense(extractValue(root, ns, companyNs,
                "InterestExpense",
                "InterestExpenseDebt",
                "InterestExpenseDebtAndCapitalLease",
                "InterestPaid",
                "InterestAndDebtExpense",
                "InterestExpenseNet",
                "InterestExpenseBorrowings",
                "InterestExpenseOther",
                "InterestExpenseTotal"
        ));

        stmt.setInterestIncome(extractValue(root, ns, companyNs,
                "InterestIncome",
                "InterestIncomeOperating",
                "InterestAndDividendIncomeOperating",
                "InvestmentIncomeInterest",
                "InterestIncomeOther"
        ));

        stmt.setOtherIncomeExpense(extractValue(root, ns, companyNs,
                "OtherNonoperatingIncomeExpense",
                "NonoperatingIncomeExpense",
                "OtherIncomeAndExpenses",
                "OtherNonoperatingIncome"
        ));

        // Taxes
        stmt.setIncomeTaxExpense(extractValue(root, ns, companyNs,
                "IncomeTaxExpenseBenefit",
                "IncomeTaxExpense",
                "IncomeTaxesPaid",
                "CurrentIncomeTaxExpenseBenefit"
        ));

        stmt.setPretaxIncome(extractValue(root, ns, companyNs,
                "IncomeLossFromContinuingOperationsBeforeIncomeTaxesExtraordinaryItemsNoncontrollingInterest",
                "IncomeLossFromContinuingOperationsBeforeIncomeTaxesMinorityInterestAndIncomeLossFromEquityMethodInvestments"
        ));

        // Net Income
        stmt.setNetIncome(extractValue(root, ns, companyNs,
                "NetIncomeLoss",
                "ProfitLoss"
        ));

        stmt.setNetIncomeAvailableToCommon(extractValue(root, ns, companyNs,
                "NetIncomeLossAvailableToCommonStockholdersBasic",
                "NetIncomeLoss"
        ));

        // Per Share Data
        stmt.setBasicEPS(extractValue(root, ns, companyNs,
                "EarningsPerShareBasic"
        ));

        stmt.setDilutedEPS(extractValue(root, ns, companyNs,
                "EarningsPerShareDiluted"
        ));

        stmt.setWeightedAverageSharesBasic(extractValue(root, ns, companyNs,
                "WeightedAverageNumberOfSharesOutstandingBasic"
        ));

        stmt.setWeightedAverageSharesDiluted(extractValue(root, ns, companyNs,
                "WeightedAverageNumberOfDilutedSharesOutstanding"
        ));

        stmt.setShareCountYearEnd(extractValue(root, ns, companyNs,
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
        int employeeCount = (int) extractValue(root, ns, companyNs,
                "NumberOfEmployees",
                "EmployeeRelatedLiabilitiesCurrentNumberOfEmployees"
        );
        stmt.setNumberOfEmployees(employeeCount);

        // Calculate fuel gallons and price if possible
        if (stmt.getAircraftFuel() > 0) {
            double gallons = extractValue(root, ns, companyNs,
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
    private DetailedBalanceSheet parseDetailedBalanceSheet(Element root, Namespace ns, Namespace companyNs, int fiscalYear) {
        DetailedBalanceSheet bs = new DetailedBalanceSheet(fiscalYear);

        // Current Assets
        bs.setCashAndCashEquivalents(extractValue(root, ns, companyNs,
                "CashAndCashEquivalentsAtCarryingValue",
                "Cash",
                "CashEquivalents"
        ));

        bs.setShortTermInvestments(extractValue(root, ns, companyNs,
                "ShortTermInvestments",
                "MarketableSecuritiesCurrent",
                "AvailableForSaleSecuritiesCurrent"
        ));

        bs.setRestrictedCash(extractValue(root, ns, companyNs,
                "RestrictedCashCurrent",
                "RestrictedCash"
        ));

        bs.setAccountsReceivable(extractValue(root, ns, companyNs,
                "AccountsReceivableNetCurrent",
                "ReceivablesNetCurrent",
                "AccountsReceivableNet"
        ));

        bs.setAllowanceForDoubtfulAccounts(extractValue(root, ns, companyNs,
                "AllowanceForDoubtfulAccountsReceivableCurrent",
                "AllowanceForDoubtfulAccounts"
        ));

        bs.setPrepaidExpenses(extractValue(root, ns, companyNs,
                "PrepaidExpenseCurrent",
                "PrepaidExpenseAndOtherAssetsCurrent"
        ));

        bs.setSparePartsAndSupplies(extractValue(root, ns, companyNs,
                "SparePartsSuppliesAndFuel",
                "InventorySparePartsSuppliesAndFuel",
                "MaterialsSuppliesAndFuel"
        ));

        bs.setTotalCurrentAssets(extractValue(root, ns, companyNs,
                "AssetsCurrent"
        ));

        // Property, Plant & Equipment
        bs.setFlightEquipment(extractValue(root, ns, companyNs,
                "FlightEquipmentGross",
                "AircraftAndFlightEquipmentGross",
                "PropertyPlantAndEquipmentAircraft"
        ));

        bs.setGroundEquipment(extractValue(root, ns, companyNs,
                "PropertyPlantAndEquipmentOther",
                "GroundPropertyAndEquipmentGross"
        ));

        bs.setBuildings(extractValue(root, ns, companyNs,
                "PropertyPlantAndEquipmentBuildings",
                "BuildingsAndImprovementsGross"
        ));

        bs.setConstructionInProgress(extractValue(root, ns, companyNs,
                "ConstructionInProgressGross",
                "PropertyPlantAndEquipmentConstructionInProgress"
        ));

        bs.setTotalPPEAtCost(extractValue(root, ns, companyNs,
                "PropertyPlantAndEquipmentGross"
        ));

        bs.setAccumulatedDepreciation(extractValue(root, ns, companyNs,
                "AccumulatedDepreciationDepletionAndAmortizationPropertyPlantAndEquipment",
                "AccumulatedDepreciation"
        ));

        bs.setNetPPE(extractValue(root, ns, companyNs,
                "PropertyPlantAndEquipmentNet"
        ));

        // Operating Lease ROU Assets (ASC 842)
        bs.setOperatingLeaseRightOfUseAssets(extractValue(root, ns, companyNs,
                "OperatingLeaseRightOfUseAsset",
                "OperatingLeaseRightOfUseAssetNoncurrent"
        ));

        // Intangibles
        bs.setGoodwill(extractValue(root, ns, companyNs,
                "Goodwill"
        ));

        bs.setIntangibleAssets(extractValue(root, ns, companyNs,
                "IntangibleAssetsNetExcludingGoodwill",
                "FiniteLivedIntangibleAssetsNet"
        ));

        bs.setRoutesAndSlots(extractValue(root, ns, companyNs,
                "RouteAuthoritiesIntangibleAsset",
                "LandingAndTakeoffSlotsIntangibleAsset",
                "AirlineRoutesIntangibleAsset"
        ));

        bs.setLongTermInvestments(extractValue(root, ns, companyNs,
                "LongTermInvestments",
                "InvestmentsNoncurrent"
        ));

        bs.setDeferredTaxAssets(extractValue(root, ns, companyNs,
                "DeferredTaxAssetsNetNoncurrent",
                "DeferredIncomeTaxAssetsNet"
        ));

        bs.setTotalAssets(extractValue(root, ns, companyNs,
                "Assets"
        ));

        // Current Liabilities
        bs.setAccountsPayable(extractValue(root, ns, companyNs,
                "AccountsPayableCurrent"
        ));

        bs.setAccruedSalariesAndBenefits(extractValue(root, ns, companyNs,
                "EmployeeRelatedLiabilitiesCurrent",
                "AccruedSalariesCurrent"
        ));

        // Air Traffic Liability - KEY for airlines!
        bs.setAirTrafficLiability(extractValue(root, ns, companyNs,
                "AirTrafficLiabilityCurrent",
                "CustomerAdvancesAndDeposits",
                "DeferredRevenueAndCustomerAdvances",
                "ContractWithCustomerLiabilityCurrent"
        ));

        bs.setCurrentDebt(extractValue(root, ns, companyNs,
                "DebtCurrent",
                "ShortTermBorrowings",
                "LongTermDebtCurrent"
        ));

        bs.setCurrentOperatingLeaseLiabilities(extractValue(root, ns, companyNs,
                "OperatingLeaseLiabilityCurrent"
        ));

        bs.setCurrentFinanceLeaseLiabilities(extractValue(root, ns, companyNs,
                "FinanceLeaseLiabilityCurrent",
                "CapitalLeaseObligationsCurrent"
        ));

        bs.setTotalCurrentLiabilities(extractValue(root, ns, companyNs,
                "LiabilitiesCurrent"
        ));

        // Long-term Liabilities
        bs.setLongTermDebt(extractValue(root, ns, companyNs,
                "LongTermDebtNoncurrent",
                "LongTermDebt"
        ));

        bs.setLongTermOperatingLeaseLiabilities(extractValue(root, ns, companyNs,
                "OperatingLeaseLiabilityNoncurrent"
        ));

        bs.setLongTermFinanceLeaseLiabilities(extractValue(root, ns, companyNs,
                "FinanceLeaseLiabilityNoncurrent",
                "CapitalLeaseObligationsNoncurrent"
        ));

        bs.setPensionLiabilities(extractValue(root, ns, companyNs,
                "DefinedBenefitPensionPlanLiabilitiesNoncurrent",
                "PensionAndOtherPostretirementDefinedBenefitPlansLiabilitiesNoncurrent"
        ));

        bs.setPostRetirementBenefits(extractValue(root, ns, companyNs,
                "OtherPostretirementDefinedBenefitPlanLiabilitiesNoncurrent"
        ));

        bs.setDeferredTaxLiabilities(extractValue(root, ns, companyNs,
                "DeferredIncomeTaxLiabilitiesNet",
                "DeferredTaxLiabilitiesNoncurrent"
        ));

        bs.setLoyaltyProgramDeferredRevenue(extractValue(root, ns, companyNs,
                "LoyaltyProgramDeferredRevenue",
                "DeferredRevenueNoncurrent"
        ));

        bs.setTotalLiabilities(extractValue(root, ns, companyNs,
                "Liabilities",
                "LiabilitiesTotal",
                "LiabilitiesAndStockholdersEquity"  // Will be corrected by validation below
        ));

        // Stockholders' Equity
        bs.setCommonStock(extractValue(root, ns, companyNs,
                "CommonStockValue"
        ));

        bs.setCommonSharesOutstanding(extractValue(root, ns, companyNs,
                "CommonStockSharesOutstanding",
                "CommonStockSharesIssued"
        ));

        bs.setAdditionalPaidInCapital(extractValue(root, ns, companyNs,
                "AdditionalPaidInCapital",
                "AdditionalPaidInCapitalCommonStock"
        ));

        bs.setTreasuryStock(extractValue(root, ns, companyNs,
                "TreasuryStockValue"
        ));

        bs.setRetainedEarnings(extractValue(root, ns, companyNs,
                "RetainedEarningsAccumulatedDeficit"
        ));

        bs.setAccumulatedOtherComprehensiveIncome(extractValue(root, ns, companyNs,
                "AccumulatedOtherComprehensiveIncomeLossNetOfTax",
                "AccumulatedOtherComprehensiveIncomeLossNetOfTaxTotal"
        ));

        bs.setTotalStockholdersEquity(extractValue(root, ns, companyNs,
                "StockholdersEquity",
                "Equity"
        ));

        // Validate and calculate Total Liabilities using accounting equation if needed
        // Assets = Liabilities + Equity, so Liabilities = Assets - Equity
        if (bs.getTotalAssets() > 0 && bs.getTotalStockholdersEquity() > 0) {
            if (bs.getTotalLiabilities() == 0) {
                // Liabilities not found in XBRL, calculate from accounting equation
                double calculatedLiabilities = bs.getTotalAssets() - bs.getTotalStockholdersEquity();
                logger.info("Calculating total liabilities from accounting equation: {} - {} = {}",
                        bs.getTotalAssets(), bs.getTotalStockholdersEquity(), calculatedLiabilities);
                bs.setTotalLiabilities(calculatedLiabilities);
            } else if (Math.abs(bs.getTotalLiabilities() - bs.getTotalAssets()) < 1000) {
                // Liabilities was incorrectly set to total assets, correct it
                double correctLiabilities = bs.getTotalAssets() - bs.getTotalStockholdersEquity();
                logger.warn("Correcting total liabilities from {} to {} using accounting equation",
                        bs.getTotalLiabilities(), correctLiabilities);
                bs.setTotalLiabilities(correctLiabilities);
            }
        }

        return bs;
    }

    /**
     * Extracts detailed cash flow statement
     */
    private DetailedCashFlow parseDetailedCashFlow(Element root, Namespace ns, Namespace companyNs, int fiscalYear) {
        DetailedCashFlow cf = new DetailedCashFlow(fiscalYear);

        // Operating Activities
        cf.setNetIncome(extractValue(root, ns, companyNs,
                "NetIncomeLoss",
                "ProfitLoss"
        ));

        cf.setDepreciation(extractValue(root, ns, companyNs,
                "Depreciation",
                "DepreciationNonproduction"
        ));

        cf.setAmortization(extractValue(root, ns, companyNs,
                "AmortizationOfIntangibleAssets"
        ));

        cf.setDeferredIncomeTaxes(extractValue(root, ns, companyNs,
                "DeferredIncomeTaxExpenseBenefit",
                "IncreaseDecreaseInDeferredIncomeTaxes"
        ));

        cf.setStockBasedCompensation(extractValue(root, ns, companyNs,
                "ShareBasedCompensation",
                "AllocatedShareBasedCompensationExpense"
        ));

        cf.setImpairmentCharges(extractValue(root, ns, companyNs,
                "AssetImpairmentCharges",
                "ImpairmentOfLongLivedAssetsHeldForUse"
        ));

        cf.setGainsLossesOnAssetSales(extractValue(root, ns, companyNs,
                "GainLossOnSaleOfPropertyPlantEquipment",
                "GainLossOnDispositionOfAssets"
        ));

        // Working Capital Changes
        cf.setChangeInReceivables(extractValue(root, ns, companyNs,
                "IncreaseDecreaseInAccountsReceivable"
        ));

        cf.setChangeInPrepaidExpenses(extractValue(root, ns, companyNs,
                "IncreaseDecreaseInPrepaidExpense",
                "IncreaseDecreaseInPrepaidDeferredExpenseAndOtherAssets"
        ));

        cf.setChangeInAccountsPayable(extractValue(root, ns, companyNs,
                "IncreaseDecreaseInAccountsPayable"
        ));

        cf.setChangeInAirTrafficLiability(extractValue(root, ns, companyNs,
                "IncreaseDecreaseInAirTrafficLiability",
                "IncreaseDecreaseInCustomerDeposits",
                "IncreaseDecreaseInContractWithCustomerLiability"
        ));

        cf.setChangeInAccruedLiabilities(extractValue(root, ns, companyNs,
                "IncreaseDecreaseInAccruedLiabilities"
        ));

        cf.setNetCashFromOperating(extractValue(root, ns, companyNs,
                "NetCashProvidedByUsedInOperatingActivities"
        ));

        // Investing Activities
        double capex = extractValue(root, ns, companyNs,
                "PaymentsToAcquirePropertyPlantAndEquipment",
                "CapitalExpendituresIncurredButNotYetPaid",
                "PaymentsForCapitalImprovements",
                "PaymentsToAcquireProductiveAssets",
                "CapitalExpenditure"
        );
        cf.setCapitalExpenditures(-Math.abs(capex));

        cf.setAircraftPurchases(extractValue(root, ns, companyNs,
                "PaymentsToAcquireAircraft",
                "PaymentsToAcquireFlightEquipment",
                "PaymentsToAcquireAircraftAndRelatedEquipment",
                "PaymentsForAircraftPurchases"
        ));

        cf.setPreDeliveryDeposits(extractValue(root, ns, companyNs,
                "PaymentsForAircraftPredeliveryDeposits",
                "IncreaseDecreaseInPrepaidDepositOnAircraft"
        ));

        cf.setProceedsFromAssetSales(extractValue(root, ns, companyNs,
                "ProceedsFromSaleOfPropertyPlantAndEquipment"
        ));

        cf.setPurchasesOfInvestments(extractValue(root, ns, companyNs,
                "PaymentsToAcquireInvestments",
                "PaymentsToAcquireMarketableSecurities"
        ));

        cf.setSalesOfInvestments(extractValue(root, ns, companyNs,
                "ProceedsFromSaleOfAvailableForSaleSecurities",
                "ProceedsFromSaleAndMaturityOfMarketableSecurities"
        ));

        cf.setNetCashFromInvesting(extractValue(root, ns, companyNs,
                "NetCashProvidedByUsedInInvestingActivities"
        ));

        // Financing Activities
        cf.setProceedsFromDebtIssuance(extractValue(root, ns, companyNs,
                "ProceedsFromIssuanceOfLongTermDebt",
                "ProceedsFromDebtNetOfIssuanceCosts"
        ));

        cf.setDebtRepayments(extractValue(root, ns, companyNs,
                "RepaymentsOfLongTermDebt",
                "RepaymentsOfDebt"
        ));

        cf.setProceedsFromSaleLeasebacks(extractValue(root, ns, companyNs,
                "ProceedsFromSaleAndLeasebackTransactions"
        ));

        cf.setFinanceLeasePayments(extractValue(root, ns, companyNs,
                "FinanceLeasePrincipalPayments",
                "RepaymentsOfLongTermCapitalLeaseObligations"
        ));

        cf.setProceedsFromStockIssuance(extractValue(root, ns, companyNs,
                "ProceedsFromIssuanceOfCommonStock"
        ));

        cf.setStockRepurchases(extractValue(root, ns, companyNs,
                "PaymentsForRepurchaseOfCommonStock"
        ));

        cf.setDividendsPaid(extractValue(root, ns, companyNs,
                "PaymentsOfDividends"
        ));

        cf.setNetCashFromFinancing(extractValue(root, ns, companyNs,
                "NetCashProvidedByUsedInFinancingActivities"
        ));

        // Summary
        cf.setNetChangeInCash(extractValue(root, ns, companyNs,
                "CashCashEquivalentsRestrictedCashAndRestrictedCashEquivalentsPeriodIncreaseDecreaseIncludingExchangeRateEffect",
                "CashAndCashEquivalentsPeriodIncreaseDecrease"
        ));

        cf.setCashAtEnd(extractValue(root, ns, companyNs,
                "CashAndCashEquivalentsAtCarryingValue",
                "Cash"
        ));

        // Supplemental
        cf.setCashPaidForInterest(extractValue(root, ns, companyNs,
                "InterestPaidNet",
                "InterestPaid"
        ));

        cf.setCashPaidForTaxes(extractValue(root, ns, companyNs,
                "IncomeTaxesPaidNet",
                "IncomeTaxesPaid"
        ));

        // Calculate free cash flow
        cf.calculateFreeCashFlow();

        return cf;
    }

    /**
     * Diagnostic method to inspect XBRL file and display available tags
     */
    public void inspectXBRLFile(File xbrlFile, String filter) throws Exception {
        System.out.println("═".repeat(120));
        System.out.println("XBRL FILE INSPECTION: " + xbrlFile.getName());
        System.out.println("═".repeat(120));
        System.out.println();

        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = saxBuilder.build(xbrlFile);
        Element rootElement = document.getRootElement();

        // Find US-GAAP namespace
        Namespace usGaapNs = findNamespace(rootElement, "us-gaap");
        System.out.println("US-GAAP Namespace: " + (usGaapNs != null ? usGaapNs.getURI() : "NOT FOUND"));
        System.out.println();

        // Get all namespaces
        System.out.println("Available Namespaces:");
        System.out.println("─".repeat(120));
        for (Namespace ns : rootElement.getNamespacesInScope()) {
            System.out.printf("%-15s %s%n", ns.getPrefix(), ns.getURI());
        }
        System.out.println();

        if (usGaapNs == null) {
            System.err.println("ERROR: US-GAAP namespace not found!");
            return;
        }

        // Collect all elements with values
        Map<String, String> tagValues = new TreeMap<>();
        collectElements(rootElement, usGaapNs, tagValues);

        // Display tags (filtered if requested)
        System.out.println("US-GAAP Tags and Values:");
        System.out.println("─".repeat(120));
        System.out.printf("%-60s %20s%n", "Tag Name", "Value");
        System.out.println("─".repeat(120));

        int count = 0;
        for (Map.Entry<String, String> entry : tagValues.entrySet()) {
            String tagName = entry.getKey();
            String value = entry.getValue();

            // Apply filter if provided
            if (filter != null && !filter.isEmpty()) {
                if (!tagName.toLowerCase().contains(filter.toLowerCase())) {
                    continue;
                }
            }

            // Format value for display
            String displayValue = value;
            if (value.length() > 18) {
                displayValue = value.substring(0, 15) + "...";
            }

            System.out.printf("%-60s %20s%n", tagName, displayValue);
            count++;

            if (count >= 100 && filter == null) {
                System.out.println("... (showing first 100 tags, use -f to filter)");
                break;
            }
        }

        System.out.println("─".repeat(120));
        System.out.println("Total tags found: " + tagValues.size());
        if (filter != null) {
            System.out.println("Tags matching filter '" + filter + "': " + count);
        }
        System.out.println("═".repeat(120));
    }

    /**
     * Helper method to recursively collect all elements with the given namespace
     */
    private void collectElements(Element element, Namespace ns, Map<String, String> tagValues) {
        // Check if this element is in the target namespace
        if (element.getNamespace().equals(ns)) {
            String tagName = element.getName();
            String value = element.getTextTrim();

            if (!value.isEmpty()) {
                // Store or update the value (last occurrence wins)
                tagValues.put(tagName, value);
            }
        }

        // Recursively process children
        for (Element child : element.getChildren()) {
            collectElements(child, ns, tagValues);
        }
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
