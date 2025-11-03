# HTML Parser Guide - Phase 3 Implementation

## Overview

Phase 3 adds **HTML table parsing** capability to extract airline-specific data that is NOT available in XBRL tags, including:
- Revenue breakdown (Passenger, Cargo, Other)
- Operating statistics (ASMs, RPMs, RASM, CASM, Load Factor)
- Segment information (by geography)

## Why HTML Parsing is Needed

### XBRL Limitations Confirmed

Investigation of UAL 10-K XBRL files confirmed:
```bash
finanalysis inspect-xbrl ual-20241231_htm.xml -f passenger
# Result: 0 tags found

finanalysis inspect-xbrl ual-20241231_htm.xml -f cargo
# Result: 0 tags found
```

**Airlines do NOT tag detailed revenue breakdowns in XBRL**. This data exists only in HTML tables in the 10-K filing.

## What's Been Implemented

### 1. New Dependency Added

**File:** `pom.xml`
```xml
<!-- HTML Parsing for 10-K tables -->
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
    <version>1.17.2</version>
</dependency>
```

### 2. HTML Parser Class

**File:** `src/main/java/com/financialanalysis/parser/AirlineHTMLParser.java`

**Capabilities:**
- **Revenue Breakdown Parsing:**
  - Searches for tables containing "Passenger Revenue", "Cargo Revenue", "Other Revenue"
  - Extracts values for specified fiscal year
  - Populates `DetailedIncomeStatement` fields that were previously empty

- **Operating Statistics Parsing:**
  - Extracts Available Seat Miles (ASMs)
  - Extracts Revenue Passenger Miles (RPMs)
  - Extracts Load Factor, PRASM, RASM, CASM, CASM-ex
  - Extracts yield, departures, aircraft count
  - Populates `AirlineOperatingMetrics` model

- **Segment Information Parsing:**
  - Extracts revenue by geography (Domestic, Atlantic, Pacific, Latin)
  - Populates `SegmentInformation` model

### 3. Data Models (Already Existed)

These models were already in place and ready:
- `DetailedIncomeStatement` has `passengerRevenue`, `cargoRevenue` fields
- `AirlineOperatingMetrics` has full set of operating metrics fields
- `Company` model already supports `operatingMetrics` list

## Current Status: READY TO INTEGRATE

### ✅ Completed:
1. JSoup dependency added to pom.xml
2. AirlineHTMLParser fully implemented
3. Revenue breakdown parser
4. Operating statistics parser
5. Segment information parser
6. Number extraction with format handling (millions, percentages, etc.)
7. Year column detection in multi-year tables

### 🚧 In Progress:
- CLI integration (partial - needs completion)

### 📋 To Do:
1. **Complete CLI Integration:**
   - Add `--html` flag to `add` command
   - Modify `processXBRLFiles()` to optionally call HTML parser
   - Pass HTML files to parser for each fiscal year

2. **Testing:**
   - Test with actual UAL 10-K HTML file
   - Verify revenue extraction
   - Verify operating statistics extraction
   - Test JBLU for comparison

3. **Display Integration:**
   - Update comparison output to show passenger/cargo split
   - Add operating metrics comparison table
   - Add segment revenue comparison

## How to Use (Once CLI Integration Complete)

### Step 1: Download 10-K HTML Files

From SEC EDGAR:
```
https://www.sec.gov/cgi-bin/browse-edgar?action=getcompany&CIK=UAL&type=10-K
```

Download both:
- XBRL file: `ual-20241231.xml`
- HTML file: `ual-20241231_htm.xml` or `ual-20241231.htm`

### Step 2: Ingest with HTML Parsing

```bash
# With HTML parsing enabled (future)
finanalysis add "United Airlines" UAL -d path/to/files --html

# Current (XBRL only)
finanalysis add "United Airlines" UAL -d path/to/files -f xbrl
```

### Step 3: View Results

```bash
finanalysis compare UAL JBLU
```

**Expected Output After Integration:**
```
OPERATING REVENUE
Passenger Revenue      42,500    41,200    ...    (instead of "-")
Cargo Revenue           1,200     1,100    ...    (instead of "-")
Other Revenue           8,129     7,500    ...    (actual breakdown)
Total Operating Rev    51,829    49,800    ...

OPERATING STATISTICS (NEW TABLE)
ASMs (millions)       239,000   232,000   ...
RPMs (millions)       203,000   197,000   ...
Load Factor (%)          84.9%     84.9%   ...
PRASM (cents)           17.78     17.68   ...
RASM (cents)            21.69     21.39   ...
CASM (cents)            19.58     20.12   ...
CASM-ex (cents)         15.23     16.80   ...
```

## Technical Details

### HTML Table Detection

The parser uses keyword matching to identify relevant tables:

**Revenue Tables:**
- Must contain: "passenger revenue" AND "cargo revenue" AND "operating revenue"

**Operating Statistics Tables:**
- Must contain 3+ of: "available seat miles", "revenue passenger miles", "load factor", "rasm", "casm"

**Segment Tables:**
- Must contain geographic keywords: "domestic", "atlantic", "pacific", "latin"
- Plus: "revenue" or "operating income"

### Number Extraction

Handles multiple formats:
- Millions: `42,500` → `42500000000`
- Decimals: `17.78` → `17.78`
- Percentages: `84.9%` → `84.9`
- Negatives: `(1,234)` → `-1234`
- Dashes: `-` or `—` → `0`

### Year Column Detection

Searches table headers for year strings:
- Direct match: "2024"
- Pattern match: "12/31/2024"
- Position matching for multi-year tables

## Example HTML Structure Parsed

```html
<table>
  <tr>
    <th>Operating Revenue</th>
    <th>2024</th>
    <th>2023</th>
    <th>2022</th>
  </tr>
  <tr>
    <td>Passenger</td>
    <td>42,500</td>
    <td>41,200</td>
    <td>32,800</td>
  </tr>
  <tr>
    <td>Cargo</td>
    <td>1,200</td>
    <td>1,100</td>
    <td>900</td>
  </tr>
  <tr>
    <td>Other</td>
    <td>8,129</td>
    <td>6,700</td>
    <td>6,300</td>
  </tr>
</table>
```

## Next Steps for Complete Integration

### 1. Update CLI.java (15 min)

```java
// In add command parameters
@Option(names = {"--html"}, description = "Parse HTML for supplementary data")
boolean parseHTML

// In processXBRLFiles method
private int processXBRLFiles(Company company, File dir, String ticker,
                             DataStore dataStore, boolean parseHTML) {
    // ... existing XBRL parsing ...

    if (parseHTML) {
        parseHTMLSupplementaryData(company, dir, ticker);
    }

    // ... save company ...
}

// New method
private void parseHTMLSupplementaryData(Company company, File dir, String ticker) {
    // Find HTML files
    // For each HTML file:
    //   - Parse revenue breakdown
    //   - Parse operating statistics
    //   - Merge with existing DetailedIncomeStatement
    //   - Add AirlineOperatingMetrics
}
```

### 2. Add Operating Metrics to Comparison Display (30 min)

Create new comparison table section in CLI.java:
```java
printOperatingMetricsComparison(companies, yearsToShow);
```

### 3. Test with Real Files (1 hour)

```bash
mvn clean package
finanalysis add "United Airlines" UAL -d data/10k-html --html -f xbrl
finanalysis compare UAL JBLU
```

Verify:
- Passenger/Cargo revenue populated
- Operating metrics show in new table
- Numbers match 10-K filing

## Benefits of HTML Parsing

### Before (XBRL Only):
- Revenue breakdown: Missing (all show "-")
- Operating metrics: None
- Segment data: None
- **Analysis Quality:** Limited

### After (XBRL + HTML):
- Revenue breakdown: Complete ✓
- Operating metrics: Full suite (ASMs, RPMs, RASM, CASM) ✓
- Segment data: By geography ✓
- **Analysis Quality:** Industry-standard ✓

## Industry Context

**All major financial platforms parse HTML for airline metrics:**
- Bloomberg
- FactSet
- S&P Capital IQ
- Seeking Alpha

**Why:** SEC doesn't require XBRL tagging for operating statistics. Airlines provide them in HTML tables only.

**Our implementation** brings this capability to the open-source financial analysis tool.

## Files Modified/Created in Phase 3

| File | Status | Purpose |
|------|--------|---------|
| `pom.xml` | ✅ Modified | Added JSoup dependency |
| `AirlineHTMLParser.java` | ✅ Created | HTML table parser implementation |
| `HTML_PARSER_GUIDE.md` | ✅ Created | This documentation |
| `CLI.java` | 🚧 Partial | Needs --html flag integration |
| Comparison display | 📋 To Do | Add operating metrics table |

## Estimated Completion Time

- CLI integration: 30 minutes
- Display enhancement: 1 hour
- Testing & debugging: 2 hours
- **Total: 3-4 hours to production-ready**

## Contact & Support

For questions about HTML parsing implementation:
1. Review `AirlineHTMLParser.java` source code
2. Check logs for "Parsing revenue breakdown" and "Parsing operating statistics"
3. Inspect HTML file structure if extraction fails

## Version History

- **v1.0** (Current): HTML parser infrastructure complete, CLI integration pending
- **v1.1** (Next): Full CLI integration with --html flag
- **v2.0** (Future): Automatic HTML fetching from SEC EDGAR API
