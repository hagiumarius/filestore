package com.unitedinternet.filestore.controllers;

import com.unitedinternet.filestore.exceptions.GenericException;
import com.unitedinternet.filestore.exceptions.RecordNotFoundException;
import com.unitedinternet.filestore.exceptions.ValidationException;
import com.unitedinternet.filestore.model.AccessType;
import com.unitedinternet.filestore.model.File;
import com.unitedinternet.filestore.repository.FileRepository;
import com.unitedinternet.filestore.service.FileStorageResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @GetMapping(value="/**")
    public ResponseEntity<Resource> getFile(HttpServletRequest request) {
        logger.info("downloading file");
        String urlPath = request.getRequestURL().toString().split("/files")[1];
        String fullPath = resolveFullPath(urlPath);
        List<File> files = fileRepository.findByFullPath(fullPath);
        Resource resource = null;
        if (files.isEmpty()) {//file not found
            throw new RecordNotFoundException("Record not found");
        } else {
            File found = files.get(0);
            resource = fileStorageResolver.retrieveFile(fullPath, found.getAccessType());
        }
        return new ResponseEntity<>(resource, HttpStatus.OK);
    }

    @GetMapping(value="/{id}")
    public ResponseEntity<Resource> getFile(@PathVariable Long id) {
        logger.info("Trying to get file for id: {}", id);

        Optional<File> optionalFile = fileRepository.findById(id);
        if (optionalFile.isEmpty()) {
            throw new RecordNotFoundException("Record not found");
        } else {
            File found = optionalFile.get();
            Resource resource = fileStorageResolver.retrieveFile(found.getFullPath(), found.getAccessType());
            return new ResponseEntity<>(resource, HttpStatus.OK);
        }
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<FileStoreResponse> createFile(@RequestParam("file") MultipartFile requestFile, @RequestParam("path") @NotEmpty String path) {
        logger.info("Trying to upload for path: {}", path);
        validateUpload(path, requestFile);
        try {
            String fullPath = resolveFullPath(path, requestFile.getOriginalFilename());
            fileStorageResolver.storeFile(fullPath, requestFile);
            File file = new File.Builder().path(path).name(requestFile.getOriginalFilename()).createdDate(LocalDateTime.now()).fullPath(fullPath).accessType(AccessType.DEFAULT).build();
            file = fileRepository.save(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(new FileStoreResponse(HttpStatus.CREATED.value(),"File uploaded successfully: " + requestFile.getOriginalFilename(), Map.of("id", file.getFullPath().replace("+","/"))));
        } catch (IOException e) {
            logger.error("Exception while storing file",e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new FileStoreResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Could not upload the file: " + e.getMessage()));
        }

    }

    @PutMapping(value="/**", consumes = "multipart/form-data")
    public ResponseEntity<FileStoreResponse> updateFile(@RequestParam("file") MultipartFile requestFile, HttpServletRequest request) {
        String urlPath = request.getRequestURL().toString().split("/files")[1];
        String fullPath = resolveFullPath(urlPath);
        logger.info("Trying to update for id: {}", fullPath);
        try {
            List<File> files = fileRepository.findByFullPath(fullPath);
            if (files.isEmpty()) {
                throw new RecordNotFoundException("Record not found");
            } else {
                File found = files.get(0);
                if (!requestFile.getOriginalFilename().equals(found.getName())) {
                    throw new ValidationException("Update cannot change file name");
                }
                fileStorageResolver.storeFile(found.getFullPath(), requestFile);
                found.setUpdatedDate(LocalDateTime.now());
                fileRepository.save(found);
                return ResponseEntity.status(HttpStatus.OK).body(new FileStoreResponse(HttpStatus.OK.value(),"File uploaded successfully: " + requestFile.getOriginalFilename(),Map.of("id", found.getFullPath().replace("+","/")) ));
            }
        } catch (IOException e) {
            logger.error("Exception while storing file",e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new FileStoreResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Could not upload the file: " + e.getMessage()));
        }

    }

    @DeleteMapping(value="/**")
    public ResponseEntity<FileStoreResponse> deleteFile(HttpServletRequest request) {
        String urlPath = request.getRequestURL().toString().split("/files")[1];
        String fullPath = resolveFullPath(urlPath);
        logger.info("Trying to delete for id: {}", fullPath);

        List<File> files = fileRepository.findByFullPath(fullPath);
        if (files.isEmpty()) {
            throw new RecordNotFoundException("Record not found");
        } else {
            File found = files.get(0);
            fileRepository.deleteById(found.getId());
            return ResponseEntity.status(HttpStatus.OK).body(new FileStoreResponse(HttpStatus.OK.value(),"File deleted successfully",Map.of("id", found.getFullPath().replace("+","/")) ));
        }
    }

    private void validateUpload(String path, MultipartFile requestFile) {
        logger.info("Validating {} and {}", requestFile.getOriginalFilename().split("\\.")[0], path);
        if (requestFile.isEmpty()) {
            throw new ValidationException("Missing request file");
        } else if ((requestFile.getOriginalFilename() != null && (requestFile.getOriginalFilename().length() > 64 || !requestFile.getOriginalFilename().split("\\.")[0].matches(FILE_NAME_REGEX)))) {
            throw new ValidationException("Wrong file name");
        } if (path.contains("/") && path.split("/").length > MAX_PATH_SEGMENTS) {
            throw new ValidationException("Path too long");
        }
        boolean wrongPathSegment = Arrays.stream(path.split("/")).anyMatch(e -> !e.matches(FILE_NAME_REGEX));
        if (wrongPathSegment) {
            throw new ValidationException("Wrong path segment");
        }
    }

    private String resolveFullPath(String path, String fileName) {
        StringBuilder storagefileName = new StringBuilder();
        if (!path.startsWith("/")) {
            storagefileName.append("+");
        }
        storagefileName.append(path.replace("/","+"));
        if (!path.endsWith("/")) {
            storagefileName.append("+");
        }
        storagefileName.append(fileName);
        return storagefileName.toString();
    }

    private String resolveFullPath(String pathSegments) {
        return pathSegments.replace("/","+");
    }


}
