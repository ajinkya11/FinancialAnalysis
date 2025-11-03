package com.financialanalysis.glossary;

import java.util.*;

/**
 * Comprehensive glossary of financial metrics and abbreviations
 */
public class Glossary {

    private static final Map<String, GlossaryEntry> ENTRIES = new LinkedHashMap<>();

    static {
        // Profitability Metrics - Margins
        addEntry("Gross Margin", "Gross Profit Margin",
                "Percentage of revenue remaining after deducting cost of goods sold (COGS). " +
                "Calculated as: (Revenue - COGS) / Revenue × 100. " +
                "Shows how efficiently a company produces its goods/services. Higher is better.");

        addEntry("Operating Margin", "Operating Profit Margin",
                "Percentage of revenue remaining after deducting operating expenses. " +
                "Calculated as: Operating Income / Revenue × 100. " +
                "Indicates operational efficiency before interest and taxes. Higher is better.");

        addEntry("Net Margin", "Net Profit Margin",
                "Percentage of revenue that becomes profit after all expenses. " +
                "Calculated as: Net Income / Revenue × 100. " +
                "The bottom line profitability metric. Higher is better.");

        addEntry("EBITDA Margin", "Earnings Before Interest, Taxes, Depreciation & Amortization Margin",
                "Percentage showing operating performance excluding non-cash expenses. " +
                "Calculated as: EBITDA / Revenue × 100. " +
                "Useful for comparing companies with different capital structures. Higher is better.");

        // Profitability Metrics - Returns
        addEntry("ROA", "Return on Assets",
                "Measures how efficiently a company uses its assets to generate profit. " +
                "Calculated as: Net Income / Total Assets × 100. " +
                "Shows management's effectiveness at using assets. Higher is better.");

        addEntry("ROE", "Return on Equity",
                "Measures return generated for shareholders' equity. " +
                "Calculated as: Net Income / Shareholder's Equity × 100. " +
                "Key metric for comparing profitability across companies. Higher is better.");

        addEntry("ROIC", "Return on Invested Capital",
                "Measures return generated on all invested capital (debt + equity). " +
                "Calculated as: NOPAT / Invested Capital × 100. " +
                "Better than ROE for companies with significant debt. Higher is better.");

        addEntry("ROCE", "Return on Capital Employed",
                "Measures return on total capital employed in the business. " +
                "Calculated as: EBIT / Capital Employed × 100. " +
                "Shows how well company generates profits from capital. Higher is better.");

        // Liquidity Metrics
        addEntry("Current Ratio", "Current Ratio",
                "Measures ability to pay short-term obligations with current assets. " +
                "Calculated as: Current Assets / Current Liabilities. " +
                "Values > 1 indicate company can cover short-term debts. Higher is better (but not too high).");

        addEntry("Quick Ratio", "Quick Ratio (Acid-Test Ratio)",
                "More conservative liquidity measure excluding inventory. " +
                "Calculated as: (Current Assets - Inventory) / Current Liabilities. " +
                "Shows ability to meet obligations without selling inventory. Higher is better.");

        addEntry("Cash Ratio", "Cash Ratio",
                "Most conservative liquidity measure using only cash and equivalents. " +
                "Calculated as: Cash & Equivalents / Current Liabilities. " +
                "Shows immediate ability to pay current debts. Higher is better.");

        addEntry("OCF Ratio", "Operating Cash Flow Ratio",
                "Measures ability to cover current liabilities with operating cash flow. " +
                "Calculated as: Operating Cash Flow / Current Liabilities. " +
                "Indicates quality of liquidity through cash generation. Higher is better.");

        // Working Capital Cycle
        addEntry("DSO", "Days Sales Outstanding",
                "Average number of days to collect receivables after a sale. " +
                "Calculated as: (Accounts Receivable / Revenue) × 365. " +
                "Lower is better - means faster cash collection from customers.");

        addEntry("DIO", "Days Inventory Outstanding",
                "Average number of days to sell inventory. " +
                "Calculated as: (Inventory / COGS) × 365. " +
                "Lower is better - means faster inventory turnover.");

        addEntry("DPO", "Days Payable Outstanding",
                "Average number of days to pay suppliers. " +
                "Calculated as: (Accounts Payable / COGS) × 365. " +
                "Higher is better - means retaining cash longer (but not so high to damage supplier relationships).");

        addEntry("Cash Conv Cycle", "Cash Conversion Cycle",
                "Number of days between paying suppliers and collecting from customers. " +
                "Calculated as: DSO + DIO - DPO. " +
                "Lower is better - means faster conversion of inventory to cash.");

        // Leverage & Solvency
        addEntry("Debt/Equity", "Debt-to-Equity Ratio",
                "Measures financial leverage - debt relative to shareholder equity. " +
                "Calculated as: Total Debt / Shareholder's Equity. " +
                "Lower is better - indicates less financial risk.");

        addEntry("Debt/Assets", "Debt-to-Assets Ratio",
                "Percentage of assets financed by debt. " +
                "Calculated as: Total Debt / Total Assets. " +
                "Lower is better - shows lower reliance on borrowed funds.");

        addEntry("Interest Coverage", "Interest Coverage Ratio",
                "Measures ability to pay interest on outstanding debt. " +
                "Calculated as: EBIT / Interest Expense. " +
                "Higher is better - values > 2.5 generally considered safe.");

        addEntry("EBITDA Coverage", "EBITDA Interest Coverage",
                "Alternative interest coverage using EBITDA instead of EBIT. " +
                "Calculated as: EBITDA / Interest Expense. " +
                "Higher is better - more conservative than interest coverage.");

        // Efficiency Metrics
        addEntry("Asset Turnover", "Asset Turnover Ratio",
                "Measures how efficiently assets generate revenue. " +
                "Calculated as: Revenue / Total Assets. " +
                "Higher is better - indicates more efficient use of assets.");

        addEntry("Fixed Asset", "Fixed Asset Turnover",
                "Measures efficiency of using fixed assets to generate revenue. " +
                "Calculated as: Revenue / Net Fixed Assets. " +
                "Higher is better - varies significantly by industry.");

        addEntry("Inventory", "Inventory Turnover",
                "Number of times inventory is sold and replaced per year. " +
                "Calculated as: COGS / Average Inventory. " +
                "Higher is better - indicates efficient inventory management.");

        addEntry("Receivables", "Receivables Turnover",
                "Number of times receivables are collected per year. " +
                "Calculated as: Revenue / Accounts Receivable. " +
                "Higher is better - indicates efficient collection.");

        addEntry("WC Turn", "Working Capital Turnover",
                "Measures efficiency of using working capital to generate revenue. " +
                "Calculated as: Revenue / Working Capital. " +
                "Higher is better - shows effective working capital management.");

        // Growth Metrics
        addEntry("Revenue Growth", "Revenue Growth Rate",
                "Year-over-year percentage change in revenue. " +
                "Calculated as: (Current Year Revenue - Prior Year Revenue) / Prior Year Revenue × 100. " +
                "Indicates top-line growth. Higher is better.");

        addEntry("OpIncome Growth", "Operating Income Growth Rate",
                "Year-over-year percentage change in operating income. " +
                "Calculated as: (Current OpIncome - Prior OpIncome) / Prior OpIncome × 100. " +
                "Shows operational improvement. Higher is better.");

        addEntry("NetIncome Growth", "Net Income Growth Rate",
                "Year-over-year percentage change in net income. " +
                "Calculated as: (Current NI - Prior NI) / Prior NI × 100. " +
                "Bottom-line profit growth. Higher is better.");

        addEntry("EBITDA Growth", "EBITDA Growth Rate",
                "Year-over-year percentage change in EBITDA. " +
                "Calculated as: (Current EBITDA - Prior EBITDA) / Prior EBITDA × 100. " +
                "Operating cash generation growth. Higher is better.");

        addEntry("EPS Growth", "Earnings Per Share Growth Rate",
                "Year-over-year percentage change in earnings per share. " +
                "Calculated as: (Current EPS - Prior EPS) / Prior EPS × 100. " +
                "Per-share profit growth. Higher is better.");

        addEntry("FCF Growth", "Free Cash Flow Growth Rate",
                "Year-over-year percentage change in free cash flow. " +
                "Calculated as: (Current FCF - Prior FCF) / Prior FCF × 100. " +
                "Cash generation growth. Higher is better.");

        // Cash Flow Metrics
        addEntry("OCF Margin", "Operating Cash Flow Margin",
                "Percentage of revenue converted to operating cash flow. " +
                "Calculated as: Operating Cash Flow / Revenue × 100. " +
                "Indicates quality of revenue. Higher is better.");

        addEntry("FCF Margin", "Free Cash Flow Margin",
                "Percentage of revenue remaining as free cash after capital expenditures. " +
                "Calculated as: Free Cash Flow / Revenue × 100. " +
                "Shows cash generation efficiency. Higher is better.");

        addEntry("CapEx/Revenue", "Capital Expenditures to Revenue",
                "Percentage of revenue spent on capital expenditures. " +
                "Calculated as: CapEx / Revenue × 100. " +
                "Lower is better for mature companies; higher may be good for growth companies.");

        addEntry("CapEx/OCF", "Capital Expenditures to Operating Cash Flow",
                "Percentage of operating cash flow spent on capital expenditures. " +
                "Calculated as: CapEx / Operating Cash Flow × 100. " +
                "Lower is better - leaves more free cash flow.");

        // Quality of Earnings
        addEntry("OCF/NetIncome", "Operating Cash Flow to Net Income",
                "Ratio showing how much of net income is backed by actual cash. " +
                "Calculated as: Operating Cash Flow / Net Income. " +
                "Values > 1 indicate high-quality earnings. Higher is better.");

        addEntry("FCF/NetIncome", "Free Cash Flow to Net Income",
                "Shows free cash generation relative to reported earnings. " +
                "Calculated as: Free Cash Flow / Net Income. " +
                "Values > 1 indicate strong cash generation. Higher is better.");

        addEntry("CF-ROA", "Cash Flow Return on Assets",
                "Return on assets measured using operating cash flow instead of net income. " +
                "Calculated as: Operating Cash Flow / Total Assets × 100. " +
                "More conservative than traditional ROA. Higher is better.");

        addEntry("CF-ROE", "Cash Flow Return on Equity",
                "Return on equity measured using operating cash flow instead of net income. " +
                "Calculated as: Operating Cash Flow / Shareholder's Equity × 100. " +
                "More conservative than traditional ROE. Higher is better.");

        // Additional Terms
        addEntry("EBITDA", "Earnings Before Interest, Taxes, Depreciation & Amortization",
                "Operating profit before non-cash expenses and financing costs. " +
                "Calculated as: Operating Income + Depreciation + Amortization. " +
                "Proxy for operating cash generation.");

        addEntry("EBIT", "Earnings Before Interest and Taxes",
                "Operating profit before financing costs and taxes. " +
                "Same as Operating Income. " +
                "Measures core business profitability.");

        addEntry("EPS", "Earnings Per Share",
                "Company's profit allocated to each share of common stock. " +
                "Calculated as: Net Income / Shares Outstanding. " +
                "Key metric for stock valuation.");

        addEntry("COGS", "Cost of Goods Sold",
                "Direct costs attributable to producing goods/services sold. " +
                "Includes materials, labor, and manufacturing overhead. " +
                "Higher values reduce gross margin.");

        addEntry("NOPAT", "Net Operating Profit After Tax",
                "Operating profit after adjusting for taxes. " +
                "Calculated as: Operating Income × (1 - Tax Rate). " +
                "Used in advanced profitability metrics like ROIC.");

        addEntry("CapEx", "Capital Expenditures",
                "Funds used to acquire, upgrade, or maintain physical assets. " +
                "Includes property, plants, equipment, technology. " +
                "Required to sustain and grow business operations.");

        addEntry("FCF", "Free Cash Flow",
                "Cash generated after capital expenditures. " +
                "Calculated as: Operating Cash Flow - CapEx. " +
                "Available for dividends, debt reduction, or growth investments.");
    }

    private static void addEntry(String abbreviation, String fullName, String description) {
        ENTRIES.put(abbreviation.toLowerCase(), new GlossaryEntry(abbreviation, fullName, description));
    }

    /**
     * Look up a term in the glossary
     */
    public static GlossaryEntry lookup(String term) {
        return ENTRIES.get(term.toLowerCase());
    }

    /**
     * Get all glossary entries
     */
    public static Collection<GlossaryEntry> getAllEntries() {
        return ENTRIES.values();
    }

    /**
     * Search for terms matching a keyword
     */
    public static List<GlossaryEntry> search(String keyword) {
        List<GlossaryEntry> results = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();

        for (GlossaryEntry entry : ENTRIES.values()) {
            if (entry.getAbbreviation().toLowerCase().contains(lowerKeyword) ||
                entry.getFullName().toLowerCase().contains(lowerKeyword) ||
                entry.getDescription().toLowerCase().contains(lowerKeyword)) {
                results.add(entry);
            }
        }

        return results;
    }

    /**
     * Get categories of metrics
     */
    public static Map<String, List<String>> getCategories() {
        Map<String, List<String>> categories = new LinkedHashMap<>();

        categories.put("Profitability - Margins", Arrays.asList(
                "Gross Margin", "Operating Margin", "Net Margin", "EBITDA Margin"));

        categories.put("Profitability - Returns", Arrays.asList(
                "ROA", "ROE", "ROIC", "ROCE"));

        categories.put("Liquidity", Arrays.asList(
                "Current Ratio", "Quick Ratio", "Cash Ratio", "OCF Ratio"));

        categories.put("Working Capital Cycle", Arrays.asList(
                "DSO", "DIO", "DPO", "Cash Conv Cycle"));

        categories.put("Leverage & Solvency", Arrays.asList(
                "Debt/Equity", "Debt/Assets", "Interest Coverage", "EBITDA Coverage"));

        categories.put("Efficiency", Arrays.asList(
                "Asset Turnover", "Fixed Asset", "Inventory", "Receivables", "WC Turn"));

        categories.put("Growth", Arrays.asList(
                "Revenue Growth", "OpIncome Growth", "NetIncome Growth", "EBITDA Growth", "EPS Growth", "FCF Growth"));

        categories.put("Cash Flow", Arrays.asList(
                "OCF Margin", "FCF Margin", "CapEx/Revenue", "CapEx/OCF"));

        categories.put("Quality of Earnings", Arrays.asList(
                "OCF/NetIncome", "FCF/NetIncome", "CF-ROA", "CF-ROE"));

        categories.put("Other Terms", Arrays.asList(
                "EBITDA", "EBIT", "EPS", "COGS", "NOPAT", "CapEx", "FCF"));

        return categories;
    }

    /**
     * Represents a single glossary entry
     */
    public static class GlossaryEntry {
        private final String abbreviation;
        private final String fullName;
        private final String description;

        public GlossaryEntry(String abbreviation, String fullName, String description) {
            this.abbreviation = abbreviation;
            this.fullName = fullName;
            this.description = description;
        }

        public String getAbbreviation() {
            return abbreviation;
        }

        public String getFullName() {
            return fullName;
        }

        public String description() {
            return description;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return abbreviation + " - " + fullName + "\n" + description;
        }
    }
}
