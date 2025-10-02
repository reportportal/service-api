package com.epam.ta.reportportal.core.tms.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.tms.controller.TmsTestPlanController;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestPlanRS;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchAddTestCasesToPlanRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchOperationResultRS;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchRemoveTestCasesFromPlanRQ;
import com.epam.ta.reportportal.core.tms.service.TmsTestPlanService;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.util.OffsetRequest;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.resolver.FilterCriteriaResolver;
import com.epam.ta.reportportal.ws.resolver.OffsetArgumentResolver;
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
            new OffsetArgumentResolver(),
            new FilterCriteriaResolver(),
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
  void getTestPlansByCriteria_WithoutFiltersTest() throws Exception {
    List<TmsTestPlanRS> content = Arrays.asList(new TmsTestPlanRS(), new TmsTestPlanRS());

    Page<TmsTestPlanRS> page = new Page<>(
        content,
        100L, // default size
        0L,   // number
        2L,   // totalElements
        1L    // totalPages
    );

    given(tmsTestPlanService.getByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class))).willReturn(page);

    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan", projectKey)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.page.totalElements").value(2));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).getByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class));
  }

  @Test
  void getTestPlansByCriteria_WithFilterTest() throws Exception {
    List<TmsTestPlanRS> content = Collections.singletonList(new TmsTestPlanRS());

    Page<TmsTestPlanRS> page = new Page<>(
        content,
        100L, // default size
        0L,   // number
        1L,   // totalElements
        1L    // totalPages
    );

    given(tmsTestPlanService.getByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class))).willReturn(page);

    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan", projectKey)
            .param("filter.cnt.name", "test plan")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.page.totalElements").value(1));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).getByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class));
  }

  @Test
  void getTestPlansByCriteria_WithPaginationTest() throws Exception {
    List<TmsTestPlanRS> content = Collections.singletonList(new TmsTestPlanRS());

    Page<TmsTestPlanRS> page = new Page<>(
        content,
        10L, // size
        1L,  // number (offset 10 / limit 10 = page 1)
        25L, // totalElements
        3L   // totalPages
    );

    given(tmsTestPlanService.getByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class))).willReturn(page);

    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan", projectKey)
            .param("offset", "10")
            .param("limit", "10")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.page.totalElements").value(25));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).getByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class));
  }

  @Test
  void getTestPlansByCriteria_WithFilterAndPaginationTest() throws Exception {
    List<TmsTestPlanRS> content = Collections.singletonList(new TmsTestPlanRS());

    Page<TmsTestPlanRS> page = new Page<>(
        content,
        20L, // size
        0L,  // number
        1L,  // totalElements
        1L   // totalPages
    );

    given(tmsTestPlanService.getByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class))).willReturn(page);

    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan", projectKey)
            .param("filter.cnt.name", "important")
            .param("offset", "0")
            .param("limit", "20")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.page.totalElements").value(1));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).getByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class));
  }

  @Test
  void getTestPlansByCriteria_WithMultipleFiltersTest() throws Exception {
    List<TmsTestPlanRS> content = Collections.singletonList(new TmsTestPlanRS());

    Page<TmsTestPlanRS> page = new Page<>(
        content,
        100L, // default size
        0L,   // number
        1L,   // totalElements
        1L    // totalPages
    );

    given(tmsTestPlanService.getByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class))).willReturn(page);

    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan", projectKey)
            .param("filter.cnt.name", "test")
            .param("filter.cnt.description", "important")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.page.totalElements").value(1));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).getByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class));
  }

  @Test
  void getTestPlansByCriteria_WithSortTest() throws Exception {
    TmsTestPlanRS plan1 = new TmsTestPlanRS();
    TmsTestPlanRS plan2 = new TmsTestPlanRS();
    List<TmsTestPlanRS> content = Arrays.asList(plan1, plan2);

    Page<TmsTestPlanRS> page = new Page<>(
        content,
        100L, // default size
        0L,   // number
        2L,   // totalElements
        1L    // totalPages
    );

    given(tmsTestPlanService.getByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class))).willReturn(page);

    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan", projectKey)
            .param("sort", "name,asc")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).getByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class));
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

    BatchOperationResultRS expectedResult = BatchOperationResultRS.builder()
        .totalCount(3)
        .successCount(3)
        .failureCount(0)
        .errors(Collections.emptyList())
        .build();

    String jsonContent = objectMapper.writeValueAsString(addRequest);

    given(tmsTestPlanService.addTestCasesToPlan(projectId, testPlanId, testCaseIds))
        .willReturn(expectedResult);

    // When & Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/batch",
            projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCount").value(3))
        .andExpect(jsonPath("$.successCount").value(3))
        .andExpect(jsonPath("$.failureCount").value(0))
        .andExpect(jsonPath("$.errors").isArray());

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

    BatchOperationResultRS expectedResult = BatchOperationResultRS.builder()
        .totalCount(1)
        .successCount(1)
        .failureCount(0)
        .errors(Collections.emptyList())
        .build();

    String jsonContent = objectMapper.writeValueAsString(addRequest);

    given(tmsTestPlanService.addTestCasesToPlan(projectId, testPlanId, testCaseIds))
        .willReturn(expectedResult);

    // When & Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/batch",
            projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCount").value(1))
        .andExpect(jsonPath("$.successCount").value(1))
        .andExpect(jsonPath("$.failureCount").value(0));

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
  void addTestCasesToPlanWithPartialSuccessTest() throws Exception {
    // Given
    Long testPlanId = 9L;
    List<Long> testCaseIds = Arrays.asList(1L, 2L, 3L);
    BatchAddTestCasesToPlanRQ addRequest = BatchAddTestCasesToPlanRQ.builder()
        .testCaseIds(testCaseIds)
        .build();

    BatchOperationResultRS expectedResult = BatchOperationResultRS.builder()
        .totalCount(3)
        .successCount(2)
        .failureCount(1)
        .errors(Collections.emptyList())
        .build();

    String jsonContent = objectMapper.writeValueAsString(addRequest);

    given(tmsTestPlanService.addTestCasesToPlan(projectId, testPlanId, testCaseIds))
        .willReturn(expectedResult);

    // When & Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/batch",
            projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCount").value(3))
        .andExpect(jsonPath("$.successCount").value(2))
        .andExpect(jsonPath("$.failureCount").value(1));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).addTestCasesToPlan(projectId, testPlanId, testCaseIds);
  }

  @Test
  void removeTestCasesFromPlanTest() throws Exception {
    // Given
    Long testPlanId = 2L;
    List<Long> testCaseIds = Arrays.asList(1L, 2L, 3L);
    BatchRemoveTestCasesFromPlanRQ removeRequest = BatchRemoveTestCasesFromPlanRQ.builder()
        .testCaseIds(testCaseIds)
        .build();

    BatchOperationResultRS expectedResult = BatchOperationResultRS.builder()
        .totalCount(3)
        .successCount(3)
        .failureCount(0)
        .errors(Collections.emptyList())
        .build();

    String jsonContent = objectMapper.writeValueAsString(removeRequest);

    given(tmsTestPlanService.removeTestCasesFromPlan(projectId, testPlanId, testCaseIds))
        .willReturn(expectedResult);

    // When & Then
    mockMvc.perform(delete("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/batch",
            projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCount").value(3))
        .andExpect(jsonPath("$.successCount").value(3))
        .andExpect(jsonPath("$.failureCount").value(0))
        .andExpect(jsonPath("$.errors").isArray());

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

    BatchOperationResultRS expectedResult = BatchOperationResultRS.builder()
        .totalCount(1)
        .successCount(1)
        .failureCount(0)
        .errors(Collections.emptyList())
        .build();

    String jsonContent = objectMapper.writeValueAsString(removeRequest);

    given(tmsTestPlanService.removeTestCasesFromPlan(projectId, testPlanId, testCaseIds))
        .willReturn(expectedResult);

    // When & Then
    mockMvc.perform(delete("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/batch",
            projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCount").value(1))
        .andExpect(jsonPath("$.successCount").value(1))
        .andExpect(jsonPath("$.failureCount").value(0));

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

    BatchOperationResultRS expectedResult = BatchOperationResultRS.builder()
        .totalCount(5)
        .successCount(5)
        .failureCount(0)
        .errors(Collections.emptyList())
        .build();

    String jsonContent = objectMapper.writeValueAsString(removeRequest);

    given(tmsTestPlanService.removeTestCasesFromPlan(projectId, testPlanId, testCaseIds))
        .willReturn(expectedResult);

    // When & Then
    mockMvc.perform(delete("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/batch",
            projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCount").value(5))
        .andExpect(jsonPath("$.successCount").value(5))
        .andExpect(jsonPath("$.failureCount").value(0));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).removeTestCasesFromPlan(projectId, testPlanId, testCaseIds);
  }

  @Test
  void removeTestCasesFromPlanWithPartialSuccessTest() throws Exception {
    // Given
    Long testPlanId = 10L;
    List<Long> testCaseIds = Arrays.asList(15L, 16L, 17L);
    BatchRemoveTestCasesFromPlanRQ removeRequest = BatchRemoveTestCasesFromPlanRQ.builder()
        .testCaseIds(testCaseIds)
        .build();

    BatchOperationResultRS expectedResult = BatchOperationResultRS.builder()
        .totalCount(3)
        .successCount(2)
        .failureCount(1)
        .errors(Collections.emptyList())
        .build();

    String jsonContent = objectMapper.writeValueAsString(removeRequest);

    given(tmsTestPlanService.removeTestCasesFromPlan(projectId, testPlanId, testCaseIds))
        .willReturn(expectedResult);

    // When & Then
    mockMvc.perform(delete("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/batch",
            projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCount").value(3))
        .andExpect(jsonPath("$.successCount").value(2))
        .andExpect(jsonPath("$.failureCount").value(1));

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

    BatchOperationResultRS expectedResult = BatchOperationResultRS.builder()
        .totalCount(10)
        .successCount(10)
        .failureCount(0)
        .errors(Collections.emptyList())
        .build();

    String jsonContent = objectMapper.writeValueAsString(addRequest);

    given(tmsTestPlanService.addTestCasesToPlan(projectId, testPlanId, testCaseIds))
        .willReturn(expectedResult);

    // When & Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/batch",
            projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalCount").value(10))
        .andExpect(jsonPath("$.successCount").value(10))
        .andExpect(jsonPath("$.failureCount").value(0));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).addTestCasesToPlan(projectId, testPlanId, testCaseIds);
  }
}
