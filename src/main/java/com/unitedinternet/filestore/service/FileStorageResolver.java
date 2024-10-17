package com.unitedinternet.filestore.service;

import com.unitedinternet.filestore.controllers.FileStorageController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class FileStorageResolver {

    Logger logger = LoggerFactory.getLogger(FileStorageResolver.class);

    @Value("${upload.defaultPath}")
    private String defaultSystemPath;

    @Value("${upload.infrequentAccessPath}")
    private String infrequentAccessSystemPath;

    public String storeFile (String path, MultipartFile requestFile) throws IOException {
        String fullPath = defaultSystemPath + resolveFileName(path, requestFile.getOriginalFilename());
        logger.info("Storing file " + requestFile.getOriginalFilename() +" of path " + path + " in " + fullPath);
        requestFile.transferTo(new java.io.File(fullPath));
        return fullPath;
    }

    private String resolveFileName(String path, String fileName) {
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

}
