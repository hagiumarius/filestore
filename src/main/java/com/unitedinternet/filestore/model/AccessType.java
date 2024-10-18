package com.unitedinternet.filestore.model;

public enum AccessType {
    DEFAULT("Default"), INFREQUENT("Infrequent");
    final String name;
    AccessType(String name) {
        this.name = name;
    }
}
