package com.unitedinternet.filestore.service;

import com.unitedinternet.filestore.exceptions.GenericException;
import com.unitedinternet.filestore.exceptions.RecordNotFoundException;
import com.unitedinternet.filestore.model.AccessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileStorageResolver {

    Logger logger = LoggerFactory.getLogger(FileStorageResolver.class);

    @Value("${upload.defaultPath}")
    private String defaultSystemPath;

    @Value("${upload.infrequentAccessPath}")
    private String infrequentAccessSystemPath;

    public void storeFile (String fullPath, MultipartFile requestFile) throws IOException {
        logger.debug("Storing file {}", fullPath);
        requestFile.transferTo(new java.io.File(defaultSystemPath + fullPath.replace("/","+")));
    }

    public Resource retrieveFile (String fullPath, AccessType accessType) {
        logger.debug("Retrieving file {}", fullPath);
        Path rootPath = null;
        if (accessType.equals(AccessType.DEFAULT)) {
            rootPath = Paths.get(defaultSystemPath);
        } else if (accessType.equals(AccessType.INFREQUENT)) {
            rootPath = Paths.get(infrequentAccessSystemPath);
        }
        Path filePath = rootPath.resolve(fullPath.replace("/","+")).normalize();
        Resource resource = null;
        try {
            resource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            logger.error("Exception while loading file", e);
            throw new GenericException("Exception while loading file");
        }
        if (!resource.exists() || !resource.isReadable()) {
            throw new RecordNotFoundException("File not found");
        } else {
            return resource;
        }

    }

}
