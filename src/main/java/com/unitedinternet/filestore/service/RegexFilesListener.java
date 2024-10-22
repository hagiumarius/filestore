package com.unitedinternet.filestore.service;

import com.unitedinternet.filestore.config.InitializingConfig;
import com.unitedinternet.filestore.controllers.FileOperation;
import com.unitedinternet.filestore.controllers.FileOperationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Class used to process member events (create, update)
 */

@Component
public class RegexFilesListener {

    private static final Logger logger = LoggerFactory.getLogger(RegexFilesListener.class);

    private final CachingService cachingService;

    @Autowired
    public RegexFilesListener(CachingService cachingService) {
        this.cachingService = cachingService;
    }

    @EventListener
    public void handleFileOperationEvent(FileOperationEvent foe) {
        logger.info("Processing a file operation event of type: {}", foe.getFileOperation());
        /*Added to overcome various inconsistent errors(e.g ERR Protocol error: invalid bulk length) with working with the local redis cache
        Probably related to the unorthodox way it was installed on local machine(https://github.com/MicrosoftArchive/redis/releases)
        */
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (cachingService.exists(InitializingConfig.REGEX_LIST)) {
            Set<String> regexSet = cachingService.getElementsFromSet(InitializingConfig.REGEX_LIST);
            if (!regexSet.isEmpty()) {
                if (foe.getFileOperation().equals(FileOperation.CREATED)) {
                    regexSet.stream().filter(regex -> foe.getFilePath().matches("(?i)" + regex)).forEach(regex -> {
                        logger.info("Adding: {} to {}", foe.getFilePath(), regex);
                        cachingService.addToList(regex, foe.getFilePath());});
                } else if (foe.getFileOperation().equals(FileOperation.DELETED)) {
                    regexSet.stream().filter(regex -> foe.getFilePath().matches("(?i)" + regex)).forEach(regex -> {
                        logger.info("Deleting {} from {}", foe.getFilePath(), regex);
                        cachingService.removeFromList(regex, foe.getFilePath());});
                }
            }
        }
    }
}