package com.unitedinternet.filestore.model;

/**
 * Enum to present the 2 access type that are relevant to the file storage.
 * DEFAULT will signal a file present in a default(low latency disk) location, while
 * INFREQUENT will signal a file present in a higher latency location
 */
public enum AccessType {
    DEFAULT("Default"), INFREQUENT("Infrequent");
    final String name;
    AccessType(String name) {
        this.name = name;
    }
}
