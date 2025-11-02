# Financial Analysis CLI

A Java-based command-line tool for analyzing company 10-K filings and generating comprehensive financial metrics.

## Features

- Parse 10-K PDF filings and extract financial data
- Calculate key financial metrics across multiple years:
  - **Profitability**: Revenue growth, gross margin, operating margin, net margin, EBITDA margin
  - **Efficiency**: ROA, ROE, asset turnover
  - **Liquidity**: Current ratio, quick ratio
  - **Solvency/Leverage**: Debt-to-equity, debt-to-assets, interest coverage
  - **Cash Flow**: Free cash flow margin, CapEx/revenue ratio
- Compare multiple companies side-by-side
- Store and retrieve company data in JSON format

## Prerequisites

- Java 11 or higher
- Maven 3.6+

## Installation

1. Clone this repository:
```bash
git clone <repository-url>
cd FinancialAnalysis
```

2. Build the project:
```bash
mvn clean package
```

This will create an executable JAR file in the `target` directory.

## Usage

### Basic Command Structure

```bash
java -jar target/financial-analysis-cli-1.0.0-jar-with-dependencies.jar <command> [options]
```

For convenience, you can create an alias:
```bash
alias finanalysis='java -jar target/financial-analysis-cli-1.0.0-jar-with-dependencies.jar'
```

### Commands

#### 1. Add a Company

Parse 10-K PDFs and add a company to the database:

```bash
finanalysis add <company-name> <ticker> [options]
```

**Options:**
- `-d, --directory <path>` : Directory containing 10-K PDFs (default: `data/10k-pdfs`)
- `-i, --industry <name>` : Industry or sector
- `-y, --year <year>` : Fiscal year (if processing a single PDF)

**Example:**
```bash
# Add Apple with 10-K PDFs in the default directory
finanalysis add "Apple Inc." AAPL -i "Technology"

# Add Microsoft with PDFs from a custom directory
finanalysis add "Microsoft Corporation" MSFT -d /path/to/msft-10ks -i "Technology"
```

**PDF File Organization:**
- Place 10-K PDF files in the `data/10k-pdfs` directory (or specify a custom directory)
- File naming: Include the year in the filename (e.g., `AAPL_10K_2023.pdf`)
- The tool will attempt to extract the fiscal year from the filename or PDF content

#### 2. Analyze a Company

Display comprehensive financial metrics for a company:

```bash
finanalysis analyze <ticker>
```

**Example:**
```bash
finanalysis analyze AAPL
```

**Output includes:**
- Profitability metrics table
- Efficiency metrics table
- Liquidity metrics table
- Leverage/solvency metrics table
- Cash flow metrics table

#### 3. Compare Companies

Compare multiple companies side-by-side:

```bash
finanalysis compare <ticker1> <ticker2> [ticker3...]
```

**Example:**
```bash
finanalysis compare AAPL MSFT GOOGL
```

#### 4. List Companies

Show all companies in the database:

```bash
finanalysis list
```

### Example Workflow

1. **Download 10-K PDFs** for companies you want to analyze (from SEC EDGAR or company investor relations pages)

2. **Organize PDFs** by company in separate directories or use a naming convention:
```
data/10k-pdfs/
├── AAPL_10K_2019.pdf
├── AAPL_10K_2020.pdf
├── AAPL_10K_2021.pdf
├── AAPL_10K_2022.pdf
└── AAPL_10K_2023.pdf
```

3. **Add companies**:
```bash
finanalysis add "Apple Inc." AAPL -i "Technology"
finanalysis add "Microsoft Corporation" MSFT -i "Technology"
```

4. **Analyze individual companies**:
```bash
finanalysis analyze AAPL
```

5. **Compare companies**:
```bash
finanalysis compare AAPL MSFT
```

## Project Structure

```
FinancialAnalysis/
├── src/main/java/com/financialanalysis/
│   ├── Main.java                    # Application entry point
│   ├── cli/
│   │   └── CLI.java                 # Command-line interface
│   ├── parser/
│   │   ├── PDFParser.java           # PDF text extraction
│   │   └── FinancialDataExtractor.java  # Financial data extraction
│   ├── model/
│   │   ├── Company.java             # Company data model
│   │   ├── FinancialStatement.java  # Financial statement data
│   │   └── FinancialMetrics.java    # Calculated metrics
│   ├── analyzer/
│   │   └── MetricsCalculator.java   # Financial metrics calculator
│   └── storage/
│       └── DataStore.java           # JSON storage layer
├── data/
│   ├── 10k-pdfs/                    # Place 10-K PDFs here
│   └── companies/                   # JSON data storage (auto-created)
├── pom.xml                          # Maven configuration
└── README.md                        # This file
```

## Technical Details

### Financial Metrics Calculated

**Profitability:**
- Revenue Growth Rate = (Current Revenue - Previous Revenue) / Previous Revenue
- Gross Margin = Gross Profit / Revenue
- Operating Margin = Operating Income / Revenue
- Net Margin = Net Income / Revenue
- EBITDA Margin = EBITDA / Revenue

**Efficiency:**
- Return on Assets (ROA) = Net Income / Total Assets
- Return on Equity (ROE) = Net Income / Shareholder Equity
- Asset Turnover = Revenue / Total Assets

**Liquidity:**
- Current Ratio = Current Assets / Current Liabilities
- Quick Ratio = (Cash + Marketable Securities + Receivables) / Current Liabilities

**Solvency/Leverage:**
- Debt-to-Equity = Total Debt / Shareholder Equity
- Debt-to-Assets = Total Debt / Total Assets
- Interest Coverage = EBIT / Interest Expense

**Cash Flow:**
- Free Cash Flow Margin = Free Cash Flow / Revenue
- CapEx to Revenue = Capital Expenditures / Revenue

### Data Storage

Company data is stored in JSON format in the `data/companies/` directory. Each company has its own JSON file named `<TICKER>.json`.

## Limitations and Notes

1. **PDF Parsing Accuracy**: 10-K formats vary significantly between companies and years. The parser uses pattern matching and may not extract all values perfectly. Manual verification is recommended.

2. **Custom Formats**: Different companies format their financial statements differently. You may need to customize the `FinancialDataExtractor.java` for specific companies.

3. **Data Validation**: Always verify extracted data against the original 10-K filings.

4. **MVP Scope**: This initial version focuses on core financial metrics (sections 2-4 from the original requirements). Additional metrics can be added in future versions.

## Future Enhancements

Potential features for future versions:
- Direct SEC EDGAR API integration
- Industry-specific operational metrics
- Trend analysis and visualization
- Export to Excel/CSV
- Valuation metrics (P/E, P/B, EV/EBITDA)
- ESG data extraction
- Multi-company portfolio analysis

## Contributing

Contributions are welcome! Please feel free to submit issues or pull requests.

## License

This project is open-source and available under the MIT License.