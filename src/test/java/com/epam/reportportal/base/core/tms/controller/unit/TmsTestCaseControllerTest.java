package com.epam.reportportal.base.core.tms.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.epam.reportportal.base.core.tms.controller.TestCaseController;
import com.epam.reportportal.base.core.tms.dto.DeleteTagsRQ;
import com.epam.reportportal.base.core.tms.dto.NewTestFolderRQ;
import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioAttachmentRQ;
import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioAttributeRQ;
import com.epam.reportportal.base.core.tms.dto.TmsStepRQ;
import com.epam.reportportal.base.core.tms.dto.TmsManualScenarioType;
import com.epam.reportportal.base.core.tms.dto.TmsStepsManualScenarioRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseAttributeRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseImportRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseRS;
import com.epam.reportportal.base.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchPatchTestCaseAttributesRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchDeleteTestCasesRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchDuplicateTestCasesRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchDuplicateTestCasesRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchPatchTestCasesRQ;
import com.epam.reportportal.base.core.tms.service.TmsTestCaseService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.commons.querygen.Filter;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.model.Page;
import com.epam.reportportal.base.util.OffsetRequest;
import com.epam.reportportal.base.util.ProjectExtractor;
import com.epam.reportportal.base.ws.resolver.FilterCriteriaResolver;
import com.epam.reportportal.base.ws.resolver.OffsetArgumentResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class TmsTestCaseControllerTest {

  private final long projectId = 1L;
  private final String projectKey = "test_project";

  @Mock
  private TmsTestCaseService tmsTestCaseService;

  @Mock
  private ProjectExtractor projectExtractor;

  @InjectMocks
  private TestCaseController testCaseController;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;
  private ReportPortalUser testUser;

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
    mockMvc = standaloneSetup(testCaseController)
        .setCustomArgumentResolvers(
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
            },
            new OffsetArgumentResolver(),
            new FilterCriteriaResolver()
        )
        .build();

    // Setup the project extractor mock to return a MembershipDetails with the projectId
    var membershipDetails = MembershipDetails.builder()
        .withProjectId(projectId)
        .withProjectKey(projectKey)
        .build();
    given(projectExtractor.extractMembershipDetails(eq(testUser), anyString()))
        .willReturn(membershipDetails);
  }

  @Test
  void getTestCaseByIdTest() throws Exception {
    // Given
    var testCaseId = 2L;
    var testCase = new TmsTestCaseRS();
    given(tmsTestCaseService.getById(projectId, testCaseId)).willReturn(testCase);

    // When/Then
    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/test-case/{testCaseId}", projectKey, testCaseId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).getById(projectId, testCaseId);
  }

  @Test
  void getTestCasesByCriteriaTest() throws Exception {
    // Given
    var testCases = List.of(new TmsTestCaseRS(), new TmsTestCaseRS());
    var page = new Page<>(testCases, 10, 0, 2, 1);

    given(tmsTestCaseService.getTestCasesByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class))).willReturn(page);

    // When/Then
    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/test-case", projectKey)
                .param("filter.cnt.name", "test")
                .param("offset", "0")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).getTestCasesByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class));
  }

  @Test
  void getTestCasesByCriteriaWithoutParametersTest() throws Exception {
    // Given
    var emptyTestCases = Collections.<TmsTestCaseRS>emptyList();
    var page = new Page<>(emptyTestCases, 100, 0, 0, 0);

    given(tmsTestCaseService.getTestCasesByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class))).willReturn(page);

    // When/Then
    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/test-case", projectKey)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).getTestCasesByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class));
  }

  @Test
  void getTestCasesByCriteriaWithFilterOnlyTest() throws Exception {
    // Given
    var testCases = List.of(new TmsTestCaseRS());
    var page = new Page<>(testCases, 100, 0, 1, 1);

    given(tmsTestCaseService.getTestCasesByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class))).willReturn(page);

    // When/Then
    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/test-case", projectKey)
                .param("filter.eq.priority", "HIGH")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).getTestCasesByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class));
  }

  @Test
  void getTestCasesByCriteriaWithPaginationOnlyTest() throws Exception {
    // Given
    var testCases = List.of(new TmsTestCaseRS(), new TmsTestCaseRS());
    var page = new Page<>(testCases, 20, 10, 2, 1);

    given(tmsTestCaseService.getTestCasesByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class))).willReturn(page);

    // When/Then
    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/test-case", projectKey)
                .param("offset", "10")
                .param("limit", "20")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).getTestCasesByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class));
  }

  @Test
  void getTestCasesByCriteriaWithSortTest() throws Exception {
    // Given
    var testCases = List.of(new TmsTestCaseRS(), new TmsTestCaseRS(), new TmsTestCaseRS());
    var page = new Page<>(testCases, 10, 0, 3, 1);

    given(tmsTestCaseService.getTestCasesByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class))).willReturn(page);

    // When/Then
    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/test-case", projectKey)
                .param("sort", "name,asc")
                .param("offset", "0")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).getTestCasesByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class));
  }

  @Test
  void getTestCasesByCriteriaWithMultipleFiltersTest() throws Exception {
    // Given
    var testCases = List.of(new TmsTestCaseRS());
    var page = new Page<>(testCases, 10, 0, 1, 1);

    given(tmsTestCaseService.getTestCasesByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class))).willReturn(page);

    // When/Then
    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/test-case", projectKey)
                .param("filter.eq.priority", "HIGH")
                .param("filter.cnt.name", "test")
                .param("offset", "0")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).getTestCasesByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class));
  }

  @Test
  void getTestCasesByCriteriaWithAllParametersTest() throws Exception {
    // Given
    var testCases = List.of(new TmsTestCaseRS(), new TmsTestCaseRS());
    var page = new Page<>(testCases, 20, 10, 2, 1);

    given(tmsTestCaseService.getTestCasesByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class))).willReturn(page);

    // When/Then
    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/test-case", projectKey)
                .param("filter.cnt.name", "comprehensive")
                .param("filter.eq.priority", "CRITICAL")
                .param("offset", "10")
                .param("limit", "20")
                .param("sort", "createdAt,desc")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).getTestCasesByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class));
  }

  @Test
  void createTestCase_WithExistingFolder() throws Exception {
    // Given
    Long existingFolderId = 5L;
    var textManualScenario = TmsTextManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.TEXT)
        .instructions("Test instructions")
        .expectedResult("Expected result example")
        .executionEstimationTime(15)
        .attachments(List.of(
            TmsManualScenarioAttachmentRQ.builder()
                .id("attachment-001")
                .build()
        ))
        .attributes(List.of(
            TmsManualScenarioAttributeRQ.builder().id(1L).value("manual").build()
        ))
        .build();

    var testCaseRequest = TmsTestCaseRQ.builder()
        .name("Test Case With Existing Folder")
        .description("Description for test case")
        .priority("HIGH")
        .externalId("EXT-001")
        .testFolderId(existingFolderId)
        .attributes(List.of(
            TmsTestCaseAttributeRQ.builder().id(1L).build(),
            TmsTestCaseAttributeRQ.builder().id(1L).build()
        ))
        .manualScenario(textManualScenario)
        .build();

    var testCase = new TmsTestCaseRS();
    var jsonContent = objectMapper.writeValueAsString(testCaseRequest);

    given(tmsTestCaseService.create(projectId, testCaseRequest)).willReturn(testCase);

    // When/Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/test-case", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).create(projectId, testCaseRequest);
  }

  @Test
  void createTestCase_WithNewFolder() throws Exception {
    // Given
    var newTestFolder = NewTestFolderRQ.builder()
        .name("New Test Folder")
        .build();
    var testCaseRequest = TmsTestCaseRQ.builder()
        .name("Test Case With New Folder")
        .description("Description for test case")
        .priority("MEDIUM")
        .testFolder(newTestFolder)
        .externalId("EXT-002")
        .attributes(List.of(
            TmsTestCaseAttributeRQ.builder().id(1L).build()
        ))
        .build();

    var testCase = new TmsTestCaseRS();
    var jsonContent = objectMapper.writeValueAsString(testCaseRequest);

    given(tmsTestCaseService.create(projectId, testCaseRequest)).willReturn(testCase);

    // When/Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/test-case", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).create(projectId, testCaseRequest);
  }

  @Test
  void createTestCase_WithNewFolderAndParent() throws Exception {
    // Given
    Long parentFolderId = 10L;
    var newTestFolder = NewTestFolderRQ.builder()
        .name("New Test Folder with Parent")
        .parentTestFolderId(parentFolderId)
        .build();

    var stepsManualScenario = TmsStepsManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.STEPS)
        .executionEstimationTime(30)
        .steps(List.of(
            TmsStepRQ.builder()
                .instructions("Step 1: Navigate to login page")
                .expectedResult("Login page should be displayed")
                .attachments(List.of(
                    TmsManualScenarioAttachmentRQ.builder()
                        .id("step-attachment-001")
                        .build()
                ))
                .build(),
            TmsStepRQ.builder()
                .instructions("Step 2: Enter valid credentials")
                .expectedResult("User should be logged in successfully")
                .build()
        ))
        .build();

    var testCaseRequest = TmsTestCaseRQ.builder()
        .name("Test Case With New Nested Folder")
        .description("Description for nested test case")
        .priority("HIGH")
        .testFolder(newTestFolder)
        .attributes(List.of(
            TmsTestCaseAttributeRQ.builder().id(1L).build(),
            TmsTestCaseAttributeRQ.builder().id(1L).build()
        ))
        .manualScenario(stepsManualScenario)
        .build();

    var testCase = new TmsTestCaseRS();
    var jsonContent = objectMapper.writeValueAsString(testCaseRequest);

    given(tmsTestCaseService.create(projectId, testCaseRequest)).willReturn(testCase);

    // When/Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/test-case", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).create(projectId, testCaseRequest);
  }

  @Test
  void updateTestCase_WithExistingFolder() throws Exception {
    // Given
    var testCaseId = 2L;
    Long existingFolderId = 7L;
    var testCaseRequest = TmsTestCaseRQ.builder()
        .name("Updated Test Case")
        .description("Updated description")
        .priority("CRITICAL")
        .testFolderId(existingFolderId)
        .externalId("EXT-UPD-001")
        .build();

    var testCase = new TmsTestCaseRS();
    var jsonContent = objectMapper.writeValueAsString(testCaseRequest);

    given(tmsTestCaseService.update(projectId, testCaseId, testCaseRequest)).willReturn(testCase);

    // When/Then
    mockMvc.perform(
            put("/v1/project/{projectKey}/tms/test-case/{testCaseId}", projectKey, testCaseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).update(projectId, testCaseId, testCaseRequest);
  }

  @Test
  void updateTestCase_WithNewFolder() throws Exception {
    // Given
    var testCaseId = 2L;
    var newTestFolder = NewTestFolderRQ.builder()
        .name("Updated New Test Folder")
        .parentTestFolderId(15L)
        .build();
    var testCaseRequest = TmsTestCaseRQ.builder()
        .name("Updated Test Case with New Folder")
        .description("Updated description")
        .priority("MEDIUM")
        .testFolder(newTestFolder)
        .attributes(List.of(
            TmsTestCaseAttributeRQ.builder().id(1L).build()
        ))
        .build();

    var testCase = new TmsTestCaseRS();
    var jsonContent = objectMapper.writeValueAsString(testCaseRequest);

    given(tmsTestCaseService.update(projectId, testCaseId, testCaseRequest)).willReturn(testCase);

    // When/Then
    mockMvc.perform(
            put("/v1/project/{projectKey}/tms/test-case/{testCaseId}", projectKey, testCaseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).update(projectId, testCaseId, testCaseRequest);
  }

  @Test
  void updateTestCase_ChangeFromExistingToNewFolder() throws Exception {
    // Given
    var testCaseId = 2L;
    var textManualScenario = TmsTextManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.TEXT)
        .instructions("Updated test instructions")
        .expectedResult("Updated expected result")
        .executionEstimationTime(25)
        .build();

    var newTestFolder = NewTestFolderRQ.builder()
        .name("New Folder for Updated Test Case")
        .build();

    var testCaseRequest = TmsTestCaseRQ.builder()
        .name("Updated Test Case With New Folder")
        .description("Updated description with new folder")
        .priority("LOW")
        .externalId("EXT-UPD-002")
        .testFolder(newTestFolder)
        .manualScenario(textManualScenario)
        .build();

    var testCase = new TmsTestCaseRS();
    var jsonContent = objectMapper.writeValueAsString(testCaseRequest);

    given(tmsTestCaseService.update(projectId, testCaseId, testCaseRequest)).willReturn(testCase);

    // When/Then
    mockMvc.perform(
            put("/v1/project/{projectKey}/tms/test-case/{testCaseId}", projectKey, testCaseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).update(projectId, testCaseId, testCaseRequest);
  }

  @Test
  void patchTestCase_OnlyName() throws Exception {
    // Given
    var testCaseId = 2L;
    var testCaseRequest = TmsTestCaseRQ.builder()
        .name("Patched Test Case Name")
        .build();

    var testCase = new TmsTestCaseRS();
    var jsonContent = objectMapper.writeValueAsString(testCaseRequest);

    given(tmsTestCaseService.patch(projectId, testCaseId, testCaseRequest)).willReturn(testCase);

    // When/Then
    mockMvc.perform(
            patch("/v1/project/{projectKey}/tms/test-case/{testCaseId}", projectKey, testCaseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).patch(projectId, testCaseId, testCaseRequest);
  }

  @Test
  void patchTestCase_WithExistingFolder() throws Exception {
    // Given
    var testCaseId = 2L;
    Long existingFolderId = 8L;
    var testCaseRequest = TmsTestCaseRQ.builder()
        .testFolderId(existingFolderId)
        .priority("HIGH")
        .build();

    var testCase = new TmsTestCaseRS();
    var jsonContent = objectMapper.writeValueAsString(testCaseRequest);

    given(tmsTestCaseService.patch(projectId, testCaseId, testCaseRequest)).willReturn(testCase);

    // When/Then
    mockMvc.perform(
            patch("/v1/project/{projectKey}/tms/test-case/{testCaseId}", projectKey, testCaseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).patch(projectId, testCaseId, testCaseRequest);
  }

  @Test
  void patchTestCase_WithNewFolder() throws Exception {
    // Given
    var testCaseId = 2L;
    var newTestFolder = NewTestFolderRQ.builder()
        .name("Patched New Folder")
        .build();
    var testCaseRequest = TmsTestCaseRQ.builder()
        .testFolder(newTestFolder)
        .description("Patched description")
        .build();

    var testCase = new TmsTestCaseRS();
    var jsonContent = objectMapper.writeValueAsString(testCaseRequest);

    given(tmsTestCaseService.patch(projectId, testCaseId, testCaseRequest)).willReturn(testCase);

    // When/Then
    mockMvc.perform(
            patch("/v1/project/{projectKey}/tms/test-case/{testCaseId}", projectKey, testCaseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).patch(projectId, testCaseId, testCaseRequest);
  }

  @Test
  void patchTestCase_WithTags() throws Exception {
    // Given
    var testCaseId = 2L;
    var testCaseRequest = TmsTestCaseRQ.builder()
        .attributes(List.of(
            TmsTestCaseAttributeRQ.builder().id(1L).build(),
            TmsTestCaseAttributeRQ.builder().id(1L).build()
        ))
        .build();

    var testCase = new TmsTestCaseRS();
    var jsonContent = objectMapper.writeValueAsString(testCaseRequest);

    given(tmsTestCaseService.patch(projectId, testCaseId, testCaseRequest)).willReturn(testCase);

    // When/Then
    mockMvc.perform(
            patch("/v1/project/{projectKey}/tms/test-case/{testCaseId}", projectKey, testCaseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).patch(projectId, testCaseId, testCaseRequest);
  }

  @Test
  void deleteTestCaseTest() throws Exception {
    // Given
    var testCaseId = 2L;

    // When/Then
    mockMvc.perform(
            delete("/v1/project/{projectKey}/tms/test-case/{testCaseId}", projectKey, testCaseId)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).delete(projectId, testCaseId);
  }

  @Test
  void deleteTestCasesTest() throws Exception {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var deleteRequest = BatchDeleteTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .build();

    var jsonContent = objectMapper.writeValueAsString(deleteRequest);

    // When/Then
    mockMvc.perform(
            delete("/v1/project/{projectKey}/tms/test-case/batch", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isNoContent());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).delete(projectId, deleteRequest);
  }

  @Test
  void deleteTestCasesWithSingleIdTest() throws Exception {
    // Given
    var testCaseIds = List.of(1L);
    var deleteRequest = BatchDeleteTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .build();

    var jsonContent = objectMapper.writeValueAsString(deleteRequest);

    // When/Then
    mockMvc.perform(
            delete("/v1/project/{projectKey}/tms/test-case/batch", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isNoContent());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).delete(projectId, deleteRequest);
  }

  @Test
  void batchPatchTestCasesTest() throws Exception {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var patchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(5L)
        .build();

    var jsonContent = objectMapper.writeValueAsString(patchRequest);

    // When/Then
    mockMvc.perform(
            patch("/v1/project/{projectKey}/tms/test-case/batch", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).patch(projectId, patchRequest);
  }

  @Test
  void batchPatchTestCasesWithSingleIdTest() throws Exception {
    // Given
    var testCaseIds = List.of(1L);
    var patchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(5L)
        .build();

    var jsonContent = objectMapper.writeValueAsString(patchRequest);

    // When/Then
    mockMvc.perform(
            patch("/v1/project/{projectKey}/tms/test-case/batch", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).patch(projectId, patchRequest);
  }

  @Test
  void batchPatchTestCasesWithNewFolderTest() throws Exception {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var newFolder = NewTestFolderRQ.builder()
        .name("New Batch Folder")
        .build();
    var patchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolder(newFolder)
        .build();

    var jsonContent = objectMapper.writeValueAsString(patchRequest);

    // When/Then
    mockMvc.perform(
            patch("/v1/project/{projectKey}/tms/test-case/batch", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).patch(projectId, patchRequest);
  }

  @Test
  void batchPatchTestCasesWithNewFolderAndParentTest() throws Exception {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L);
    var parentFolderId = 10L;
    var newFolder = NewTestFolderRQ.builder()
        .name("New Nested Batch Folder")
        .parentTestFolderId(parentFolderId)
        .build();
    var patchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolder(newFolder)
        .build();

    var jsonContent = objectMapper.writeValueAsString(patchRequest);

    // When/Then
    mockMvc.perform(
            patch("/v1/project/{projectKey}/tms/test-case/batch", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).patch(projectId, patchRequest);
  }

  @Test
  void batchPatchTestCasesWithNewFolderAndPriorityTest() throws Exception {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var newFolder = NewTestFolderRQ.builder()
        .name("New Folder With Priority")
        .build();
    var patchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolder(newFolder)
        .priority("HIGH")
        .build();

    var jsonContent = objectMapper.writeValueAsString(patchRequest);

    // When/Then
    mockMvc.perform(
            patch("/v1/project/{projectKey}/tms/test-case/batch", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).patch(projectId, patchRequest);
  }

  @Test
  void batchPatchTestCasesWithOnlyPriorityTest() throws Exception {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var patchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .priority("CRITICAL")
        .build();

    var jsonContent = objectMapper.writeValueAsString(patchRequest);

    // When/Then
    mockMvc.perform(
            patch("/v1/project/{projectKey}/tms/test-case/batch", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).patch(projectId, patchRequest);
  }

  @Test
  void batchPatchTestCasesWithTestFolderIdAndPriorityTest() throws Exception {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L);
    var patchRequest = BatchPatchTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(7L)
        .priority("MEDIUM")
        .build();

    var jsonContent = objectMapper.writeValueAsString(patchRequest);

    // When/Then
    mockMvc.perform(
            patch("/v1/project/{projectKey}/tms/test-case/batch", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).patch(projectId, patchRequest);
  }

  @Test
  void importTestCasesWithTestFolderIdTest() throws Exception {
    // Given
    var fileContent = "test,case,data";
    var file = new MockMultipartFile("file", "test.csv", "text/csv", fileContent.getBytes());
    var testFolderId = 3L;
    var importResult = TmsTestCaseImportRS.of(List.of(1L, 2L), 3L, 2);

    given(tmsTestCaseService.importFromFile(eq(projectId), eq(testFolderId), eq(null), eq(file)))
        .willReturn(importResult);

    // When/Then
    mockMvc.perform(
            multipart("/v1/project/{projectKey}/tms/test-case/import", projectKey)
                .file(file)
                .param("testFolderId", String.valueOf(testFolderId))
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).importFromFile(eq(projectId), eq(testFolderId), eq(null), eq(file));
  }

  @Test
  void importTestCasesWithTestFolderNameTest() throws Exception {
    // Given
    var fileContent = "test,case,data";
    var file = new MockMultipartFile("file", "test.csv", "text/csv", fileContent.getBytes());
    var testFolderName = "Test Folder";
    var importResult = TmsTestCaseImportRS.of(List.of(1L), 3L, 1);

    given(tmsTestCaseService.importFromFile(eq(projectId), eq(null), eq(testFolderName), eq(file)))
        .willReturn(importResult);

    // When/Then
    mockMvc.perform(
            multipart("/v1/project/{projectKey}/tms/test-case/import", projectKey)
                .file(file)
                .param("testFolderName", testFolderName)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).importFromFile(eq(projectId), eq(null), eq(testFolderName), eq(file));
  }

  @Test
  void importTestCasesWithoutFolderParametersTest() throws Exception {
    // Given
    var fileContent = "test,case,data";
    var file = new MockMultipartFile("file", "test.csv", "text/csv", fileContent.getBytes());
    var importResult = TmsTestCaseImportRS.of(List.of(1L), 3L, 1);

    given(tmsTestCaseService.importFromFile(eq(projectId), eq(null), eq(null), eq(file)))
        .willReturn(importResult);

    // When/Then
    mockMvc.perform(
            multipart("/v1/project/{projectKey}/tms/test-case/import", projectKey)
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).importFromFile(eq(projectId), eq(null), eq(null), eq(file));
  }

  @Test
  void importTestCasesWithBothFolderParametersTest() throws Exception {
    // Given
    var fileContent = "test,case,data";
    var file = new MockMultipartFile("file", "test.csv", "text/csv", fileContent.getBytes());
    var testFolderId = 3L;
    var testFolderName = "Test Folder";
    var importResult = TmsTestCaseImportRS.of(List.of(1L), 3L, 1);

    given(tmsTestCaseService.importFromFile(eq(projectId), eq(testFolderId), eq(testFolderName), eq(file)))
        .willReturn(importResult);

    // When/Then
    mockMvc.perform(
            multipart("/v1/project/{projectKey}/tms/test-case/import", projectKey)
                .file(file)
                .param("testFolderId", String.valueOf(testFolderId))
                .param("testFolderName", testFolderName)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).importFromFile(eq(projectId), eq(testFolderId), eq(testFolderName), eq(file));
  }

  @Test
  void exportTestCasesWithIdsTest() throws Exception {
    // Given
    var testCaseIds = List.of(1L, 2L, 3L);
    var format = "JSON";
    var includeAttachments = true;

    // When/Then
    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/test-case/export", projectKey)
                .param("ids", "1,2,3")
                .param("format", format)
                .param("includeAttachments", "true")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).exportToFile(eq(projectId), eq(testCaseIds), eq(format),
        eq(includeAttachments), any(HttpServletResponse.class));
  }

  @Test
  void exportTestCasesWithoutParametersTest() throws Exception {
    // When/Then
    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/test-case/export", projectKey)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).exportToFile(eq(projectId), eq(null), eq("JSON"),
        eq(false), any(HttpServletResponse.class));
  }

  @Test
  void exportTestCasesWithCsvFormatTest() throws Exception {
    // Given
    var format = "CSV";

    // When/Then
    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/test-case/export", projectKey)
                .param("format", format)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).exportToFile(eq(projectId), eq(null), eq(format),
        eq(false), any(HttpServletResponse.class));
  }

  @Test
  void deleteTagsFromTestCaseTest() throws Exception {
    // Given
    var testCaseId = 2L;
    var tagIds = Arrays.asList(1L, 2L, 3L);
    var deleteRequest = new DeleteTagsRQ();
    deleteRequest.setTagIds(tagIds);

    var jsonContent = objectMapper.writeValueAsString(deleteRequest);

    // When/Then
    mockMvc.perform(
            delete("/v1/project/{projectKey}/tms/test-case/{testCaseId}/tags", projectKey, testCaseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isNoContent());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).deleteAttributesFromTestCase(projectId, testCaseId, tagIds);
  }

  @Test
  void deleteTagsFromTestCaseWithSingleTagTest() throws Exception {
    // Given
    var testCaseId = 2L;
    var tagIds = List.of(1L);
    var deleteRequest = new DeleteTagsRQ();
    deleteRequest.setTagIds(tagIds);

    var jsonContent = objectMapper.writeValueAsString(deleteRequest);

    // When/Then
    mockMvc.perform(
            delete("/v1/project/{projectKey}/tms/test-case/{testCaseId}/tags", projectKey, testCaseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isNoContent());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).deleteAttributesFromTestCase(projectId, testCaseId, tagIds);
  }

  @Test
  void patchTestCaseAttributesTest() throws Exception {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var attributesToRemove = Arrays.asList(4L, 5L);
    var attributeIdsToAdd = Arrays.asList(6L, 7L);
    var patchRequest = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(testCaseIds)
        .attributesToRemove(attributesToRemove)
        .attributeIdsToAdd(attributeIdsToAdd)
        .build();

    var jsonContent = objectMapper.writeValueAsString(patchRequest);

    // When/Then
    mockMvc.perform(
            patch("/v1/project/{projectKey}/tms/test-case/attributes/batch", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).patchTestCaseAttributes(projectId, patchRequest);
  }

  @Test
  void patchTestCaseAttributesWithSingleTestCaseTest() throws Exception {
    // Given
    var testCaseIds = List.of(1L);
    var attributesToRemove = Arrays.asList(4L, 5L, 6L);
    var attributeIdsToAdd = Arrays.asList(7L, 8L);
    var patchRequest = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(testCaseIds)
        .attributesToRemove(attributesToRemove)
        .attributeIdsToAdd(attributeIdsToAdd)
        .build();

    var jsonContent = objectMapper.writeValueAsString(patchRequest);

    // When/Then
    mockMvc.perform(
            patch("/v1/project/{projectKey}/tms/test-case/attributes/batch", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).patchTestCaseAttributes(projectId, patchRequest);
  }

  @Test
  void patchTestCaseAttributesOnlyRemoveTest() throws Exception {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L);
    var attributesToRemove = Arrays.asList(4L, 5L);
    var patchRequest = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(testCaseIds)
        .attributesToRemove(attributesToRemove)
        .build();

    var jsonContent = objectMapper.writeValueAsString(patchRequest);

    // When/Then
    mockMvc.perform(
            patch("/v1/project/{projectKey}/tms/test-case/attributes/batch", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).patchTestCaseAttributes(projectId, patchRequest);
  }

  @Test
  void patchTestCaseAttributesOnlyAddTest() throws Exception {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var attributeIdsToAdd = Arrays.asList(6L, 7L, 8L);
    var patchRequest = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(testCaseIds)
        .attributeIdsToAdd(attributeIdsToAdd)
        .build();

    var jsonContent = objectMapper.writeValueAsString(patchRequest);

    // When/Then
    mockMvc.perform(
            patch("/v1/project/{projectKey}/tms/test-case/attributes/batch", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).patchTestCaseAttributes(projectId, patchRequest);
  }

  @Test
  void patchTestCaseAttributesWithEmptyAttributeListsTest() throws Exception {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L);
    var patchRequest = BatchPatchTestCaseAttributesRQ.builder()
        .testCaseIds(testCaseIds)
        .attributesToRemove(Collections.emptyList())
        .attributeIdsToAdd(Collections.emptyList())
        .build();

    var jsonContent = objectMapper.writeValueAsString(patchRequest);

    // When/Then
    mockMvc.perform(
            patch("/v1/project/{projectKey}/tms/test-case/attributes/batch", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isBadRequest());
  }

  @Test
  void duplicateTestCasesTest() throws Exception {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var testFolderId = 5L;
    var duplicateRequest = BatchDuplicateTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(testFolderId)
        .build();

    var duplicatedTestCases = List.of(new TmsTestCaseRS(), new TmsTestCaseRS(), new TmsTestCaseRS());
    var duplicateResponse = BatchDuplicateTestCasesRS.builder()
        .testFolderId(testFolderId)
        .testCases(duplicatedTestCases)
        .build();

    var jsonContent = objectMapper.writeValueAsString(duplicateRequest);

    given(tmsTestCaseService.duplicate(projectId, duplicateRequest)).willReturn(duplicateResponse);

    // When/Then
    mockMvc.perform(
            post("/v1/project/{projectKey}/tms/test-case/batch/duplicate", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).duplicate(projectId, duplicateRequest);
  }

  @Test
  void duplicateTestCasesWithSingleIdTest() throws Exception {
    // Given
    var testCaseIds = List.of(1L);
    var testFolderId = 5L;
    var duplicateRequest = BatchDuplicateTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(testFolderId)
        .build();

    var duplicatedTestCases = List.of(new TmsTestCaseRS());
    var duplicateResponse = BatchDuplicateTestCasesRS.builder()
        .testFolderId(testFolderId)
        .testCases(duplicatedTestCases)
        .build();

    var jsonContent = objectMapper.writeValueAsString(duplicateRequest);

    given(tmsTestCaseService.duplicate(projectId, duplicateRequest)).willReturn(duplicateResponse);

    // When/Then
    mockMvc.perform(
            post("/v1/project/{projectKey}/tms/test-case/batch/duplicate", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).duplicate(projectId, duplicateRequest);
  }

  @Test
  void duplicateTestCasesWithMultipleIdsTest() throws Exception {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);
    var testFolderId = 10L;
    var duplicateRequest = BatchDuplicateTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolderId(testFolderId)
        .build();

    var duplicatedTestCases = List.of(
        new TmsTestCaseRS(), new TmsTestCaseRS(), new TmsTestCaseRS(),
        new TmsTestCaseRS(), new TmsTestCaseRS()
    );
    var duplicateResponse = BatchDuplicateTestCasesRS.builder()
        .testFolderId(testFolderId)
        .testCases(duplicatedTestCases)
        .build();

    var jsonContent = objectMapper.writeValueAsString(duplicateRequest);

    given(tmsTestCaseService.duplicate(projectId, duplicateRequest)).willReturn(duplicateResponse);

    // When/Then
    mockMvc.perform(
            post("/v1/project/{projectKey}/tms/test-case/batch/duplicate", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).duplicate(projectId, duplicateRequest);
  }

  @Test
  void duplicateTestCasesWithNewFolderTest() throws Exception {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L);
    var newFolder = NewTestFolderRQ.builder()
        .name("New Duplicate Folder")
        .parentTestFolderId(3L)
        .build();
    var duplicateRequest = BatchDuplicateTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .testFolder(newFolder)
        .build();

    var duplicatedTestCases = List.of(new TmsTestCaseRS(), new TmsTestCaseRS());
    var createdFolderId = 15L;
    var duplicateResponse = BatchDuplicateTestCasesRS.builder()
        .testFolderId(createdFolderId)
        .testCases(duplicatedTestCases)
        .build();

    var jsonContent = objectMapper.writeValueAsString(duplicateRequest);

    given(tmsTestCaseService.duplicate(projectId, duplicateRequest)).willReturn(duplicateResponse);

    // When/Then
    mockMvc.perform(
            post("/v1/project/{projectKey}/tms/test-case/batch/duplicate", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).duplicate(projectId, duplicateRequest);
  }

  @Test
  void duplicateTestCasesWithEmptyListTest() throws Exception {
    // Given
    var testCaseIds = Collections.<Long>emptyList();
    var duplicateRequest = BatchDuplicateTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .build();

    var jsonContent = objectMapper.writeValueAsString(duplicateRequest);

    // When/Then
    mockMvc.perform(
            post("/v1/project/{projectKey}/tms/test-case/batch/duplicate", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isBadRequest());

    verify(tmsTestCaseService, never()).duplicate(projectId, duplicateRequest);
  }
}
