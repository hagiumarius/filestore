package com.unitedinternet.filestore.controllers;

import com.unitedinternet.filestore.exceptions.GenericException;
import com.unitedinternet.filestore.model.File;
import com.unitedinternet.filestore.repository.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileStorageAdminController {

    Logger logger = LoggerFactory.getLogger(FileStorageAdminController.class);

    private final FileRepository fileRepository;

    public FileStorageAdminController(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @GetMapping()
    public ResponseEntity<List<String>> getFile(@RequestParam(value = "regex") String regex) {

        String decodedRegex = null;
        try {
            decodedRegex = URLDecoder.decode(regex, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new GenericException("Unparseable regex");
        }
        logger.info("trying to find files by regex {}", decodedRegex);
        List<File> foundList = fileRepository.findAllFilesMatchingRegex(decodedRegex);
        List<String> foundNames = foundList.stream().map(e -> e.getName()).toList();
        return new ResponseEntity<>(foundNames, HttpStatus.OK);

    }

}
