package com.unitedInternet;

import com.unitedinternet.filestore.FilestoreApplication;
import com.unitedinternet.filestore.service.FileStorageResolver;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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

    @Mock
    FileStorageResolver fileStorageResolver;

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


    }

}
