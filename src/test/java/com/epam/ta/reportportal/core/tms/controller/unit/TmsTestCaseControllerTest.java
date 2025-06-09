package com.epam.ta.reportportal.core.tms.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.tms.controller.TestCaseController;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.service.TmsTestCaseService;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class TmsTestCaseControllerTest {

  @Mock
  private TmsTestCaseService tmsTestCaseService;

  @Mock
  private ProjectExtractor projectExtractor;

  @InjectMocks
  private TestCaseController testCaseController;

  private MockMvc mockMvc;
  private ObjectMapper mapper;
  private final long projectId = 1L;
  private final String projectKey = "test_project";
  private ReportPortalUser testUser;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    // Create a test user
    testUser = ReportPortalUser.userBuilder()
        .withUserName("testUser")
        .withPassword("password")
        .withUserId(1L)
        .withActive(true)
        .withAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
        .build();

    // Configure MockMvc with a custom argument resolver for @AuthenticationPrincipal
    mockMvc = standaloneSetup(testCaseController)
        .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
          @Override
          public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterAnnotation(AuthenticationPrincipal.class) != null;
          }

          @Override
          public Object resolveArgument(MethodParameter parameter,
                                        ModelAndViewContainer mavContainer,
              NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return testUser;
          }
        })
        .build();

    mapper = new ObjectMapper();

    // Setup the project extractor mock to return a MembershipDetails with the projectId
    MembershipDetails membershipDetails = MembershipDetails.builder()
        .withProjectId(projectId)
        .withProjectKey(projectKey)
        .build();
    given(projectExtractor.extractProjectDetailsAdmin(anyString()))
        .willReturn(membershipDetails);
  }

  @Test
  void getTestCaseByIdTest() throws Exception {
    // Given
    long testCaseId = 2L;
    TmsTestCaseRS testCase = new TmsTestCaseRS();
    given(tmsTestCaseService.getById(projectId, testCaseId)).willReturn(testCase);

    // When/Then
    mockMvc.perform(
            get("/project/{projectKey}/tms/test-case/{testCaseId}", projectKey, testCaseId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
    verify(tmsTestCaseService).getById(projectId, testCaseId);
  }

  @Test
  void createTestCaseTest() throws Exception {
    // Given
    TmsTestCaseRQ testCaseRequest = new TmsTestCaseRQ();
    testCaseRequest.setName("Test Case");
    testCaseRequest.setTestFolderId(3L);

    TmsTestCaseRS testCase = new TmsTestCaseRS();
    String jsonContent = mapper.writeValueAsString(testCaseRequest);

    given(tmsTestCaseService.create(projectId, testCaseRequest)).willReturn(testCase);

    // When/Then
    mockMvc.perform(post("/project/{projectKey}/tms/test-case", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
    verify(tmsTestCaseService).create(projectId, testCaseRequest);
  }

  @Test
  void updateTestCaseTest() throws Exception {
    // Given
    long testCaseId = 2L;
    TmsTestCaseRQ testCaseRequest = new TmsTestCaseRQ();
    testCaseRequest.setName("Updated Test Case");
    testCaseRequest.setTestFolderId(3L);

    TmsTestCaseRS testCase = new TmsTestCaseRS();
    String jsonContent = mapper.writeValueAsString(testCaseRequest);

    given(tmsTestCaseService.update(projectId, testCaseId, testCaseRequest)).willReturn(testCase);

    // When/Then
    mockMvc.perform(
            put("/project/{projectKey}/tms/test-case/{testCaseId}", projectKey, testCaseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
    verify(tmsTestCaseService).update(projectId, testCaseId, testCaseRequest);
  }

  @Test
  void patchTestCaseTest() throws Exception {
    // Given
    long testCaseId = 2L;
    TmsTestCaseRQ testCaseRequest = new TmsTestCaseRQ();
    testCaseRequest.setName("Patched Test Case");
    testCaseRequest.setTestFolderId(3L);

    TmsTestCaseRS testCase = new TmsTestCaseRS();
    String jsonContent = mapper.writeValueAsString(testCaseRequest);

    given(tmsTestCaseService.patch(projectId, testCaseId, testCaseRequest)).willReturn(testCase);

    // When/Then
    mockMvc.perform(
            patch("/project/{projectKey}/tms/test-case/{testCaseId}", projectKey, testCaseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
    verify(tmsTestCaseService).patch(projectId, testCaseId, testCaseRequest);
  }
}
