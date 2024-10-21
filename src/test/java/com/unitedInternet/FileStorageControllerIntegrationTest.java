package com.unitedInternet;

import com.unitedinternet.filestore.FilestoreApplication;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FilestoreApplication.class })
@WebAppConfiguration
@TestPropertySource(
        locations = "classpath:application-integrationtest.properties")
public class FileStorageControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    public void givenWac_whenServletContext_thenItProvidesMemberController() {
        ServletContext servletContext = webApplicationContext.getServletContext();

        assertNotNull(servletContext);
        assertTrue(servletContext instanceof MockServletContext);
        assertNotNull(webApplicationContext.getBean("fileStorageController"));
        assertNotNull(webApplicationContext.getBean("fileStorageAdminController"));
    }

    @Test
    public void givenTEST_whenGet_thenReturnsOk() throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(get("/files/test").contentType("application/json"))
                .andDo(print()).andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void givenPOSTMultipartFile_whenPostWithRequestPart_thenReturnsOK() throws Exception {
        MockMultipartFile contentFile = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Whatever Content".getBytes());
        mockMvc.perform(multipart("/files")
                        .file(contentFile)
                        .param("path","/categories/binoculars/hunting"))
                .andExpect(status().isCreated());

    }

}
