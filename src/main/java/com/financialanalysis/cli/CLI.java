package com.financialanalysis.cli;

import com.financialanalysis.analyzer.MetricsCalculator;
import com.financialanalysis.glossary.Glossary;
import com.financialanalysis.model.AirlineOperatingMetrics;
import com.financialanalysis.model.Company;
import com.financialanalysis.model.FinancialMetrics;
import com.financialanalysis.model.FinancialStatement;
import com.financialanalysis.model.DetailedIncomeStatement;
import com.financialanalysis.model.DetailedBalanceSheet;
import com.financialanalysis.model.DetailedCashFlow;
import com.financialanalysis.parser.AirlineHTMLParser;
import com.financialanalysis.parser.FinancialDataExtractor;
import com.financialanalysis.parser.PDFParser;
import com.financialanalysis.parser.XBRLParser;
import com.financialanalysis.storage.DataStore;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "finanalysis", mixinStandardHelpOptions = true, version = "1.0",
        description = "Financial Analysis CLI - Analyze company 10-K filings")
public class CLI implements Callable<Integer> {

    @Command(name = "add", description = "Add a company by parsing 10-K files (PDF or XBRL)")
    public int addCompany(
            @Parameters(index = "0", description = "Company name") String companyName,
            @Parameters(index = "1", description = "Company ticker symbol") String ticker,
            @Option(names = {"-d", "--directory"}, description = "Directory containing 10-K files",
                    defaultValue = "data/10k-pdfs") String directory,
            @Option(names = {"-i", "--industry"}, description = "Industry/sector") String industry,
            @Option(names = {"-f", "--format"}, description = "File format: pdf or xbrl (default: pdf)",
                    defaultValue = "pdf") String format,
            @Option(names = {"-y", "--year"}, description = "Fiscal year (if single file)") Integer year,
            @Option(names = {"--html"}, description = "Parse HTML files for supplementary data (airline metrics, revenue breakdown)") boolean parseHTML
    ) {
        System.out.println("Adding company: " + companyName + " (" + ticker + ")");
        System.out.println("Format: " + format.toUpperCase());
        if (parseHTML) {
            System.out.println("HTML Parsing: ENABLED (will extract operating metrics and revenue breakdown)");
        }

        try {
            DataStore dataStore = new DataStore();
            Company company = new Company(companyName, ticker, industry);

            File dir = new File(directory);
            if (!dir.exists() || !dir.isDirectory()) {
                System.err.println("Error: Directory not found: " + directory);
                return 1;
            }

            // Process based on format
            if ("xbrl".equalsIgnoreCase(format)) {
                return processXBRLFiles(company, dir, ticker, dataStore, parseHTML);
            } else {
                return processPDFFiles(company, dir, ticker, year, dataStore);
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * Process PDF files for a company
     */
    private int processPDFFiles(Company company, File dir, String ticker, Integer year, DataStore dataStore) throws Exception {
        // Filter PDF files by ticker symbol in filename
        File[] pdfFiles = dir.listFiles((d, name) ->
            name.toLowerCase().endsWith(".pdf") &&
            name.toLowerCase().contains(ticker.toLowerCase())
        );

        if (pdfFiles == null || pdfFiles.length == 0) {
            System.err.println("Error: No PDF files found for ticker '" + ticker + "' in directory: " + dir.getPath());
            System.err.println("Please ensure PDF filenames contain the ticker symbol (e.g., " + ticker + "_10K_2023.pdf)");
            return 1;
        }

        System.out.println("Found " + pdfFiles.length + " PDF file(s) to process");

        PDFParser pdfParser = new PDFParser();
        FinancialDataExtractor extractor = new FinancialDataExtractor();

        for (File pdfFile : pdfFiles) {
            System.out.println("Processing: " + pdfFile.getName());

            try {
                // Extract text from PDF
                String text = pdfParser.extractText(pdfFile);

                // Extract fiscal year from filename or use provided year
                int fiscalYear = (year != null) ? year : extractYearFromFilename(pdfFile.getName());

                // Extract financial data
                FinancialStatement statement = extractor.extract(text, fiscalYear);
                company.addFinancialStatement(statement);

                System.out.println("  ✓ Extracted data for fiscal year " + statement.getFiscalYear());
            } catch (Exception e) {
                System.err.println("  ✗ Error processing " + pdfFile.getName() + ": " + e.getMessage());
            }
        }

        if (company.getFinancialStatements().isEmpty()) {
            System.err.println("Error: No financial data extracted from PDFs");
            return 1;
        }

        // Save company data
        dataStore.saveCompany(company);
        System.out.println("\n✓ Successfully added company: " + company.getName() + " with " +
                company.getFinancialStatements().size() + " year(s) of data");

        return 0;
    }

    /**
     * Process XBRL files for a company
     */
    private int processXBRLFiles(Company company, File dir, String ticker, DataStore dataStore, boolean parseHTML) throws Exception {
        // Filter XBRL files by ticker symbol in filename
        File[] xbrlFiles = dir.listFiles((d, name) ->
            name.toLowerCase().endsWith(".xml") &&
            name.toLowerCase().contains(ticker.toLowerCase()) &&
            !name.contains("_cal") && !name.contains("_def") &&
            !name.contains("_lab") && !name.contains("_pre")
        );

        if (xbrlFiles == null || xbrlFiles.length == 0) {
            System.err.println("Error: No XBRL files found for ticker '" + ticker + "' in directory: " + dir.getPath());
            System.err.println("Please ensure XBRL filenames contain the ticker symbol (e.g., " + ticker.toLowerCase() + "-20231231.xml)");
            System.err.println("See XBRL_DOWNLOAD_GUIDE.md for download instructions");
            return 1;
        }

        System.out.println("Found " + xbrlFiles.length + " XBRL file(s) to process");

        XBRLParser xbrlParser = new XBRLParser();

        for (File xbrlFile : xbrlFiles) {
            System.out.println("Processing: " + xbrlFile.getName());

            try {
                // Parse XBRL file - basic extraction (for backward compatibility)
                FinancialStatement statement = xbrlParser.parse(xbrlFile);
                company.addFinancialStatement(statement);

                // Parse detailed financial statements (new comprehensive extraction)
                XBRLParser.DetailedFinancialData detailedData = xbrlParser.parseDetailed(xbrlFile);
                company.addDetailedIncomeStatement(detailedData.getIncomeStatement());
                company.addDetailedBalanceSheet(detailedData.getBalanceSheet());
                company.addDetailedCashFlow(detailedData.getCashFlow());

                System.out.println("  ✓ Extracted data for fiscal year " + statement.getFiscalYear());
                System.out.println("  ✓ Extracted detailed financial statements");
            } catch (Exception e) {
                System.err.println("  ✗ Error processing " + xbrlFile.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (company.getFinancialStatements().isEmpty()) {
            System.err.println("Error: No financial data extracted from XBRL files");
            return 1;
        }

        // Parse HTML files for supplementary data if requested
        if (parseHTML) {
            System.out.println("\n--- Parsing HTML files for operating metrics and revenue breakdown ---");
            parseHTMLSupplementaryData(company, dir, ticker);
        }

        // Save company data
        dataStore.saveCompany(company);
        System.out.println("\n✓ Successfully added company: " + company.getName() + " with " +
                company.getFinancialStatements().size() + " year(s) of data");

        return 0;
    }

    /**
     * Parse HTML files for supplementary data not available in XBRL
     * - Revenue breakdown (Passenger, Cargo, Other)
     * - Operating statistics (ASMs, RPMs, RASM, CASM, Load Factor)
     * - Segment information (by geography)
     */
    private void parseHTMLSupplementaryData(Company company, File dir, String ticker) {
        // Filter HTML files by ticker symbol in filename
        File[] htmlFiles = dir.listFiles((d, name) -> {
            String lowerName = name.toLowerCase();
            return (lowerName.endsWith(".htm") || lowerName.endsWith(".html")) &&
                   lowerName.contains(ticker.toLowerCase());
        });

        if (htmlFiles == null || htmlFiles.length == 0) {
            System.err.println("  ⚠ No HTML files found for ticker '" + ticker + "' in directory: " + dir.getPath());
            System.err.println("  ⚠ HTML files are needed for revenue breakdown and operating metrics");
            System.err.println("  ⚠ Download HTML filings from SEC EDGAR (e.g., " + ticker.toLowerCase() + "-20241231.htm)");
            return;
        }

        System.out.println("  Found " + htmlFiles.length + " HTML file(s) to parse");

        AirlineHTMLParser htmlParser = new AirlineHTMLParser();

        for (File htmlFile : htmlFiles) {
            System.out.println("  Processing HTML: " + htmlFile.getName());

            try {
                // Extract fiscal year from filename
                int fiscalYear = extractYearFromFilename(htmlFile.getName());

                // Find the corresponding DetailedIncomeStatement
                DetailedIncomeStatement incomeStatement = company.getDetailedIncomeStatements().stream()
                    .filter(stmt -> stmt.getFiscalYear() == fiscalYear)
                    .findFirst()
                    .orElse(null);

                if (incomeStatement == null) {
                    System.err.println("    ⚠ No matching income statement found for year " + fiscalYear);
                    continue;
                }

                // Parse revenue breakdown
                htmlParser.parseRevenueBreakdown(htmlFile, incomeStatement, fiscalYear);

                if (incomeStatement.getPassengerRevenue() > 0) {
                    System.out.println("    ✓ Extracted revenue breakdown:");
                    System.out.println("      - Passenger: $" + String.format("%.0fM", incomeStatement.getPassengerRevenue() / 1_000_000));
                    System.out.println("      - Cargo: $" + String.format("%.0fM", incomeStatement.getCargoRevenue() / 1_000_000));
                    System.out.println("      - Other: $" + String.format("%.0fM", incomeStatement.getOtherOperatingRevenue() / 1_000_000));
                }

                // Parse operating statistics
                AirlineOperatingMetrics metrics = htmlParser.parseOperatingStatistics(htmlFile, fiscalYear);

                if (metrics.getAvailableSeatMiles() > 0) {
                    // Calculate RASM/CASM if not directly extracted from HTML
                    // Find corresponding balance sheet and cash flow for complete calculation
                    DetailedBalanceSheet balanceSheet = company.getDetailedBalanceSheets().stream()
                        .filter(bs -> bs.getFiscalYear() == fiscalYear)
                        .findFirst()
                        .orElse(null);

                    DetailedCashFlow cashFlow = company.getDetailedCashFlows().stream()
                        .filter(cf -> cf.getFiscalYear() == fiscalYear)
                        .findFirst()
                        .orElse(null);

                    htmlParser.calculateUnitMetrics(metrics, incomeStatement, balanceSheet, cashFlow);

                    company.addOperatingMetrics(metrics);
                    System.out.println("    ✓ Extracted operating metrics:");
                    System.out.println("      - ASMs: " + String.format("%.0fM", metrics.getAvailableSeatMiles() / 1_000_000.0));
                    System.out.println("      - RPMs: " + String.format("%.0fM", metrics.getRevenuePassengerMiles() / 1_000_000.0));
                    System.out.println("      - Load Factor: " + String.format("%.1f%%", metrics.getLoadFactor()));
                    System.out.println("      - RASM: " + String.format("%.2f¢", metrics.getTotalRevenuePerASM()));
                    System.out.println("      - CASM: " + String.format("%.2f¢", metrics.getOperatingCostPerASM()));
                }

            } catch (Exception e) {
                System.err.println("    ✗ Error processing HTML file " + htmlFile.getName() + ": " + e.getMessage());
            }
        }
    }

    @Command(name = "analyze", description = "Analyze a company's financial metrics")
    public int analyzeCompany(
            @Parameters(index = "0", description = "Company ticker symbol") String ticker
    ) {
        System.out.println("Analyzing company: " + ticker);
        System.out.println();

        try {
            DataStore dataStore = new DataStore();
            Company company = dataStore.loadCompany(ticker);

            if (company == null) {
                System.err.println("Error: Company not found: " + ticker);
                System.err.println("Use 'finanalysis list' to see available companies");
                return 1;
            }

            MetricsCalculator calculator = new MetricsCalculator();
            List<FinancialMetrics> metricsList = calculator.calculateMetrics(company);

            if (metricsList.isEmpty()) {
                System.err.println("Error: No metrics calculated");
                return 1;
            }

            // Display company info
            System.out.println("Company: " + company.getName() + " (" + company.getTicker() + ")");
            if (company.getIndustry() != null) {
                System.out.println("Industry: " + company.getIndustry());
            }
            System.out.println();

            // Display comprehensive metrics
            printProfitabilityMetrics(metricsList);
            System.out.println();

            printReturnMetrics(metricsList);
            System.out.println();

            printGrowthMetrics(metricsList);
            System.out.println();

            printLiquidityMetrics(metricsList);
            System.out.println();

            printWorkingCapitalMetrics(metricsList);
            System.out.println();

            printLeverageMetrics(metricsList);
            System.out.println();

            printEfficiencyMetrics(metricsList);
            System.out.println();

            printCashFlowMetrics(metricsList);
            System.out.println();

            printCashFlowQualityMetrics(metricsList);

            // Print glossary tip
            System.out.println();
            System.out.println("═".repeat(100));
            System.out.println("\033[1;36mℹ TIP:\033[0m Need help understanding a metric? Use: \033[1mfinanalysis glossary <term>\033[0m");
            System.out.println("      Example: finanalysis glossary ROE");
            System.out.println("═".repeat(100));

            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    @Command(name = "compare", description = "Compare multiple companies side by side")
    public int compareCompanies(
            @Option(names = {"-y", "--years"}, description = "Number of years to compare (default: 3)",
                    defaultValue = "3") int yearsToCompare,
            @Parameters(description = "Company ticker symbols to compare") String... tickers
    ) {
        if (tickers.length < 2) {
            System.err.println("Error: Please provide at least 2 companies to compare");
            return 1;
        }

        System.out.println("═".repeat(120));
        System.out.println("COMPARATIVE FINANCIAL ANALYSIS: " + String.join(" vs ", tickers));
        System.out.println("═".repeat(120));
        System.out.println();

        try {
            DataStore dataStore = new DataStore();
            MetricsCalculator calculator = new MetricsCalculator();

            // Load all companies and calculate metrics
            List<Company> companies = new ArrayList<>();
            List<List<FinancialMetrics>> allMetrics = new ArrayList<>();

            for (String ticker : tickers) {
                Company company = dataStore.loadCompany(ticker);
                if (company == null) {
                    System.err.println("Error: Company not found: " + ticker);
                    continue;
                }

                List<FinancialMetrics> metricsList = calculator.calculateMetrics(company);
                if (metricsList.isEmpty()) {
                    System.err.println("Error: No metrics for " + ticker);
                    continue;
                }

                companies.add(company);
                allMetrics.add(metricsList);
            }

            if (companies.size() < 2) {
                System.err.println("Error: Need at least 2 valid companies to compare");
                return 1;
            }

            // Print side-by-side comparison tables
            printComparisonOverview(companies, allMetrics);
            System.out.println();

            printComparisonTable("PROFITABILITY - Margins", companies, allMetrics, yearsToCompare,
                    new String[]{"Gross Margin", "Operating Margin", "Net Margin", "EBITDA Margin"},
                    new MetricExtractor[]{
                            m -> MetricsCalculator.formatPercentage(m.getGrossMargin()),
                            m -> MetricsCalculator.formatPercentage(m.getOperatingMargin()),
                            m -> MetricsCalculator.formatPercentage(m.getNetMargin()),
                            m -> MetricsCalculator.formatPercentage(m.getEbitdaMargin())
                    });

            printComparisonTable("PROFITABILITY - Returns on Capital", companies, allMetrics, yearsToCompare,
                    new String[]{"ROA", "ROE", "ROIC", "ROCE"},
                    new MetricExtractor[]{
                            m -> MetricsCalculator.formatPercentage(m.getReturnOnAssets()),
                            m -> MetricsCalculator.formatPercentage(m.getReturnOnEquity()),
                            m -> MetricsCalculator.formatPercentage(m.getReturnOnInvestedCapital()),
                            m -> MetricsCalculator.formatPercentage(m.getReturnOnCapitalEmployed())
                    });

            printComparisonTable("GROWTH - Year-over-Year", companies, allMetrics, yearsToCompare,
                    new String[]{"Revenue Growth", "OpIncome Growth", "NetIncome Growth", "EPS Growth"},
                    new MetricExtractor[]{
                            m -> MetricsCalculator.formatPercentage(m.getRevenueGrowthRate()),
                            m -> MetricsCalculator.formatPercentage(m.getOperatingIncomeGrowthRate()),
                            m -> MetricsCalculator.formatPercentage(m.getNetIncomeGrowthRate()),
                            m -> MetricsCalculator.formatPercentage(m.getEpsGrowthRate())
                    });

            printComparisonTable("LIQUIDITY", companies, allMetrics, yearsToCompare,
                    new String[]{"Current Ratio", "Quick Ratio", "Cash Ratio", "OCF Ratio"},
                    new MetricExtractor[]{
                            m -> MetricsCalculator.formatRatio(m.getCurrentRatio()),
                            m -> MetricsCalculator.formatRatio(m.getQuickRatio()),
                            m -> MetricsCalculator.formatRatio(m.getCashRatio()),
                            m -> MetricsCalculator.formatRatio(m.getOperatingCashFlowRatio())
                    });

            printComparisonTable("WORKING CAPITAL - Days (Lower is Better)", companies, allMetrics, yearsToCompare,
                    new String[]{"DSO", "DIO", "Cash Conv Cycle"},
                    new MetricExtractor[]{
                            m -> MetricsCalculator.formatDays(m.getDaysSalesOutstanding()),
                            m -> MetricsCalculator.formatDays(m.getDaysInventoryOutstanding()),
                            m -> MetricsCalculator.formatDays(m.getCashConversionCycle())
                    });

            printComparisonTable("LEVERAGE & SOLVENCY", companies, allMetrics, yearsToCompare,
                    new String[]{"Debt/Equity", "Debt/Assets", "Interest Coverage", "EBITDA Coverage"},
                    new MetricExtractor[]{
                            m -> MetricsCalculator.formatRatio(m.getDebtToEquity()),
                            m -> MetricsCalculator.formatRatio(m.getDebtToAssets()),
                            m -> MetricsCalculator.formatRatio(m.getInterestCoverage()),
                            m -> MetricsCalculator.formatRatio(m.getEbitdaCoverage())
                    });

            printComparisonTable("EFFICIENCY - Turnover Ratios", companies, allMetrics, yearsToCompare,
                    new String[]{"Asset Turnover", "Fixed Asset", "Inventory", "Receivables"},
                    new MetricExtractor[]{
                            m -> MetricsCalculator.formatRatio(m.getAssetTurnover()),
                            m -> MetricsCalculator.formatRatio(m.getFixedAssetTurnover()),
                            m -> MetricsCalculator.formatRatio(m.getInventoryTurnover()),
                            m -> MetricsCalculator.formatRatio(m.getReceivablesTurnover())
                    });

            printComparisonTable("CASH FLOW", companies, allMetrics, yearsToCompare,
                    new String[]{"OCF Margin", "FCF Margin", "CapEx/Revenue", "CapEx/OCF"},
                    new MetricExtractor[]{
                            m -> MetricsCalculator.formatPercentage(m.getOperatingCashFlowMargin()),
                            m -> MetricsCalculator.formatPercentage(m.getFreeCashFlowMargin()),
                            m -> MetricsCalculator.formatPercentage(m.getCapexToRevenue()),
                            m -> MetricsCalculator.formatPercentage(m.getCapexToOperatingCashFlow())
                    });

            printComparisonTable("QUALITY OF EARNINGS (Higher is Better)", companies, allMetrics, yearsToCompare,
                    new String[]{"OCF/NetIncome", "FCF/NetIncome", "CF-ROA", "CF-ROE"},
                    new MetricExtractor[]{
                            m -> MetricsCalculator.formatRatio(m.getCashFlowToNetIncome()),
                            m -> MetricsCalculator.formatRatio(m.getFreeCashFlowToNetIncome()),
                            m -> MetricsCalculator.formatPercentage(m.getCashFlowReturnOnAssets()),
                            m -> MetricsCalculator.formatPercentage(m.getCashFlowReturnOnEquity())
                    });

            // Print detailed financial statements comparison
            printDetailedStatementsComparison(companies, yearsToCompare);

            // Print glossary tip
            System.out.println();
            System.out.println("═".repeat(160));
            System.out.println("\033[1;36mℹ TIP:\033[0m Need help understanding a metric? Use the glossary command:");
            System.out.println("      \033[1mfinanalysis glossary <term>\033[0m  - Look up a specific term (e.g., finanalysis glossary ROE)");
            System.out.println("      \033[1mfinanalysis glossary -l\033[0m      - List all available terms by category");
            System.out.println("      \033[1mfinanalysis glossary -s <word>\033[0m - Search for terms containing a keyword");
            System.out.println("═".repeat(160));

            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    // Helper interface for extracting metrics
    @FunctionalInterface
    private interface MetricExtractor {
        String extract(FinancialMetrics metrics);
    }

    private void printComparisonOverview(List<Company> companies, List<List<FinancialMetrics>> allMetrics) {
        System.out.println("COMPANY OVERVIEW");
        System.out.println("─".repeat(120));
        System.out.print(String.format("%-30s", ""));
        for (Company company : companies) {
            System.out.print(String.format("%-25s", company.getTicker()));
        }
        System.out.println();
        System.out.println("─".repeat(120));

        System.out.print(String.format("%-30s", "Company Name:"));
        for (Company company : companies) {
            System.out.print(String.format("%-25s", truncate(company.getName(), 24)));
        }
        System.out.println();

        System.out.print(String.format("%-30s", "Industry:"));
        for (Company company : companies) {
            String industry = company.getIndustry() != null ? company.getIndustry() : "N/A";
            System.out.print(String.format("%-25s", truncate(industry, 24)));
        }
        System.out.println();

        System.out.print(String.format("%-30s", "Fiscal Year (Latest):"));
        for (List<FinancialMetrics> metrics : allMetrics) {
            int latestYear = metrics.get(metrics.size() - 1).getFiscalYear();
            System.out.print(String.format("%-25s", latestYear));
        }
        System.out.println();

        System.out.print(String.format("%-30s", "Years of Data:"));
        for (List<FinancialMetrics> metrics : allMetrics) {
            System.out.print(String.format("%-25s", metrics.size()));
        }
        System.out.println();
        System.out.println("─".repeat(120));
    }

    private void printComparisonTable(String title, List<Company> companies,
                                      List<List<FinancialMetrics>> allMetrics,
                                      int yearsToCompare,
                                      String[] metricNames, MetricExtractor[] extractors) {
        System.out.println();
        System.out.println("═".repeat(160));
        System.out.println(title);
        System.out.println("═".repeat(160));

        // Determine the actual years to display (from most recent backwards)
        int maxYears = yearsToCompare;

        // Print header with company names
        System.out.print(String.format("%-30s", "Metric"));
        for (int companyIdx = 0; companyIdx < companies.size(); companyIdx++) {
            Company company = companies.get(companyIdx);
            List<FinancialMetrics> metrics = allMetrics.get(companyIdx);

            // Show ticker and year range
            int startIdx = Math.max(0, metrics.size() - maxYears);
            int endYear = metrics.get(metrics.size() - 1).getFiscalYear();
            int startYear = metrics.get(startIdx).getFiscalYear();
            String header = String.format("%s (%d-%d)", company.getTicker(), startYear, endYear);

            // Calculate width needed for this company's data (15 chars per year)
            int width = Math.min(maxYears, metrics.size()) * 15;
            System.out.print(String.format("%-" + width + "s", header));
        }
        System.out.println();

        // Print year headers
        System.out.print(String.format("%-30s", ""));
        for (List<FinancialMetrics> metrics : allMetrics) {
            int startIdx = Math.max(0, metrics.size() - maxYears);
            for (int i = metrics.size() - 1; i >= startIdx; i--) {
                System.out.print(String.format("%-15s", metrics.get(i).getFiscalYear()));
            }
        }
        System.out.println();
        System.out.println("─".repeat(160));

        // Print each metric row with multi-year data
        for (int metricIdx = 0; metricIdx < metricNames.length; metricIdx++) {
            System.out.print(String.format("%-30s", metricNames[metricIdx]));

            for (List<FinancialMetrics> metrics : allMetrics) {
                int startIdx = Math.max(0, metrics.size() - maxYears);

                // Print values from most recent to oldest
                for (int i = metrics.size() - 1; i >= startIdx; i--) {
                    String value = extractors[metricIdx].extract(metrics.get(i));
                    System.out.print(String.format("%-15s", value));
                }
            }
            System.out.println();
        }
    }

    @Command(name = "list", description = "List all companies in the database")
    public int listCompanies() {
        System.out.println("Companies in database:");
        System.out.println();

        try {
            DataStore dataStore = new DataStore();
            List<Company> companies = dataStore.loadAllCompanies();

            if (companies.isEmpty()) {
                System.out.println("No companies found. Use 'finanalysis add' to add companies.");
                return 0;
            }

            System.out.println(String.format("%-10s %-30s %-20s %s", "Ticker", "Name", "Industry", "Years"));
            System.out.println("─".repeat(80));

            for (Company company : companies) {
                String industry = (company.getIndustry() != null) ? company.getIndustry() : "N/A";
                System.out.println(String.format("%-10s %-30s %-20s %d",
                        company.getTicker(),
                        truncate(company.getName(), 30),
                        truncate(industry, 20),
                        company.getFinancialStatements().size()));
            }

            System.out.println();
            System.out.println("Total companies: " + companies.size());

            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    @Command(name = "income-statement", description = "Show detailed income statement")
    public int showIncomeStatement(
            @Parameters(index = "0", description = "Company ticker symbol") String ticker,
            @Option(names = {"-y", "--years"}, description = "Number of years to show (default: 3)",
                    defaultValue = "3") int yearsToShow
    ) {
        try {
            DataStore dataStore = new DataStore();
            Company company = dataStore.loadCompany(ticker);

            if (company == null) {
                System.err.println("Error: Company not found: " + ticker);
                return 1;
            }

            List<DetailedIncomeStatement> statements = company.getDetailedIncomeStatements();
            if (statements.isEmpty()) {
                System.err.println("Error: No detailed income statement data found for " + ticker);
                System.err.println("Please re-run: finanalysis add with XBRL files to extract detailed data");
                return 1;
            }

            // Display income statement
            printDetailedIncomeStatement(company, statements, yearsToShow);

            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    @Command(name = "balance-sheet", description = "Show detailed balance sheet")
    public int showBalanceSheet(
            @Parameters(index = "0", description = "Company ticker symbol") String ticker,
            @Option(names = {"-y", "--years"}, description = "Number of years to show (default: 3)",
                    defaultValue = "3") int yearsToShow
    ) {
        try {
            DataStore dataStore = new DataStore();
            Company company = dataStore.loadCompany(ticker);

            if (company == null) {
                System.err.println("Error: Company not found: " + ticker);
                return 1;
            }

            List<DetailedBalanceSheet> statements = company.getDetailedBalanceSheets();
            if (statements.isEmpty()) {
                System.err.println("Error: No detailed balance sheet data found for " + ticker);
                System.err.println("Please re-run: finanalysis add with XBRL files to extract detailed data");
                return 1;
            }

            // Display balance sheet
            printDetailedBalanceSheet(company, statements, yearsToShow);

            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    @Command(name = "cash-flow", description = "Show detailed cash flow statement")
    public int showCashFlow(
            @Parameters(index = "0", description = "Company ticker symbol") String ticker,
            @Option(names = {"-y", "--years"}, description = "Number of years to show (default: 3)",
                    defaultValue = "3") int yearsToShow
    ) {
        try {
            DataStore dataStore = new DataStore();
            Company company = dataStore.loadCompany(ticker);

            if (company == null) {
                System.err.println("Error: Company not found: " + ticker);
                return 1;
            }

            List<DetailedCashFlow> statements = company.getDetailedCashFlows();
            if (statements.isEmpty()) {
                System.err.println("Error: No detailed cash flow data found for " + ticker);
                System.err.println("Please re-run: finanalysis add with XBRL files to extract detailed data");
                return 1;
            }

            // Display cash flow
            printDetailedCashFlow(company, statements, yearsToShow);

            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    @Command(name = "inspect-xbrl", description = "Inspect an XBRL file to see available tags and values")
    public int inspectXBRL(
            @Parameters(index = "0", description = "Path to XBRL file") String xbrlFilePath,
            @Option(names = {"-f", "--filter"}, description = "Filter tags containing keyword") String filter
    ) {
        try {
            File xbrlFile = new File(xbrlFilePath);
            if (!xbrlFile.exists()) {
                System.err.println("Error: XBRL file not found: " + xbrlFilePath);
                return 1;
            }

            XBRLParser parser = new XBRLParser();
            parser.inspectXBRLFile(xbrlFile, filter);

            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    @Command(name = "glossary", description = "Look up financial metrics and abbreviations")
    public int showGlossary(
            @Parameters(index = "0", description = "Term to look up (optional)", arity = "0..1") String term,
            @Option(names = {"-l", "--list"}, description = "List all terms by category") boolean listAll,
            @Option(names = {"-s", "--search"}, description = "Search for terms containing keyword") String searchKeyword
    ) {
        try {
            // Search mode
            if (searchKeyword != null && !searchKeyword.isEmpty()) {
                List<Glossary.GlossaryEntry> results = Glossary.search(searchKeyword);
                if (results.isEmpty()) {
                    System.out.println("No terms found matching: " + searchKeyword);
                    return 0;
                }

                System.out.println("═".repeat(100));
                System.out.println("GLOSSARY SEARCH RESULTS: " + searchKeyword);
                System.out.println("═".repeat(100));
                System.out.println();

                for (Glossary.GlossaryEntry entry : results) {
                    printGlossaryEntry(entry);
                    System.out.println();
                }
                return 0;
            }

            // List all mode
            if (listAll) {
                System.out.println("═".repeat(100));
                System.out.println("FINANCIAL METRICS GLOSSARY - ALL TERMS BY CATEGORY");
                System.out.println("═".repeat(100));
                System.out.println();

                Map<String, List<String>> categories = Glossary.getCategories();
                for (Map.Entry<String, List<String>> category : categories.entrySet()) {
                    System.out.println("▶ " + category.getKey().toUpperCase());
                    System.out.println("─".repeat(100));

                    for (String abbr : category.getValue()) {
                        Glossary.GlossaryEntry entry = Glossary.lookup(abbr);
                        if (entry != null) {
                            System.out.println(String.format("  %-20s - %s",
                                    formatTerm(entry.getAbbreviation()),
                                    entry.getFullName()));
                        }
                    }
                    System.out.println();
                }

                System.out.println("TIP: Use 'finanalysis glossary <term>' to see detailed information");
                System.out.println("     Example: finanalysis glossary ROE");
                return 0;
            }

            // Lookup specific term
            if (term != null && !term.isEmpty()) {
                Glossary.GlossaryEntry entry = Glossary.lookup(term);
                if (entry == null) {
                    System.out.println("Term not found: " + term);
                    System.out.println();
                    System.out.println("Try:");
                    System.out.println("  finanalysis glossary -l           (list all terms)");
                    System.out.println("  finanalysis glossary -s <keyword> (search for terms)");
                    return 1;
                }

                System.out.println("═".repeat(100));
                System.out.println("GLOSSARY ENTRY");
                System.out.println("═".repeat(100));
                System.out.println();
                printGlossaryEntry(entry);
                return 0;
            }

            // No arguments - show usage
            System.out.println("Financial Metrics Glossary");
            System.out.println();
            System.out.println("Usage:");
            System.out.println("  finanalysis glossary <term>      Look up a specific term (e.g., ROE, EBITDA)");
            System.out.println("  finanalysis glossary -l          List all terms by category");
            System.out.println("  finanalysis glossary -s <word>   Search for terms");
            System.out.println();
            System.out.println("Examples:");
            System.out.println("  finanalysis glossary ROE");
            System.out.println("  finanalysis glossary \"Current Ratio\"");
            System.out.println("  finanalysis glossary -s cash");
            System.out.println("  finanalysis glossary -l");

            return 0;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return 1;
        }
    }

    private void printGlossaryEntry(Glossary.GlossaryEntry entry) {
        System.out.println(formatTerm(entry.getAbbreviation()) + " - " + entry.getFullName());
        System.out.println("─".repeat(100));

        // Wrap description text to fit width
        String[] words = entry.getDescription().split(" ");
        StringBuilder line = new StringBuilder();
        int lineWidth = 0;
        int maxWidth = 98;

        for (String word : words) {
            if (lineWidth + word.length() + 1 > maxWidth) {
                System.out.println(line.toString());
                line = new StringBuilder(word);
                lineWidth = word.length();
            } else {
                if (line.length() > 0) {
                    line.append(" ");
                    lineWidth++;
                }
                line.append(word);
                lineWidth += word.length();
            }
        }
        if (line.length() > 0) {
            System.out.println(line.toString());
        }
    }

    private String formatTerm(String term) {
        // Use ANSI codes to make term bold and underlined
        return "\033[1;4m" + term + "\033[0m";
    }

    // Helper methods for printing comprehensive tables

    private void printProfitabilityMetrics(List<FinancialMetrics> metricsList) {
        System.out.println("═".repeat(100));
        System.out.println("PROFITABILITY METRICS - Margins");
        System.out.println("═".repeat(100));
        System.out.println(String.format("%-8s %14s %14s %14s %14s",
                "Year", "Gross Margin", "Op Margin", "Net Margin", "EBITDA Margin"));
        System.out.println("─".repeat(100));

        for (FinancialMetrics metrics : metricsList) {
            System.out.println(String.format("%-8d %14s %14s %14s %14s",
                    metrics.getFiscalYear(),
                    MetricsCalculator.formatPercentage(metrics.getGrossMargin()),
                    MetricsCalculator.formatPercentage(metrics.getOperatingMargin()),
                    MetricsCalculator.formatPercentage(metrics.getNetMargin()),
                    MetricsCalculator.formatPercentage(metrics.getEbitdaMargin())));
        }
    }

    private void printReturnMetrics(List<FinancialMetrics> metricsList) {
        System.out.println("═".repeat(100));
        System.out.println("PROFITABILITY METRICS - Returns on Capital");
        System.out.println("═".repeat(100));
        System.out.println(String.format("%-8s %14s %14s %14s %14s",
                "Year", "ROA", "ROE", "ROIC", "ROCE"));
        System.out.println("─".repeat(100));

        for (FinancialMetrics metrics : metricsList) {
            System.out.println(String.format("%-8d %14s %14s %14s %14s",
                    metrics.getFiscalYear(),
                    MetricsCalculator.formatPercentage(metrics.getReturnOnAssets()),
                    MetricsCalculator.formatPercentage(metrics.getReturnOnEquity()),
                    MetricsCalculator.formatPercentage(metrics.getReturnOnInvestedCapital()),
                    MetricsCalculator.formatPercentage(metrics.getReturnOnCapitalEmployed())));
        }
    }

    private void printGrowthMetrics(List<FinancialMetrics> metricsList) {
        System.out.println("═".repeat(120));
        System.out.println("GROWTH METRICS - Year-over-Year Growth");
        System.out.println("═".repeat(120));
        System.out.println(String.format("%-8s %14s %14s %14s %14s %14s %14s",
                "Year", "Revenue", "OpIncome", "NetIncome", "EBITDA", "EPS", "FCF"));
        System.out.println("─".repeat(120));

        for (FinancialMetrics metrics : metricsList) {
            System.out.println(String.format("%-8d %14s %14s %14s %14s %14s %14s",
                    metrics.getFiscalYear(),
                    MetricsCalculator.formatPercentage(metrics.getRevenueGrowthRate()),
                    MetricsCalculator.formatPercentage(metrics.getOperatingIncomeGrowthRate()),
                    MetricsCalculator.formatPercentage(metrics.getNetIncomeGrowthRate()),
                    MetricsCalculator.formatPercentage(metrics.getEbitdaGrowthRate()),
                    MetricsCalculator.formatPercentage(metrics.getEpsGrowthRate()),
                    MetricsCalculator.formatPercentage(metrics.getFreeCashFlowGrowthRate())));
        }
    }

    private void printLiquidityMetrics(List<FinancialMetrics> metricsList) {
        System.out.println("═".repeat(100));
        System.out.println("LIQUIDITY METRICS");
        System.out.println("═".repeat(100));
        System.out.println(String.format("%-8s %16s %16s %16s %16s",
                "Year", "Current Ratio", "Quick Ratio", "Cash Ratio", "OCF Ratio"));
        System.out.println("─".repeat(100));

        for (FinancialMetrics metrics : metricsList) {
            System.out.println(String.format("%-8d %16s %16s %16s %16s",
                    metrics.getFiscalYear(),
                    MetricsCalculator.formatRatio(metrics.getCurrentRatio()),
                    MetricsCalculator.formatRatio(metrics.getQuickRatio()),
                    MetricsCalculator.formatRatio(metrics.getCashRatio()),
                    MetricsCalculator.formatRatio(metrics.getOperatingCashFlowRatio())));
        }
    }

    private void printWorkingCapitalMetrics(List<FinancialMetrics> metricsList) {
        System.out.println("═".repeat(100));
        System.out.println("WORKING CAPITAL CYCLE - (Lower is Better)");
        System.out.println("═".repeat(100));
        System.out.println(String.format("%-8s %20s %20s %20s %20s",
                "Year", "DSO", "DIO", "DPO", "Cash Conv Cycle"));
        System.out.println("─".repeat(100));

        for (FinancialMetrics metrics : metricsList) {
            System.out.println(String.format("%-8d %20s %20s %20s %20s",
                    metrics.getFiscalYear(),
                    MetricsCalculator.formatDays(metrics.getDaysSalesOutstanding()),
                    MetricsCalculator.formatDays(metrics.getDaysInventoryOutstanding()),
                    MetricsCalculator.formatDays(metrics.getDaysPayableOutstanding()),
                    MetricsCalculator.formatDays(metrics.getCashConversionCycle())));
        }
    }

    private void printLeverageMetrics(List<FinancialMetrics> metricsList) {
        System.out.println("═".repeat(100));
        System.out.println("LEVERAGE & SOLVENCY METRICS");
        System.out.println("═".repeat(100));
        System.out.println(String.format("%-8s %14s %14s %18s %18s",
                "Year", "Debt/Equity", "Debt/Assets", "Interest Cov", "EBITDA Cov"));
        System.out.println("─".repeat(100));

        for (FinancialMetrics metrics : metricsList) {
            System.out.println(String.format("%-8d %14s %14s %18s %18s",
                    metrics.getFiscalYear(),
                    MetricsCalculator.formatRatio(metrics.getDebtToEquity()),
                    MetricsCalculator.formatRatio(metrics.getDebtToAssets()),
                    MetricsCalculator.formatRatio(metrics.getInterestCoverage()),
                    MetricsCalculator.formatRatio(metrics.getEbitdaCoverage())));
        }
    }

    private void printEfficiencyMetrics(List<FinancialMetrics> metricsList) {
        System.out.println("═".repeat(110));
        System.out.println("EFFICIENCY METRICS - Asset Turnover");
        System.out.println("═".repeat(110));
        System.out.println(String.format("%-8s %16s %16s %16s %16s %16s",
                "Year", "Asset Turn", "Fixed Asset", "Inventory", "Receivables", "WC Turn"));
        System.out.println("─".repeat(110));

        for (FinancialMetrics metrics : metricsList) {
            System.out.println(String.format("%-8d %16s %16s %16s %16s %16s",
                    metrics.getFiscalYear(),
                    MetricsCalculator.formatRatio(metrics.getAssetTurnover()),
                    MetricsCalculator.formatRatio(metrics.getFixedAssetTurnover()),
                    MetricsCalculator.formatRatio(metrics.getInventoryTurnover()),
                    MetricsCalculator.formatRatio(metrics.getReceivablesTurnover()),
                    MetricsCalculator.formatRatio(metrics.getWorkingCapitalTurnover())));
        }
    }

    private void printCashFlowMetrics(List<FinancialMetrics> metricsList) {
        System.out.println("═".repeat(110));
        System.out.println("CASH FLOW METRICS");
        System.out.println("═".repeat(110));
        System.out.println(String.format("%-8s %16s %16s %18s %18s",
                "Year", "OCF Margin", "FCF Margin", "CapEx/Revenue", "CapEx/OCF"));
        System.out.println("─".repeat(110));

        for (FinancialMetrics metrics : metricsList) {
            System.out.println(String.format("%-8d %16s %16s %18s %18s",
                    metrics.getFiscalYear(),
                    MetricsCalculator.formatPercentage(metrics.getOperatingCashFlowMargin()),
                    MetricsCalculator.formatPercentage(metrics.getFreeCashFlowMargin()),
                    MetricsCalculator.formatPercentage(metrics.getCapexToRevenue()),
                    MetricsCalculator.formatPercentage(metrics.getCapexToOperatingCashFlow())));
        }
    }

    private void printCashFlowQualityMetrics(List<FinancialMetrics> metricsList) {
        System.out.println("═".repeat(100));
        System.out.println("CASH FLOW QUALITY - Quality of Earnings (Higher is Better)");
        System.out.println("═".repeat(100));
        System.out.println(String.format("%-8s %20s %20s %20s %20s",
                "Year", "OCF/NetIncome", "FCF/NetIncome", "CF-ROA", "CF-ROE"));
        System.out.println("─".repeat(100));

        for (FinancialMetrics metrics : metricsList) {
            System.out.println(String.format("%-8d %20s %20s %20s %20s",
                    metrics.getFiscalYear(),
                    MetricsCalculator.formatRatio(metrics.getCashFlowToNetIncome()),
                    MetricsCalculator.formatRatio(metrics.getFreeCashFlowToNetIncome()),
                    MetricsCalculator.formatPercentage(metrics.getCashFlowReturnOnAssets()),
                    MetricsCalculator.formatPercentage(metrics.getCashFlowReturnOnEquity())));
        }
    }

    private int extractYearFromFilename(String filename) {
        // Try to extract year from filename (e.g., "company_10k_2023.pdf" -> 2023)
        String pattern = "\\d{4}";
        java.util.regex.Pattern r = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = r.matcher(filename);
        if (m.find()) {
            return Integer.parseInt(m.group(0));
        }
        return 0; // Will be extracted from PDF content
    }

    private String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    // Display methods for detailed financial statements

    private void printDetailedIncomeStatement(Company company, List<DetailedIncomeStatement> statements, int yearsToShow) {
        System.out.println("═".repeat(140));
        System.out.println("DETAILED INCOME STATEMENT: " + company.getName() + " (" + company.getTicker() + ")");
        System.out.println("═".repeat(140));
        System.out.println();

        // Get recent years
        int startIdx = Math.max(0, statements.size() - yearsToShow);
        List<DetailedIncomeStatement> recentStmts = statements.subList(startIdx, statements.size());

        // Revenue Section
        System.out.println("OPERATING REVENUE");
        System.out.println("─".repeat(140));
        System.out.printf("%-40s", "");
        for (DetailedIncomeStatement stmt : recentStmts) {
            System.out.printf("%20d", stmt.getFiscalYear());
        }
        System.out.println("\n" + "─".repeat(140));

        printIncomeStatementLine("Passenger Revenue", recentStmts,
                stmt -> stmt.getPassengerRevenue(),
                stmt -> String.format("%.1f%%", stmt.getPassengerRevenuePercentage()));

        printIncomeStatementLine("Cargo Revenue", recentStmts,
                stmt -> stmt.getCargoRevenue(),
                stmt -> String.format("%.1f%%", stmt.getCargoRevenuePercentage()));

        printIncomeStatementLine("Other Operating Revenue", recentStmts,
                stmt -> stmt.getOtherOperatingRevenue(), null);

        printIncomeStatementLine("Total Operating Revenue", recentStmts,
                stmt -> stmt.getTotalOperatingRevenue(), null);

        // Operating Expenses Section
        System.out.println("\nOPERATING EXPENSES");
        System.out.println("─".repeat(140));

        printIncomeStatementLine("Aircraft Fuel", recentStmts,
                stmt -> stmt.getAircraftFuel(),
                stmt -> String.format("%.1f%%", stmt.getFuelAsPercentOfOpex()));

        printIncomeStatementLine("Salaries & Related Costs", recentStmts,
                stmt -> stmt.getSalariesAndRelatedCosts(),
                stmt -> String.format("%.1f%% of rev", stmt.getSalariesAsPercentOfRevenue()));

        printIncomeStatementLine("Regional Capacity Purchase", recentStmts,
                stmt -> stmt.getRegionalCapacityPurchase(), null);

        printIncomeStatementLine("Landing Fees & Rent", recentStmts,
                stmt -> stmt.getLandingFeesAndRent(), null);

        printIncomeStatementLine("Aircraft Maintenance", recentStmts,
                stmt -> stmt.getAircraftMaintenance(), null);

        printIncomeStatementLine("Depreciation", recentStmts,
                stmt -> stmt.getDepreciation(), null);

        printIncomeStatementLine("Amortization", recentStmts,
                stmt -> stmt.getAmortization(), null);

        printIncomeStatementLine("Aircraft Rent", recentStmts,
                stmt -> stmt.getAircraftRent(), null);

        printIncomeStatementLine("Other Operating Expenses", recentStmts,
                stmt -> stmt.getOtherOperatingExpenses(), null);

        printIncomeStatementLine("Total Operating Expenses", recentStmts,
                stmt -> stmt.getTotalOperatingExpenses(), null);

        // Operating Income
        System.out.println("\n" + "─".repeat(140));
        printIncomeStatementLine("Operating Income", recentStmts,
                stmt -> stmt.getOperatingIncome(), null);

        // Non-Operating
        System.out.println("\nNON-OPERATING ITEMS");
        System.out.println("─".repeat(140));

        printIncomeStatementLine("Interest Expense", recentStmts,
                stmt -> stmt.getInterestExpense(), null);

        printIncomeStatementLine("Interest Income", recentStmts,
                stmt -> stmt.getInterestIncome(), null);

        printIncomeStatementLine("Other Income/(Expense)", recentStmts,
                stmt -> stmt.getOtherIncomeExpense(), null);

        // Bottom Line
        System.out.println("\n" + "─".repeat(140));
        printIncomeStatementLine("Pre-tax Income", recentStmts,
                stmt -> stmt.getPretaxIncome(), null);

        printIncomeStatementLine("Income Tax Expense", recentStmts,
                stmt -> stmt.getIncomeTaxExpense(),
                stmt -> String.format("%.1f%% rate", stmt.getEffectiveTaxRate()));

        printIncomeStatementLine("Net Income", recentStmts,
                stmt -> stmt.getNetIncome(), null);

        // Per Share
        System.out.println("\nPER SHARE DATA");
        System.out.println("─".repeat(140));

        printIncomeStatementLine("Basic EPS", recentStmts,
                stmt -> stmt.getBasicEPS(), null);

        printIncomeStatementLine("Diluted EPS", recentStmts,
                stmt -> stmt.getDilutedEPS(), null);

        System.out.println("\n" + "═".repeat(140));
        System.out.println("All figures in millions except per-share data");
        System.out.println("═".repeat(140));
    }

    private void printIncomeStatementLine(String label, List<DetailedIncomeStatement> statements,
                                          java.util.function.Function<DetailedIncomeStatement, Double> valueExtractor,
                                          java.util.function.Function<DetailedIncomeStatement, String> percentExtractor) {
        System.out.printf("%-40s", label);
        for (DetailedIncomeStatement stmt : statements) {
            double value = valueExtractor.apply(stmt);
            if (value == 0) {
                System.out.printf("%20s", "-");
            } else {
                System.out.printf("%,20.2f", value / 1_000_000); // Convert to millions
            }
        }
        if (percentExtractor != null) {
            System.out.print("    ");
            for (DetailedIncomeStatement stmt : statements) {
                System.out.printf("%-12s", percentExtractor.apply(stmt));
            }
        }
        System.out.println();
    }

    private void printDetailedBalanceSheet(Company company, List<DetailedBalanceSheet> statements, int yearsToShow) {
        System.out.println("═".repeat(140));
        System.out.println("DETAILED BALANCE SHEET: " + company.getName() + " (" + company.getTicker() + ")");
        System.out.println("═".repeat(140));
        System.out.println();

        // Get recent years
        int startIdx = Math.max(0, statements.size() - yearsToShow);
        List<DetailedBalanceSheet> recentStmts = statements.subList(startIdx, statements.size());

        // Header
        System.out.printf("%-40s", "");
        for (DetailedBalanceSheet stmt : recentStmts) {
            System.out.printf("%20d", stmt.getFiscalYear());
        }
        System.out.println("\n" + "─".repeat(140));

        // Current Assets
        System.out.println("CURRENT ASSETS");
        System.out.println("─".repeat(140));

        printBalanceSheetLine("Cash & Cash Equivalents", recentStmts, bs -> bs.getCashAndCashEquivalents());
        printBalanceSheetLine("Short-term Investments", recentStmts, bs -> bs.getShortTermInvestments());
        printBalanceSheetLine("Restricted Cash", recentStmts, bs -> bs.getRestrictedCash());
        printBalanceSheetLine("Accounts Receivable", recentStmts, bs -> bs.getAccountsReceivable());
        printBalanceSheetLine("Prepaid Expenses", recentStmts, bs -> bs.getPrepaidExpenses());
        printBalanceSheetLine("Spare Parts & Supplies", recentStmts, bs -> bs.getSparePartsAndSupplies());
        printBalanceSheetLine("Total Current Assets", recentStmts, bs -> bs.getTotalCurrentAssets());

        // PP&E
        System.out.println("\nPROPERTY, PLANT & EQUIPMENT");
        System.out.println("─".repeat(140));

        printBalanceSheetLine("Flight Equipment", recentStmts, bs -> bs.getFlightEquipment());
        printBalanceSheetLine("Ground Equipment", recentStmts, bs -> bs.getGroundEquipment());
        printBalanceSheetLine("Buildings", recentStmts, bs -> bs.getBuildings());
        printBalanceSheetLine("Total PP&E at Cost", recentStmts, bs -> bs.getTotalPPEAtCost());
        printBalanceSheetLine("Less: Accumulated Depreciation", recentStmts, bs -> bs.getAccumulatedDepreciation());
        printBalanceSheetLine("Net PP&E", recentStmts, bs -> bs.getNetPPE());

        // Other Assets
        System.out.println("\nOTHER ASSETS");
        System.out.println("─".repeat(140));

        printBalanceSheetLine("Operating Lease ROU Assets", recentStmts, bs -> bs.getOperatingLeaseRightOfUseAssets());
        printBalanceSheetLine("Goodwill", recentStmts, bs -> bs.getGoodwill());
        printBalanceSheetLine("Intangible Assets", recentStmts, bs -> bs.getIntangibleAssets());
        printBalanceSheetLine("Long-term Investments", recentStmts, bs -> bs.getLongTermInvestments());

        System.out.println("\n" + "─".repeat(140));
        printBalanceSheetLine("TOTAL ASSETS", recentStmts, bs -> bs.getTotalAssets());

        // Current Liabilities
        System.out.println("\n\nCURRENT LIABILITIES");
        System.out.println("─".repeat(140));

        printBalanceSheetLine("Accounts Payable", recentStmts, bs -> bs.getAccountsPayable());
        printBalanceSheetLine("Accrued Salaries & Benefits", recentStmts, bs -> bs.getAccruedSalariesAndBenefits());
        printBalanceSheetLine("Air Traffic Liability", recentStmts, bs -> bs.getAirTrafficLiability());
        printBalanceSheetLine("Current Debt", recentStmts, bs -> bs.getCurrentDebt());
        printBalanceSheetLine("Current Operating Lease Liab", recentStmts, bs -> bs.getCurrentOperatingLeaseLiabilities());
        printBalanceSheetLine("Total Current Liabilities", recentStmts, bs -> bs.getTotalCurrentLiabilities());

        // Long-term Liabilities
        System.out.println("\nLONG-TERM LIABILITIES");
        System.out.println("─".repeat(140));

        printBalanceSheetLine("Long-term Debt", recentStmts, bs -> bs.getLongTermDebt());
        printBalanceSheetLine("LT Operating Lease Liabilities", recentStmts, bs -> bs.getLongTermOperatingLeaseLiabilities());
        printBalanceSheetLine("LT Finance Lease Liabilities", recentStmts, bs -> bs.getLongTermFinanceLeaseLiabilities());
        printBalanceSheetLine("Pension Liabilities", recentStmts, bs -> bs.getPensionLiabilities());
        printBalanceSheetLine("Loyalty Program Deferred Rev", recentStmts, bs -> bs.getLoyaltyProgramDeferredRevenue());

        System.out.println("\n" + "─".repeat(140));
        printBalanceSheetLine("TOTAL LIABILITIES", recentStmts, bs -> bs.getTotalLiabilities());

        // Equity
        System.out.println("\n\nSTOCKHOLDERS' EQUITY");
        System.out.println("─".repeat(140));

        printBalanceSheetLine("Common Stock", recentStmts, bs -> bs.getCommonStock());
        printBalanceSheetLine("Additional Paid-in Capital", recentStmts, bs -> bs.getAdditionalPaidInCapital());
        printBalanceSheetLine("Retained Earnings", recentStmts, bs -> bs.getRetainedEarnings());
        printBalanceSheetLine("Accum Other Comprehensive Inc", recentStmts, bs -> bs.getAccumulatedOtherComprehensiveIncome());

        System.out.println("\n" + "─".repeat(140));
        printBalanceSheetLine("TOTAL EQUITY", recentStmts, bs -> bs.getTotalStockholdersEquity());

        System.out.println("\n" + "═".repeat(140));
        System.out.println("All figures in millions");
        System.out.println("═".repeat(140));
    }

    private void printBalanceSheetLine(String label, List<DetailedBalanceSheet> statements,
                                       java.util.function.Function<DetailedBalanceSheet, Double> valueExtractor) {
        System.out.printf("%-40s", label);
        for (DetailedBalanceSheet stmt : statements) {
            double value = valueExtractor.apply(stmt);
            if (value == 0) {
                System.out.printf("%20s", "-");
            } else {
                System.out.printf("%,20.2f", value / 1_000_000); // Convert to millions
            }
        }
        System.out.println();
    }

    private void printDetailedCashFlow(Company company, List<DetailedCashFlow> statements, int yearsToShow) {
        System.out.println("═".repeat(140));
        System.out.println("DETAILED CASH FLOW STATEMENT: " + company.getName() + " (" + company.getTicker() + ")");
        System.out.println("═".repeat(140));
        System.out.println();

        // Get recent years
        int startIdx = Math.max(0, statements.size() - yearsToShow);
        List<DetailedCashFlow> recentStmts = statements.subList(startIdx, statements.size());

        // Header
        System.out.printf("%-40s", "");
        for (DetailedCashFlow stmt : recentStmts) {
            System.out.printf("%20d", stmt.getFiscalYear());
        }
        System.out.println("\n" + "─".repeat(140));

        // Operating Activities
        System.out.println("OPERATING ACTIVITIES");
        System.out.println("─".repeat(140));

        printCashFlowLine("Net Income", recentStmts, cf -> cf.getNetIncome());
        printCashFlowLine("Depreciation", recentStmts, cf -> cf.getDepreciation());
        printCashFlowLine("Amortization", recentStmts, cf -> cf.getAmortization());
        printCashFlowLine("Stock-based Compensation", recentStmts, cf -> cf.getStockBasedCompensation());
        printCashFlowLine("Deferred Income Taxes", recentStmts, cf -> cf.getDeferredIncomeTaxes());

        System.out.println("\nChanges in Working Capital:");
        printCashFlowLine("  Change in Receivables", recentStmts, cf -> cf.getChangeInReceivables());
        printCashFlowLine("  Change in Payables", recentStmts, cf -> cf.getChangeInAccountsPayable());
        printCashFlowLine("  Change in Air Traffic Liab", recentStmts, cf -> cf.getChangeInAirTrafficLiability());

        System.out.println("\n" + "─".repeat(140));
        printCashFlowLine("Net Cash from Operating", recentStmts, cf -> cf.getNetCashFromOperating());

        // Investing Activities
        System.out.println("\n\nINVESTING ACTIVITIES");
        System.out.println("─".repeat(140));

        printCashFlowLine("Capital Expenditures", recentStmts, cf -> cf.getCapitalExpenditures());
        printCashFlowLine("Aircraft Purchases", recentStmts, cf -> cf.getAircraftPurchases());
        printCashFlowLine("Pre-delivery Deposits", recentStmts, cf -> cf.getPreDeliveryDeposits());
        printCashFlowLine("Proceeds from Asset Sales", recentStmts, cf -> cf.getProceedsFromAssetSales());
        printCashFlowLine("Purchases of Investments", recentStmts, cf -> cf.getPurchasesOfInvestments());
        printCashFlowLine("Sales of Investments", recentStmts, cf -> cf.getSalesOfInvestments());

        System.out.println("\n" + "─".repeat(140));
        printCashFlowLine("Net Cash from Investing", recentStmts, cf -> cf.getNetCashFromInvesting());

        // Financing Activities
        System.out.println("\n\nFINANCING ACTIVITIES");
        System.out.println("─".repeat(140));

        printCashFlowLine("Proceeds from Debt Issuance", recentStmts, cf -> cf.getProceedsFromDebtIssuance());
        printCashFlowLine("Debt Repayments", recentStmts, cf -> cf.getDebtRepayments());
        printCashFlowLine("Proceeds from Sale-Leasebacks", recentStmts, cf -> cf.getProceedsFromSaleLeasebacks());
        printCashFlowLine("Stock Repurchases", recentStmts, cf -> cf.getStockRepurchases());
        printCashFlowLine("Dividends Paid", recentStmts, cf -> cf.getDividendsPaid());

        System.out.println("\n" + "─".repeat(140));
        printCashFlowLine("Net Cash from Financing", recentStmts, cf -> cf.getNetCashFromFinancing());

        // Summary
        System.out.println("\n\nSUMMARY");
        System.out.println("─".repeat(140));

        printCashFlowLine("Net Change in Cash", recentStmts, cf -> cf.getNetChangeInCash());
        printCashFlowLine("Cash at End of Period", recentStmts, cf -> cf.getCashAtEnd());

        System.out.println("\n" + "─".repeat(140));
        printCashFlowLine("FREE CASH FLOW", recentStmts, cf -> cf.getFreeCashFlow());

        System.out.println("\n" + "═".repeat(140));
        System.out.println("All figures in millions");
        System.out.println("═".repeat(140));
    }

    private void printCashFlowLine(String label, List<DetailedCashFlow> statements,
                                   java.util.function.Function<DetailedCashFlow, Double> valueExtractor) {
        System.out.printf("%-40s", label);
        for (DetailedCashFlow stmt : statements) {
            double value = valueExtractor.apply(stmt);
            if (value == 0) {
                System.out.printf("%20s", "-");
            } else {
                System.out.printf("%,20.2f", value / 1_000_000); // Convert to millions
            }
        }
        System.out.println();
    }

    private void printDetailedStatementsComparison(List<Company> companies, int yearsToShow) {
        // Check if any companies have detailed statements
        boolean hasDetailedData = companies.stream()
                .anyMatch(c -> !c.getDetailedIncomeStatements().isEmpty());

        if (!hasDetailedData) {
            System.out.println();
            System.out.println("═".repeat(160));
            System.out.println("ℹ INFO: Detailed financial statements not available. Run 'add' command with XBRL files to extract detailed data.");
            System.out.println("═".repeat(160));
            return;
        }

        // Print detailed income statement comparison
        printDetailedIncomeStatementComparison(companies, yearsToShow);

        // Print detailed balance sheet comparison
        printDetailedBalanceSheetComparison(companies, yearsToShow);

        // Print detailed cash flow comparison
        printDetailedCashFlowComparison(companies, yearsToShow);
    }

    private void printDetailedIncomeStatementComparison(List<Company> companies, int yearsToShow) {
        System.out.println();
        System.out.println("═".repeat(160));
        System.out.println("DETAILED INCOME STATEMENT COMPARISON ($ millions)");
        System.out.println("═".repeat(160));

        // Print header with company names and years
        System.out.printf("%-40s", "");
        for (Company company : companies) {
            List<DetailedIncomeStatement> stmts = company.getDetailedIncomeStatements();
            if (stmts.isEmpty()) continue;

            int startIdx = Math.max(0, stmts.size() - yearsToShow);
            List<DetailedIncomeStatement> recentStmts = stmts.subList(startIdx, stmts.size());

            for (DetailedIncomeStatement stmt : recentStmts) {
                System.out.printf("%12s", company.getTicker() + " " + stmt.getFiscalYear());
            }
        }
        System.out.println();
        System.out.println("─".repeat(160));

        // OPERATING REVENUE
        System.out.println("OPERATING REVENUE");
        printComparisonLine("Passenger Revenue", companies, yearsToShow,
                stmt -> stmt.getPassengerRevenue());
        printComparisonLine("Cargo Revenue", companies, yearsToShow,
                stmt -> stmt.getCargoRevenue());
        printComparisonLine("Other Revenue", companies, yearsToShow,
                stmt -> stmt.getOtherOperatingRevenue());
        printComparisonLine("Total Operating Revenue", companies, yearsToShow,
                stmt -> stmt.getTotalOperatingRevenue());

        // OPERATING EXPENSES
        System.out.println("\nOPERATING EXPENSES");
        printComparisonLine("Aircraft Fuel", companies, yearsToShow,
                stmt -> stmt.getAircraftFuel());
        printComparisonLine("Salaries & Benefits", companies, yearsToShow,
                stmt -> stmt.getSalariesAndRelatedCosts());
        printComparisonLine("Regional Capacity Purchase", companies, yearsToShow,
                stmt -> stmt.getRegionalCapacityPurchase());
        printComparisonLine("Landing Fees & Rent", companies, yearsToShow,
                stmt -> stmt.getLandingFeesAndRent());
        printComparisonLine("Aircraft Maintenance", companies, yearsToShow,
                stmt -> stmt.getAircraftMaintenance());
        printComparisonLine("Depreciation", companies, yearsToShow,
                stmt -> stmt.getDepreciation());
        printComparisonLine("Amortization", companies, yearsToShow,
                stmt -> stmt.getAmortization());
        printComparisonLine("Aircraft Rent", companies, yearsToShow,
                stmt -> stmt.getAircraftRent());
        printComparisonLine("Distribution Expenses", companies, yearsToShow,
                stmt -> stmt.getDistributionExpenses());
        printComparisonLine("Special Charges", companies, yearsToShow,
                stmt -> stmt.getSpecialCharges());
        printComparisonLine("Other Operating Expenses", companies, yearsToShow,
                stmt -> stmt.getOtherOperatingExpenses());
        printComparisonLine("Total Operating Expenses", companies, yearsToShow,
                stmt -> stmt.getTotalOperatingExpenses());

        // PROFITABILITY
        System.out.println("\nPROFITABILITY");
        printComparisonLine("Operating Income", companies, yearsToShow,
                stmt -> stmt.getOperatingIncome());
        printComparisonLine("EBIT", companies, yearsToShow,
                stmt -> stmt.getEbit());
        printComparisonLine("EBITDA", companies, yearsToShow,
                stmt -> stmt.getEbitda());
        printComparisonLine("Interest Expense", companies, yearsToShow,
                stmt -> stmt.getInterestExpense());
        printComparisonLine("Income Tax Expense", companies, yearsToShow,
                stmt -> stmt.getIncomeTaxExpense());
        printComparisonLine("Net Income", companies, yearsToShow,
                stmt -> stmt.getNetIncome());
        System.out.println("─".repeat(160));
    }

    private void printDetailedBalanceSheetComparison(List<Company> companies, int yearsToShow) {
        System.out.println();
        System.out.println("═".repeat(160));
        System.out.println("DETAILED BALANCE SHEET COMPARISON ($ millions)");
        System.out.println("═".repeat(160));

        // Print header
        System.out.printf("%-40s", "");
        for (Company company : companies) {
            List<DetailedBalanceSheet> sheets = company.getDetailedBalanceSheets();
            if (sheets.isEmpty()) continue;

            int startIdx = Math.max(0, sheets.size() - yearsToShow);
            List<DetailedBalanceSheet> recentSheets = sheets.subList(startIdx, sheets.size());

            for (DetailedBalanceSheet sheet : recentSheets) {
                System.out.printf("%12s", company.getTicker() + " " + sheet.getFiscalYear());
            }
        }
        System.out.println();
        System.out.println("─".repeat(160));

        // ASSETS
        System.out.println("ASSETS");
        printBalanceSheetComparisonLine("Cash & Equivalents", companies, yearsToShow,
                bs -> bs.getCashAndCashEquivalents());
        printBalanceSheetComparisonLine("Total Current Assets", companies, yearsToShow,
                bs -> bs.getTotalCurrentAssets());
        printBalanceSheetComparisonLine("Flight Equipment", companies, yearsToShow,
                bs -> bs.getFlightEquipment());
        printBalanceSheetComparisonLine("Operating Lease ROU Assets", companies, yearsToShow,
                bs -> bs.getOperatingLeaseRightOfUseAssets());
        printBalanceSheetComparisonLine("Total Assets", companies, yearsToShow,
                bs -> bs.getTotalAssets());

        // LIABILITIES
        System.out.println("\nLIABILITIES");
        printBalanceSheetComparisonLine("Air Traffic Liability", companies, yearsToShow,
                bs -> bs.getAirTrafficLiability());
        printBalanceSheetComparisonLine("Total Current Liabilities", companies, yearsToShow,
                bs -> bs.getTotalCurrentLiabilities());
        printBalanceSheetComparisonLine("Long-Term Debt", companies, yearsToShow,
                bs -> bs.getLongTermDebt());
        printBalanceSheetComparisonLine("Total Liabilities", companies, yearsToShow,
                bs -> bs.getTotalLiabilities());

        // EQUITY
        System.out.println("\nSTOCKHOLDERS' EQUITY");
        printBalanceSheetComparisonLine("Total Equity", companies, yearsToShow,
                bs -> bs.getTotalStockholdersEquity());
        System.out.println("─".repeat(160));
    }

    private void printDetailedCashFlowComparison(List<Company> companies, int yearsToShow) {
        System.out.println();
        System.out.println("═".repeat(160));
        System.out.println("DETAILED CASH FLOW COMPARISON ($ millions)");
        System.out.println("═".repeat(160));

        // Print header
        System.out.printf("%-40s", "");
        for (Company company : companies) {
            List<DetailedCashFlow> flows = company.getDetailedCashFlows();
            if (flows.isEmpty()) continue;

            int startIdx = Math.max(0, flows.size() - yearsToShow);
            List<DetailedCashFlow> recentFlows = flows.subList(startIdx, flows.size());

            for (DetailedCashFlow flow : recentFlows) {
                System.out.printf("%12s", company.getTicker() + " " + flow.getFiscalYear());
            }
        }
        System.out.println();
        System.out.println("─".repeat(160));

        // OPERATING ACTIVITIES
        System.out.println("OPERATING ACTIVITIES");
        printCashFlowComparisonLine("Net Income", companies, yearsToShow,
                cf -> cf.getNetIncome());
        printCashFlowComparisonLine("Depreciation & Amortization", companies, yearsToShow,
                cf -> cf.getDepreciation() + cf.getAmortization());
        printCashFlowComparisonLine("Change in Air Traffic Liability", companies, yearsToShow,
                cf -> cf.getChangeInAirTrafficLiability());
        printCashFlowComparisonLine("Net Cash from Operations", companies, yearsToShow,
                cf -> cf.getNetCashFromOperating());

        // INVESTING ACTIVITIES
        System.out.println("\nINVESTING ACTIVITIES");
        printCashFlowComparisonLine("Capital Expenditures", companies, yearsToShow,
                cf -> cf.getCapitalExpenditures());
        printCashFlowComparisonLine("Aircraft Purchases", companies, yearsToShow,
                cf -> cf.getAircraftPurchases());
        printCashFlowComparisonLine("Net Cash from Investing", companies, yearsToShow,
                cf -> cf.getNetCashFromInvesting());

        // FINANCING ACTIVITIES
        System.out.println("\nFINANCING ACTIVITIES");
        printCashFlowComparisonLine("Proceeds from Debt", companies, yearsToShow,
                cf -> cf.getProceedsFromDebtIssuance());
        printCashFlowComparisonLine("Debt Repayments", companies, yearsToShow,
                cf -> cf.getDebtRepayments());
        printCashFlowComparisonLine("Net Cash from Financing", companies, yearsToShow,
                cf -> cf.getNetCashFromFinancing());

        // SUMMARY
        System.out.println("\nCASH FLOW SUMMARY");
        printCashFlowComparisonLine("Free Cash Flow", companies, yearsToShow,
                cf -> cf.getFreeCashFlow());
        System.out.println("─".repeat(160));
    }

    private void printComparisonLine(String label, List<Company> companies, int yearsToShow,
                                     java.util.function.Function<DetailedIncomeStatement, Double> valueExtractor) {
        System.out.printf("%-40s", label);
        for (Company company : companies) {
            List<DetailedIncomeStatement> stmts = company.getDetailedIncomeStatements();
            if (stmts.isEmpty()) continue;

            int startIdx = Math.max(0, stmts.size() - yearsToShow);
            List<DetailedIncomeStatement> recentStmts = stmts.subList(startIdx, stmts.size());

            for (DetailedIncomeStatement stmt : recentStmts) {
                double value = valueExtractor.apply(stmt);
                if (value == 0) {
                    System.out.printf("%12s", "-");
                } else {
                    System.out.printf("%,12.1f", value / 1_000_000); // Convert to millions
                }
            }
        }
        System.out.println();
    }

    private void printBalanceSheetComparisonLine(String label, List<Company> companies, int yearsToShow,
                                                 java.util.function.Function<DetailedBalanceSheet, Double> valueExtractor) {
        System.out.printf("%-40s", label);
        for (Company company : companies) {
            List<DetailedBalanceSheet> sheets = company.getDetailedBalanceSheets();
            if (sheets.isEmpty()) continue;

            int startIdx = Math.max(0, sheets.size() - yearsToShow);
            List<DetailedBalanceSheet> recentSheets = sheets.subList(startIdx, sheets.size());

            for (DetailedBalanceSheet sheet : recentSheets) {
                double value = valueExtractor.apply(sheet);
                if (value == 0) {
                    System.out.printf("%12s", "-");
                } else {
                    System.out.printf("%,12.1f", value / 1_000_000); // Convert to millions
                }
            }
        }
        System.out.println();
    }

    private void printCashFlowComparisonLine(String label, List<Company> companies, int yearsToShow,
                                            java.util.function.Function<DetailedCashFlow, Double> valueExtractor) {
        System.out.printf("%-40s", label);
        for (Company company : companies) {
            List<DetailedCashFlow> flows = company.getDetailedCashFlows();
            if (flows.isEmpty()) continue;

            int startIdx = Math.max(0, flows.size() - yearsToShow);
            List<DetailedCashFlow> recentFlows = flows.subList(startIdx, flows.size());

            for (DetailedCashFlow flow : recentFlows) {
                double value = valueExtractor.apply(flow);
                if (value == 0) {
                    System.out.printf("%12s", "-");
                } else {
                    System.out.printf("%,12.1f", value / 1_000_000); // Convert to millions
                }
            }
        }
        System.out.println();
    }

    @Override
    public Integer call() {
        // Default behavior when no subcommand is specified
        CommandLine.usage(this, System.out);
        return 0;
    }
}
