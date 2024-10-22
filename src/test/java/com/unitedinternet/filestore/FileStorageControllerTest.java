package com.unitedinternet.filestore;

import com.unitedinternet.filestore.controllers.FileStorageController;
import com.unitedinternet.filestore.controllers.FileStoreResponse;
import com.unitedinternet.filestore.model.AccessType;
import com.unitedinternet.filestore.model.File;
import com.unitedinternet.filestore.repository.FileRepository;
import com.unitedinternet.filestore.service.FileStorageResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class FileStorageControllerTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private FileStorageResolver fileStorageResolver;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private FileStorageController fileStorageController;

    @Test
    public void givenPOSTMultipartFile_whenStorageResolverThrowsException_thenReturnsServerError() throws Exception {
        //setup
        doThrow(new IOException()).when(fileStorageResolver).storeFile(isA(String.class), isA(MultipartFile.class));

        //test
        MockMultipartFile contentFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Whatever Content".getBytes());
        ResponseEntity<FileStoreResponse> response  = fileStorageController.createFile(contentFile, "/categories/binoculars/hunting/");
        assertSame(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        assertTrue(response.getBody().getMessage().contains("Could not upload the file"));
    }

    @Test
    public void givenPUTMultipartFile_whenStorageResolverThrowsException_thenReturnsServerError() throws Exception {
        //setup
        File file = new File.Builder().path("/categories/binoculars/hunting").name("test.txt").createdDate(LocalDateTime.now()).fullPath("/categories/binoculars/hunting/filestorage.txt").accessType(AccessType.DEFAULT).build();
        when(fileRepository.findByFullPath(isA(String.class))).thenReturn(List.of(file));
        doThrow(new IOException()).when(fileStorageResolver).storeFile(isA(String.class), isA(MultipartFile.class));

        //test
        MockMultipartFile contentFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Whatever Content".getBytes());
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/files/categories/binoculars/hunting/test.txt");
        ResponseEntity<FileStoreResponse> response  = fileStorageController.updateFile(contentFile, mockRequest);
        assertSame(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        assertTrue(response.getBody().getMessage().contains("Could not upload the file"));
    }

}
