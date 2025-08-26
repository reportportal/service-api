package com.epam.ta.reportportal.core.tms.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.tms.controller.TestCaseController;
import com.epam.ta.reportportal.core.tms.dto.DeleteTagsRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsAttributeRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioAttachmentRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioStepRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsManualScenarioType;
import com.epam.ta.reportportal.core.tms.dto.TmsStepsManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTextManualScenarioRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDeleteTagsRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchDeleteTestCasesRQ;
import com.epam.ta.reportportal.core.tms.dto.batch.BatchPatchTestCasesRQ;
import com.epam.ta.reportportal.core.tms.service.TmsTestCaseService;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.util.ProjectExtractor;
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
import org.springframework.data.domain.Pageable;
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
    var search = "test search";
    var testFolderId = 5L;
    var testPlanId = 3L;
    var testCases = List.of(new TmsTestCaseRS(), new TmsTestCaseRS());
    var page = new Page<>(testCases, 10, 0, 2, 1);

    given(tmsTestCaseService.getTestCasesByCriteria(eq(projectId), eq(search),
        eq(testFolderId), eq(testPlanId), any(Pageable.class))).willReturn(page);

    // When/Then
    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/test-case", projectKey)
                .param("search", search)
                .param("testFolderId", Long.toString(testFolderId))
                .param("testPlanId", Long.toString(testPlanId))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).getTestCasesByCriteria(eq(projectId), eq(search),
        eq(testFolderId), eq(testPlanId), any(Pageable.class));
  }

  @Test
  void getTestCasesByCriteriaWithoutParametersTest() throws Exception {
    // Given
    var emptyTestCases = Collections.<TmsTestCaseRS>emptyList();
    var page = new Page<>(emptyTestCases, 10, 0, 0, 0);

    given(tmsTestCaseService.getTestCasesByCriteria(eq(projectId), isNull(),
        isNull(), isNull(), any(Pageable.class))).willReturn(page);

    // When/Then
    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/test-case", projectKey)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).getTestCasesByCriteria(eq(projectId), isNull(),
        isNull(), isNull(), any(Pageable.class));
  }

  @Test
  void getTestCasesByCriteriaWithSearchOnlyTest() throws Exception {
    // Given
    var search = "search query";
    var testCases = List.of(new TmsTestCaseRS());
    var page = new Page<>(testCases, 10, 0, 1, 1);

    given(tmsTestCaseService.getTestCasesByCriteria(eq(projectId), eq(search),
        isNull(), isNull(), any(Pageable.class))).willReturn(page);

    // When/Then
    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/test-case", projectKey)
                .param("search", search)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).getTestCasesByCriteria(eq(projectId), eq(search),
        isNull(), isNull(), any(Pageable.class));
  }

  @Test
  void getTestCasesByCriteriaWithTestFolderIdOnlyTest() throws Exception {
    // Given
    var testFolderId = 10L;
    var testCases = List.of(new TmsTestCaseRS(), new TmsTestCaseRS());
    var page = new Page<>(testCases, 10, 0, 2, 1);

    given(tmsTestCaseService.getTestCasesByCriteria(eq(projectId), isNull(),
        eq(testFolderId), isNull(), any(Pageable.class))).willReturn(page);

    // When/Then
    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/test-case", projectKey)
                .param("testFolderId", Long.toString(testFolderId))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).getTestCasesByCriteria(eq(projectId), isNull(),
        eq(testFolderId), isNull(), any(Pageable.class));
  }

  @Test
  void getTestCasesByCriteriaWithTestPlanIdOnlyTest() throws Exception {
    // Given
    var testPlanId = 7L;
    var testCases = List.of(new TmsTestCaseRS(), new TmsTestCaseRS(), new TmsTestCaseRS());
    var page = new Page<>(testCases, 10, 0, 3, 1);

    given(tmsTestCaseService.getTestCasesByCriteria(eq(projectId), isNull(),
        isNull(), eq(testPlanId), any(Pageable.class))).willReturn(page);

    // When/Then
    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/test-case", projectKey)
                .param("testPlanId", Long.toString(testPlanId))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).getTestCasesByCriteria(eq(projectId), isNull(),
        isNull(), eq(testPlanId), any(Pageable.class));
  }

  @Test
  void getTestCasesByCriteriaWithTestFolderAndTestPlanIdsTest() throws Exception {
    // Given
    var testFolderId = 15L;
    var testPlanId = 12L;
    var testCases = List.of(new TmsTestCaseRS());
    var page = new Page<>(testCases, 10, 0, 1, 1);

    given(tmsTestCaseService.getTestCasesByCriteria(eq(projectId), isNull(),
        eq(testFolderId), eq(testPlanId), any(Pageable.class))).willReturn(page);

    // When/Then
    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/test-case", projectKey)
                .param("testFolderId", Long.toString(testFolderId))
                .param("testPlanId", Long.toString(testPlanId))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).getTestCasesByCriteria(eq(projectId), isNull(),
        eq(testFolderId), eq(testPlanId), any(Pageable.class));
  }

  @Test
  void getTestCasesByCriteriaWithAllParametersTest() throws Exception {
    // Given
    var search = "comprehensive search";
    var testFolderId = 20L;
    var testPlanId = 25L;
    var testCases = List.of(new TmsTestCaseRS(), new TmsTestCaseRS());
    var page = new Page<>(testCases, 10, 0, 2, 1);

    given(tmsTestCaseService.getTestCasesByCriteria(eq(projectId), eq(search),
        eq(testFolderId), eq(testPlanId), any(Pageable.class))).willReturn(page);

    // When/Then
    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/test-case", projectKey)
                .param("search", search)
                .param("testFolderId", Long.toString(testFolderId))
                .param("testPlanId", Long.toString(testPlanId))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).getTestCasesByCriteria(eq(projectId), eq(search),
        eq(testFolderId), eq(testPlanId), any(Pageable.class));
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
        .linkToRequirements("https://requirements.example.com")
        .attachments(List.of(
            TmsManualScenarioAttachmentRQ.builder()
                .id("attachment-001")
                .build()
        ))
        .tags(List.of(
            TmsAttributeRQ.builder().key("scenario").value("manual").build()
        ))
        .build();

    var testCaseRequest = TmsTestCaseRQ.builder()
        .name("Test Case With Existing Folder")
        .description("Description for test case")
        .priority("HIGH")
        .externalId("EXT-001")
        .testFolderId(existingFolderId)
        .tags(List.of(
            TmsAttributeRQ.builder().key("severity").value("critical").build(),
            TmsAttributeRQ.builder().key("component").value("auth").build()
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
    var newTestFolder = TmsTestCaseTestFolderRQ.builder()
        .name("New Test Folder")
        .build();
    var testCaseRequest = TmsTestCaseRQ.builder()
        .name("Test Case With New Folder")
        .description("Description for test case")
        .priority("MEDIUM")
        .testFolder(newTestFolder)
        .externalId("EXT-002")
        .tags(List.of(
            TmsAttributeRQ.builder().key("type").value("smoke").build()
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
    var newTestFolder = TmsTestCaseTestFolderRQ.builder()
        .name("New Test Folder with Parent")
        .parentTestFolderId(parentFolderId)
        .build();

    var stepsManualScenario = TmsStepsManualScenarioRQ.builder()
        .manualScenarioType(TmsManualScenarioType.STEPS)
        .executionEstimationTime(30)
        .steps(List.of(
            TmsManualScenarioStepRQ.builder()
                .instructions("Step 1: Navigate to login page")
                .expectedResult("Login page should be displayed")
                .attachments(List.of(
                    TmsManualScenarioAttachmentRQ.builder()
                        .id("step-attachment-001")
                        .build()
                ))
                .build(),
            TmsManualScenarioStepRQ.builder()
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
        .tags(List.of(
            TmsAttributeRQ.builder().key("area").value("ui").build(),
            TmsAttributeRQ.builder().key("browser").value("chrome").build()
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
    var newTestFolder = TmsTestCaseTestFolderRQ.builder()
        .name("Updated New Test Folder")
        .parentTestFolderId(15L)
        .build();
    var testCaseRequest = TmsTestCaseRQ.builder()
        .name("Updated Test Case with New Folder")
        .description("Updated description")
        .priority("MEDIUM")
        .testFolder(newTestFolder)
        .tags(List.of(
            TmsAttributeRQ.builder().key("updated").value("true").build()
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

    var newTestFolder = TmsTestCaseTestFolderRQ.builder()
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
    var newTestFolder = TmsTestCaseTestFolderRQ.builder()
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
        .tags(List.of(
            TmsAttributeRQ.builder().key("patched").value("yes").build(),
            TmsAttributeRQ.builder().key("version").value("2.0").build()
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
  void importTestCasesWithTestFolderIdTest() throws Exception {
    // Given
    var fileContent = "test,case,data";
    var file = new MockMultipartFile("file", "test.csv", "text/csv", fileContent.getBytes());
    var testFolderId = 3L;
    var importedTestCases = List.of(new TmsTestCaseRS(), new TmsTestCaseRS());

    given(tmsTestCaseService.importFromFile(eq(projectId), eq(testFolderId), isNull(), eq(file)))
        .willReturn(importedTestCases);

    // When/Then
    mockMvc.perform(
            multipart("/v1/project/{projectKey}/tms/test-case/import", projectKey)
                .file(file)
                .param("testFolderId", String.valueOf(testFolderId))
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).importFromFile(eq(projectId), eq(testFolderId), isNull(), eq(file));
  }

  @Test
  void importTestCasesWithTestFolderNameTest() throws Exception {
    // Given
    var fileContent = "test,case,data";
    var file = new MockMultipartFile("file", "test.json", "application/json", fileContent.getBytes());
    var testFolderName = "Test Folder";
    var importedTestCases = List.of(new TmsTestCaseRS());

    given(tmsTestCaseService.importFromFile(eq(projectId), isNull(), eq(testFolderName), eq(file)))
        .willReturn(importedTestCases);

    // When/Then
    mockMvc.perform(
            multipart("/v1/project/{projectKey}/tms/test-case/import", projectKey)
                .file(file)
                .param("testFolderName", testFolderName)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).importFromFile(eq(projectId), isNull(), eq(testFolderName), eq(file));
  }

  @Test
  void importTestCasesWithoutFolderParametersTest() throws Exception {
    // Given
    var fileContent = "test,case,data";
    var file = new MockMultipartFile("file", "test.json", "application/json", fileContent.getBytes());
    var importedTestCases = List.of(new TmsTestCaseRS());

    given(tmsTestCaseService.importFromFile(eq(projectId), isNull(), isNull(), eq(file)))
        .willReturn(importedTestCases);

    // When/Then
    mockMvc.perform(
            multipart("/v1/project/{projectKey}/tms/test-case/import", projectKey)
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).importFromFile(eq(projectId), isNull(), isNull(), eq(file));
  }

  @Test
  void importTestCasesWithBothFolderParametersTest() throws Exception {
    // Given
    var fileContent = "test,case,data";
    var file = new MockMultipartFile("file", "test.json", "application/json", fileContent.getBytes());
    var testFolderId = 3L;
    var testFolderName = "Test Folder";
    var importedTestCases = List.of(new TmsTestCaseRS());

    given(tmsTestCaseService.importFromFile(eq(projectId), eq(testFolderId), eq(testFolderName), eq(file)))
        .willReturn(importedTestCases);

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
    verify(tmsTestCaseService).exportToFile(eq(projectId), isNull(), eq("JSON"),
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
    verify(tmsTestCaseService).exportToFile(eq(projectId), isNull(), eq(format),
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
    verify(tmsTestCaseService).deleteTagsFromTestCase(projectId, testCaseId, tagIds);
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
    verify(tmsTestCaseService).deleteTagsFromTestCase(projectId, testCaseId, tagIds);
  }

  @Test
  void deleteTagsFromMultipleTestCasesTest() throws Exception {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var tagIds = Arrays.asList(4L, 5L, 6L);
    var deleteRequest = new BatchDeleteTagsRQ();
    deleteRequest.setTestCaseIds(testCaseIds);
    deleteRequest.setTagIds(tagIds);

    var jsonContent = objectMapper.writeValueAsString(deleteRequest);

    // When/Then
    mockMvc.perform(
            delete("/v1/project/{projectKey}/tms/test-case/tags/batch", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isNoContent());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).deleteTagsFromTestCases(projectId, testCaseIds, tagIds);
  }

  @Test
  void deleteTagsFromMultipleTestCasesWithSingleIdsTest() throws Exception {
    // Given
    var testCaseIds = List.of(1L);
    var tagIds = List.of(4L);
    var deleteRequest = new BatchDeleteTagsRQ();
    deleteRequest.setTestCaseIds(testCaseIds);
    deleteRequest.setTagIds(tagIds);

    var jsonContent = objectMapper.writeValueAsString(deleteRequest);

    // When/Then
    mockMvc.perform(
            delete("/v1/project/{projectKey}/tms/test-case/tags/batch", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isNoContent());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).deleteTagsFromTestCases(projectId, testCaseIds, tagIds);
  }

  @Test
  void deleteMultipleTagsFromSingleTestCaseTest() throws Exception {
    // Given
    var testCaseIds = List.of(1L);
    var tagIds = Arrays.asList(4L, 5L, 6L, 7L);
    var deleteRequest = new BatchDeleteTagsRQ();
    deleteRequest.setTestCaseIds(testCaseIds);
    deleteRequest.setTagIds(tagIds);

    var jsonContent = objectMapper.writeValueAsString(deleteRequest);

    // When/Then
    mockMvc.perform(
            delete("/v1/project/{projectKey}/tms/test-case/tags/batch", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isNoContent());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).deleteTagsFromTestCases(projectId, testCaseIds, tagIds);
  }

  @Test
  void deleteSingleTagFromMultipleTestCasesTest() throws Exception {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L, 4L);
    var tagIds = List.of(5L);
    var deleteRequest = new BatchDeleteTagsRQ();
    deleteRequest.setTestCaseIds(testCaseIds);
    deleteRequest.setTagIds(tagIds);

    var jsonContent = objectMapper.writeValueAsString(deleteRequest);

    // When/Then
    mockMvc.perform(
            delete("/v1/project/{projectKey}/tms/test-case/tags/batch", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isNoContent());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestCaseService).deleteTagsFromTestCases(projectId, testCaseIds, tagIds);
  }
}
