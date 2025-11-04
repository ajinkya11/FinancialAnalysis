package com.financialanalysis.exceptions;

/**
 * Exception thrown when an invalid ticker symbol is provided
 */
public class InvalidTickerException extends RuntimeException {
    public InvalidTickerException(String ticker) {
        super("Invalid or not found ticker symbol: " + ticker);
    }

    public InvalidTickerException(String ticker, Throwable cause) {
        super("Invalid or not found ticker symbol: " + ticker, cause);
    }
}
