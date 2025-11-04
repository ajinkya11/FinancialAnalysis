package com.financialanalysis.cli;

import com.financialanalysis.data.FinancialDataService;
import com.financialanalysis.models.ComparisonResult;
import com.financialanalysis.reporting.ConsoleReportGenerator;
import com.financialanalysis.reporting.ExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Main CLI command for the Financial Analyzer application
 */
@Slf4j
@Component
@Command(
        name = "financial-analyzer",
        description = "Comprehensive CLI tool for comparative financial analysis of airline companies",
        version = "1.0.0",
        mixinStandardHelpOptions = true
)
public class FinancialAnalyzerCommand implements Runnable {

    private final FinancialDataService dataService;
    private final ConsoleReportGenerator reportGenerator;
    private final ExportService exportService;

    public FinancialAnalyzerCommand(FinancialDataService dataService,
                                   ConsoleReportGenerator reportGenerator,
                                   ExportService exportService) {
        this.dataService = dataService;
        this.reportGenerator = reportGenerator;
        this.exportService = exportService;
    }

    @Override
    public void run() {
        // Show help if no subcommand specified
        CommandLine.usage(this, System.out);
    }

    /**
     * Compare command - performs comparative analysis between two companies
     */
    @Command(
            name = "compare",
            description = "Compare financial metrics of two airline companies",
            mixinStandardHelpOptions = true
    )
    public static class CompareCommand implements Runnable {

        @Parameters(index = "0", description = "First company ticker symbol (e.g., UAL)")
        private String ticker1;

        @Parameters(index = "1", description = "Second company ticker symbol (e.g., JBLU)")
        private String ticker2;

        @Option(
                names = {"-y", "--years"},
                description = "Number of years to analyze (default: ${DEFAULT-VALUE})",
                defaultValue = "1"
        )
        private int years;

        @Option(
                names = {"-o", "--output"},
                description = "Output file path for results (CSV or JSON based on extension)"
        )
        private String outputFile;

        @Option(
                names = {"--format"},
                description = "Output format: console, csv, json, or all (default: ${DEFAULT-VALUE})",
                defaultValue = "console"
        )
        private String format;

        @Option(
                names = {"--metrics"},
                description = "Metrics to display: all, financial, or operational (default: ${DEFAULT-VALUE})",
                defaultValue = "all"
        )
        private String metricsFilter;

        @Option(
                names = {"--clear-cache"},
                description = "Clear cached data before analysis"
        )
        private boolean clearCache;

        @CommandLine.Spec
        private CommandLine.Model.CommandSpec spec;

        @Override
        public void run() {
            try {
                // Get service beans from parent command
                FinancialAnalyzerCommand parent = spec.parent().userObject();
                FinancialDataService dataService = parent.dataService;
                ConsoleReportGenerator reportGenerator = parent.reportGenerator;
                ExportService exportService = parent.exportService;

                // Clear cache if requested
                if (clearCache) {
                    System.out.println("Clearing cache...");
                    dataService.clearCache();
                }

                // Validate tickers
                ticker1 = ticker1.toUpperCase();
                ticker2 = ticker2.toUpperCase();

                System.out.println("\n" + "=".repeat(80));
                System.out.println("AIRLINE FINANCIAL ANALYZER");
                System.out.println("=".repeat(80));
                System.out.println("\nComparing: " + ticker1 + " vs " + ticker2);
                System.out.println("Analysis period: " + years + " year(s)");
                System.out.println("\nFetching data from SEC EDGAR...");

                // Perform comparative analysis
                ComparisonResult result = dataService.compareCompanies(ticker1, ticker2, years);

                // Generate output based on format
                switch (format.toLowerCase()) {
                    case "console":
                        reportGenerator.generateReport(result);
                        break;

                    case "csv":
                        if (outputFile == null) {
                            outputFile = String.format("%s_vs_%s_comparison.csv", ticker1, ticker2);
                        }
                        exportService.exportToCSV(result, outputFile);
                        System.out.println("\nResults exported to: " + outputFile);
                        break;

                    case "json":
                        if (outputFile == null) {
                            outputFile = String.format("%s_vs_%s_comparison.json", ticker1, ticker2);
                        }
                        exportService.exportToJSON(result, outputFile);
                        System.out.println("\nResults exported to: " + outputFile);
                        break;

                    case "all":
                        // Display on console
                        reportGenerator.generateReport(result);

                        // Export to CSV
                        String csvFile = String.format("%s_vs_%s_comparison.csv", ticker1, ticker2);
                        exportService.exportToCSV(result, csvFile);
                        System.out.println("\nCSV exported to: " + csvFile);

                        // Export to JSON
                        String jsonFile = String.format("%s_vs_%s_comparison.json", ticker1, ticker2);
                        exportService.exportToJSON(result, jsonFile);
                        System.out.println("JSON exported to: " + jsonFile);
                        break;

                    default:
                        System.err.println("Unknown format: " + format);
                        System.err.println("Supported formats: console, csv, json, all");
                        break;
                }

                // Show cache stats
                System.out.println("\nCache statistics: " + dataService.getCacheStats());

            } catch (Exception e) {
                System.err.println("\nError: " + e.getMessage());
                log.error("Error during comparison", e);
                System.exit(1);
            }
        }
    }

    /**
     * Info command - shows information about a single company
     */
    @Command(
            name = "info",
            description = "Display financial information for a single company",
            mixinStandardHelpOptions = true
    )
    public static class InfoCommand implements Runnable {

        @Parameters(index = "0", description = "Company ticker symbol (e.g., UAL)")
        private String ticker;

        @Option(
                names = {"-y", "--years"},
                description = "Number of years to analyze (default: ${DEFAULT-VALUE})",
                defaultValue = "1"
        )
        private int years;

        @CommandLine.Spec
        private CommandLine.Model.CommandSpec spec;

        @Override
        public void run() {
            try {
                FinancialAnalyzerCommand parent = spec.parent().userObject();
                FinancialDataService dataService = parent.dataService;

                ticker = ticker.toUpperCase();

                System.out.println("\nFetching financial data for: " + ticker);
                System.out.println("Analysis period: " + years + " year(s)\n");

                var companyData = dataService.getCompanyData(ticker, years);

                // Display company information
                System.out.println("Company: " + companyData.getCompanyName());
                System.out.println("Ticker: " + companyData.getTicker());
                System.out.println("CIK: " + companyData.getCik());
                System.out.println("Fiscal Year End: " + companyData.getFiscalYearEnd());
                System.out.println("\nLatest Financial Metrics:");

                var metrics = companyData.getLatestFinancialMetrics();
                if (metrics != null) {
                    System.out.println(String.format("  Revenue: $%.2fM",
                            companyData.getLatestIncomeStatement().getTotalRevenue()
                                    .divide(java.math.BigDecimal.valueOf(1000000), 2,
                                            java.math.RoundingMode.HALF_UP)));
                    System.out.println(String.format("  Operating Margin: %.2f%%", metrics.getOperatingMargin()));
                    System.out.println(String.format("  Net Margin: %.2f%%", metrics.getNetMargin()));
                    System.out.println(String.format("  ROA: %.2f%%", metrics.getReturnOnAssets()));
                    System.out.println(String.format("  ROE: %.2f%%", metrics.getReturnOnEquity()));
                }

                var airlineMetrics = companyData.getLatestOperatingMetrics();
                if (airlineMetrics != null) {
                    System.out.println("\nAirline Operating Metrics:");
                    System.out.println(String.format("  Load Factor: %.2f%%", airlineMetrics.getLoadFactor()));
                    System.out.println(String.format("  RASM: %.4f cents", airlineMetrics.getRasm()));
                    System.out.println(String.format("  CASM: %.4f cents", airlineMetrics.getCasm()));
                    System.out.println(String.format("  Yield: %.4f cents", airlineMetrics.getYield()));
                }

                System.out.println("\nData successfully retrieved!");

            } catch (Exception e) {
                System.err.println("\nError: " + e.getMessage());
                log.error("Error fetching company info", e);
                System.exit(1);
            }
        }
    }

    /**
     * Cache command - manage cache
     */
    @Command(
            name = "cache",
            description = "Manage cached data",
            mixinStandardHelpOptions = true
    )
    public static class CacheCommand implements Runnable {

        @Option(
                names = {"--clear"},
                description = "Clear all cached data"
        )
        private boolean clear;

        @Option(
                names = {"--stats"},
                description = "Show cache statistics"
        )
        private boolean stats;

        @CommandLine.Spec
        private CommandLine.Model.CommandSpec spec;

        @Override
        public void run() {
            FinancialAnalyzerCommand parent = spec.parent().userObject();
            FinancialDataService dataService = parent.dataService;

            if (clear) {
                System.out.println("Clearing all caches...");
                dataService.clearCache();
                System.out.println("Cache cleared successfully!");
            }

            if (stats || (!clear)) {
                System.out.println("\nCache Statistics:");
                System.out.println(dataService.getCacheStats());
            }
        }
    }
}
