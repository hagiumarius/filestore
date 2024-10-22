package com.unitedinternet;

import com.unitedinternet.filestore.FilestoreApplication;
import com.unitedinternet.filestore.config.InitializingConfig;
import com.unitedinternet.filestore.repository.FileRepository;
import com.unitedinternet.filestore.service.CachingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = FilestoreApplication.class)
@TestPropertySource(
        locations = "classpath:application-integrationtest.properties")
public class FileStorageAdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    FileRepository fileRepository;

    @Autowired
    CachingService cachingService;

    @BeforeEach
    void clearDatabase(@Autowired FileRepository fileRepository) {
        fileRepository.deleteAll();
        cachingService.cleanAll();
    }

    @Test
    public void givenMultipartFile_whenGetFileCount_thenReturnsOK() throws Exception {
        MockMultipartFile contentFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Whatever Content".getBytes());
        MvcResult postResult = mockMvc.perform(multipart("/files")
                .file(contentFile)
                .param("path","/categories/binoculars/hunting")
                .header("Authorization","Bearer sample")).andReturn();
        assertEquals(postResult.getResponse().getStatus(), HttpStatus.CREATED.value());

        mockMvc.perform(get("/files/count")
                .header("Authorization","Bearer sample")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filesCount").value("1"));
        assertTrue(cachingService.exists(InitializingConfig.FILES_COUNT));

        //read from cache
        mockMvc.perform(get("/files/count")
                        .header("Authorization","Bearer sample")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filesCount").value("1"));
        assertEquals("1", cachingService.getValue(InitializingConfig.FILES_COUNT));

        //update cache
        MvcResult postResult2 = mockMvc.perform(multipart("/files")
                .file(contentFile)
                .param("path","/categories/binoculars/view")
                .header("Authorization","Bearer sample")).andReturn();
        assertEquals(postResult.getResponse().getStatus(), HttpStatus.CREATED.value());

        //update cache
        mockMvc.perform(delete("/files/categories/binoculars/hunting/test.txt")
                        .header("Authorization","Bearer sample"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200"))
                .andExpect(jsonPath("$.message").value(containsString("File deleted successfully")));
        mockMvc.perform(get("/files/count")
                        .header("Authorization","Bearer sample")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filesCount").value("1"));
        assertEquals("1", cachingService.getValue(InitializingConfig.FILES_COUNT));
    }

    @Test
    public void givenMultipartFile_whenGetFilesByRegex_thenReturnsOK() throws Exception {
        MockMultipartFile contentFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Whatever Content".getBytes());
        MvcResult postResult = mockMvc.perform(multipart("/files")
                .file(contentFile)
                .param("path","/categories/binoculars/hunting")
                .header("Authorization","Bearer sample")).andReturn();
        assertEquals(postResult.getResponse().getStatus(), HttpStatus.CREATED.value());

        mockMvc.perform(get("/files")
                        .queryParam("regex","%5E.%2AST.%2A%24")
                        .header("Authorization","Bearer sample")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("test.txt")));
        assertTrue(cachingService.exists(InitializingConfig.REGEX_LIST));
        assertTrue(cachingService.getElementsFromList(URLDecoder.decode("%5E.%2AST.%2A%24", StandardCharsets.UTF_8)).get(0).contains("test.txt"));

        //read from cache
        mockMvc.perform(get("/files")
                        .queryParam("regex","%5E.%2AST.%2A%24")
                        .header("Authorization","Bearer sample")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("test.txt")));
        assertTrue(cachingService.exists(InitializingConfig.REGEX_LIST));
        assertTrue(cachingService.getElementsFromList(URLDecoder.decode("%5E.%2AST.%2A%24", StandardCharsets.UTF_8)).get(0).contains("test.txt"));

        //update cache
        mockMvc.perform(delete("/files/categories/binoculars/hunting/test.txt")
                        .header("Authorization","Bearer sample"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200"))
                .andExpect(jsonPath("$.message").value(containsString("File deleted successfully")));

        //read from cache (not correctly reachable cause of Thread sleep issue)
        mockMvc.perform(get("/files")
                        .queryParam("regex","%5E.%2AST.%2A%24")
                        .header("Authorization","Bearer sample")).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("test.txt")));
        assertTrue(cachingService.getElementsFromList(URLDecoder.decode("%5E.%2AST.%2A%24", StandardCharsets.UTF_8)).get(0).contains("test.txt"));

    }

}
