package com.epam.ta.reportportal.core.tms.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.epam.ta.reportportal.core.tms.controller.TestCaseController;
import com.epam.ta.reportportal.core.tms.dto.AttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.ManualScenarioRS;
import com.epam.ta.reportportal.core.tms.dto.TestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TestCaseRS;
import com.epam.ta.reportportal.core.tms.dto.TestCaseVersionRS;
import com.epam.ta.reportportal.core.tms.service.TestCaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

class TestCaseControllerTest {

  @Mock
  private TestCaseService testCaseService;

  @InjectMocks
  private TestCaseController testCaseController;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = standaloneSetup(testCaseController).build();
  }

  @Test
  void getTestCaseByIdTest() throws Exception {
    long projectId = 1L;
    long testCaseId = 100L;
    ManualScenarioRS manualScenario = new ManualScenarioRS(1L, "name", "doc");
    TestCaseVersionRS caseVersion = new TestCaseVersionRS(1L, "name", true, true, manualScenario);
    TestCaseRS mockResponse = new TestCaseRS(1L, "name", "doc", Set.of(caseVersion));

    given(testCaseService.getTestCaseById(projectId, testCaseId)).willReturn(mockResponse);

    mockMvc.perform(get("/project/{projectId}/tms/testcase/{testCaseId}", projectId, testCaseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").exists());
    verify(testCaseService).getTestCaseById(projectId, testCaseId);
  }

  @Test
  void getTestCaseByProjectIdTest() throws Exception {
    long projectId = 1L;
    ManualScenarioRS manualScenario = new ManualScenarioRS(1L, "name", "doc");
    TestCaseVersionRS caseVersion = new TestCaseVersionRS(1L, "name", true, true, manualScenario);
    TestCaseRS mockResponse1 = new TestCaseRS(1L, "name", "doc", Set.of(caseVersion));
    TestCaseRS mockResponse2 = new TestCaseRS(1L, "name", "doc", Set.of(caseVersion));
    List<TestCaseRS> mockResponses = Arrays.asList(mockResponse1, mockResponse2);

    given(testCaseService.getTestCaseByProjectId(projectId)).willReturn(mockResponses);

    mockMvc.perform(get("/project/{projectId}/tms/testcase/", projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    verify(testCaseService).getTestCaseByProjectId(projectId);
  }

  @Test
  void createTestCaseTest() throws Exception {
    long projectId = 1L;
    AttributeRQ attribute = new AttributeRQ("key", "value");
    TestCaseRQ requestData = new TestCaseRQ("name", "value", 1L, List.of(attribute));
    ManualScenarioRS manualScenario = new ManualScenarioRS(1L, "name", "doc");
    TestCaseVersionRS caseVersion = new TestCaseVersionRS(1L, "name", true, true, manualScenario);
    TestCaseRS expectedResponse = new TestCaseRS(1L, "name", "doc", Set.of(caseVersion));
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(requestData);

    given(testCaseService.createTestCase(requestData)).willReturn(expectedResponse);

    mockMvc.perform(post("/project/{projectId}/tms/testcase", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk());
    verify(testCaseService).createTestCase(requestData);
  }

  @Test
  void updateTestCaseTest() throws Exception {
    long projectId = 1L;
    long testCaseId = 1L;
    AttributeRQ attribute = new AttributeRQ("key", "value");
    TestCaseRQ requestData = new TestCaseRQ("name", "value", 1L, List.of(attribute));
    ManualScenarioRS manualScenario = new ManualScenarioRS(1L, "name", "value");
    TestCaseVersionRS caseVersion = new TestCaseVersionRS(1L, "name", true, true, manualScenario);
    TestCaseRS expectedResponse = new TestCaseRS(1L, "name", "value", Set.of(caseVersion));
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(requestData);

    given(testCaseService.updateTestCase(eq(testCaseId), any(TestCaseRQ.class)))
                .willReturn(expectedResponse);

    mockMvc.perform(put("/project/{projectId}/tms/testcase/{testCaseId}", projectId, testCaseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk());
    verify(testCaseService).updateTestCase(testCaseId, requestData);
  }
}
