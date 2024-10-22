package com.unitedinternet.filestore.controllers;

import com.unitedinternet.filestore.config.InitializingConfig;
import com.unitedinternet.filestore.model.File;
import com.unitedinternet.filestore.repository.FileRepository;
import com.unitedinternet.filestore.service.CachingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/files")
public class FileStorageAdminController {

    Logger logger = LoggerFactory.getLogger(FileStorageAdminController.class);

    private final FileRepository fileRepository;

    private final CachingService cachingService;

    public FileStorageAdminController(FileRepository fileRepository, CachingService cachingService) {
        this.fileRepository = fileRepository;
        this.cachingService = cachingService;
    }

    @GetMapping()
    public ResponseEntity<List<String>> getFile(@RequestParam(value = "regex") String regex) {

        String decodedRegex = URLDecoder.decode(regex, StandardCharsets.UTF_8);
        List<String> foundPaths = null;
        logger.info("trying to find files by regex {}", decodedRegex);
        if (cachingService.exists(decodedRegex)) {
            foundPaths = cachingService.getElementsFromList(decodedRegex);
        } else {
            List<File> foundList = fileRepository.findAllFilesMatchingRegex(decodedRegex);
            foundPaths = foundList.stream().map(e -> e.getFullPath()).toList();
            foundPaths.stream().forEach(e -> cachingService.addToList(decodedRegex, e));
            cachingService.addToSet(InitializingConfig.REGEX_LIST, decodedRegex);
        }

        return new ResponseEntity<>(foundPaths, HttpStatus.OK);

    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getFileCount() {
        long filesCount;
        String filesCountCached = cachingService.getValue(InitializingConfig.FILES_COUNT);
        if (filesCountCached != null) {
            filesCount = Long.parseLong(filesCountCached);
        } else {
            filesCount = fileRepository.count();
            cachingService.setKeyValue(InitializingConfig.FILES_COUNT, String.valueOf(filesCount));
        }
        return new ResponseEntity<>(Map.of("filesCount", filesCount), HttpStatus.OK);

    }

}
