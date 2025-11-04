package com.financialanalysis.exceptions;

/**
 * Exception thrown when there's an error parsing XBRL or other financial documents
 */
public class ParsingException extends RuntimeException {
    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
