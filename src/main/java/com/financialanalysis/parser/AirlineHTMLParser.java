package com.financialanalysis.parser;

import com.financialanalysis.model.AirlineOperatingMetrics;
import com.financialanalysis.model.DetailedIncomeStatement;
import com.financialanalysis.model.SegmentInformation;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses HTML 10-K filings to extract airline-specific data not available in XBRL
 *
 * This parser extracts:
 * - Revenue breakdown (Passenger, Cargo, Other)
 * - Operating statistics (ASMs, RPMs, Load Factor, RASM, CASM)
 * - Segment information (by geography)
 * - Detailed operating expense breakdown
 */
public class AirlineHTMLParser {
    private static final Logger logger = LoggerFactory.getLogger(AirlineHTMLParser.class);

    /**
     * Parses revenue breakdown from 10-K HTML tables
     * Looks for tables containing passenger revenue, cargo revenue, and other operating revenue
     */
    public void parseRevenueBreakdown(File htmlFile, DetailedIncomeStatement statement, int fiscalYear) {
        try {
            Document doc = Jsoup.parse(htmlFile, "UTF-8");
            logger.info("Parsing revenue breakdown for fiscal year {}", fiscalYear);

            // Look for revenue tables - they typically have headers like:
            // "Operating Revenue", "Passenger", "Cargo", "Other"
            Elements tables = doc.select("table");
            logger.debug("Found {} total tables in HTML", tables.size());

            int tableIndex = 0;
            for (Element table : tables) {
                tableIndex++;
                // Check if this table contains revenue data
                String tableText = table.text().toLowerCase();

                // Log table preview for debugging
                if (containsRevenueKeywords(tableText) || tableText.contains("revenue")) {
                    logger.debug("Table {} contains revenue keywords. Preview: {}",
                        tableIndex, tableText.substring(0, Math.min(200, tableText.length())));
                }

                if (containsRevenueKeywords(tableText)) {
                    extractRevenueFromTable(table, statement, fiscalYear);
                    if (statement.getPassengerRevenue() > 0) {
                        logger.info("Successfully extracted revenue breakdown from table {}", tableIndex);
                        return;  // Found and extracted, done
                    }
                }
            }

            logger.warn("Could not find revenue breakdown table in HTML");
            logger.debug("Searched {} tables for revenue data", tables.size());
        } catch (IOException e) {
            logger.error("Error parsing HTML file: {}", e.getMessage());
        }
    }

    /**
     * Parses operating statistics table
     * Typically found in a section titled "Operating Statistics" or "Statistical Data"
     */
    public AirlineOperatingMetrics parseOperatingStatistics(File htmlFile, int fiscalYear) {
        AirlineOperatingMetrics metrics = new AirlineOperatingMetrics(fiscalYear, "Annual");

        try {
            Document doc = Jsoup.parse(htmlFile, "UTF-8");
            logger.info("Parsing operating statistics for fiscal year {}", fiscalYear);

            // Look for operating statistics tables
            Elements tables = doc.select("table");
            logger.debug("Found {} total tables in HTML", tables.size());

            int tableIndex = 0;
            for (Element table : tables) {
                tableIndex++;
                String tableText = table.text().toLowerCase();

                // Log tables that contain any operating metric keywords
                if (tableText.contains("available seat") || tableText.contains("asm") ||
                    tableText.contains("passenger miles") || tableText.contains("rpm") ||
                    tableText.contains("load factor") || tableText.contains("statistic")) {
                    logger.debug("Table {} may contain operating stats. Preview: {}",
                        tableIndex, tableText.substring(0, Math.min(200, tableText.length())));
                }

                if (containsOperatingStatsKeywords(tableText)) {
                    extractOperatingStatsFromTable(table, metrics, fiscalYear);
                    if (metrics.getAvailableSeatMiles() > 0) {
                        logger.info("Successfully extracted operating statistics from table {}", tableIndex);
                        // Calculate derived metrics
                        metrics.calculateLoadFactor();
                        metrics.calculateEmployeeProductivity();
                        return metrics;
                    }
                }
            }

            logger.warn("Could not find operating statistics table in HTML");
            logger.debug("Searched {} tables for operating statistics", tables.size());
        } catch (IOException e) {
            logger.error("Error parsing HTML file: {}", e.getMessage());
        }

        return metrics;
    }

    /**
     * Parses segment information (revenue by geography)
     */
    public List<SegmentInformation> parseSegmentInformation(File htmlFile, int fiscalYear) {
        List<SegmentInformation> segments = new ArrayList<>();

        try {
            Document doc = Jsoup.parse(htmlFile, "UTF-8");
            logger.info("Parsing segment information for fiscal year {}", fiscalYear);

            Elements tables = doc.select("table");

            for (Element table : tables) {
                String tableText = table.text().toLowerCase();
                if (containsSegmentKeywords(tableText)) {
                    extractSegmentsFromTable(table, segments, fiscalYear);
                    if (!segments.isEmpty()) {
                        logger.info("Successfully extracted {} segments", segments.size());
                        return segments;
                    }
                }
            }

            logger.warn("Could not find segment information table in HTML");
        } catch (IOException e) {
            logger.error("Error parsing HTML file: {}", e.getMessage());
        }

        return segments;
    }

    // Helper methods for keyword detection
    private boolean containsRevenueKeywords(String text) {
        // More flexible matching: check if table contains revenue-related terms
        // and at least two of the common revenue categories
        boolean hasRevenueContext = text.contains("operating revenue") ||
                                     text.contains("total revenue") ||
                                     (text.contains("revenue") && (text.contains("operating") || text.contains("total")));

        int categoryCount = 0;
        if (text.contains("passenger")) categoryCount++;
        if (text.contains("cargo") || text.contains("freight")) categoryCount++;
        if (text.contains("other") || text.contains("ancillary") || text.contains("loyalty")) categoryCount++;

        // Match if we have revenue context and at least 2 revenue categories
        return hasRevenueContext && categoryCount >= 2;
    }

    private boolean containsOperatingStatsKeywords(String text) {
        int keywordCount = 0;
        // Check for ASM variations
        if (text.contains("available seat miles") || text.contains("available seat-miles") ||
            text.contains("asm") || text.contains("asms") || text.contains("capacity")) keywordCount++;

        // Check for RPM variations
        if (text.contains("revenue passenger miles") || text.contains("revenue passenger-miles") ||
            text.contains("rpm") || text.contains("rpms") || text.contains("traffic")) keywordCount++;

        // Check for load factor
        if (text.contains("load factor") || text.contains("passenger load")) keywordCount++;

        // Check for unit revenue metrics
        if (text.contains("rasm") || text.contains("prasm") || text.contains("revenue per asm") ||
            text.contains("unit revenue") || text.contains("yield")) keywordCount++;

        // Check for unit cost metrics
        if (text.contains("casm") || text.contains("cost per asm") || text.contains("unit cost")) keywordCount++;

        // Check for passengers or departures
        if (text.contains("passengers") && !text.contains("revenue passenger miles")) keywordCount++;
        if (text.contains("departures") || text.contains("flights")) keywordCount++;

        // Need at least 2 key metrics present (lowered from 3 for more flexibility)
        return keywordCount >= 2;
    }

    private boolean containsSegmentKeywords(String text) {
        return (text.contains("domestic") || text.contains("atlantic") || text.contains("pacific") || text.contains("latin"))
            && (text.contains("revenue") || text.contains("operating income"));
    }

    // Revenue extraction logic
    private void extractRevenueFromTable(Element table, DetailedIncomeStatement statement, int fiscalYear) {
        Elements rows = table.select("tr");
        logger.debug("Extracting revenue from table with {} rows", rows.size());

        // Log table headers for debugging
        Element headerRow = table.selectFirst("tr");
        if (headerRow != null) {
            Elements headerCells = headerRow.select("th, td");
            StringBuilder headerPreview = new StringBuilder("Table headers: [");
            for (int i = 0; i < Math.min(headerCells.size(), 10); i++) {
                headerPreview.append("Col").append(i).append(": '")
                    .append(headerCells.get(i).text()).append("', ");
            }
            logger.debug(headerPreview.toString() + "...]");
        }

        // Determine year column once for entire table
        int yearColumnIndex = findYearColumnForTable(table, fiscalYear);
        logger.debug("Using column {} for year {}", yearColumnIndex, fiscalYear);

        for (Element row : rows) {
            Elements cells = row.select("td, th");
            if (cells.isEmpty()) continue;

            String firstCell = cells.get(0).text().toLowerCase().trim();

            // Skip header rows
            if (firstCell.isEmpty() || cells.size() <= yearColumnIndex) continue;

            String cellValue = cells.get(yearColumnIndex).text();

            // Extract values based on row labels - more flexible matching
            // Passenger revenue
            if ((firstCell.contains("passenger") && (firstCell.contains("revenue") || firstCell.contains("operating"))) &&
                !firstCell.contains("cargo") && !firstCell.contains("freight")) {
                double value = extractNumber(cellValue);
                if (value > 0) {
                    statement.setPassengerRevenue(value * 1_000_000); // Convert to actual value
                    logger.debug("Extracted passenger revenue: {} (raw: '{}') from row: {}", value, cellValue, firstCell);
                }
            }
            // Cargo/Freight revenue
            else if ((firstCell.contains("cargo") || firstCell.contains("freight")) &&
                     (firstCell.contains("revenue") || firstCell.contains("operating"))) {
                double value = extractNumber(cellValue);
                if (value > 0) {
                    statement.setCargoRevenue(value * 1_000_000);
                    logger.debug("Extracted cargo revenue: {} (raw: '{}') from row: {}", value, cellValue, firstCell);
                }
            }
            // Other revenue (includes ancillary, loyalty, etc.)
            else if ((firstCell.contains("other") || firstCell.contains("ancillary") ||
                      firstCell.contains("loyalty") || firstCell.contains("mileageplus")) &&
                     (firstCell.contains("revenue") || firstCell.contains("operating"))) {
                double value = extractNumber(cellValue);
                if (value > 0) {
                    statement.setOtherOperatingRevenue(value * 1_000_000);
                    logger.debug("Extracted other revenue: {} (raw: '{}') from row: {}", value, cellValue, firstCell);
                }
            }
            // Total operating revenue
            else if (firstCell.contains("total") && (firstCell.contains("operating revenue") ||
                     firstCell.contains("operating revenues") || firstCell.equals("total revenue"))) {
                double value = extractNumber(cellValue);
                if (value > 0) {
                    statement.setTotalOperatingRevenue(value * 1_000_000);
                    logger.debug("Extracted total revenue: {} (raw: '{}') from row: {}", value, cellValue, firstCell);
                }
            }
        }

        // If we have passenger and cargo but not other, calculate it
        if (statement.getPassengerRevenue() > 0 && statement.getTotalOperatingRevenue() > 0
            && statement.getOtherOperatingRevenue() == 0) {
            double other = statement.getTotalOperatingRevenue()
                         - statement.getPassengerRevenue()
                         - statement.getCargoRevenue();
            if (other > 0) {
                statement.setOtherOperatingRevenue(other);
                logger.debug("Calculated other revenue: {}", other / 1_000_000);
            }
        }

        logger.debug("Revenue extraction complete. Passenger: {}, Cargo: {}, Other: {}, Total: {}",
            statement.getPassengerRevenue() / 1_000_000,
            statement.getCargoRevenue() / 1_000_000,
            statement.getOtherOperatingRevenue() / 1_000_000,
            statement.getTotalOperatingRevenue() / 1_000_000);
    }

    // Operating statistics extraction logic
    private void extractOperatingStatsFromTable(Element table, AirlineOperatingMetrics metrics, int fiscalYear) {
        Elements rows = table.select("tr");
        logger.debug("Extracting operating stats from table with {} rows", rows.size());

        // Log table headers for debugging
        Element headerRow = table.selectFirst("tr");
        if (headerRow != null) {
            Elements headerCells = headerRow.select("th, td");
            StringBuilder headerPreview = new StringBuilder("Table headers: [");
            for (int i = 0; i < Math.min(headerCells.size(), 10); i++) {
                headerPreview.append("Col").append(i).append(": '")
                    .append(headerCells.get(i).text()).append("', ");
            }
            logger.debug(headerPreview.toString() + "...]");
        }

        // Determine year column once for entire table
        int yearColumnIndex = findYearColumnForTable(table, fiscalYear);
        logger.debug("Using column {} for year {}", yearColumnIndex, fiscalYear);

        for (Element row : rows) {
            Elements cells = row.select("td, th");
            if (cells.isEmpty()) continue;

            String firstCell = cells.get(0).text().toLowerCase().trim();

            // Skip header rows or rows with insufficient columns
            if (firstCell.isEmpty() || cells.size() <= yearColumnIndex) continue;

            String cellValue = cells.get(yearColumnIndex).text();

            // Extract various operating metrics with more flexible matching
            // Available Seat Miles (ASM)
            if (firstCell.contains("available seat") || firstCell.contains("available seat-miles") ||
                firstCell.matches("^asms?$") || firstCell.contains("capacity (asm")) {
                long value = (long) extractNumber(cellValue);
                if (value > 0) {
                    metrics.setAvailableSeatMiles(value * 1_000_000); // Usually in millions
                    logger.debug("Extracted ASMs: {} (raw: '{}') from row: {}", value, cellValue, firstCell);
                }
            }
            // Revenue Passenger Miles (RPM)
            else if (firstCell.contains("revenue passenger") || firstCell.contains("revenue passenger-miles") ||
                     firstCell.matches("^rpms?$") || firstCell.contains("traffic (rpm")) {
                long value = (long) extractNumber(cellValue);
                if (value > 0) {
                    metrics.setRevenuePassengerMiles(value * 1_000_000);
                    logger.debug("Extracted RPMs: {} (raw: '{}') from row: {}", value, cellValue, firstCell);
                }
            }
            // Load Factor
            else if (firstCell.contains("load factor") || firstCell.contains("passenger load")) {
                double value = extractNumber(cellValue);
                if (value > 0) {
                    metrics.setLoadFactor(value);
                    logger.debug("Extracted load factor: {}% (raw: '{}') from row: {}", value, cellValue, firstCell);
                }
            }
            // Passenger Revenue per ASM (PRASM)
            else if (firstCell.contains("prasm") || firstCell.contains("passenger revenue per asm") ||
                     (firstCell.contains("passenger") && firstCell.contains("yield") && firstCell.contains("asm"))) {
                double value = extractNumber(cellValue);
                if (value > 0) {
                    metrics.setPassengerRevenuePerASM(value);
                    logger.debug("Extracted PRASM: {} cents (raw: '{}') from row: {}", value, cellValue, firstCell);
                }
            }
            // Total Revenue per ASM (RASM)
            else if ((firstCell.contains("rasm") && !firstCell.contains("prasm")) ||
                     firstCell.contains("total revenue per asm") || firstCell.contains("unit revenue")) {
                double value = extractNumber(cellValue);
                if (value > 0) {
                    metrics.setTotalRevenuePerASM(value);
                    logger.debug("Extracted RASM: {} cents (raw: '{}') from row: {}", value, cellValue, firstCell);
                }
            }
            // Cost per ASM (CASM)
            else if ((firstCell.contains("casm") && !firstCell.contains("ex") && !firstCell.contains("excluding")) ||
                     firstCell.contains("total cost per asm") || firstCell.contains("unit cost")) {
                double value = extractNumber(cellValue);
                if (value > 0) {
                    metrics.setOperatingCostPerASM(value);
                    logger.debug("Extracted CASM: {} cents (raw: '{}') from row: {}", value, cellValue, firstCell);
                }
            }
            // CASM excluding fuel
            else if ((firstCell.contains("casm") && (firstCell.contains("ex") || firstCell.contains("excluding"))) ||
                     firstCell.contains("cost per asm excluding fuel")) {
                double value = extractNumber(cellValue);
                if (value > 0) {
                    metrics.setCasmExcludingFuel(value);
                    logger.debug("Extracted CASM-ex: {} cents (raw: '{}') from row: {}", value, cellValue, firstCell);
                }
            }
            // Yield (passenger yield)
            else if (firstCell.contains("yield") && !firstCell.contains("asm")) {
                double value = extractNumber(cellValue);
                if (value > 0) {
                    metrics.setYield(value);
                    logger.debug("Extracted yield: {} cents (raw: '{}') from row: {}", value, cellValue, firstCell);
                }
            }
            // Passengers carried
            else if ((firstCell.contains("passengers") || firstCell.contains("passenger enplanements")) &&
                     !firstCell.contains("revenue passenger miles")) {
                long value = (long) extractNumber(cellValue);
                if (value > 0) {
                    metrics.setPassengersCarried(value * 1_000_000); // Usually in millions
                    logger.debug("Extracted passengers: {} (raw: '{}') from row: {}", value, cellValue, firstCell);
                }
            }
            // Departures / Flights
            else if (firstCell.contains("departures") || firstCell.contains("flights operated")) {
                int value = (int) extractNumber(cellValue);
                if (value > 0) {
                    metrics.setDepartures(value);
                    logger.debug("Extracted departures: {} (raw: '{}') from row: {}", value, cellValue, firstCell);
                }
            }
            // Aircraft count
            else if (firstCell.contains("aircraft") && (firstCell.contains("end") || firstCell.contains("period") ||
                     firstCell.contains("fleet"))) {
                int value = (int) extractNumber(cellValue);
                if (value > 0) {
                    metrics.setAircraftAtPeriodEnd(value);
                    logger.debug("Extracted aircraft count: {} (raw: '{}') from row: {}", value, cellValue, firstCell);
                }
            }
        }

        logger.debug("Operating stats extraction complete. ASMs: {}, RPMs: {}, Load Factor: {}%",
            metrics.getAvailableSeatMiles() / 1_000_000,
            metrics.getRevenuePassengerMiles() / 1_000_000,
            metrics.getLoadFactor());
    }

    // Segment extraction logic
    private void extractSegmentsFromTable(Element table, List<SegmentInformation> segments, int fiscalYear) {
        Elements rows = table.select("tr");
        SegmentInformation segmentInfo = new SegmentInformation(fiscalYear);

        for (Element row : rows) {
            Elements cells = row.select("td, th");
            if (cells.isEmpty()) continue;

            String firstCell = cells.get(0).text().toLowerCase().trim();

            // Look for segment names
            if (firstCell.contains("domestic") || firstCell.contains("atlantic")
                || firstCell.contains("pacific") || firstCell.contains("latin")) {

                int yearColumnIndex = findYearColumn(row, fiscalYear);
                if (yearColumnIndex < 0) continue;

                SegmentInformation.Segment segment = new SegmentInformation.Segment();
                segment.setName(capitalizeWords(firstCell));
                segment.setType("Geographic");

                double value = extractNumber(cells.get(yearColumnIndex).text());
                if (value > 0) {
                    segment.setTotalRevenue(value * 1_000_000);
                    segmentInfo.addSegment(segment);
                    logger.debug("Extracted segment {}: {}", segment.getName(), value);
                }
            }
        }

        // Add the SegmentInformation object to the list if it has segments
        if (!segmentInfo.getSegments().isEmpty()) {
            segments.add(segmentInfo);
        }
    }

    // Helper method to find which column contains data for the target year (for entire table)
    private int findYearColumnForTable(Element table, int targetYear) {
        String yearStr = String.valueOf(targetYear);

        // Check all header rows (including those with th tags or in thead)
        Elements headerRows = table.select("thead tr, tr:has(th)");

        // If no explicit header rows, check first few rows
        if (headerRows.isEmpty()) {
            Elements allRows = table.select("tr");
            headerRows = allRows.size() > 0 ? new Elements(allRows.subList(0, Math.min(3, allRows.size()))) : allRows;
        }

        for (Element headerRow : headerRows) {
            Elements headerCells = headerRow.select("th, td");
            for (int i = 0; i < headerCells.size(); i++) {
                String headerText = headerCells.get(i).text().trim();

                // Look for year in various formats: "2019", "Year Ended 2019", "December 31, 2019", etc.
                if (headerText.contains(yearStr)) {
                    logger.debug("Found year {} in table header column {} with text: '{}'", yearStr, i, headerText);
                    return i;
                }
            }
        }

        // If year not found in headers, default to first data column (index 1)
        logger.debug("Year {} not found in table headers, defaulting to column 1", yearStr);
        return 1;
    }

    // Helper method to find which column contains data for the target year (per row - legacy)
    private int findYearColumn(Element row, int targetYear) {
        Elements cells = row.select("td, th");
        String yearStr = String.valueOf(targetYear);

        // First pass: exact year match
        for (int i = 0; i < cells.size(); i++) {
            String cellText = cells.get(i).text();
            if (cellText.contains(yearStr)) {
                logger.trace("Found year {} in column {} with text: {}", yearStr, i, cellText);
                return i;
            }
        }

        // Second pass: check header row for years
        Element table = row.parent();
        if (table != null) {
            Elements headerRows = table.select("thead tr, tr:has(th)");
            for (Element headerRow : headerRows) {
                Elements headerCells = headerRow.select("th, td");
                for (int i = 0; i < headerCells.size(); i++) {
                    String headerText = headerCells.get(i).text();
                    if (headerText.contains(yearStr)) {
                        logger.trace("Found year {} in header column {}", yearStr, i);
                        return i;
                    }
                }
            }
        }

        // If still not found, default to first data column (index 1, after the label column)
        if (cells.size() > 1) {
            logger.trace("Year {} not found in row, defaulting to column 1", yearStr);
            return 1;
        }

        return -1; // Year not found in this row
    }

    // Helper method to extract numbers from text (handles millions, thousands, decimals, percentages)
    private double extractNumber(String text) {
        if (text == null || text.trim().isEmpty() || text.equals("-") || text.equals("—")) {
            return 0;
        }

        // Remove common non-numeric characters
        text = text.replaceAll("[,%$]", "").trim();

        // Handle parentheses (negative numbers)
        boolean isNegative = text.startsWith("(") && text.endsWith(")");
        if (isNegative) {
            text = text.substring(1, text.length() - 1);
        }

        // Extract the number
        Pattern pattern = Pattern.compile("([0-9]+\\.?[0-9]*)");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            double value = Double.parseDouble(matcher.group(1));
            return isNegative ? -value : value;
        }

        return 0;
    }

    // Helper method to capitalize words
    private String capitalizeWords(String text) {
        String[] words = text.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                result.append(word.substring(1).toLowerCase());
                result.append(" ");
            }
        }

        return result.toString().trim();
    }
}
