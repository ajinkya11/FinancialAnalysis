package com.financialanalysis.parser;

import com.financialanalysis.model.FinancialStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts financial data from 10-K text content
 * Note: This is a simplified extractor that uses pattern matching.
 * Real-world 10-Ks vary significantly in format, so this may need customization.
 */
public class FinancialDataExtractor {
    private static final Logger logger = LoggerFactory.getLogger(FinancialDataExtractor.class);

    /**
     * Extracts financial data from 10-K text
     *
     * @param text the extracted text from a 10-K PDF
     * @param fiscalYear the fiscal year of the filing
     * @return a FinancialStatement object with extracted data
     */
    public FinancialStatement extract(String text, int fiscalYear) {
        logger.info("Extracting financial data for fiscal year {}", fiscalYear);

        FinancialStatement statement = new FinancialStatement(fiscalYear);

        // Extract fiscal year from text if not provided
        if (fiscalYear == 0) {
            fiscalYear = extractFiscalYear(text);
            statement.setFiscalYear(fiscalYear);
        }

        // Extract income statement items
        statement.setRevenue(extractValue(text, "revenue", "revenues", "net sales", "total revenue"));
        statement.setCostOfGoodsSold(extractValue(text, "cost of revenue", "cost of sales", "cost of goods sold"));
        statement.setGrossProfit(extractValue(text, "gross profit", "gross income"));
        statement.setOperatingIncome(extractValue(text, "operating income", "income from operations"));
        statement.setNetIncome(extractValue(text, "net income", "net earnings"));
        statement.setEbit(extractValue(text, "ebit", "earnings before interest and tax"));
        statement.setEbitda(extractValue(text, "ebitda"));
        statement.setInterestExpense(extractValue(text, "interest expense", "interest paid"));

        // Extract balance sheet items
        statement.setTotalAssets(extractValue(text, "total assets"));
        statement.setCurrentAssets(extractValue(text, "total current assets", "current assets"));
        statement.setCash(extractValue(text, "cash and cash equivalents", "cash"));
        statement.setMarketableSecurities(extractValue(text, "marketable securities", "short-term investments"));
        statement.setAccountsReceivable(extractValue(text, "accounts receivable", "receivables"));
        statement.setInventory(extractValue(text, "inventories", "inventory"));

        statement.setTotalLiabilities(extractValue(text, "total liabilities"));
        statement.setCurrentLiabilities(extractValue(text, "total current liabilities", "current liabilities"));
        statement.setTotalDebt(extractValue(text, "total debt", "total borrowings"));
        statement.setLongTermDebt(extractValue(text, "long-term debt", "long term debt"));
        statement.setShareholderEquity(extractValue(text, "total equity", "stockholders' equity", "shareholders' equity", "total shareholders' equity"));

        // Extract cash flow items
        statement.setOperatingCashFlow(extractValue(text, "cash from operating activities", "operating cash flow", "net cash provided by operating"));
        statement.setCapitalExpenditures(extractValue(text, "capital expenditures", "purchases of property and equipment", "capex"));
        statement.setFreeCashFlow(extractValue(text, "free cash flow"));
        statement.setDividendsPaid(extractValue(text, "dividends paid", "cash dividends"));
        statement.setShareRepurchases(extractValue(text, "share repurchases", "stock repurchased", "treasury stock"));

        // Extract per-share data
        statement.setEarningsPerShare(extractValue(text, "basic earnings per share", "earnings per share"));
        statement.setDilutedEPS(extractValue(text, "diluted earnings per share", "diluted eps"));
        statement.setSharesOutstanding(extractSharesOutstanding(text));

        // Calculate derived values
        calculateDerivedValues(statement);

        logger.info("Extraction complete for fiscal year {}", fiscalYear);
        return statement;
    }

    /**
     * Calculates derived financial values from extracted data
     */
    private void calculateDerivedValues(FinancialStatement statement) {
        // Calculate gross profit if not found
        if (statement.getGrossProfit() == 0 && statement.getRevenue() > 0 && statement.getCostOfGoodsSold() > 0) {
            statement.setGrossProfit(statement.getRevenue() - statement.getCostOfGoodsSold());
        }

        // Calculate free cash flow if not found
        if (statement.getFreeCashFlow() == 0 && statement.getOperatingCashFlow() > 0) {
            statement.setFreeCashFlow(statement.getOperatingCashFlow() - Math.abs(statement.getCapitalExpenditures()));
        }

        // Calculate book value per share if shares outstanding is available
        if (statement.getSharesOutstanding() > 0 && statement.getShareholderEquity() > 0) {
            statement.setBookValuePerShare(statement.getShareholderEquity() / statement.getSharesOutstanding());
        }

        // Calculate total debt if not found
        if (statement.getTotalDebt() == 0 && statement.getLongTermDebt() > 0) {
            double shortTermDebt = extractShortTermDebt(statement);
            statement.setTotalDebt(statement.getLongTermDebt() + shortTermDebt);
        }
    }

    private double extractShortTermDebt(FinancialStatement statement) {
        // This is a simplified calculation - in reality would need to parse from text
        return 0;
    }

    /**
     * Extracts fiscal year from the text
     */
    private int extractFiscalYear(String text) {
        Pattern pattern = Pattern.compile("(?:fiscal|year ended?|for the year)\\s+(\\d{4})", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    /**
     * Extracts shares outstanding from text
     */
    private long extractSharesOutstanding(String text) {
        Pattern pattern = Pattern.compile("(?:shares outstanding|common shares outstanding)[:\\s]+([\\d,]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String value = matcher.group(1).replaceAll(",", "");
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                logger.warn("Could not parse shares outstanding: {}", matcher.group(1));
            }
        }
        return 0;
    }

    /**
     * Extracts a financial value from text using multiple possible labels
     *
     * @param text the text to search
     * @param labels possible labels for the value
     * @return the extracted value in dollars, or 0 if not found
     */
    private double extractValue(String text, String... labels) {
        for (String label : labels) {
            // Pattern to match financial values in various formats
            // Examples: "Revenue 123,456", "Revenue: $123.5 million", "Revenue (in thousands): 123,456"
            String patternStr = "(?:" + Pattern.quote(label) + ")\\s*[:.]?\\s*(?:\\$)?\\s*([\\d,\\.]+)\\s*(million|billion|thousand)?";
            Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                String valueStr = matcher.group(1).replaceAll(",", "");
                String unit = matcher.group(2);

                try {
                    double value = Double.parseDouble(valueStr);

                    // Apply unit multiplier
                    if (unit != null) {
                        unit = unit.toLowerCase();
                        if (unit.contains("billion")) {
                            value *= 1_000_000_000;
                        } else if (unit.contains("million")) {
                            value *= 1_000_000;
                        } else if (unit.contains("thousand")) {
                            value *= 1_000;
                        }
                    }

                    logger.debug("Extracted {} = {} (from: {})", label, value, matcher.group(0));
                    return value;
                } catch (NumberFormatException e) {
                    logger.warn("Could not parse value for {}: {}", label, matcher.group(1));
                }
            }
        }

        logger.debug("Could not find value for labels: {}", String.join(", ", labels));
        return 0;
    }
}
