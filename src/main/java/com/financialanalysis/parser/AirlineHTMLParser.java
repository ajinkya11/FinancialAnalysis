package com.financialanalysis.parser;

import com.financialanalysis.model.AirlineOperatingMetrics;
import com.financialanalysis.model.DetailedBalanceSheet;
import com.financialanalysis.model.DetailedCashFlow;
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

                // Skip quarterly/interim tables - we want annual data only
                if (isQuarterlyTable(tableText)) {
                    logger.debug("Skipping table {} - appears to be quarterly/interim data", tableIndex);
                    continue;
                }

                if (containsRevenueKeywords(tableText)) {
                    // Prefer tables that explicitly mention annual periods
                    boolean isAnnualTable = tableText.contains("year ended") ||
                                           tableText.contains("twelve months") ||
                                           tableText.contains("years ended");

                    extractRevenueFromTable(table, statement, fiscalYear);
                    if (statement.getPassengerRevenue() > 0) {
                        // Validate: passenger revenue should be significant portion of total
                        if (validateRevenueData(statement)) {
                            logger.info("Successfully extracted revenue breakdown from table {}", tableIndex);
                            return;  // Found and extracted, done
                        } else {
                            logger.warn("Table {} extracted revenue but validation failed (likely quarterly data), continuing search...", tableIndex);
                            // Reset and continue looking
                            statement.setPassengerRevenue(0);
                            statement.setCargoRevenue(0);
                            statement.setOtherOperatingRevenue(0);
                        }
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

                // Skip quarterly/interim tables
                if (isQuarterlyTable(tableText)) {
                    logger.debug("Skipping table {} - appears to be quarterly/interim data", tableIndex);
                    continue;
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
        // Check for revenue context
        boolean hasRevenueContext = text.contains("operating revenue") ||
                                     text.contains("total revenue") ||
                                     text.contains("operating revenues") ||
                                     text.contains("total revenues") ||
                                     text.contains("revenue by source") ||
                                     text.contains("revenue by type") ||
                                     text.contains("operating revenues:") ||
                                     text.contains("passenger revenues") ||
                                     (text.contains("revenue") && (text.contains("operating") || text.contains("total")));

        int categoryCount = 0;
        // Count revenue categories - airlines use various terms
        if (text.contains("passenger") || text.contains("transportation")) categoryCount++;
        if (text.contains("cargo") || text.contains("freight") || text.contains("mail")) categoryCount++;
        if (text.contains("other") || text.contains("ancillary") || text.contains("loyalty") ||
            text.contains("trueblue") || text.contains("mileageplus") || text.contains("mileage plus")) categoryCount++;

        // Check for explicit revenue table indicators
        boolean isRevenueTable = text.contains("revenue composition") ||
                                  text.contains("revenue breakdown") ||
                                  text.contains("revenues by category") ||
                                  text.contains("revenues by type") ||
                                  text.contains("operating revenues by") ||
                                  (text.contains("consolidated statements of operations") && categoryCount >= 2);

        // Match if we have revenue context with categories, OR it's an explicit revenue table
        boolean matches = (hasRevenueContext && categoryCount >= 1) || (isRevenueTable && categoryCount >= 1);

        if (matches) {
            logger.trace("Revenue keywords matched - context: {}, categories: {}, explicit: {}",
                hasRevenueContext, categoryCount, isRevenueTable);
        }

        return matches;
    }

    private boolean containsOperatingStatsKeywords(String text) {
        int keywordCount = 0;

        // Check for ASM variations - more flexible
        boolean hasASM = text.contains("available seat miles") || text.contains("available seat-miles") ||
                         text.contains("available seat mile") ||
                         text.contains("capacity (asm") || text.contains("capacity(asm") ||
                         text.contains("capacity, asm");
        // Also check for standalone ASM (avoiding RASM, PRASM, CASM)
        if (!hasASM && text.contains(" asm") && !text.contains("rasm") && !text.contains("prasm") && !text.contains("casm")) {
            hasASM = true;
        }
        if (hasASM) keywordCount++;

        // Check for RPM variations - more flexible
        boolean hasRPM = text.contains("revenue passenger miles") || text.contains("revenue passenger-miles") ||
                         text.contains("revenue passenger mile") ||
                         text.contains("traffic (rpm") || text.contains("traffic(rpm") ||
                         text.contains("traffic, rpm");
        // Also check for standalone RPM (avoiding PRPM)
        if (!hasRPM && text.contains(" rpm") && !text.contains("prpm")) {
            hasRPM = true;
        }
        if (hasRPM) keywordCount++;

        // Check for load factor
        if (text.contains("load factor") || text.contains("passenger load") ||
            text.contains("load %") || text.contains("load factor %")) keywordCount++;

        // Check for unit revenue metrics
        if (text.contains("rasm") || text.contains("prasm") || text.contains("revenue per asm") ||
            text.contains("unit revenue") || text.contains("passenger yield") ||
            text.contains("yield per") || text.contains("total revenue per asm")) keywordCount++;

        // Check for unit cost metrics
        if (text.contains("casm") || text.contains("cost per asm") || text.contains("unit cost") ||
            text.contains("operating expense per asm") || text.contains("operating cost per asm")) keywordCount++;

        // Check for passengers or departures (common in operating stats tables)
        if (text.contains("passengers enplaned") || text.contains("passengers carried") ||
            text.contains("scheduled service passengers") ||
            (text.contains("passengers") && !text.contains("revenue passenger miles"))) keywordCount++;
        if (text.contains("departures") || text.contains("flights operated") ||
            text.contains("flight operations") || text.contains("scheduled departures")) keywordCount++;

        // Check for aircraft count
        if (text.contains("aircraft in service") || text.contains("aircraft at period end") ||
            text.contains("average aircraft") || text.contains("aircraft operated")) keywordCount++;

        // Check for block hours or other flight metrics
        if (text.contains("block hours") || text.contains("flight hours") ||
            text.contains("aircraft utilization")) keywordCount++;

        // Check for explicit operating statistics labels
        boolean isOperatingStatsTable = text.contains("operating statistics") ||
                                         text.contains("operational statistics") ||
                                         text.contains("operating data") ||
                                         text.contains("statistical data") ||
                                         text.contains("operating performance") ||
                                         text.contains("key operating statistics");

        // More lenient: Match if we have at least 2 keywords OR explicit operating stats label with at least 1 keyword
        boolean matches = keywordCount >= 2 || (isOperatingStatsTable && keywordCount >= 1);

        if (matches) {
            logger.trace("Operating stats keywords matched - keyword count: {}, explicit label: {}",
                keywordCount, isOperatingStatsTable);
        }

        return matches;
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
        logger.info("Revenue table: Using column {} for year {}", yearColumnIndex, fiscalYear);

        for (Element row : rows) {
            Elements cells = row.select("td, th");
            if (cells.isEmpty()) continue;

            String firstCell = cells.get(0).text().toLowerCase().trim();

            // Skip header rows
            if (firstCell.isEmpty() || cells.size() <= yearColumnIndex) continue;

            String cellValue = cells.get(yearColumnIndex).text();

            // Skip rows that are clearly headers or subtotals (but allow "total operating revenue")
            boolean isSubtotal = (firstCell.contains("subtotal") || firstCell.contains("sub-total")) &&
                                 !firstCell.contains("total operating");

            if (isSubtotal) {
                logger.trace("Skipping subtotal row: {}", firstCell);
                continue;
            }

            // Log every row we're examining
            logger.trace("Examining row: '{}' with value: '{}'", firstCell, cellValue);

            // Extract values based on row labels - more flexible matching
            // Passenger revenue (MOST SPECIFIC FIRST) - includes "transportation" for airlines like JetBlue
            if (((firstCell.contains("passenger") || firstCell.contains("transportation")) &&
                 (firstCell.contains("revenue") || firstCell.contains("operating"))) &&
                !firstCell.contains("cargo") && !firstCell.contains("freight") &&
                !firstCell.contains("per") && !firstCell.contains("yield") &&
                !firstCell.contains("total")) {
                double value = extractNumber(cellValue);
                if (value > 0) {
                    statement.setPassengerRevenue(value * 1_000_000); // Convert to actual value
                    logger.debug("Extracted passenger revenue: {} (raw: '{}') from row: {}", value, cellValue, firstCell);
                }
            }
            // Cargo/Freight revenue
            else if ((firstCell.contains("cargo") || firstCell.contains("freight")) &&
                     (firstCell.contains("revenue") || firstCell.contains("operating")) &&
                     !firstCell.contains("per") && !firstCell.contains("total")) {
                double value = extractNumber(cellValue);
                if (value > 0) {
                    statement.setCargoRevenue(value * 1_000_000);
                    logger.debug("Extracted cargo revenue: {} (raw: '{}') from row: {}", value, cellValue, firstCell);
                }
            }
            // Total operating revenue (CHECK THIS BEFORE "OTHER" to avoid confusion)
            else if ((firstCell.contains("total operating revenue") ||
                      firstCell.contains("total operating revenues") ||
                      (firstCell.startsWith("total") && firstCell.contains("revenue") && !firstCell.contains("passenger") && !firstCell.contains("cargo"))) &&
                     !firstCell.contains("per") && !firstCell.contains("excluding")) {
                double value = extractNumber(cellValue);
                if (value > 0) {
                    // Only set if this looks like a reasonable total
                    statement.setTotalOperatingRevenue(value * 1_000_000);
                    logger.debug("Extracted total revenue: {} (raw: '{}') from row: {}", value, cellValue, firstCell);
                }
            }
            // Other revenue (includes ancillary, loyalty, etc.) - AFTER total check
            else if ((firstCell.contains("other") || firstCell.contains("ancillary") ||
                      firstCell.contains("loyalty") || firstCell.contains("mileageplus")) &&
                     (firstCell.contains("revenue") || firstCell.contains("operating")) &&
                     !firstCell.contains("per") && !firstCell.contains("total")) {
                double value = extractNumber(cellValue);
                if (value > 0) {
                    statement.setOtherOperatingRevenue(value * 1_000_000);
                    logger.debug("Extracted other revenue: {} (raw: '{}') from row: {}", value, cellValue, firstCell);
                }
            }
        }

        // Auto-calculate missing revenue components
        // Priority 1: If we have passenger and cargo but not other, calculate other
        if (statement.getPassengerRevenue() > 0 && statement.getTotalOperatingRevenue() > 0
            && statement.getOtherOperatingRevenue() == 0) {
            double other = statement.getTotalOperatingRevenue()
                         - statement.getPassengerRevenue()
                         - statement.getCargoRevenue();
            if (other > 0) {
                statement.setOtherOperatingRevenue(other);
                logger.debug("Calculated other revenue from total: {}", other / 1_000_000);
            }
        }

        // Priority 2: If we have components but not total, calculate total
        // This is CRITICAL - total MUST be sum of components if not extracted
        if (statement.getPassengerRevenue() > 0 && statement.getTotalOperatingRevenue() == 0) {
            double total = statement.getPassengerRevenue()
                         + statement.getCargoRevenue()
                         + statement.getOtherOperatingRevenue();
            if (total > 0) {
                statement.setTotalOperatingRevenue(total);
                logger.info("CALCULATED total revenue from components: {} (Passenger: {} + Cargo: {} + Other: {})",
                    total / 1_000_000,
                    statement.getPassengerRevenue() / 1_000_000,
                    statement.getCargoRevenue() / 1_000_000,
                    statement.getOtherOperatingRevenue() / 1_000_000);
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
        logger.info("Operating stats table: Using column {} for year {}", yearColumnIndex, fiscalYear);

        for (Element row : rows) {
            Elements cells = row.select("td, th");
            if (cells.isEmpty()) continue;

            String firstCell = cells.get(0).text().toLowerCase().trim();

            // Skip header rows or rows with insufficient columns
            if (firstCell.isEmpty() || cells.size() <= yearColumnIndex) continue;

            String cellValue = cells.get(yearColumnIndex).text();

            // Log every row we're examining for operating stats
            logger.trace("Examining operating stats row: '{}' with value: '{}'", firstCell, cellValue);

            // Extract various operating metrics with more flexible matching
            // Available Seat Miles (ASM)
            boolean isASM = firstCell.contains("available seat") ||
                           firstCell.contains("available seat-miles") ||
                           firstCell.contains("capacity (asm") ||
                           firstCell.contains("capacity(asm") ||
                           (firstCell.matches("^asms?$")) ||
                           (firstCell.contains("asm") && !firstCell.contains("rasm") &&
                            !firstCell.contains("prasm") && !firstCell.contains("casm") &&
                            !firstCell.contains("per") && !firstCell.contains("yield"));

            if (isASM) {
                long value = (long) extractNumber(cellValue);
                if (value > 0) {
                    metrics.setAvailableSeatMiles(value * 1_000_000); // Usually in millions
                    logger.debug("Extracted ASMs: {} (raw: '{}') from row: {}", value, cellValue, firstCell);
                }
            }
            // Revenue Passenger Miles (RPM)
            else if (firstCell.contains("revenue passenger") || firstCell.contains("revenue passenger-miles") ||
                     firstCell.contains("traffic (rpm") || firstCell.contains("traffic(rpm") ||
                     (firstCell.matches("^rpms?$")) ||
                     (firstCell.contains("rpm") && !firstCell.contains("per") && !firstCell.contains("yield"))) {
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

    /**
     * Check if a table contains quarterly or interim period data
     */
    private boolean isQuarterlyTable(String tableText) {
        return tableText.contains("three months") ||
               tableText.contains("quarter ended") ||
               tableText.contains("quarterly") ||
               tableText.contains("q1 ") || tableText.contains("q2 ") ||
               tableText.contains("q3 ") || tableText.contains("q4 ") ||
               tableText.contains("first quarter") ||
               tableText.contains("second quarter") ||
               tableText.contains("third quarter") ||
               tableText.contains("fourth quarter") ||
               (tableText.contains("period ended") && !tableText.contains("year"));
    }

    /**
     * Validate extracted revenue data to ensure it's reasonable
     * Passenger revenue should typically be 80%+ of total revenue for passenger airlines
     */
    private boolean validateRevenueData(DetailedIncomeStatement statement) {
        if (statement.getPassengerRevenue() <= 0) {
            logger.debug("Validation failed: No passenger revenue extracted");
            return false;
        }

        // If we have total revenue from XBRL, validate against it
        if (statement.getTotalOperatingRevenue() > 0) {
            double passengerPct = statement.getPassengerRevenue() / statement.getTotalOperatingRevenue();

            logger.debug("Validation check: Passenger revenue = {}, Total revenue = {}, Passenger % = {}",
                statement.getPassengerRevenue() / 1_000_000,
                statement.getTotalOperatingRevenue() / 1_000_000,
                passengerPct * 100);

            // Passenger revenue should be at least 70% for passenger airlines
            // (allowing some flexibility for cargo-heavy carriers)
            if (passengerPct < 0.70) {
                logger.warn("Validation failed: Passenger revenue ({}) is only {}% of total revenue ({}), expected >= 70%",
                    statement.getPassengerRevenue() / 1_000_000,
                    passengerPct * 100,
                    statement.getTotalOperatingRevenue() / 1_000_000);
                return false;
            }
        }

        // Additional sanity check: passenger revenue should be in billions for major airlines
        // (at least $100M annually)
        if (statement.getPassengerRevenue() < 100_000_000) {
            logger.warn("Validation failed: Passenger revenue (${}) seems too low for annual data",
                statement.getPassengerRevenue() / 1_000_000);
            return false;
        }

        logger.debug("Validation passed for revenue data");
        return true;
    }

    /**
     * Calculate RASM and CASM if not directly available in the HTML
     * This is called as a fallback when metrics are not explicitly listed
     */
    public void calculateUnitMetrics(AirlineOperatingMetrics metrics, DetailedIncomeStatement incomeStatement,
                                     DetailedBalanceSheet balanceSheet, DetailedCashFlow cashFlow) {
        if (metrics.getAvailableSeatMiles() <= 0) {
            logger.debug("Cannot calculate unit metrics: ASMs not available");
            return;
        }

        double asms = metrics.getAvailableSeatMiles();

        // Calculate RASM if not already set
        if (metrics.getTotalRevenuePerASM() == 0 && incomeStatement != null && incomeStatement.getTotalOperatingRevenue() > 0) {
            // RASM = (Total Revenue / ASMs) * 100 to get cents
            double revenueInDollars = incomeStatement.getTotalOperatingRevenue();
            double rasm = (revenueInDollars / asms) * 100.0;
            metrics.setTotalRevenuePerASM(rasm);
            logger.info("Calculated RASM: {} cents (Revenue=${} / ASMs={} * 100)",
                String.format("%.2f", rasm),
                String.format("%.0fM", revenueInDollars / 1_000_000),
                String.format("%.0fM", asms / 1_000_000));
        }

        // Calculate PRASM if not already set
        if (metrics.getPassengerRevenuePerASM() == 0 && incomeStatement != null && incomeStatement.getPassengerRevenue() > 0) {
            double passengerRevenue = incomeStatement.getPassengerRevenue();
            double prasm = (passengerRevenue / asms) * 100.0;
            metrics.setPassengerRevenuePerASM(prasm);
            logger.info("Calculated PRASM: {} cents (PaxRev=${} / ASMs={} * 100)",
                String.format("%.2f", prasm),
                String.format("%.0fM", passengerRevenue / 1_000_000),
                String.format("%.0fM", asms / 1_000_000));
        }

        // Calculate CASM if not already set
        if (metrics.getOperatingCostPerASM() == 0 && incomeStatement != null && incomeStatement.getTotalOperatingExpenses() > 0) {
            // CASM = (Total Operating Expenses / ASMs) * 100 to get cents
            double expenses = incomeStatement.getTotalOperatingExpenses();
            double casm = (expenses / asms) * 100.0;
            metrics.setOperatingCostPerASM(casm);
            logger.info("Calculated CASM: {} cents (OpEx=${} / ASMs={} * 100)",
                String.format("%.2f", casm),
                String.format("%.0fM", expenses / 1_000_000),
                String.format("%.0fM", asms / 1_000_000));
        }

        // Calculate CASM-ex (excluding fuel) if not already set
        if (metrics.getCasmExcludingFuel() == 0 && incomeStatement != null &&
            incomeStatement.getTotalOperatingExpenses() > 0 && incomeStatement.getAircraftFuel() > 0) {
            double casmEx = ((incomeStatement.getTotalOperatingExpenses() - incomeStatement.getAircraftFuel()) / asms) * 100.0;
            metrics.setCasmExcludingFuel(casmEx);
            logger.info("Calculated CASM-ex: {} cents", String.format("%.2f", casmEx));
        }
    }
}
