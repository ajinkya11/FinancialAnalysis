# Usage Examples

This document provides comprehensive examples of using the Airline Financial Analyzer.

## Table of Contents
1. [Basic Comparisons](#basic-comparisons)
2. [Multi-Year Analysis](#multi-year-analysis)
3. [Export Options](#export-options)
4. [Single Company Analysis](#single-company-analysis)
5. [Cache Management](#cache-management)
6. [Advanced Scenarios](#advanced-scenarios)

## Basic Comparisons

### Example 1: Compare United Airlines vs JetBlue

```bash
./run.sh compare UAL JBLU
```

**Output includes:**
- Executive summary
- Financial metrics comparison (Revenue, Margins, ROE, ROA)
- Airline operating metrics (RASM, CASM, Load Factor, Yield)
- Strengths and weaknesses for each company
- Investment recommendation

### Example 2: Compare Delta vs American Airlines

```bash
./run.sh compare DAL AAL
```

### Example 3: Compare Southwest vs Alaska Airlines

```bash
./run.sh compare LUV ALK
```

## Multi-Year Analysis

### Example 4: 3-Year Comparison

```bash
./run.sh compare UAL JBLU --years 3
```

This fetches the last 3 years of 10-K filings and performs trend analysis.

### Example 5: 5-Year Historical Analysis

```bash
./run.sh compare DAL AAL --years 5
```

## Export Options

### Example 6: Export to CSV

```bash
./run.sh compare UAL JBLU --output ual_vs_jblu.csv
```

Creates a CSV file with:
- All metrics in tabular format
- Winner for each metric
- Percentage differences
- Strengths and weaknesses
- Recommendations

### Example 7: Export to JSON

```bash
./run.sh compare UAL JBLU --output results.json
```

Creates a JSON file with complete analysis data, suitable for:
- Further processing
- Integration with other tools
- Archiving results

### Example 8: Export to All Formats

```bash
./run.sh compare UAL JBLU --format all
```

Generates:
- Console display (colored output)
- CSV file: `UAL_vs_JBLU_comparison.csv`
- JSON file: `UAL_vs_JBLU_comparison.json`

### Example 9: Custom Output File

```bash
./run.sh compare DAL AAL --output "./reports/delta_american_Q4_2023.csv"
```

## Single Company Analysis

### Example 10: Get Information for United Airlines

```bash
./run.sh info UAL
```

**Output includes:**
- Company name and ticker
- CIK number
- Fiscal year end
- Latest financial metrics (Revenue, margins, ROA, ROE)
- Latest airline operating metrics (Load Factor, RASM, CASM, Yield)

### Example 11: Multi-Year Company Info

```bash
./run.sh info JBLU --years 3
```

## Cache Management

### Example 12: View Cache Statistics

```bash
./run.sh cache --stats
```

Shows:
- Number of cached XBRL files
- Number of cached company data entries
- Cache efficiency

### Example 13: Clear Cache

```bash
./run.sh cache --clear
```

Clears all cached data. Useful when:
- New 10-K filings are available
- Troubleshooting data issues
- Testing with fresh data

### Example 14: Compare with Fresh Data

```bash
./run.sh compare UAL JBLU --clear-cache
```

Clears cache before performing analysis, ensuring latest data is fetched.

## Advanced Scenarios

### Example 15: Quarterly Analysis Workflow

```bash
# Q1 Analysis
./run.sh compare UAL JBLU --output "reports/Q1_2024_UAL_vs_JBLU.csv"

# Q2 Analysis
./run.sh compare UAL JBLU --clear-cache --output "reports/Q2_2024_UAL_vs_JBLU.csv"

# Q3 Analysis
./run.sh compare UAL JBLU --clear-cache --output "reports/Q3_2024_UAL_vs_JBLU.csv"
```

### Example 16: Competitive Analysis Report

```bash
# Compare all major carriers
./run.sh compare UAL DAL --output "reports/UAL_vs_DAL.csv"
./run.sh compare UAL AAL --output "reports/UAL_vs_AAL.csv"
./run.sh compare UAL LUV --output "reports/UAL_vs_LUV.csv"
./run.sh compare UAL JBLU --output "reports/UAL_vs_JBLU.csv"
```

### Example 17: Before/After Event Analysis

```bash
# Pre-event analysis (cached)
./run.sh compare UAL JBLU --output "pre_event_analysis.csv"

# ... wait for new 10-K filings after event ...

# Post-event analysis (fresh data)
./run.sh compare UAL JBLU --clear-cache --output "post_event_analysis.csv"
```

### Example 18: Data Validation Workflow

```bash
# 1. Get individual company data
./run.sh info UAL
./run.sh info JBLU

# 2. Verify data looks reasonable

# 3. Perform comparison
./run.sh compare UAL JBLU --format all

# 4. Review JSON for detailed data
cat UAL_vs_JBLU_comparison.json | jq '.'
```

## Integration Examples

### Example 19: Automated Monthly Report

Create a shell script `monthly_report.sh`:

```bash
#!/bin/bash
DATE=$(date +%Y_%m)
REPORT_DIR="monthly_reports/$DATE"

mkdir -p "$REPORT_DIR"

# Generate reports
./run.sh compare UAL JBLU --clear-cache --output "$REPORT_DIR/UAL_vs_JBLU.csv"
./run.sh compare DAL AAL --output "$REPORT_DIR/DAL_vs_AAL.csv"
./run.sh compare LUV ALK --output "$REPORT_DIR/LUV_vs_ALK.csv"

echo "Monthly reports generated in $REPORT_DIR"
```

### Example 20: Python Integration

```python
import subprocess
import json

# Run analysis and capture JSON output
result = subprocess.run([
    './run.sh', 'compare', 'UAL', 'JBLU',
    '--output', 'temp_results.json'
], capture_output=True, text=True)

# Load and process results
with open('temp_results.json', 'r') as f:
    analysis = json.load(f)

# Extract key metrics
ual_roa = analysis['company1']['latestFinancialMetrics']['returnOnAssets']
jblu_roa = analysis['company2']['latestFinancialMetrics']['returnOnAssets']

print(f"UAL ROA: {ual_roa}%")
print(f"JBLU ROA: {jblu_roa}%")
```

## Tips and Best Practices

### Performance Tips

1. **Use Cache**: For repeated analyses, cache significantly speeds up execution
2. **Clear Cache Periodically**: When new 10-K filings are released
3. **Batch Processing**: Run multiple comparisons in sequence

### Data Quality Tips

1. **Verify Ticker Symbols**: Double-check ticker symbols before running
2. **Review Logs**: Check `logs/financial-analyzer.log` for warnings
3. **Compare Results**: Cross-reference with official SEC filings
4. **Handle Anomalies**: Some metrics may be missing or unusual - check logs

### Error Handling

```bash
# Capture errors
./run.sh compare UAL JBLU 2> errors.log

# Check if successful
if [ $? -eq 0 ]; then
    echo "Analysis completed successfully"
else
    echo "Analysis failed - check errors.log"
fi
```

## Sample Output Interpretation

### Understanding Console Output

```
EXECUTIVE SUMMARY
United Airlines demonstrates stronger overall financial performance compared to
JetBlue Airways, leading in 5 out of 8 key metrics analyzed.

Key Highlights:
  ✓ Company 1 shows significantly better Operating Margin (25.50% difference)
  ✓ Company 1 shows significantly better ROE (31.20% difference)

Red Flags:
  ⚠ JetBlue Airways has high debt-to-equity ratio
```

**Interpretation:**
- United Airlines is financially stronger
- United has better profitability (Operating Margin, ROE)
- JetBlue has leverage concerns

### Understanding Airline Metrics

```
AIRLINE OPERATING METRICS COMPARISON
Metric                   UAL                       JBLU
Load Factor              85.2%                     82.1%
RASM                     14.5 cents                13.2 cents
CASM                     12.1 cents                12.8 cents
Yield                    15.2 cents                14.1 cents
```

**Interpretation:**
- UAL has higher load factor (more efficient capacity utilization)
- UAL has higher RASM (generates more revenue per seat mile)
- UAL has lower CASM (more cost-efficient operations)
- UAL has higher yield (premium pricing power)

## Troubleshooting Examples

### Issue: Slow First Run

```bash
# Normal on first run (downloading data)
./run.sh compare UAL JBLU
# Takes ~15 seconds

# Much faster on subsequent runs (cached)
./run.sh compare UAL JBLU
# Takes ~2 seconds
```

### Issue: Ticker Not Found

```bash
# Error: Invalid or not found ticker symbol: XYZ

# Solution: Check supported tickers
./run.sh --help
```

### Issue: Stale Data

```bash
# Clear cache and fetch fresh data
./run.sh cache --clear
./run.sh compare UAL JBLU
```

---

For more information, see [README_V1.md](README_V1.md)
