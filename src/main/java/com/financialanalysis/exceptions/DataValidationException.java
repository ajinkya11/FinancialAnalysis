package com.financialanalysis.exceptions;

/**
 * Exception thrown when financial data fails validation checks
 */
public class DataValidationException extends RuntimeException {
    public DataValidationException(String message) {
        super(message);
    }

    public DataValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
