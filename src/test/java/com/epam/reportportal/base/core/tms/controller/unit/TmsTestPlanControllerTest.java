package com.epam.reportportal.base.core.tms.controller.unit;

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

import com.epam.reportportal.base.core.tms.controller.TmsTestPlanController;
import com.epam.reportportal.base.core.tms.dto.DuplicateTmsTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseInTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestPlanRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestPlanRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchAddTestCasesToPlanRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchRemoveTestCasesFromPlanRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.base.core.tms.service.TmsTestPlanService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.model.Page;
import com.epam.reportportal.base.util.OffsetRequest;
import com.epam.reportportal.base.util.ProjectExtractor;
import com.epam.reportportal.base.ws.resolver.FilterCriteriaResolver;
import com.epam.reportportal.base.ws.resolver.OffsetArgumentResolver;
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
  private final long testPlanId = 100L;
  private final long testCaseId = 10L;
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

    BatchTestCaseOperationResultRS expectedResult = BatchTestCaseOperationResultRS.builder()
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

    BatchTestCaseOperationResultRS expectedResult = BatchTestCaseOperationResultRS.builder()
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

    BatchTestCaseOperationResultRS expectedResult = BatchTestCaseOperationResultRS.builder()
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

    BatchTestCaseOperationResultRS expectedResult = BatchTestCaseOperationResultRS.builder()
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

    BatchTestCaseOperationResultRS expectedResult = BatchTestCaseOperationResultRS.builder()
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

    BatchTestCaseOperationResultRS expectedResult = BatchTestCaseOperationResultRS.builder()
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

    BatchTestCaseOperationResultRS expectedResult = BatchTestCaseOperationResultRS.builder()
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

    BatchTestCaseOperationResultRS expectedResult = BatchTestCaseOperationResultRS.builder()
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

  @Test
  void duplicateTestPlanTest() throws Exception {
    // Given
    Long testPlanId = 2L;
    Long duplicatedTestPlanId = 3L;
    TmsTestPlanRQ duplicateTestPlanRQ = new TmsTestPlanRQ();
    duplicateTestPlanRQ.setName("Duplicated Test Plan");
    duplicateTestPlanRQ.setDescription("This is a duplicated test plan");

    DuplicateTmsTestPlanRS expectedResult = DuplicateTmsTestPlanRS.builder()
        .id(duplicatedTestPlanId)
        .build();

    String jsonContent = objectMapper.writeValueAsString(duplicateTestPlanRQ);

    given(tmsTestPlanService.duplicate(projectId, testPlanId, duplicateTestPlanRQ))
        .willReturn(expectedResult);

    // When & Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/duplicate",
            projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(duplicatedTestPlanId));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).duplicate(projectId, testPlanId, duplicateTestPlanRQ);
  }

  @Test
  void duplicateTestPlanWithDifferentProjectTest() throws Exception {
    // Given
    Long testPlanId = 5L;
    Long duplicatedTestPlanId = 6L;
    TmsTestPlanRQ duplicateTestPlanRQ = new TmsTestPlanRQ();
    duplicateTestPlanRQ.setName("Another Duplicated Test Plan");
    duplicateTestPlanRQ.setDescription("Test plan duplicated in different project");

    DuplicateTmsTestPlanRS expectedResult = DuplicateTmsTestPlanRS.builder()
        .id(duplicatedTestPlanId)
        .build();

    String jsonContent = objectMapper.writeValueAsString(duplicateTestPlanRQ);

    given(tmsTestPlanService.duplicate(projectId, testPlanId, duplicateTestPlanRQ))
        .willReturn(expectedResult);

    // When & Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/duplicate",
            projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(duplicatedTestPlanId));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).duplicate(projectId, testPlanId, duplicateTestPlanRQ);
  }

  @Test
  void duplicateTestPlanWithMinimalDataTest() throws Exception {
    // Given
    Long testPlanId = 7L;
    Long duplicatedTestPlanId = 8L;
    TmsTestPlanRQ duplicateTestPlanRQ = new TmsTestPlanRQ();
    // Minimal data - only required fields if any

    DuplicateTmsTestPlanRS expectedResult = DuplicateTmsTestPlanRS.builder()
        .id(duplicatedTestPlanId)
        .build();

    String jsonContent = objectMapper.writeValueAsString(duplicateTestPlanRQ);

    given(tmsTestPlanService.duplicate(projectId, testPlanId, duplicateTestPlanRQ))
        .willReturn(expectedResult);

    // When & Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/duplicate",
            projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(duplicatedTestPlanId));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).duplicate(projectId, testPlanId, duplicateTestPlanRQ);
  }

  @Test
  void getTestCasesAddedToPlan_ShouldReturnPagedTestCases() throws Exception {
    // Given
    var testCaseInPlanRS = TmsTestCaseInTestPlanRS.builder()
        .id(testCaseId)
        .name("Test Case 1")
        .build();

    Page<TmsTestCaseInTestPlanRS> page = new Page<>(
        List.of(testCaseInPlanRS),
        10L,
        0L,
        1L,
        1L
    );

    given(tmsTestPlanService.getTestCasesAddedToPlan(eq(projectId), eq(testPlanId), any()))
        .willReturn(page);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case",
            projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].id").value(testCaseId))
        .andExpect(jsonPath("$.content[0].name").value("Test Case 1"))
        .andExpect(jsonPath("$.page.totalElements").value(1));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).getTestCasesAddedToPlan(eq(projectId), eq(testPlanId), any());
  }

  @Test
  void getTestCasesAddedToPlan_WithPagination_ShouldReturnPagedResults() throws Exception {
    // Given
    var testCaseInPlanRS = TmsTestCaseInTestPlanRS.builder()
        .id(testCaseId)
        .name("Test Case 1")
        .build();

    Page<TmsTestCaseInTestPlanRS> page = new Page<>(
        List.of(testCaseInPlanRS),
        20L,
        1L,
        50L,
        3L
    );

    given(tmsTestPlanService.getTestCasesAddedToPlan(eq(projectId), eq(testPlanId), any()))
        .willReturn(page);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case",
            projectKey, testPlanId)
            .param("offset", "20")
            .param("limit", "20")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.page.totalElements").value(50));

    verify(tmsTestPlanService).getTestCasesAddedToPlan(eq(projectId), eq(testPlanId), any());
  }

  @Test
  void getTestCaseInTestPlan_ShouldReturnTestCaseWithExecutions() throws Exception {
    // Given
    var testCaseInPlanRS = TmsTestCaseInTestPlanRS.builder()
        .id(testCaseId)
        .name("Test Case 1")
        .description("Description")
        .build();

    given(tmsTestPlanService.getTestCaseInTestPlan(projectId, testPlanId, testCaseId))
        .willReturn(testCaseInPlanRS);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case/{id}",
            projectKey, testPlanId, testCaseId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(testCaseId))
        .andExpect(jsonPath("$.name").value("Test Case 1"))
        .andExpect(jsonPath("$.description").value("Description"));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).getTestCaseInTestPlan(projectId, testPlanId, testCaseId);
  }

  @Test
  void getTestCasesAddedToPlan_WithEmptyResults_ShouldReturnEmptyPage() throws Exception {
    // Given
    Page<TmsTestCaseInTestPlanRS> emptyPage = new Page<>(
        Collections.emptyList(),
        10L,
        0L,
        0L,
        0L
    );

    given(tmsTestPlanService.getTestCasesAddedToPlan(eq(projectId), eq(testPlanId), any()))
        .willReturn(emptyPage);

    // When/Then
    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{testPlanId}/test-case",
            projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.page.totalElements").value(0));

    verify(tmsTestPlanService).getTestCasesAddedToPlan(eq(projectId), eq(testPlanId), any());
  }

  // ==================== TEST PLAN FOLDER RETRIEVAL TESTS ====================

  @Test
  public void testGetTestFoldersFromPlan_Success() throws Exception {
    Long testPlanId = 10L;
    TmsTestFolderRS folder1 = TmsTestFolderRS.builder()
        .id(1L)
        .name("Folder 1")
        .description("Description 1")
        .countOfTestCases(5L)
        .build();
    TmsTestFolderRS folder2 = TmsTestFolderRS.builder()
        .id(2L)
        .name("Folder 2")
        .description("Description 2")
        .countOfTestCases(3L)
        .build();

    Page<TmsTestFolderRS> expectedResponse = new Page<>(
        Arrays.asList(folder1, folder2),
        100L, // default size
        0L,   // number
        2L,   // totalElements
        1L    // totalPages
    );

    given(tmsTestPlanService.getTestFoldersFromPlan(eq(projectId), eq(testPlanId),
        any(OffsetRequest.class))).willReturn(expectedResponse);

    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{id}/folder", projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.content[0].name").value("Folder 1"))
        .andExpect(jsonPath("$.content[0].countOfTestCases").value(5))
        .andExpect(jsonPath("$.content[1].id").value(2))
        .andExpect(jsonPath("$.content[1].name").value("Folder 2"))
        .andExpect(jsonPath("$.content[1].countOfTestCases").value(3))
        .andExpect(jsonPath("$.page.totalElements").value(2));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).getTestFoldersFromPlan(eq(projectId), eq(testPlanId),
        any(OffsetRequest.class));
  }

  @Test
  public void testGetTestFoldersFromPlan_EmptyPage() throws Exception {
    Long testPlanId = 15L;
    Page<TmsTestFolderRS> emptyResponse = new Page<>(
        Collections.emptyList(),
        100L, // default size
        0L,   // number
        0L,   // totalElements
        0L    // totalPages
    );

    given(tmsTestPlanService.getTestFoldersFromPlan(eq(projectId), eq(testPlanId),
        any(OffsetRequest.class))).willReturn(emptyResponse);

    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{id}/folder", projectKey, testPlanId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(0))
        .andExpect(jsonPath("$.page.totalElements").value(0));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).getTestFoldersFromPlan(eq(projectId), eq(testPlanId),
        any(OffsetRequest.class));
  }

  @Test
  public void testGetTestFoldersFromPlan_WithPagination() throws Exception {
    Long testPlanId = 20L;
    TmsTestFolderRS folder1 = TmsTestFolderRS.builder()
        .id(3L)
        .name("Folder 3")
        .description("Description 3")
        .countOfTestCases(10L)
        .build();

    Page<TmsTestFolderRS> expectedResponse = new Page<>(
        Collections.singletonList(folder1),
        10L, // size
        1L,  // number (offset 10 / limit 10 = page 1)
        15L, // totalElements
        2L   // totalPages
    );

    given(tmsTestPlanService.getTestFoldersFromPlan(eq(projectId), eq(testPlanId),
        any(OffsetRequest.class))).willReturn(expectedResponse);

    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{id}/folder", projectKey, testPlanId)
            .param("offset", "10")
            .param("limit", "10")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].id").value(3))
        .andExpect(jsonPath("$.content[0].name").value("Folder 3"))
        .andExpect(jsonPath("$.content[0].countOfTestCases").value(10))
        .andExpect(jsonPath("$.page.totalElements").value(15))
        .andExpect(jsonPath("$.page.number").value(1));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).getTestFoldersFromPlan(eq(projectId), eq(testPlanId),
        any(OffsetRequest.class));
  }

  @Test
  public void testGetTestFoldersFromPlan_WithSort() throws Exception {
    Long testPlanId = 25L;
    TmsTestFolderRS folder1 = TmsTestFolderRS.builder()
        .id(1L)
        .name("A Folder")
        .description("Description A")
        .countOfTestCases(2L)
        .build();
    TmsTestFolderRS folder2 = TmsTestFolderRS.builder()
        .id(2L)
        .name("B Folder")
        .description("Description B")
        .countOfTestCases(3L)
        .build();

    Page<TmsTestFolderRS> expectedResponse = new Page<>(
        Arrays.asList(folder1, folder2),
        100L, // default size
        0L,   // number
        2L,   // totalElements
        1L    // totalPages
    );

    given(tmsTestPlanService.getTestFoldersFromPlan(eq(projectId), eq(testPlanId),
        any(OffsetRequest.class))).willReturn(expectedResponse);

    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{id}/folder", projectKey, testPlanId)
            .param("sort", "name,asc")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content" ).isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].name").value("A Folder"))
        .andExpect(jsonPath("$.content[1].name").value("B Folder"));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).getTestFoldersFromPlan(eq(projectId), eq(testPlanId),
        any(OffsetRequest.class));
  }

  @Test
  public void testGetTestFoldersFromPlan_WithPaginationAndSort() throws Exception {
    Long testPlanId = 30L;
    TmsTestFolderRS folder = TmsTestFolderRS.builder()
        .id(5L)
        .name("Folder 5")
        .description("Description 5")
        .countOfTestCases(7L)
        .build();

    Page<TmsTestFolderRS> expectedResponse = new Page<>(
        Collections.singletonList(folder),
        20L, // size
        0L,  // number
        1L,  // totalElements
        1L   // totalPages
    );

    given(tmsTestPlanService.getTestFoldersFromPlan(eq(projectId), eq(testPlanId),
        any(OffsetRequest.class))).willReturn(expectedResponse);

    mockMvc.perform(get("/v1/project/{projectKey}/tms/test-plan/{id}/folder", projectKey, testPlanId)
            .param("offset", "0")
            .param("limit", "20")
            .param("sort", "name,desc")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].id").value(5))
        .andExpect(jsonPath("$.content[0].name").value("Folder 5"))
        .andExpect(jsonPath("$.page.totalElements").value(1));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestPlanService).getTestFoldersFromPlan(eq(projectId), eq(testPlanId),
        any(OffsetRequest.class));
  }
}
