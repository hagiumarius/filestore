package com.unitedinternet.filestore.exceptions;

/**
 * Exception to be thrown when an error happens while validating client input
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String msg) {
        super(msg);
    }

}
