package com.unitedinternet.filestore.controllers;

import java.util.Map;

public class FileStoreResponse {

    /**
    Similar to http status code
     */
    private final int statusCode;

    /**
     * Relevant for error messages as for successful messages
     */
    private final String message;

    /**
     * Could contain an id for example
     */
    private Map<String, String> details = null;

    public FileStoreResponse(int statusCode, String message, Map<String, String> details) {
        this.statusCode = statusCode;
        this.message = message;
        this.details = details;
    }

    public FileStoreResponse(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
