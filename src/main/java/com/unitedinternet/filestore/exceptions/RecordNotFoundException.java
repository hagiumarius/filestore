package com.unitedinternet.filestore.exceptions;

/**
 * Exception to be thrown when a record is not found in db
 */
public class RecordNotFoundException extends RuntimeException {
    public RecordNotFoundException(String message) {
        super(message);
    }
}
