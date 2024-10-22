package com.unitedinternet.filestore.service;

import com.unitedinternet.filestore.config.InitializingConfig;
import com.unitedinternet.filestore.controllers.FileOperation;
import com.unitedinternet.filestore.controllers.FileOperationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Class used to process member events (create, update)
 */

@Component
public class FileCounterListener {

    private static final Logger logger = LoggerFactory.getLogger(FileCounterListener.class);

    private final CachingService cachingService;

    @Autowired
    public FileCounterListener(CachingService cachingService) {
        this.cachingService = cachingService;
    }

    @EventListener
    public void handleFileOperationEvent(FileOperationEvent foe) {
        logger.info("Processing a file operation event of type: {}", foe.getFileOperation());
        if (foe.getFileOperation().equals(FileOperation.CREATED)) {
            String filesCount = cachingService.getValue(InitializingConfig.FILES_COUNT);
            if (filesCount != null) {
                cachingService.updateKey(InitializingConfig.FILES_COUNT, String.valueOf(Long.parseLong(filesCount) + 1));
            }
        }
        if (foe.getFileOperation().equals(FileOperation.DELETED)) {
            String filesCount = cachingService.getValue(InitializingConfig.FILES_COUNT);
            if (filesCount != null) {
                cachingService.updateKey(InitializingConfig.FILES_COUNT, String.valueOf(Long.parseLong(filesCount) - 1));
            }
        }
    }
}