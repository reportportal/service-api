package com.epam.ta.reportportal.core.tms.controller.unit;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.epam.ta.reportportal.core.tms.controller.ProductVersionController;
import com.epam.ta.reportportal.core.tms.dto.ProductVersionRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsProductVersionRS;
import com.epam.ta.reportportal.core.tms.service.ProductVersionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class ProductVersionControllerTest {

  @Mock
  private ProductVersionService productVersionService;

  @InjectMocks
  private ProductVersionController productVersionController;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = standaloneSetup(productVersionController).build();
  }

  @Test
  void getByIdTest() throws Exception {
    long projectId = 1L;
    long productVersionId = 1L;
    TmsProductVersionRS productVersionRS = new TmsProductVersionRS();
    productVersionRS.setId(1L);
    productVersionRS.setVersion("version");
    productVersionRS.setDocumentation("doc");
    productVersionRS.setProjectId(1L);

    given(productVersionService.getById(projectId, productVersionId)).willReturn(productVersionRS);

    mockMvc.perform(get("/project/{projectId}/tms/productversion/{productVersionId}", projectId,
            productVersionId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").exists());
    verify(productVersionService).getById(projectId, productVersionId);
  }

  @Test
  void createVersionTest() throws Exception {
    long projectId = 1L;
    ProductVersionRQ request = new ProductVersionRQ(1L, "version", "doc", 1L);
    TmsProductVersionRS expectedResponse = new TmsProductVersionRS();
    expectedResponse.setId(1L);
    expectedResponse.setVersion("version");
    expectedResponse.setDocumentation("doc");
    expectedResponse.setProjectId(1L);

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    given(productVersionService.create(projectId, request)).willReturn(expectedResponse);

    mockMvc.perform(post("/project/{projectId}/tms/productversion", projectId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());
    verify(productVersionService).create(projectId, request);
  }

  @Test
  void updateVersionTest() throws Exception {
    long projectId = 1L;
    long productVersionId = 1L;
    ProductVersionRQ request = new ProductVersionRQ(productVersionId, "version", "doc", projectId);
    TmsProductVersionRS expectedResponse = new TmsProductVersionRS();
    expectedResponse.setId(1L);
    expectedResponse.setVersion("version");
    expectedResponse.setDocumentation("doc");
    expectedResponse.setProjectId(1L);

    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    given(productVersionService.update(projectId, productVersionId, request))
        .willReturn(expectedResponse);

    mockMvc.perform(put("/project/{projectId}/tms/productversion/{productVersionId}",
            projectId, productVersionId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());
    verify(productVersionService).update(projectId, productVersionId, request);
  }

  @Test
  void deleteVersionTest() throws Exception {
    long projectId = 1L;
    long productVersionId = 1L;

    doNothing().when(productVersionService).delete(projectId, productVersionId);

    mockMvc.perform(delete("/project/{projectId}/tms/productversion/{productVersionId}",
            projectId, productVersionId))
        .andExpect(status().isOk());
    verify(productVersionService).delete(projectId, productVersionId);
  }
}
