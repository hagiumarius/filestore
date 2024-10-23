package com.unitedinternet.filestore.service;

import com.unitedinternet.filestore.config.InitializingConfig;
import com.unitedinternet.filestore.controllers.FileOperation;
import com.unitedinternet.filestore.controllers.FileOperationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Class used to process member events (create, update) in relation to regex file finder
 */
@Component
public class RegexFilesListener {

    private static final Logger logger = LoggerFactory.getLogger(RegexFilesListener.class);

    private final CachingService cachingService;

    @Autowired
    public RegexFilesListener(CachingService cachingService) {
        this.cachingService = cachingService;
    }

    /**
     * If a file is added and matches an already registered(in cache) regex
     * it will be added to the matching files of that regex, no db query will be needed for that regex
     * Of course, if the file is deleted, it will also be removed from the cached regex file list
     * This way db queries are needed only for new regexes
     * @param foe
     */
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
                    /**
                     * Be careful as we have 2 ways of validating regex matchings,
                     * one in rdbms layer(see the below native query)
                     * and another one in the service layer(@link com.unitedinternet.filestore.repository.FileRepository#findAllFilesMatchingRegex())
                     * They both have to match in applying regex specifications
                     */
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