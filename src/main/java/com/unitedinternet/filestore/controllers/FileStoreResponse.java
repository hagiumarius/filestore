package com.unitedinternet.filestore.controllers;

public class FileStoreResponse {
    private final int statusCode;

    private final String message;

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
}
