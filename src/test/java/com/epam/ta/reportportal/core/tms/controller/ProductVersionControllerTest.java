package com.epam.ta.reportportal.core.tms.controller;

import com.epam.ta.reportportal.core.tms.db.entity.TmsMilestone;
import com.epam.ta.reportportal.core.tms.db.entity.TmsTestPlan;
import com.epam.ta.reportportal.core.tms.dto.ProductVersionRQ;
import com.epam.ta.reportportal.core.tms.dto.ProductVersionRS;
import com.epam.ta.reportportal.core.tms.service.ProductVersionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Set;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

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
    TmsTestPlan plan = new TmsTestPlan();
    TmsMilestone milestone = new TmsMilestone();
    ProductVersionRS expectedResponse = new ProductVersionRS(1L, "version", "doc",
                                                             Set.of(plan), Set.of(milestone));

    given(productVersionService.getById(projectId, productVersionId)).willReturn(expectedResponse);

    mockMvc.perform(get("/project/{projectId}/tms/productversion/{productVersionId}", projectId,
                        productVersionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    verify(productVersionService).getById(projectId, productVersionId);
  }

  @Test
  void createVersionTest() throws Exception {
    long projectId = 1L;
    ProductVersionRQ request = new ProductVersionRQ(1L, "version", "doc", Set.of(2L), Set.of(2L));
    TmsTestPlan plan = new TmsTestPlan();
    TmsMilestone milestone = new TmsMilestone();
    ProductVersionRS expectedResponse = new ProductVersionRS(1L, "version", "doc",
                                                              Set.of(plan), Set.of(milestone));
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
    ProductVersionRQ request = new ProductVersionRQ(1L, "version", "doc", Set.of(2L), Set.of(2L));
    TmsTestPlan plan = new TmsTestPlan();
    TmsMilestone milestone = new TmsMilestone();
    ProductVersionRS expectedResponse = new ProductVersionRS(1L, "version", "doc",
                                                             Set.of(plan), Set.of(milestone));
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