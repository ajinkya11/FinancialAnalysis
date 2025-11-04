# Airline Financial Analyzer v1.0

A comprehensive Java CLI application for comparative financial analysis of airline companies using SEC 10-K filings.

## Features

### Core Functionality
- ✅ Fetch and parse SEC 10-K filings (XBRL format)
- ✅ Extract comprehensive financial metrics from XBRL data
- ✅ Calculate airline-specific operating metrics (RASM, CASM, Load Factor, Yield)
- ✅ Perform comparative analysis between two companies
- ✅ Generate detailed reports in multiple formats (Console, CSV, JSON)
- ✅ Intelligent caching mechanism to avoid re-downloading filings
- ✅ Colored console output for better readability
- ✅ Comprehensive error handling and data validation

### Financial Metrics Calculated

#### Income Statement Metrics
- Revenue (total operating revenue)
- Cost of Revenue / Operating Expenses
- Gross Profit & Gross Margin %
- Operating Income & Operating Margin %
- EBITDA & EBITDA Margin %
- Net Income & Net Margin %
- EPS (Basic & Diluted)

#### Balance Sheet Metrics
- Total Assets, Liabilities, and Equity
- Current Ratio & Quick Ratio
- Debt-to-Equity Ratio
- Working Capital
- Net Debt
- Book Value per Share

#### Cash Flow Metrics
- Operating Cash Flow
- Free Cash Flow (OCF - CapEx)
- Cash Conversion Ratio

#### Profitability Ratios
- Return on Assets (ROA)
- Return on Equity (ROE)
- Return on Invested Capital (ROIC)

#### Airline-Specific Metrics
- **RASM** (Revenue per Available Seat Mile)
- **CASM** (Cost per Available Seat Mile)
- **CASM-ex** (CASM excluding fuel)
- **Load Factor** (RPM / ASM × 100)
- **Yield** (Passenger Revenue / RPM)
- **Break-even Load Factor**
- Fuel costs as % of operating expenses
- Labor costs as % of operating expenses

## Requirements

- Java 17 or higher
- Maven 3.6 or higher
- Internet connection (for fetching SEC data)

## Building the Application

```bash
# Clean and build the project
mvn clean package

# This creates an executable JAR:
# target/airline-financial-analyzer-1.0.0.jar
```

## Usage

### Basic Comparison

Compare two airline companies:

```bash
java -jar target/airline-financial-analyzer-1.0.0.jar compare UAL JBLU
```

### Multi-Year Analysis

Analyze multiple years of data:

```bash
java -jar target/airline-financial-analyzer-1.0.0.jar compare UAL JBLU --years 3
```

### Export Results

Export to CSV:
```bash
java -jar target/airline-financial-analyzer-1.0.0.jar compare UAL JBLU --output results.csv
```

Export to JSON:
```bash
java -jar target/airline-financial-analyzer-1.0.0.jar compare UAL JBLU --output results.json
```

Export to all formats:
```bash
java -jar target/airline-financial-analyzer-1.0.0.jar compare UAL JBLU --format all
```

### Single Company Information

Display information for a single company:

```bash
java -jar target/airline-financial-analyzer-1.0.0.jar info UAL
```

### Cache Management

View cache statistics:
```bash
java -jar target/airline-financial-analyzer-1.0.0.jar cache --stats
```

Clear cache:
```bash
java -jar target/airline-financial-analyzer-1.0.0.jar cache --clear
```

Compare with fresh data (bypass cache):
```bash
java -jar target/airline-financial-analyzer-1.0.0.jar compare UAL JBLU --clear-cache
```

## Supported Airlines

The application currently supports the following airline ticker symbols:

- **UAL** - United Airlines
- **JBLU** - JetBlue Airways
- **DAL** - Delta Air Lines
- **AAL** - American Airlines
- **LUV** - Southwest Airlines
- **ALK** - Alaska Air Group

## Architecture

The application follows a clean, modular architecture:

```
com.financialanalysis
├── models/              # POJOs for financial data
├── data/                # SEC EDGAR API client and XBRL parser
├── metrics/             # Financial and airline metrics calculators
├── analysis/            # Comparative analysis logic
├── reporting/           # Report generation (Console, CSV, JSON)
├── cli/                 # CLI interface (Picocli)
├── config/              # Spring Boot configuration
└── exceptions/          # Custom exceptions
```

### Key Components

1. **SECEdgarClient**: Fetches 10-K filings from SEC EDGAR API with retry logic
2. **XBRLParser**: Parses XBRL files and extracts financial data with validation
3. **FinancialMetricsCalculator**: Calculates comprehensive financial ratios
4. **AirlineMetricsCalculator**: Calculates airline-specific operational metrics
5. **ComparativeAnalysisService**: Performs comparative analysis between companies
6. **ConsoleReportGenerator**: Generates colored console reports
7. **ExportService**: Exports results to CSV and JSON formats
8. **FinancialDataService**: Orchestrates all components with caching

## Data Quality & Validation

The application implements robust data quality checks:

- ✅ Revenue validation to prevent double-counting
- ✅ Balance sheet equation validation
- ✅ Airline metrics sanity checks (e.g., load factor 0-100%)
- ✅ Margin validation for reasonableness
- ✅ Comprehensive logging for troubleshooting

### CRITICAL: Revenue Extraction

The XBRL parser correctly identifies aggregate revenue concepts to avoid double-counting:
- Uses standard XBRL tags: `us-gaap:Revenues`, `us-gaap:OperatingRevenue`
- **Does NOT sum** individual revenue line items
- Validates extracted values against reported totals

## Output Examples

### Console Output

The console report includes:
1. **Executive Summary**: Key highlights and red flags
2. **Company Overview**: Basic information
3. **Financial Metrics Comparison**: Side-by-side comparison with color coding
4. **Airline Operating Metrics**: Operational efficiency comparison
5. **Strengths & Weaknesses**: For each company
6. **Investment Recommendation**: Based on analysis

### CSV Export

CSV export includes:
- All metrics in tabular format
- Winner and difference percentage for each metric
- Executive summary and recommendations
- Strengths and weaknesses for both companies

### JSON Export

JSON export includes the complete `ComparisonResult` object with all nested data.

## Caching

The application uses Caffeine cache for improved performance:

- **XBRL Cache**: Stores downloaded XBRL files (24-hour TTL, max 100 entries)
- **Company Data Cache**: Stores parsed company data (12-hour TTL, max 50 entries)

Benefits:
- Faster subsequent analyses
- Reduced load on SEC EDGAR servers
- Respects SEC rate limits

## Error Handling

The application provides clear error messages for:
- Invalid ticker symbols
- Missing or corrupt XBRL data
- Network failures (with automatic retry)
- Data validation failures

## Logging

Logs are written to:
- Console (INFO level)
- `logs/financial-analyzer.log` (rolling daily, 30-day retention)

## Testing

Run unit tests:
```bash
mvn test
```

The test suite includes:
- Financial metrics calculation tests
- Airline metrics calculation tests
- Edge case handling
- Null value handling

## Performance

- First analysis: ~10-15 seconds (includes download and parsing)
- Cached analysis: ~1-2 seconds
- Memory usage: ~200-300 MB

## Limitations

1. **Ticker Support**: Currently limited to major US airlines (easily extensible)
2. **XBRL Parsing**: Some company-specific XBRL tags may not be recognized
3. **Historical Data**: Limited to available 10-K filings
4. **Data Availability**: Depends on SEC EDGAR API availability

## Future Enhancements

Potential improvements for future versions:
- Support for all SEC-listed companies
- Multi-year trend analysis with charts
- Industry benchmark comparisons
- Real-time data integration
- Web-based UI
- Database persistence
- Email report delivery

## Troubleshooting

### Issue: "Invalid ticker symbol"
**Solution**: Verify the ticker is supported or add it to `SECEdgarClient.getTickerCIK()`

### Issue: "Failed to parse XBRL"
**Solution**:
- Clear cache and retry
- Check logs for specific parsing errors
- Company may use non-standard XBRL tags

### Issue: Network timeouts
**Solution**: The application automatically retries with exponential backoff (up to 4 attempts)

### Issue: Invalid metrics calculated
**Solution**:
- Check logs for validation warnings
- Verify source data quality in SEC filings
- Some metrics may be unavailable for certain companies

## Contributing

To add support for more ticker symbols:
1. Find the CIK number from SEC EDGAR
2. Add mapping to `SECEdgarClient.getTickerCIK()`

## License

Educational use only. This application is for learning purposes.

## Disclaimer

This tool is for educational and informational purposes only. It should not be used as the sole basis for investment decisions. Always verify data against official SEC filings and consult with financial professionals.

## Contact & Support

For issues, questions, or contributions, please refer to the project repository.

---

**Version**: 1.0.0
**Last Updated**: 2025-11-04
**Built with**: Java 17, Spring Boot 3.1.5, Picocli 4.7.5
