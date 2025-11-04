package com.financialanalysis.reporting;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.financialanalysis.models.ComparisonResult;
import com.financialanalysis.models.MetricComparison;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for exporting analysis results to CSV and JSON
 */
@Slf4j
@Service
public class ExportService {

    private final ObjectMapper jsonMapper;
    private final CsvMapper csvMapper;

    public ExportService() {
        this.jsonMapper = new ObjectMapper();
        this.jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);

        this.csvMapper = new CsvMapper();
    }

    /**
     * Export comparison result to JSON
     */
    public void exportToJSON(ComparisonResult result, String filePath) {
        log.info("Exporting results to JSON: {}", filePath);

        try {
            jsonMapper.writeValue(new File(filePath), result);
            log.info("Successfully exported to JSON: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to export to JSON", e);
            throw new RuntimeException("Failed to export to JSON: " + filePath, e);
        }
    }

    /**
     * Export comparison result to CSV
     */
    public void exportToCSV(ComparisonResult result, String filePath) {
        log.info("Exporting results to CSV: {}", filePath);

        try (FileWriter writer = new FileWriter(filePath)) {
            // Write header
            writer.write("Metric,Company1 (" + result.getCompany1().getTicker() + ")," +
                    "Company2 (" + result.getCompany2().getTicker() + "),Winner,Difference %\n");

            // Write metrics
            for (Map.Entry<String, MetricComparison> entry : result.getMetricComparisons().entrySet()) {
                MetricComparison comp = entry.getValue();
                writer.write(String.format("%s,%s,%s,%s,%.2f\n",
                        escapeCsv(comp.getMetricName()),
                        escapeCsv(formatValue(comp.getCompany1Value())),
                        escapeCsv(formatValue(comp.getCompany2Value())),
                        escapeCsv(comp.getWinner()),
                        comp.getDifferencePercentage() != null ? comp.getDifferencePercentage() : 0.0
                ));
            }

            // Write summary section
            writer.write("\n");
            writer.write("EXECUTIVE SUMMARY\n");
            writer.write(escapeCsv(result.getExecutiveSummary()) + "\n");

            // Write strengths
            writer.write("\n");
            writer.write("COMPANY 1 STRENGTHS\n");
            for (String strength : result.getCompany1Strengths()) {
                writer.write(escapeCsv(strength) + "\n");
            }

            // Write weaknesses
            writer.write("\n");
            writer.write("COMPANY 1 WEAKNESSES\n");
            for (String weakness : result.getCompany1Weaknesses()) {
                writer.write(escapeCsv(weakness) + "\n");
            }

            // Write strengths
            writer.write("\n");
            writer.write("COMPANY 2 STRENGTHS\n");
            for (String strength : result.getCompany2Strengths()) {
                writer.write(escapeCsv(strength) + "\n");
            }

            // Write weaknesses
            writer.write("\n");
            writer.write("COMPANY 2 WEAKNESSES\n");
            for (String weakness : result.getCompany2Weaknesses()) {
                writer.write(escapeCsv(weakness) + "\n");
            }

            // Write recommendation
            writer.write("\n");
            writer.write("RECOMMENDATION\n");
            writer.write(escapeCsv(result.getInvestmentRecommendation()) + "\n");

            log.info("Successfully exported to CSV: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to export to CSV", e);
            throw new RuntimeException("Failed to export to CSV: " + filePath, e);
        }
    }

    /**
     * Format value for CSV export
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "N/A";
        }
        if (value instanceof java.math.BigDecimal) {
            return ((java.math.BigDecimal) value).toPlainString();
        }
        return value.toString();
    }

    /**
     * Escape CSV special characters
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
