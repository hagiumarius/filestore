package com.unitedinternet.filestore.config;

import com.unitedinternet.filestore.service.CachingService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * Configuration to take care of any specific bootstrapping needed for the application.
 */
@Service
public class InitializingConfig {

    Logger logger = LoggerFactory.getLogger(InitializingConfig.class);

    public static final String FILES_COUNT = "filesCount";
    public static final String REGEX_LIST = "regexList";

    @Value("${redis.cleanCache: true}")
    private boolean cleanCache;

    CachingService cachingService;

    @Autowired
    public InitializingConfig(CachingService cachingService) {
        this.cachingService = cachingService;
    }

    /**
     * As we're using a single instance local variation of redis cache,
     * cleaning caches at the start of the application is a good idea
     */
    @PostConstruct
    public void cleanCache() {
        if (cleanCache) {
            logger.info("Flushing full cache");
            cachingService.cleanAll();
        }
    }

}
