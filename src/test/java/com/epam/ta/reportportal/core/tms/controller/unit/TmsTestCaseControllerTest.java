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
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseRS;
import com.epam.ta.reportportal.core.tms.dto.TmsTestCaseTestFolderRQ;
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
  private ObjectMapper mapper;
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
    var membershipDetails = MembershipDetails.builder()
        .withProjectId(projectId)
        .withProjectKey(projectKey)
        .build();
    given(projectExtractor.extractProjectDetailsAdmin(anyString()))
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

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
    verify(tmsTestCaseService).getById(projectId, testCaseId);
  }

  @Test
  void getTestCasesByCriteriaTest() throws Exception {
    // Given
    var search = "test search";
    var testFolderId = 5L;
    var testCases = List.of(new TmsTestCaseRS(), new TmsTestCaseRS());
    var page = new Page<>(testCases, 10, 0, 2, 1);

    given(tmsTestCaseService.getTestCasesByCriteria(eq(projectId), eq(search),
        eq(testFolderId), any(Pageable.class))).willReturn(page);

    // When/Then
    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/test-case", projectKey)
                .param("search", search)
                .param("testFolderId", Long.toString(testFolderId))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
    verify(tmsTestCaseService).getTestCasesByCriteria(eq(projectId), eq(search),
        eq(testFolderId), any(Pageable.class));
  }

  @Test
  void getTestCasesByCriteriaWithoutParametersTest() throws Exception {
    // Given
    var emptyTestCases = Collections.<TmsTestCaseRS>emptyList();
    var page = new Page<>(emptyTestCases, 10, 0, 0, 0);

    given(tmsTestCaseService.getTestCasesByCriteria(eq(projectId), isNull(),
        isNull(), any(Pageable.class))).willReturn(page);

    // When/Then
    mockMvc.perform(
            get("/v1/project/{projectKey}/tms/test-case", projectKey)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
    verify(tmsTestCaseService).getTestCasesByCriteria(eq(projectId), isNull(),
        isNull(), any(Pageable.class));
  }

  @Test
  void createTestCaseTest() throws Exception {
    // Given
    var testCaseRequest = new TmsTestCaseRQ();
    testCaseRequest.setName("Test Case");
    testCaseRequest.setTestFolder(TmsTestCaseTestFolderRQ.builder().id(3L).build());

    var testCase = new TmsTestCaseRS();
    var jsonContent = mapper.writeValueAsString(testCaseRequest);

    given(tmsTestCaseService.create(projectId, testCaseRequest)).willReturn(testCase);

    // When/Then
    mockMvc.perform(post("/v1/project/{projectKey}/tms/test-case", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
    verify(tmsTestCaseService).create(projectId, testCaseRequest);
  }

  @Test
  void updateTestCaseTest() throws Exception {
    // Given
    var testCaseId = 2L;
    var testCaseRequest = new TmsTestCaseRQ();
    testCaseRequest.setName("Updated Test Case");
    testCaseRequest.setTestFolder(TmsTestCaseTestFolderRQ.builder().id(3L).build());

    var testCase = new TmsTestCaseRS();
    var jsonContent = mapper.writeValueAsString(testCaseRequest);

    given(tmsTestCaseService.update(projectId, testCaseId, testCaseRequest)).willReturn(testCase);

    // When/Then
    mockMvc.perform(
            put("/v1/project/{projectKey}/tms/test-case/{testCaseId}", projectKey, testCaseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
    verify(tmsTestCaseService).update(projectId, testCaseId, testCaseRequest);
  }

  @Test
  void patchTestCaseTest() throws Exception {
    // Given
    var testCaseId = 2L;
    var testCaseRequest = new TmsTestCaseRQ();
    testCaseRequest.setName("Patched Test Case");
    testCaseRequest.setTestFolder(TmsTestCaseTestFolderRQ.builder().id(3L).build());

    var testCase = new TmsTestCaseRS();
    var jsonContent = mapper.writeValueAsString(testCaseRequest);

    given(tmsTestCaseService.patch(projectId, testCaseId, testCaseRequest)).willReturn(testCase);

    // When/Then
    mockMvc.perform(
            patch("/v1/project/{projectKey}/tms/test-case/{testCaseId}", projectKey, testCaseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
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

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
    verify(tmsTestCaseService).delete(projectId, testCaseId);
  }

  @Test
  void deleteTestCasesTest() throws Exception {
    // Given
    var testCaseIds = Arrays.asList(1L, 2L, 3L);
    var deleteRequest = BatchDeleteTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .build();

    var jsonContent = mapper.writeValueAsString(deleteRequest);

    // When/Then
    mockMvc.perform(
            delete("/v1/project/{projectKey}/tms/test-case/batch", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isNoContent());

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
    verify(tmsTestCaseService).delete(projectId, deleteRequest);
  }

  @Test
  void deleteTestCasesWithSingleIdTest() throws Exception {
    // Given
    var testCaseIds = List.of(1L);
    var deleteRequest = BatchDeleteTestCasesRQ.builder()
        .testCaseIds(testCaseIds)
        .build();

    var jsonContent = mapper.writeValueAsString(deleteRequest);

    // When/Then
    mockMvc.perform(
            delete("/v1/project/{projectKey}/tms/test-case/batch", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isNoContent());

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
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

    var jsonContent = mapper.writeValueAsString(patchRequest);

    // When/Then
    mockMvc.perform(
            patch("/v1/project/{projectKey}/tms/test-case/batch", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
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

    var jsonContent = mapper.writeValueAsString(patchRequest);

    // When/Then
    mockMvc.perform(
            patch("/v1/project/{projectKey}/tms/test-case/batch", projectKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
    verify(tmsTestCaseService).patch(projectId, patchRequest);
  }

  @Test
  void importTestCasesTest() throws Exception {
    // Given
    var fileContent = "test,case,data";
    var file = new MockMultipartFile("file", "test.csv", "text/csv", fileContent.getBytes());
    var importedTestCases = List.of(new TmsTestCaseRS(), new TmsTestCaseRS());

    given(tmsTestCaseService.importFromFile(projectId, file)).willReturn(importedTestCases);

    // When/Then
    mockMvc.perform(
            multipart("/v1/project/{projectKey}/tms/test-case/import", projectKey)
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk());

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
    verify(tmsTestCaseService).importFromFile(projectId, file);
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

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
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

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
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

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
    verify(tmsTestCaseService).exportToFile(eq(projectId), isNull(), eq(format),
        eq(false), any(HttpServletResponse.class));
  }
}
