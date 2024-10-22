package com.unitedinternet.filestore;

import com.unitedinternet.filestore.model.File;
import com.unitedinternet.filestore.repository.FileRepository;
import com.unitedinternet.filestore.service.CachingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = FilestoreApplication.class)
@TestPropertySource(
        locations = "classpath:application-integrationtest.properties")
public class FileStorageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired FileRepository fileRepository;

    @Autowired
    CachingService cachingService;

    @BeforeEach
    void clearDatabase(@Autowired FileRepository fileRepository) {
        fileRepository.deleteAll();
        cachingService.cleanAll();
    }

    @Test
    public void givenTEST_whenGet_thenReturnsOk() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/files/test")
                        .contentType("application/json")
                        .header("Authorization","Bearer sample"))
                .andDo(print()).andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void givenPOSTMultipartFile_whenPostWithRequestPart_thenReturnsOK() throws Exception {
        MockMultipartFile contentFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Whatever Content".getBytes());
        mockMvc.perform(multipart("/files")
                        .file(contentFile)
                        .param("path","/categories/binoculars/hunting")
                        .header("Authorization","Bearer sample"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value("201"))
                .andExpect(jsonPath("$.message").value(containsString("File uploaded successfully")))
                .andExpect(jsonPath("$.details.id").value(containsString("test.txt")));
        List<File> foundFiles = fileRepository.findByFullPath("categories/binoculars/hunting/test.txt");
        assertEquals(0, foundFiles.size());
    }

    @Test
    public void givenPOSTMultipartFile_whenPostWithMissingAuthHeader_thenReturnsForbidden() throws Exception {
        MockMultipartFile contentFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Whatever Content".getBytes());
        mockMvc.perform(multipart("/files")
                        .file(contentFile)
                        .param("path","/categories/binoculars/hunting"))
                .andExpect(status().isForbidden());

    }

    @Test
    public void givenPOSTMultipartFile_whenPostWithRequestPartWrongFileName_thenReturnsBadRequest() throws Exception {
        MockMultipartFile contentFile = new MockMultipartFile("file", "te st.txt", MediaType.TEXT_PLAIN_VALUE, "Whatever Content".getBytes());
        mockMvc.perform(multipart("/files")
                        .file(contentFile)
                        .param("path","/categories/binoculars/hunting")
                        .header("Authorization","Bearer sample"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("Wrong file name"));
    }

    @Test
    public void givenPOSTMultipartFile_whenPostWithRequestPartMissingFile_thenReturnsBadRequest() throws Exception {
        MockMultipartFile contentFile = new MockMultipartFile("file", (String) null, MediaType.MULTIPART_FORM_DATA_VALUE, (byte[]) null);
        mockMvc.perform(multipart("/files")
                        .file(contentFile)
                        .param("path","/categories/binoculars/hunting")
                        .header("Authorization","Bearer sample"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("Missing request file"));
    }

    @Test
    public void givenPOSTMultipartFile_whenPostWithRequestPartPathTooLong_thenReturnsBadRequest() throws Exception {
        MockMultipartFile contentFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Whatever Content".getBytes());
        mockMvc.perform(multipart("/files")
                        .file(contentFile)
                        .param("path","/categories/binoculars/hunting/whatever2/whatever3/whatever4")
                        .header("Authorization","Bearer sample"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("Path too long"));
    }

    @Test
    public void givenPOSTMultipartFile_whenPostWithRequestPartPathWrongRegex_thenReturnsBadRequest() throws Exception {
        MockMultipartFile contentFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Whatever Content".getBytes());
        mockMvc.perform(multipart("/files")
                        .file(contentFile)
                        .param("path","/categories/binoculars/hun ting")
                        .header("Authorization","Bearer sample"))
                .andDo(print())
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.statusCode").value("400"))
                .andExpect(jsonPath("$.message").value("Wrong path segment name"));
    }

    @Test
    public void givenMultipartFile_whenGetFile_thenReturnsOK() throws Exception {
        MockMultipartFile contentFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Whatever Content".getBytes());
        MvcResult postResult = mockMvc.perform(multipart("/files")
                .file(contentFile)
                .param("path","/categories/binoculars/hunting")
                .header("Authorization","Bearer sample")).andReturn();
        assertEquals(postResult.getResponse().getStatus(), HttpStatus.CREATED.value());

        MvcResult getResult = mockMvc.perform(get("/files/categories/binoculars/hunting/test.txt")
                .header("Authorization","Bearer sample")).andDo(print()).andReturn();
        assertEquals(getResult.getResponse().getStatus(), HttpStatus.OK.value());
        assertEquals(getResult.getResponse().getContentAsString(),"Whatever Content");
    }

    @Test
    public void givenMultipartFile_whenPutFile_thenReturnsOK() throws Exception {
        MockMultipartFile contentFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Whatever Content".getBytes());
        MvcResult postResult = mockMvc.perform(multipart("/files")
                .file(contentFile)
                .param("path","/categories/binoculars/hunting")
                .header("Authorization","Bearer sample")).andReturn();
        assertEquals(postResult.getResponse().getStatus(), HttpStatus.CREATED.value());

        contentFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Whatever Content2".getBytes());
        mockMvc.perform(multipart(HttpMethod.PUT, "/files/categories/binoculars/hunting/test.txt")
                .file(contentFile)
                .header("Authorization","Bearer sample"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200"))
                .andExpect(jsonPath("$.message").value(containsString("File uploaded successfully")))
                .andExpect(jsonPath("$.details.id").value(containsString("test.txt")));
        List<File> foundFiles = fileRepository.findByFullPath("categories/binoculars/hunting/test.txt");
        assertEquals(0, foundFiles.size());

    }

    @Test
    public void givenMultipartFile_whenPutFileWithWrongPath_thenReturnsNotFound() throws Exception {
        MockMultipartFile contentFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Whatever Content".getBytes());
        MvcResult postResult = mockMvc.perform(multipart("/files")
                .file(contentFile)
                .param("path","/categories/binoculars/hunting")
                .header("Authorization","Bearer sample")).andReturn();
        assertEquals(postResult.getResponse().getStatus(), HttpStatus.CREATED.value());

        contentFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Whatever Content2".getBytes());
        mockMvc.perform(multipart(HttpMethod.PUT, "/files/categories/binculars/hunting/test.txt")
                        .file(contentFile)
                        .header("Authorization","Bearer sample"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404"))
                .andExpect(jsonPath("$.message").value(containsString("Record not found")));

    }

    @Test
    public void givenMultipartFile_whenDeleteFile_thenReturnsOK() throws Exception {
        MockMultipartFile contentFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Whatever Content".getBytes());
        MvcResult postResult = mockMvc.perform(multipart("/files")
                .file(contentFile)
                .param("path","/categories/binoculars/hunting")
                .header("Authorization","Bearer sample")).andReturn();
        assertEquals(postResult.getResponse().getStatus(), HttpStatus.CREATED.value());

        mockMvc.perform(delete("/files/categories/binoculars/hunting/test.txt")
                        .header("Authorization","Bearer sample"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("200"))
                .andExpect(jsonPath("$.message").value(containsString("File deleted successfully")));
        List<File> foundFiles = fileRepository.findByFullPath("categories/binoculars/hunting/test.txt");
        assertEquals(0, foundFiles.size());
        }

    @Test
    public void givenMultipartFile_whenDeleteFileWithWrongName_thenReturnsNotFound() throws Exception {
        MockMultipartFile contentFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Whatever Content".getBytes());
        MvcResult postResult = mockMvc.perform(multipart("/files")
                .file(contentFile)
                .param("path","/categories/binoculars/hunting")
                .header("Authorization","Bearer sample")).andReturn();
        assertEquals(postResult.getResponse().getStatus(), HttpStatus.CREATED.value());

        mockMvc.perform(delete("/files/categories/binoculars/hunting/test1.txt")
                        .header("Authorization","Bearer sample"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusCode").value("404"))
                .andExpect(jsonPath("$.message").value(containsString("Record not found")));

    }

}
