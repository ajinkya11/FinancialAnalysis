# How to Download XBRL Files from SEC EDGAR

XBRL (eXtensible Business Reporting Language) files are structured financial data files that are **much more reliable** than PDFs for automated parsing.

## Why XBRL?

- ✅ **Standardized format**: All companies use the same tags
- ✅ **Machine-readable**: No regex needed, just parse XML
- ✅ **High accuracy**: 95%+ vs 30-50% with PDFs
- ✅ **Free**: Available from SEC for all public companies
- ✅ **Required by SEC**: All 10-K filings since 2009 include XBRL

## Step-by-Step Download Instructions

### Method 1: Direct Link (FASTEST)

1. **Go to SEC EDGAR Company Search**:
   ```
   https://www.sec.gov/edgar/searchedgar/companysearch.html
   ```

2. **Search for your company** (e.g., "United Airlines" or "UAL")

3. **Click on the company name** to see all filings

4. **Filter by "10-K"** in the filing type dropdown

5. **Click "Documents" button** next to the filing you want

6. **Look for files ending in `.xml`** (usually multiple files):
   - Files with company ticker in name (e.g., `ual-20231231.xml`)
   - These are the XBRL instance documents
   - Download ALL `.xml` files

7. **Save to**:  `data/xbrl-files/<TICKER>/`

### Method 2: Direct URL Pattern (ADVANCED)

If you know the company's CIK number and accession number:

```
https://www.sec.gov/cgi-bin/viewer?action=view&cik=[CIK]&accession_number=[ACCESSION]&xbrl_type=v
```

## Example: Downloading United Airlines (UAL) XBRL Files

### Step-by-Step:

1. Go to: https://www.sec.gov/cgi-bin/browse-edgar?action=getcompany&CIK=UAL&type=10-K&owner=exclude

2. You'll see recent 10-K filings:
   ```
   Filing Date    Form   Documents
   2024-02-21     10-K   [Documents] [Interactive Data]
   2023-02-22     10-K   [Documents] [Interactive Data]
   2022-02-23     10-K   [Documents] [Interactive Data]
   ```

3. Click **"Documents"** for 2024 filing

4. You'll see a list of files like:
   ```
   ual-20231231.xml          (Main XBRL instance document)
   ual-20231231_cal.xml      (Calculation linkbase)
   ual-20231231_def.xml      (Definition linkbase)
   ual-20231231_lab.xml      (Label linkbase)
   ual-20231231_pre.xml      (Presentation linkbase)
   ```

5. **Download `ual-20231231.xml`** (the main instance document)
   - This contains all the financial data

6. Save it to: `data/xbrl-files/UAL/ual-20231231.xml`

7. Repeat for other years

## File Organization

Organize downloaded XBRL files like this:

```
data/xbrl-files/
├── UAL/
│   ├── ual-20211231.xml
│   ├── ual-20221231.xml
│   └── ual-20231231.xml
├── JBLU/
│   ├── jblu-20211231.xml
│   ├── jblu-20221231.xml
│   └── jblu-20231231.xml
└── AAPL/
    ├── aapl-20210925.xml
    ├── aapl-20220924.xml
    └── aapl-20230930.xml
```

## Finding Company CIK Numbers

If you need the CIK (Central Index Key) number:

- Search here: https://www.sec.gov/edgar/searchedgar/companysearch.html
- Or use this mapping file: https://www.sec.gov/files/company_tickers.json

Common examples:
- Apple (AAPL): 320193
- Microsoft (MSFT): 789019
- United Airlines (UAL): 100517
- JetBlue (JBLU): 1158463

## What's in an XBRL File?

XBRL files contain XML data like this:

```xml
<us-gaap:Revenues contextRef="FY2023" unitRef="USD" decimals="-6">
    53723000000
</us-gaap:Revenues>

<us-gaap:NetIncomeLoss contextRef="FY2023" unitRef="USD" decimals="-6">
    2091000000
</us-gaap:NetIncomeLoss>
```

Much easier to parse than PDFs!

## Tips

1. **Download main instance document only**: The `[ticker]-[date].xml` file contains all financial data
2. **Don't need linkbase files**: The `_cal.xml`, `_def.xml`, etc. are optional metadata
3. **Check fiscal year end date**: Companies have different fiscal year ends (Apple ends in September, most end in December)
4. **Download 3-5 years**: For trend analysis

## Next Steps

Once you have XBRL files downloaded, use the updated CLI:

```bash
# Add company from XBRL files
finanalysis add "United Airlines" UAL -i "Airlines" --format xbrl

# Or specify directory
finanalysis add "United Airlines" UAL -i "Airlines" --format xbrl -d data/xbrl-files/UAL
```

## Troubleshooting

**Q: Can't find XBRL files?**
A: Click "Documents" not "Interactive Data". Look for `.xml` files.

**Q: Too many XML files?**
A: You only need the main one: `[ticker]-[date].xml` (without `_cal`, `_def`, etc. suffixes)

**Q: Different fiscal years?**
A: Companies have different fiscal year ends. Apple ends 9/30, Microsoft 6/30, most others 12/31.

## Automated Download (Future)

We're working on adding automated XBRL download to the CLI. For now, manual download is fastest and most reliable.
