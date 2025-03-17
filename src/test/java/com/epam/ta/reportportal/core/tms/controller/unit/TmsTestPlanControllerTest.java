package com.epam.ta.reportportal.core.tms.controller.unit;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.epam.ta.reportportal.core.tms.controller.TmsTestPlanController;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRS;
import com.epam.ta.reportportal.core.tms.service.TmsTestPlanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

public class TmsTestPlanControllerTest {

  @Mock
  private TmsTestPlanService tmsTestPlanService;

  @InjectMocks
  private TmsTestPlanController testPlanController;

  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    mockMvc = standaloneSetup(testPlanController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
  }

  @Test
  void createTestPlanTest() throws Exception {
    Long projectId = 1L;
    TmsTestPlanRQ tmsTestPlanRequest = new TmsTestPlanRQ();
    TmsTestPlanRS testPlan = new TmsTestPlanRS();
    given(tmsTestPlanService.create(projectId, tmsTestPlanRequest)).willReturn(testPlan);
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(tmsTestPlanRequest);

    mockMvc.perform(post("/project/{projectId}/tms/test-plan", projectId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonContent))
            .andExpect(status().isOk());

    verify(tmsTestPlanService).create(projectId, tmsTestPlanRequest);
  }

  @Test
  void getTestPlansByCriteriaTest() throws Exception {
    Long projectId = 1L;
    Pageable pageable = PageRequest.of(0, 1);
    List<TmsTestPlanRS> content = List.of(new TmsTestPlanRS(), new TmsTestPlanRS());
    Page<TmsTestPlanRS> page = new PageImpl<>(content, pageable, content.size());

    given(tmsTestPlanService.getByCriteria(projectId, List.of(1L), List.of(2L), pageable))
                                           .willReturn(page);

    mockMvc.perform(get("/project/{projectId}/tms/test-plan", projectId)
                    .contentType(MediaType.APPLICATION_JSON)
            .param("environmentId", "1")
            .param("productVersionId", "2")
            .param("page", "0")
            .param("size", "1"))
            .andExpect(status().isOk());

    verify(tmsTestPlanService).getByCriteria(projectId, List.of(1L), List.of(2L), pageable);
  }

  @Test
  void updateTestPlanTest() throws Exception {
    Long projectId = 1L;
    Long testPlanId = 2L;
    TmsTestPlanRQ tmsTestPlanRequest = new TmsTestPlanRQ();
    TmsTestPlanRS testPlan = new TmsTestPlanRS();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(tmsTestPlanRequest);

    given(tmsTestPlanService.update(projectId, testPlanId, tmsTestPlanRequest))
                                .willReturn(testPlan);

    mockMvc.perform(put("/project/{projectId}/tms/test-plan/{testPlanId}", projectId, testPlanId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonContent))
            .andExpect(status().isOk());

    verify(tmsTestPlanService).update(projectId, testPlanId, tmsTestPlanRequest);
  }

  @Test
  void getTestPlanByIdTest() throws Exception {
    Long projectId = 1L;
    Long testPlanId = 2L;
    TmsTestPlanRS testPlan = new TmsTestPlanRS();
    given(tmsTestPlanService.getById(projectId, testPlanId)).willReturn(testPlan);

    mockMvc.perform(get("/project/{projectId}/tms/test-plan/{testPlanId}", projectId, testPlanId)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

    verify(tmsTestPlanService).getById(projectId, testPlanId);
  }

  @Test
  void deleteTestPlanTest() throws Exception {
    Long projectId = 1L;
    Long testPlanId = 2L;

    mockMvc.perform(delete("/project/{projectId}/tms/test-plan/{testPlanId}", projectId, testPlanId)
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

    verify(tmsTestPlanService).delete(projectId, testPlanId);
  }

  @Test
  void patchTestPlanTest() throws Exception {
    Long projectId = 1L;
    Long testPlanId = 2L;
    TmsTestPlanRQ tmsTestPlanRequest = new TmsTestPlanRQ();
    TmsTestPlanRS testPlan = new TmsTestPlanRS();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(tmsTestPlanRequest);

    given(tmsTestPlanService.patch(projectId, testPlanId, tmsTestPlanRequest)).willReturn(testPlan);

    mockMvc.perform(patch("/project/{projectId}/tms/test-plan/{testPlanId}", projectId, testPlanId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonContent))
            .andExpect(status().isOk());

    verify(tmsTestPlanService).patch(projectId, testPlanId, tmsTestPlanRequest);
  }
}
