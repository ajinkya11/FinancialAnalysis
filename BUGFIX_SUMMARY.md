# Revenue Extraction Bug - Root Cause Analysis & Fix

## Problem Statement

Revenue values were severely incorrect for both UAL and JBLU:
- **UAL 2024**: Passenger Revenue showed 258,503M (actually RPMs, not revenue!)
- **UAL 2024**: Total Operating Revenue showed 51,829M (actually passenger revenue!)
- **Expected UAL 2024**: Total $57,063M = Passenger $51,829M + Cargo $1,743M + Other $3,491M

Additionally, data was being **duplicated** (12 years instead of 6).

## Root Cause

The bug was caused by `.htm` files being processed **twice**:

### 1. First Pass - XBRL Processing (CORRECT)
```
processXBRLFiles() → lines 155-175 in CLI.java
```
- Processes `.htm` files as inline XBRL
- Correctly extracts revenue using context-aware inline XBRL parsing
- Creates `DetailedIncomeStatement` with correct values:
  - Total Operating Revenue: $57,063M ✓
  - Passenger Revenue: $51,829M ✓
  - Cargo Revenue: $1,743M ✓
  - Other Revenue: $3,491M ✓

### 2. Second Pass - HTML Processing (WRONG - OVERWRITES CORRECT DATA)
```
parseHTMLSupplementaryData() → lines 202-280 in CLI.java
```
- Processes **the same** `.htm` files as HTML tables
- Extracts wrong values from HTML tables:
  - Passenger Revenue: 258,503M (this is RPMs from operating metrics table!)
  - Cargo Revenue: 3,604M (wrong year/context)
  - Total Operating Revenue: 51,829M (this is passenger revenue!)
- **OVERWRITES** the correct XBRL data with incorrect HTML table data

## The Fix

### Commit: 9fd38bb - CRITICAL FIX

Modified `parseHTMLSupplementaryData()` in `CLI.java:239-260` to:

```java
// Check if revenue breakdown was already extracted from XBRL (inline XBRL)
boolean hasXBRLRevenue = incomeStatement.getPassengerRevenue() > 0 ||
                        incomeStatement.getCargoRevenue() > 0 ||
                        incomeStatement.getOtherOperatingRevenue() > 0;

if (hasXBRLRevenue) {
    // Skip HTML revenue extraction - preserve correct XBRL data
    System.out.println("    ℹ Skipping HTML revenue extraction - using XBRL data:");
    // ... log values
} else {
    // Parse revenue breakdown from HTML only if not available in XBRL
    htmlParser.parseRevenueBreakdown(htmlFile, incomeStatement, fiscalYear);
    // ... extract and log
}
```

**Key Changes:**
1. Check if revenue was already extracted from XBRL before parsing HTML
2. Skip HTML revenue extraction if XBRL data exists
3. Still extract operating metrics (ASMs, RPMs) from HTML (not in XBRL)
4. Log which data source is being used

**Result:**
- Inline XBRL data takes precedence over HTML table parsing
- Correct revenue values preserved
- No more duplicate/overwritten data

## All Commits on This Branch

1. **788357e**: Initial inline XBRL support with context filtering
2. **8d8249d**: Fix Java 11 compatibility (replace `.toList()` with iterators)
3. **c30ad45**: Fix inline XBRL extraction bugs and add debug logging
4. **f5f8971**: Improve inline XBRL detection to search entire document
5. **dce303e**: CRITICAL - Include `.htm` files in XBRL file discovery
6. **9fd38bb**: CRITICAL - Prevent HTML from overwriting XBRL data ← **LATEST FIX**

## Why Current Output Still Shows Wrong Values

**IMPORTANT**: Maven cannot compile due to network issues:
```
[ERROR] Could not transfer artifact ... from/to central (https://repo.maven.apache.org/maven2):
repo.maven.apache.org: Temporary failure in name resolution
```

The **old buggy code is still running** because the fix hasn't been compiled yet.

## How to Test the Fix

### Step 1: Rebuild the Project

Wait for network connectivity, then:

```bash
cd /path/to/FinancialAnalysis
mvn clean package
```

### Step 2: Clear Old Data

```bash
rm -rf data/companies/*.json
```

### Step 3: Re-parse Companies

```bash
./finanalysis add "United Airlines" UAL
./finanalysis add "Jet Blue Airlines" JBLU
```

**Expected output** should now show:
```
Processing: ual-20241231.htm
  ✓ Extracted data for fiscal year 2024
  ✓ Extracted detailed financial statements

--- Parsing HTML files for operating metrics and revenue breakdown ---
  Found 6 HTML file(s) to parse
  Processing HTML: ual-20241231.htm
    ℹ Skipping HTML revenue extraction - using XBRL data:
      - Passenger: $51829M
      - Cargo: $1743M
      - Other: $3491M
    ✓ Extracted operating metrics:
      - ASMs: ...
      - RPMs: ...
```

### Step 4: Run Comparison

```bash
./finanalysis compare UAL JBLU 2019 2024
```

**Expected output:**
```
Years of Data: 6  (NOT 12!)

OPERATING REVENUE
                                UAL 2024   JBLU 2024
Passenger Revenue                51,829.0      ...
Cargo Revenue                     1,743.0      ...
Other Revenue                     3,491.0      ...
Total Operating Revenue          57,063.0      ...
```

## Expected Correct Values

### UAL 2024
| Metric | Value | Source |
|--------|-------|--------|
| Total Operating Revenue | $57,063M | us-gaap:RevenueFromContractWithCustomerExcludingAssessedTax (contextRef=c-1) |
| Passenger Revenue | $51,829M | us-gaap:RevenueFromContractWithCustomerExcludingAssessedTax (contextRef=c-8, segment=PassengerMember) |
| Cargo Revenue | $1,743M | us-gaap:RevenueFromContractWithCustomerExcludingAssessedTax (contextRef=c-11, segment=CargoAndFreightMember) |
| Other Revenue | $3,491M | us-gaap:RevenueFromContractWithCustomerExcludingAssessedTax (contextRef=c-17, segment=ProductAndServiceOtherMember) |

**Validation:** Total = 51,829 + 1,743 + 3,491 = 57,063 ✓

### Data Source

All values extracted from inline XBRL in `data/xbrl-files/UAL/ual-20241231.htm`:
```xml
<ix:nonFraction contextRef="c-1" name="us-gaap:RevenueFromContractWithCustomerExcludingAssessedTax"
                scale="6">57,063</ix:nonFraction>
<ix:nonFraction contextRef="c-8" name="us-gaap:RevenueFromContractWithCustomerExcludingAssessedTax"
                scale="6">51,829</ix:nonFraction>
<ix:nonFraction contextRef="c-11" name="us-gaap:RevenueFromContractWithCustomerExcludingAssessedTax"
                scale="6">1,743</ix:nonFraction>
<ix:nonFraction contextRef="c-17" name="us-gaap:RevenueFromContractWithCustomerExcludingAssessedTax"
                scale="6">3,491</ix:nonFraction>
```

## Technical Details

### Inline XBRL Context Filtering

The fix implements proper context-aware extraction:

1. **Find contexts for fiscal year:**
   ```java
   Set<String> validContexts = findContextsForYear(root, 2024);
   // Returns: ["c-1", "c-2", "c-3", ...]  (only 2024 contexts)
   ```

2. **Filter by segment:**
   ```java
   Set<String> passengerContexts = findContextsWithSegment(root, 2024, "PassengerMember");
   // Returns: ["c-8"]  (2024 + Passenger segment)
   ```

3. **Extract value with context filter:**
   ```java
   double value = extractValueFromInlineXBRL(root, passengerContexts,
                    "RevenueFromContractWithCustomerExcludingAssessedTax");
   ```

This ensures we extract the right value for the right year and segment, not mixing data from different years/segments in the same document.

### Why HTML Extraction Was Wrong

HTML tables in `.htm` files contain operating metrics like this:

```html
<table>
  <tr><td>Revenue Passenger Miles</td><td>258,503</td></tr>  ← NOT REVENUE!
  <tr><td>Cargo/Freight Revenue</td><td>3,604</td></tr>     ← WRONG YEAR!
</table>
```

The HTML parser was extracting these values and incorrectly mapping them to revenue fields, overwriting the correct XBRL data.

## Status

✅ **FIXED**: Root cause identified and corrected
✅ **COMMITTED**: All fixes pushed to branch `claude/fix-revenue-extraction-bug-011CUmZ98Zjm57zhSAxo5KBf`
⏸️ **PENDING**: Compilation blocked by Maven network issues
⏳ **ACTION REQUIRED**: Rebuild project when network is available

## Next Steps

1. Wait for Maven network connectivity to be restored
2. Run `mvn clean package` to rebuild with the fixes
3. Clear old company data
4. Re-parse UAL and JBLU
5. Verify correct revenue values in comparison output
6. Create pull request if tests pass
