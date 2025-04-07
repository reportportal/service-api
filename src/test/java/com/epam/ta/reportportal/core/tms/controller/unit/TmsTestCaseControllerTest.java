package com.epam.ta.reportportal.core.tms.controller.unit;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.epam.ta.reportportal.core.tms.controller.TestCaseController;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.service.TmsTestCaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

public class TmsTestCaseControllerTest {

  @Mock
  private TmsTestCaseService tmsTestCaseService;

  @InjectMocks
  private TestCaseController testCaseController;

  private MockMvc mockMvc;
  private ObjectMapper mapper;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    mockMvc = standaloneSetup(testCaseController).build();
    mapper = new ObjectMapper();
  }

  @Test
  void getTestCaseByIdTest() throws Exception {
    // Given
    long projectId = 1L;
    long testCaseId = 2L;
    TmsTestCaseRS testCase = new TmsTestCaseRS();
    given(tmsTestCaseService.getById(projectId, testCaseId)).willReturn(testCase);

    // When/Then
    mockMvc.perform(
            get("/project/{projectId}/tms/test-case/{testCaseId}", projectId, testCaseId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(tmsTestCaseService).getById(projectId, testCaseId);
  }

  @Test
  void getTestCaseByProjectIdTest() throws Exception {
    // Given
    long projectId = 1L;
    List<TmsTestCaseRS> testCases = new ArrayList<>();
    testCases.add(new TmsTestCaseRS());
    testCases.add(new TmsTestCaseRS());

    given(tmsTestCaseService.getTestCaseByProjectId(projectId)).willReturn(testCases);

    // When/Then
    mockMvc.perform(get("/project/{projectId}/tms/test-case", projectId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(tmsTestCaseService).getTestCaseByProjectId(projectId);
  }

  @Test
  void createTestCaseTest() throws Exception {
    // Given
    long projectId = 1L;
    TmsTestCaseRQ testCaseRequest = new TmsTestCaseRQ();
    testCaseRequest.setName("Test Case");
    testCaseRequest.setTestFolderId(3L);

    TmsTestCaseRS testCase = new TmsTestCaseRS();
    String jsonContent = mapper.writeValueAsString(testCaseRequest);

    given(tmsTestCaseService.create(projectId, testCaseRequest)).willReturn(testCase);

    // When/Then
    mockMvc.perform(post("/project/{projectId}/tms/test-case", projectId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(tmsTestCaseService).create(projectId, testCaseRequest);
  }

  @Test
  void updateTestCaseTest() throws Exception {
    // Given
    long projectId = 1L;
    long testCaseId = 2L;
    TmsTestCaseRQ testCaseRequest = new TmsTestCaseRQ();
    testCaseRequest.setName("Updated Test Case");
    testCaseRequest.setTestFolderId(3L);

    TmsTestCaseRS testCase = new TmsTestCaseRS();
    String jsonContent = mapper.writeValueAsString(testCaseRequest);

    given(tmsTestCaseService.update(projectId, testCaseId, testCaseRequest)).willReturn(testCase);

    // When/Then
    mockMvc.perform(
            put("/project/{projectId}/tms/test-case/{testCaseId}", projectId, testCaseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(tmsTestCaseService).update(projectId, testCaseId, testCaseRequest);
  }

  @Test
  void patchTestCaseTest() throws Exception {
    // Given
    long projectId = 1L;
    long testCaseId = 2L;
    TmsTestCaseRQ testCaseRequest = new TmsTestCaseRQ();
    testCaseRequest.setName("Patched Test Case");
    testCaseRequest.setTestFolderId(3L);

    TmsTestCaseRS testCase = new TmsTestCaseRS();
    String jsonContent = mapper.writeValueAsString(testCaseRequest);

    given(tmsTestCaseService.patch(projectId, testCaseId, testCaseRequest)).willReturn(testCase);

    // When/Then
    mockMvc.perform(
            patch("/project/{projectId}/tms/test-case/{testCaseId}", projectId, testCaseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(tmsTestCaseService).patch(projectId, testCaseId, testCaseRequest);
  }
}
