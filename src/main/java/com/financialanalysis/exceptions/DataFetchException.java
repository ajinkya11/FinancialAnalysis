package com.financialanalysis.exceptions;

/**
 * Exception thrown when there's an error fetching data from SEC EDGAR
 */
public class DataFetchException extends RuntimeException {
    public DataFetchException(String message) {
        super(message);
    }

    public DataFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
