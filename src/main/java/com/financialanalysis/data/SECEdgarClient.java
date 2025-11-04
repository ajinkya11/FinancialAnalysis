package com.financialanalysis.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financialanalysis.exceptions.DataFetchException;
import com.financialanalysis.exceptions.InvalidTickerException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Client for interacting with SEC EDGAR API
 */
@Slf4j
@Service
public class SECEdgarClient {
    private static final String SEC_API_BASE = "https://data.sec.gov";
    private static final String EDGAR_BROWSE_BASE = "https://www.sec.gov/cgi-bin/browse-edgar";
    private static final String USER_AGENT = "FinancialAnalyzer/1.0 (Educational Purpose)";

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SECEdgarClient() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get CIK number for a given ticker symbol
     */
    public String getCIKFromTicker(String ticker) {
        log.info("Fetching CIK for ticker: {}", ticker);

        try {
            String url = SEC_API_BASE + "/submissions/CIK" + String.format("%010d", getTickerCIK(ticker)) + ".json";
            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .build();

            try (Response response = executeWithRetry(request)) {
                if (!response.isSuccessful()) {
                    throw new InvalidTickerException(ticker);
                }

                JsonNode json = objectMapper.readTree(response.body().string());
                return json.get("cik").asText();
            }
        } catch (IOException e) {
            log.error("Error fetching CIK for ticker: {}", ticker, e);
            throw new DataFetchException("Failed to fetch CIK for ticker: " + ticker, e);
        }
    }

    /**
     * Fetch company submissions data
     */
    public JsonNode getCompanySubmissions(String cik) {
        log.info("Fetching submissions for CIK: {}", cik);

        try {
            String paddedCIK = String.format("%010d", Long.parseLong(cik));
            String url = SEC_API_BASE + "/submissions/CIK" + paddedCIK + ".json";

            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", USER_AGENT)
                    .build();

            try (Response response = executeWithRetry(request)) {
                if (!response.isSuccessful()) {
                    throw new DataFetchException("Failed to fetch submissions for CIK: " + cik);
                }

                return objectMapper.readTree(response.body().string());
            }
        } catch (IOException e) {
            log.error("Error fetching submissions for CIK: {}", cik, e);
            throw new DataFetchException("Failed to fetch submissions for CIK: " + cik, e);
        }
    }

    /**
     * Get 10-K filing URLs for a given CIK
     */
    public List<FilingInfo> get10KFilings(String cik, int numberOfYears) {
        log.info("Fetching 10-K filings for CIK: {}, years: {}", cik, numberOfYears);

        try {
            JsonNode submissions = getCompanySubmissions(cik);
            JsonNode recentFilings = submissions.get("filings").get("recent");

            List<FilingInfo> filings = new ArrayList<>();
            JsonNode forms = recentFilings.get("form");
            JsonNode accessionNumbers = recentFilings.get("accessionNumber");
            JsonNode filingDates = recentFilings.get("filingDate");
            JsonNode primaryDocuments = recentFilings.get("primaryDocument");

            for (int i = 0; i < forms.size() && filings.size() < numberOfYears; i++) {
                String form = forms.get(i).asText();
                if ("10-K".equals(form)) {
                    String accessionNumber = accessionNumbers.get(i).asText().replace("-", "");
                    String filingDate = filingDates.get(i).asText();
                    String primaryDoc = primaryDocuments.get(i).asText();

                    String paddedCIK = String.format("%010d", Long.parseLong(cik));
                    String baseUrl = "https://www.sec.gov/Archives/edgar/data/" + cik + "/" + accessionNumber;

                    FilingInfo filing = FilingInfo.builder()
                            .cik(cik)
                            .accessionNumber(accessionNumbers.get(i).asText())
                            .filingDate(filingDate)
                            .primaryDocumentUrl(baseUrl + "/" + primaryDoc)
                            .xbrlUrl(getXBRLUrl(baseUrl, accessionNumber))
                            .build();

                    filings.add(filing);
                    log.info("Found 10-K filing: {}", filingDate);
                }
            }

            if (filings.isEmpty()) {
                throw new DataFetchException("No 10-K filings found for CIK: " + cik);
            }

            return filings;
        } catch (Exception e) {
            log.error("Error fetching 10-K filings for CIK: {}", cik, e);
            throw new DataFetchException("Failed to fetch 10-K filings for CIK: " + cik, e);
        }
    }

    /**
     * Download XBRL file content
     */
    public String downloadXBRLFile(String xbrlUrl) {
        log.info("Downloading XBRL file: {}", xbrlUrl);

        Request request = new Request.Builder()
                .url(xbrlUrl)
                .header("User-Agent", USER_AGENT)
                .build();

        try (Response response = executeWithRetry(request)) {
            if (!response.isSuccessful()) {
                throw new DataFetchException("Failed to download XBRL file: " + xbrlUrl);
            }

            String content = response.body().string();
            log.info("Successfully downloaded XBRL file, size: {} bytes", content.length());
            return content;
        } catch (IOException e) {
            log.error("Error downloading XBRL file: {}", xbrlUrl, e);
            throw new DataFetchException("Failed to download XBRL file: " + xbrlUrl, e);
        }
    }

    /**
     * Execute HTTP request with retry logic
     */
    private Response executeWithRetry(Request request) throws IOException {
        int maxRetries = 4;
        int retryDelay = 2000; // Start with 2 seconds

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                Response response = httpClient.newCall(request).execute();
                if (response.isSuccessful() || response.code() == 404) {
                    return response;
                }

                // If rate limited (429) or server error (5xx), retry
                if (response.code() == 429 || response.code() >= 500) {
                    log.warn("Request failed with code {}, attempt {}/{}",
                            response.code(), attempt + 1, maxRetries + 1);
                    response.close();

                    if (attempt < maxRetries) {
                        Thread.sleep(retryDelay);
                        retryDelay *= 2; // Exponential backoff
                        continue;
                    }
                }

                return response;
            } catch (IOException e) {
                if (attempt < maxRetries) {
                    log.warn("Request failed, attempt {}/{}: {}",
                            attempt + 1, maxRetries + 1, e.getMessage());
                    try {
                        Thread.sleep(retryDelay);
                        retryDelay *= 2;
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted during retry", ie);
                    }
                } else {
                    throw e;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted during retry", e);
            }
        }

        throw new IOException("Failed after " + (maxRetries + 1) + " attempts");
    }

    /**
     * Construct XBRL URL from base URL
     */
    private String getXBRLUrl(String baseUrl, String accessionNumber) {
        // XBRL files typically end with _htm.xml
        return baseUrl + "/" + accessionNumber + "_htm.xml";
    }

    /**
     * Get approximate CIK from ticker (simplified - in production use ticker-to-CIK mapping)
     */
    private int getTickerCIK(String ticker) {
        // This is a placeholder. In production, maintain a ticker-to-CIK mapping
        // For common airlines:
        switch (ticker.toUpperCase()) {
            case "UAL":
                return 100517;
            case "JBLU":
                return 1158463;
            case "DAL":
                return 27904;
            case "AAL":
                return 6201;
            case "LUV":
                return 92380;
            case "ALK":
                return 766421;
            default:
                throw new InvalidTickerException(ticker);
        }
    }

    /**
     * Filing information
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FilingInfo {
        private String cik;
        private String accessionNumber;
        private String filingDate;
        private String primaryDocumentUrl;
        private String xbrlUrl;
    }
}
