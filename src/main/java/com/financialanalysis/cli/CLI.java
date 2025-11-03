package com.financialanalysis.cli;

import com.financialanalysis.analyzer.MetricsCalculator;
import com.financialanalysis.glossary.Glossary;
import com.financialanalysis.model.Company;
import com.financialanalysis.model.FinancialMetrics;
import com.financialanalysis.model.FinancialStatement;
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
            @Option(names = {"-y", "--year"}, description = "Fiscal year (if single file)") Integer year
    ) {
        System.out.println("Adding company: " + companyName + " (" + ticker + ")");
        System.out.println("Format: " + format.toUpperCase());

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
                return processXBRLFiles(company, dir, ticker, dataStore);
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
    private int processXBRLFiles(Company company, File dir, String ticker, DataStore dataStore) throws Exception {
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
                // Parse XBRL file
                FinancialStatement statement = xbrlParser.parse(xbrlFile);
                company.addFinancialStatement(statement);

                System.out.println("  ✓ Extracted data for fiscal year " + statement.getFiscalYear());
            } catch (Exception e) {
                System.err.println("  ✗ Error processing " + xbrlFile.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (company.getFinancialStatements().isEmpty()) {
            System.err.println("Error: No financial data extracted from XBRL files");
            return 1;
        }

        // Save company data
        dataStore.saveCompany(company);
        System.out.println("\n✓ Successfully added company: " + company.getName() + " with " +
                company.getFinancialStatements().size() + " year(s) of data");

        return 0;
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

    @Override
    public Integer call() {
        // Default behavior when no subcommand is specified
        CommandLine.usage(this, System.out);
        return 0;
    }
}
