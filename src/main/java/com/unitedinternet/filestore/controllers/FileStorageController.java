package com.unitedinternet.filestore.controllers;

import com.unitedinternet.filestore.exceptions.ValidationException;
import com.unitedinternet.filestore.model.File;
import com.unitedinternet.filestore.repository.FileRepository;

import com.unitedinternet.filestore.service.FileStorageResolver;
import jakarta.validation.constraints.NotEmpty;
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
import java.util.Arrays;

@RestController
@RequestMapping("/files")
public class FileStorageController {

    Logger logger = LoggerFactory.getLogger(FileStorageController.class);

    private final String FILE_NAME_REGEX = "^[a-zA-Z0-9_-]*$";
    private final int MAX_PATH_SEGMENTS = 6;

    private final FileRepository fileRepository;

    private final FileStorageResolver fileStorageResolver;

    @Autowired
    public FileStorageController(FileRepository fileRepository, FileStorageResolver fileStorageResolver) {
        this.fileRepository = fileRepository;
        this.fileStorageResolver = fileStorageResolver;
    }

    @GetMapping(value="/test", produces="application/json", consumes="application/json")
    public ResponseEntity<String> getTest() {

        return new ResponseEntity<>("test", HttpStatus.OK);
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<FileStoreResponse> createFile(@RequestParam("file") MultipartFile requestFile, @RequestParam("path") @NotEmpty String path) {
        logger.info("Trying to upload for path: {}", path);
        validateUpload(path, requestFile);
        try {
            String fullPath = fileStorageResolver.storeFile(path, requestFile);
            File file = new File.Builder().path(path).name(requestFile.getOriginalFilename()).createdDate(LocalDateTime.now()).fullPath(fullPath).build();
            fileRepository.save(file);
            return ResponseEntity.status(HttpStatus.OK).body(new FileStoreResponse(HttpStatus.CREATED.value(),"File uploaded successfully: " + requestFile.getOriginalFilename()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new FileStoreResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Could not upload the file: " + e.getMessage()));
        }

    }

    private void validateUpload(String path, MultipartFile requestFile) {
        logger.info("Validating {} and {}", requestFile.getOriginalFilename().split("\\.")[0], path);
        if (requestFile.isEmpty()) {
            throw new ValidationException("Missing request file");
        } else if (!(requestFile.getOriginalFilename() != null && requestFile.getOriginalFilename().split("\\.")[0].matches(FILE_NAME_REGEX))) {
            throw new ValidationException("Wrong file name");
        } if (path.contains("/") && path.split("/").length > MAX_PATH_SEGMENTS) {
            throw new ValidationException("Path too long");
        }
        boolean wrongPathSegment = Arrays.stream(path.split("/")).anyMatch(e -> !e.matches(FILE_NAME_REGEX));
        if (wrongPathSegment) {
            throw new ValidationException("Wrong path segment");
        }
    }


}
