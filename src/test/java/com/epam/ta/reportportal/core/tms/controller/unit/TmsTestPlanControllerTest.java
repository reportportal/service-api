package com.epam.ta.reportportal.core.tms.controller.unit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.tms.controller.TmsTestPlanController;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRS;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchAddTestCasesToPlanRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchRemoveTestCasesFromPlanRQ;
import com.epam.ta.reportportal.core.tms.service.TmsTestPlanService;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class TmsTestPlanControllerTest {

  private final long projectId = 1L;
  private final String projectKey = "test_project";

  @Mock
  private TmsTestPlanService tmsTestPlanService;
  @Mock
  private ProjectExtractor projectExtractor;
  @InjectMocks
  private TmsTestPlanController testPlanController;
  private MockMvc mockMvc;
  private ReportPortalUser testUser;
  private ObjectMapper objectMapper;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);
    objectMapper = new ObjectMapper();

    // Create a test user
    testUser = ReportPortalUser.userBuilder()
        .withUserName("testUser")
        .withPassword("password")
        .withUserId(1L)
        .withActive(true)
        .withAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
        .build();

    // Configure MockMvc with custom argument resolvers
    mockMvc = standaloneSetup(testPlanController)
        .setCustomArgumentResolvers(
            new PageableHandlerMethodArgumentResolver(),
            new HandlerMethodArgumentResolver() {
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
            }
        )
        .build();

    // Setup the project extractor mock to return a MembershipDetails with the projectId
    MembershipDetails membershipDetails = MembershipDetails.builder()
        .withProjectId(projectId)
        .withProjectKey(projectKey)
        .build();
    given(projectExtractor.extractMembershipDetails(eq(testUser), anyString()))
        .willReturn(membershipDetails);
  }

  @Test
  void createTestPlanTest() throws Exception {
    TmsTestPlanRQ tmsTestPlanRequest = new TmsTestPlanRQ();
    TmsTestPlanRS testPlan = new TmsTestPlanRS();
    given(tmsTestPlanService.create(projectId, tmsTestPlanRequest)).willReturn(testPlan);
    String jsonContent = objectMapper.writeValueAsString(tmsTestPlanRequest);

    mockMvc.perform(post("/v1/project/{projectKey}/tms/test-plan", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).create(projectId, tmsTestPlanRequest);
  }

  @Test
  void getTestPlansByCriteriaTest() throws Exception {
    Pageable pageable = PageRequest.of(0, 1);
    List<TmsTestPlanRS> content = List.of(new TmsTestPlanRS(), new TmsTestPlanRS());
    Page<TmsTestPlanRS> page = new PageImpl<>(content, pageable, content.size());

    given(tmsTestPlanService.getByCriteria(projectId, pageable))
        .willReturn(page);

    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .param("environmentId", "1")
            .param("productVersionId", "2")
            .param("page", "0")
            .param("size", "1"))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).getByCriteria(projectId, pageable);
  }

  @Test
  void updateTestPlanTest() throws Exception {
    Long testPlanId = 2L;
    TmsTestPlanRQ tmsTestPlanRequest = new TmsTestPlanRQ();
    TmsTestPlanRS testPlan = new TmsTestPlanRS();
    String jsonContent = objectMapper.writeValueAsString(tmsTestPlanRequest);

    given(tmsTestPlanService.update(projectId, testPlanId, tmsTestPlanRequest))
        .willReturn(testPlan);

    mockMvc.perform(
            put("/v1/project/{projectKey}/tms/test-plan/{testPlanId}", projectKey, testPlanId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).update(projectId, testPlanId, tmsTestPlanRequest);
  }

  @Test
  void getTestPlanByIdTest() throws Exception {
    Long testPlanId = 2L;
    TmsTestPlanRS testPlan = new TmsTestPlanRS();
    given(tmsTestPlanService.getById(projectId, testPlanId)).willReturn(testPlan);

    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}", projectKey, testPlanId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).getById(projectId, testPlanId);
  }

  @Test
  void deleteTestPlanTest() throws Exception {
    Long testPlanId = 2L;

    mockMvc.perform(delete("/v1/project/{projectKey}/tms/test-plan/{testPlanId}",
            projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).delete(projectId, testPlanId);
  }

  @Test
  void patchTestPlanTest() throws Exception {
    Long testPlanId = 2L;
    TmsTestPlanRQ tmsTestPlanRequest = new TmsTestPlanRQ();
    TmsTestPlanRS testPlan = new TmsTestPlanRS();
    String jsonContent = objectMapper.writeValueAsString(tmsTestPlanRequest);

    given(tmsTestPlanService.patch(projectId, testPlanId, tmsTestPlanRequest)).willReturn(testPlan);

    mockMvc.perform(patch("/v1/project/{projectKey}/tms/test-plan/{testPlanId}",
            projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).patch(projectId, testPlanId, tmsTestPlanRequest);
  }

  @Test
  void addTestCasesToPlanTest() throws Exception {
    // Given
    Long testPlanId = 2L;
    List<Long> testCaseIds = Arrays.asList(1L, 2L, 3L);
    BatchAddTestCasesToPlanRQ addRequest = BatchAddTestCasesToPlanRQ.builder()
        .testCaseIds(testCaseIds)
        .build();
    String jsonContent = objectMapper.writeValueAsString(addRequest);

    // When & Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/batch",
            projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isNoContent());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).addTestCasesToPlan(projectId, testPlanId, testCaseIds);
  }

  @Test
  void addTestCasesToPlanWithSingleTestCaseTest() throws Exception {
    // Given
    Long testPlanId = 3L;
    List<Long> testCaseIds = List.of(1L);
    BatchAddTestCasesToPlanRQ addRequest = BatchAddTestCasesToPlanRQ.builder()
        .testCaseIds(testCaseIds)
        .build();
    String jsonContent = objectMapper.writeValueAsString(addRequest);

    // When & Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/batch",
            projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isNoContent());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).addTestCasesToPlan(projectId, testPlanId, testCaseIds);
  }

  @Test
  void addTestCasesToPlanWithEmptyListTest() throws Exception {
    // Given
    Long testPlanId = 4L;
    List<Long> testCaseIds = Collections.emptyList();
    BatchAddTestCasesToPlanRQ addRequest = BatchAddTestCasesToPlanRQ.builder()
        .testCaseIds(testCaseIds)
        .build();
    String jsonContent = objectMapper.writeValueAsString(addRequest);

    // When & Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/batch",
            projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isBadRequest());
  }

  @Test
  void removeTestCasesFromPlanTest() throws Exception {
    // Given
    Long testPlanId = 2L;
    List<Long> testCaseIds = Arrays.asList(1L, 2L, 3L);
    BatchRemoveTestCasesFromPlanRQ removeRequest = BatchRemoveTestCasesFromPlanRQ.builder()
        .testCaseIds(testCaseIds)
        .build();
    String jsonContent = objectMapper.writeValueAsString(removeRequest);

    // When & Then
    mockMvc.perform(delete("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/batch",
            projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isNoContent());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).removeTestCasesFromPlan(projectId, testPlanId, testCaseIds);
  }

  @Test
  void removeTestCasesFromPlanWithSingleTestCaseTest() throws Exception {
    // Given
    Long testPlanId = 5L;
    List<Long> testCaseIds = List.of(10L);
    BatchRemoveTestCasesFromPlanRQ removeRequest = BatchRemoveTestCasesFromPlanRQ.builder()
        .testCaseIds(testCaseIds)
        .build();
    String jsonContent = objectMapper.writeValueAsString(removeRequest);

    // When & Then
    mockMvc.perform(delete("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/batch",
            projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isNoContent());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).removeTestCasesFromPlan(projectId, testPlanId, testCaseIds);
  }

  @Test
  void removeTestCasesFromPlanWithEmptyListTest() throws Exception {
    // Given
    Long testPlanId = 6L;
    List<Long> testCaseIds = Collections.emptyList();
    BatchRemoveTestCasesFromPlanRQ removeRequest = BatchRemoveTestCasesFromPlanRQ.builder()
        .testCaseIds(testCaseIds)
        .build();
    String jsonContent = objectMapper.writeValueAsString(removeRequest);

    // When & Then
    mockMvc.perform(delete("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/batch",
            projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isBadRequest());
  }

  @Test
  void removeTestCasesFromPlanWithMultipleTestCasesTest() throws Exception {
    // Given
    Long testPlanId = 7L;
    List<Long> testCaseIds = Arrays.asList(15L, 16L, 17L, 18L, 19L);
    BatchRemoveTestCasesFromPlanRQ removeRequest = BatchRemoveTestCasesFromPlanRQ.builder()
        .testCaseIds(testCaseIds)
        .build();
    String jsonContent = objectMapper.writeValueAsString(removeRequest);

    // When & Then
    mockMvc.perform(delete("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/batch",
            projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isNoContent());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).removeTestCasesFromPlan(projectId, testPlanId, testCaseIds);
  }

  @Test
  void addTestCasesToPlanWithLargeListTest() throws Exception {
    // Given
    Long testPlanId = 8L;
    List<Long> testCaseIds = Arrays.asList(20L, 21L, 22L, 23L, 24L, 25L, 26L, 27L, 28L, 29L);
    BatchAddTestCasesToPlanRQ addRequest = BatchAddTestCasesToPlanRQ.builder()
        .testCaseIds(testCaseIds)
        .build();
    String jsonContent = objectMapper.writeValueAsString(addRequest);

    // When & Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/batch",
            projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isNoContent());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).addTestCasesToPlan(projectId, testPlanId, testCaseIds);
  }
}
