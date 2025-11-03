# Critical Data Quality Fixes Required

## Summary of Issues Found

### 1. **Free Cash Flow Calculation ERROR**
**Current:** FCF = OCF (showing same values)
**Root Cause:** CapEx stored as negative, then subtracted → adds instead of subtracts
**Fix:** Store CapEx as positive, subtract from OCF

### 2. **Revenue Breakdown Backwards**
**Current:** "Other Revenue" = Total Revenue, Passenger/Cargo = 0
**Root Cause:** Passenger/Cargo tags not being extracted from XBRL
**Fix:** Need to inspect actual XBRL tags, may need company-specific extraction

### 3. **Depreciation Too Low**
**Current:** UAL shows $155M on $52.7B fleet (0.3% rate)
**Expected:** Should be $2.1-2.6B (4-5% rate)
**Root Cause:** Only extracting one depreciation component, missing aircraft depreciation
**Fix:** Search for separate aircraft/flight equipment depreciation tags

### 4. **Operating Expenses Don't Add Up**
**Current:** Sum of line items = $32.5B, Total = $51.9B, Missing $19.4B
**Root Cause:** Not extracting all expense categories
**Fix:** Add missing expense categories (regional capacity, landing fees, other opex)

### 5. **Total Liabilities Missing for UAL**
**Current:** Shows "-" even after accounting equation fix
**Root Cause:** Tag not found AND calculation not being applied
**Status:** Should be fixed by recent changes, verify after re-ingestion

### 6. **Capital Expenditures Not Showing**
**Current:** Shows "-" in detailed cash flow table
**Root Cause:** CapEx extraction failing in detailed parser
**Fix:** Add comprehensive CapEx tag search

## Recommended Investigation Steps

### Step 1: Inspect Actual XBRL Files
```bash
# Check what tags exist for key missing data
finanalysis inspect-xbrl data/10k-pdfs/ual-20241231.xml -f PassengerRevenue
finanalysis inspect-xbrl data/10k-pdfs/ual-20241231.xml -f Depreciation
finanalysis inspect-xbrl data/10k-pdfs/ual-20241231.xml -f OperatingExpense
finanalysis inspect-xbrl data/10k-pdfs/ual-20241231.xml -f CapitalExpenditure
```

### Step 2: Compare with 10-K PDF/HTML
- Revenue breakdown is typically on face of income statement
- Operating expenses fully detailed on income statement
- Depreciation in cash flow and/or footnotes
- CapEx always in cash flow statement

### Step 3: Consider Alternative Data Sources
For missing XBRL tags, may need to:
- Parse HTML/PDF tables directly
- Use SEC API for structured data
- Extract from footnotes/MD&A sections

## Priority Fixes to Implement

### Priority 1 (Calculation Errors - Code Fixes)
1. Fix FCF calculation sign issue
2. Fix depreciation aggregation
3. Add all operating expense categories

### Priority 2 (Missing Data - Tag Expansion)
4. Add comprehensive depreciation tags
5. Add CapEx tag variations (already done, verify)
6. Add interest expense tags
7. Add operating expense breakout tags

### Priority 3 (Data Validation)
8. Add data validation checks
9. Add reconciliation warnings (e.g., "expenses don't sum to total")
10. Log which namespace/tag provided each value

### Priority 4 (Operating Metrics)
11. Parse operating statistics tables (may require HTML/PDF parsing)
12. Extract ASM, RPM, RASM, CASM from tables
13. Extract fleet count and composition

## Notes on Airline-Specific Metrics

Operating statistics (ASMs, RPMs, etc.) are typically NOT in XBRL tags. They appear in:
- HTML tables in 10-K filing
- "Statistical Supplement" section
- May need separate table parser for this data
