package com.unitedinternet.filestore.exceptions;

/**
 * Exception to be thrown when an unchecked exception happens at service level
 */
public class GenericException extends RuntimeException {
    public GenericException(String message) {
        super(message);
    }
}
