package com.unitedinternet.filestore.controllers;

import com.unitedinternet.filestore.model.File;
import com.unitedinternet.filestore.repository.FileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/files")
public class FileStorageController {

    Logger logger = LoggerFactory.getLogger(FileStorageController.class);

    @Value("${upload.path}")
    private String systemPath;

    private FileRepository fileRepository;

    @Autowired
    public FileStorageController(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @GetMapping(value="/test", produces="application/json", consumes="application/json")
    public ResponseEntity<String> getTest() {

        return new ResponseEntity<>("test", HttpStatus.OK);
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<String> createFile(@RequestParam("file") MultipartFile requestFile, @RequestParam("path") String path) {
        logger.info("Trying to upload for path: {}", path);
        try {
            requestFile.transferTo(new java.io.File(systemPath + requestFile.getOriginalFilename()));
            File file = new File.Builder().path(path).name(requestFile.getOriginalFilename()).createdDate(LocalDateTime.now()).systemPath(systemPath).build();
            fileRepository.save(file);
            return ResponseEntity.status(HttpStatus.OK).body("File uploaded successfully: " + requestFile.getOriginalFilename());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not upload the file: " + e.getMessage());
        }

    }

}
