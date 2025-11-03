#!/usr/bin/env python3
"""
Helper script to download XBRL files from SEC EDGAR
Usage: python3 download_xbrl.py <ticker> [--years <num_years>]
"""

import argparse
import sys
from pathlib import Path
from sec_edgar_downloader import Downloader

def download_xbrl(ticker, num_years=5, download_dir="data/xbrl"):
    """
    Download 10-K XBRL files for a given ticker

    Args:
        ticker: Company ticker symbol (e.g., 'AAPL', 'UAL')
        num_years: Number of years to download (default: 5)
        download_dir: Directory to save files (default: 'data/xbrl')
    """
    print(f"Downloading XBRL files for {ticker}...")
    print(f"Number of filings to download: {num_years}")
    print(f"Download directory: {download_dir}")
    print()

    # Create downloader instance
    # Note: SEC requires a user agent with your email
    # Format: Company Name your-email@domain.com
    dl = Downloader("FinancialAnalysisCLI", "user@example.com", download_dir)

    try:
        # Download 10-K filings
        # This will create a directory structure: download_dir/sec-edgar-filings/{ticker}/10-K/
        num_downloaded = dl.get("10-K", ticker, limit=num_years)

        print(f"\n✓ Successfully downloaded {num_downloaded} 10-K filing(s) for {ticker}")
        print(f"\nFiles saved to: {download_dir}/sec-edgar-filings/{ticker}/10-K/")
        print("\nEach filing directory contains:")
        print("  - full-submission.txt (complete filing)")
        print("  - Financial data files (*.xml, *.xsd)")
        print("  - HTML rendering files")

        # Show what was downloaded
        ticker_dir = Path(download_dir) / "sec-edgar-filings" / ticker / "10-K"
        if ticker_dir.exists():
            print(f"\nDownloaded filings:")
            for filing_dir in sorted(ticker_dir.iterdir()):
                if filing_dir.is_dir():
                    xml_files = list(filing_dir.glob("*.xml"))
                    print(f"  - {filing_dir.name} ({len(xml_files)} XML files)")

        return True

    except Exception as e:
        print(f"\n✗ Error downloading filings: {e}", file=sys.stderr)
        return False

def main():
    parser = argparse.ArgumentParser(
        description="Download XBRL files from SEC EDGAR for financial analysis"
    )
    parser.add_argument("ticker", help="Company ticker symbol (e.g., AAPL, UAL)")
    parser.add_argument(
        "--years",
        type=int,
        default=5,
        help="Number of years to download (default: 5)"
    )
    parser.add_argument(
        "--dir",
        default="data/xbrl",
        help="Download directory (default: data/xbrl)"
    )

    args = parser.parse_args()

    success = download_xbrl(args.ticker.upper(), args.years, args.dir)
    sys.exit(0 if success else 1)

if __name__ == "__main__":
    main()
