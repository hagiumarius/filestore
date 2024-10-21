package com.unitedinternet.filestore.controllers;

import com.unitedinternet.filestore.model.File;
import org.springframework.context.ApplicationEvent;

public class FileOperationEvent extends ApplicationEvent {

    private final String filePath;

    private final FileOperation fileOperation;

    public FileOperationEvent(Object source, String filePath, FileOperation operation) {
        super(source);
        this.filePath = filePath;
        this.fileOperation = operation;
    }

    public String getFilePath() {
        return filePath;
    }

    public FileOperation getFileOperation() {
        return fileOperation;
    }
}
