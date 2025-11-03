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

            for (Element table : tables) {
                // Check if this table contains revenue data
                String tableText = table.text().toLowerCase();
                if (containsRevenueKeywords(tableText)) {
                    extractRevenueFromTable(table, statement, fiscalYear);
                    if (statement.getPassengerRevenue() > 0) {
                        logger.info("Successfully extracted revenue breakdown");
                        return;  // Found and extracted, done
                    }
                }
            }

            logger.warn("Could not find revenue breakdown table in HTML");
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

            for (Element table : tables) {
                String tableText = table.text().toLowerCase();
                if (containsOperatingStatsKeywords(tableText)) {
                    extractOperatingStatsFromTable(table, metrics, fiscalYear);
                    if (metrics.getAvailableSeatMiles() > 0) {
                        logger.info("Successfully extracted operating statistics");
                        // Calculate derived metrics
                        metrics.calculateLoadFactor();
                        metrics.calculateEmployeeProductivity();
                        return metrics;
                    }
                }
            }

            logger.warn("Could not find operating statistics table in HTML");
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
        return (text.contains("passenger revenue") || text.contains("passenger") && text.contains("revenue"))
            && (text.contains("cargo revenue") || text.contains("cargo") && text.contains("revenue"))
            && (text.contains("operating revenue") || text.contains("total revenue"));
    }

    private boolean containsOperatingStatsKeywords(String text) {
        int keywordCount = 0;
        if (text.contains("available seat miles") || text.contains("asm")) keywordCount++;
        if (text.contains("revenue passenger miles") || text.contains("rpm")) keywordCount++;
        if (text.contains("load factor")) keywordCount++;
        if (text.contains("rasm") || text.contains("revenue per asm")) keywordCount++;
        if (text.contains("casm") || text.contains("cost per asm")) keywordCount++;

        // Need at least 3 key metrics present to be confident it's the right table
        return keywordCount >= 3;
    }

    private boolean containsSegmentKeywords(String text) {
        return (text.contains("domestic") || text.contains("atlantic") || text.contains("pacific") || text.contains("latin"))
            && (text.contains("revenue") || text.contains("operating income"));
    }

    // Revenue extraction logic
    private void extractRevenueFromTable(Element table, DetailedIncomeStatement statement, int fiscalYear) {
        Elements rows = table.select("tr");

        for (Element row : rows) {
            Elements cells = row.select("td, th");
            if (cells.isEmpty()) continue;

            String firstCell = cells.get(0).text().toLowerCase().trim();

            // Find the column index for the target year
            int yearColumnIndex = findYearColumn(row, fiscalYear);
            if (yearColumnIndex < 0) continue;

            // Extract values based on row labels
            if (firstCell.contains("passenger") && firstCell.contains("revenue") && !firstCell.contains("cargo")) {
                double value = extractNumber(cells.get(yearColumnIndex).text());
                if (value > 0) {
                    statement.setPassengerRevenue(value * 1_000_000); // Convert to actual value
                    logger.debug("Extracted passenger revenue: {}", value);
                }
            } else if (firstCell.contains("cargo") && firstCell.contains("revenue")) {
                double value = extractNumber(cells.get(yearColumnIndex).text());
                if (value > 0) {
                    statement.setCargoRevenue(value * 1_000_000);
                    logger.debug("Extracted cargo revenue: {}", value);
                }
            } else if ((firstCell.contains("other") || firstCell.contains("ancillary")) && firstCell.contains("revenue")) {
                double value = extractNumber(cells.get(yearColumnIndex).text());
                if (value > 0) {
                    statement.setOtherOperatingRevenue(value * 1_000_000);
                    logger.debug("Extracted other revenue: {}", value);
                }
            } else if (firstCell.contains("total operating revenue") || firstCell.equals("total revenue")) {
                double value = extractNumber(cells.get(yearColumnIndex).text());
                if (value > 0) {
                    statement.setTotalOperatingRevenue(value * 1_000_000);
                    logger.debug("Extracted total revenue: {}", value);
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
    }

    // Operating statistics extraction logic
    private void extractOperatingStatsFromTable(Element table, AirlineOperatingMetrics metrics, int fiscalYear) {
        Elements rows = table.select("tr");

        for (Element row : rows) {
            Elements cells = row.select("td, th");
            if (cells.isEmpty()) continue;

            String firstCell = cells.get(0).text().toLowerCase().trim();
            int yearColumnIndex = findYearColumn(row, fiscalYear);
            if (yearColumnIndex < 0) continue;

            // Extract various operating metrics
            if (firstCell.contains("available seat miles") || firstCell.equals("asm") || firstCell.equals("asms")) {
                long value = (long) extractNumber(cells.get(yearColumnIndex).text());
                if (value > 0) {
                    metrics.setAvailableSeatMiles(value * 1_000_000); // Usually in millions
                    logger.debug("Extracted ASMs: {}", value);
                }
            } else if (firstCell.contains("revenue passenger miles") || firstCell.equals("rpm") || firstCell.equals("rpms")) {
                long value = (long) extractNumber(cells.get(yearColumnIndex).text());
                if (value > 0) {
                    metrics.setRevenuePassengerMiles(value * 1_000_000);
                    logger.debug("Extracted RPMs: {}", value);
                }
            } else if (firstCell.contains("load factor")) {
                double value = extractNumber(cells.get(yearColumnIndex).text());
                if (value > 0) {
                    metrics.setLoadFactor(value);
                    logger.debug("Extracted load factor: {}%", value);
                }
            } else if (firstCell.contains("prasm") || (firstCell.contains("passenger") && firstCell.contains("per asm"))) {
                double value = extractNumber(cells.get(yearColumnIndex).text());
                if (value > 0) {
                    metrics.setPassengerRevenuePerASM(value);
                    logger.debug("Extracted PRASM: {} cents", value);
                }
            } else if (firstCell.contains("rasm") && !firstCell.contains("prasm")) {
                double value = extractNumber(cells.get(yearColumnIndex).text());
                if (value > 0) {
                    metrics.setTotalRevenuePerASM(value);
                    logger.debug("Extracted RASM: {} cents", value);
                }
            } else if (firstCell.contains("casm") && !firstCell.contains("ex")) {
                double value = extractNumber(cells.get(yearColumnIndex).text());
                if (value > 0) {
                    metrics.setOperatingCostPerASM(value);
                    logger.debug("Extracted CASM: {} cents", value);
                }
            } else if (firstCell.contains("casm") && (firstCell.contains("ex") || firstCell.contains("excluding"))) {
                double value = extractNumber(cells.get(yearColumnIndex).text());
                if (value > 0) {
                    metrics.setCasmExcludingFuel(value);
                    logger.debug("Extracted CASM-ex: {} cents", value);
                }
            } else if (firstCell.contains("yield")) {
                double value = extractNumber(cells.get(yearColumnIndex).text());
                if (value > 0) {
                    metrics.setYield(value);
                    logger.debug("Extracted yield: {} cents", value);
                }
            } else if (firstCell.contains("passengers") && !firstCell.contains("revenue")) {
                long value = (long) extractNumber(cells.get(yearColumnIndex).text());
                if (value > 0) {
                    metrics.setPassengersCarried(value * 1_000_000); // Usually in millions
                    logger.debug("Extracted passengers: {}", value);
                }
            } else if (firstCell.contains("departures")) {
                int value = (int) extractNumber(cells.get(yearColumnIndex).text());
                if (value > 0) {
                    metrics.setDepartures(value);
                    logger.debug("Extracted departures: {}", value);
                }
            } else if (firstCell.contains("aircraft") && (firstCell.contains("end") || firstCell.contains("period"))) {
                int value = (int) extractNumber(cells.get(yearColumnIndex).text());
                if (value > 0) {
                    metrics.setAircraftAtPeriodEnd(value);
                    logger.debug("Extracted aircraft count: {}", value);
                }
            }
        }
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

    // Helper method to find which column contains data for the target year
    private int findYearColumn(Element row, int targetYear) {
        Elements cells = row.select("td, th");
        String yearStr = String.valueOf(targetYear);

        for (int i = 0; i < cells.size(); i++) {
            String cellText = cells.get(i).text();
            if (cellText.contains(yearStr)) {
                return i;
            }
        }

        // If not found, try the pattern where year is last 4 digits
        for (int i = 0; i < cells.size(); i++) {
            String cellText = cells.get(i).text();
            if (cellText.matches(".*" + yearStr + ".*")) {
                return i;
            }
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
