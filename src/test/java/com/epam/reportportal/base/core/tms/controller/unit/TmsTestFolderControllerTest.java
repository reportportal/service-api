package com.epam.reportportal.base.core.tms.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.epam.reportportal.base.core.tms.controller.TmsTestFolderController;
import com.epam.reportportal.base.core.tms.dto.DuplicateTmsTestFolderRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestFolderExportFileType;
import com.epam.reportportal.base.core.tms.dto.TmsTestFolderRQ;
import com.epam.reportportal.base.core.tms.dto.NewTestFolderRQ;
import com.epam.reportportal.base.core.tms.dto.TmsTestFolderRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchFolderOperationError;
import com.epam.reportportal.base.core.tms.dto.batch.BatchFolderOperationResultRS;
import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationError;
import com.epam.reportportal.base.core.tms.dto.batch.BatchTestCaseOperationResultRS;
import com.epam.reportportal.base.core.tms.service.TmsTestFolderService;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Unit tests for TmsTestFolderController.
 * Tests CRUD operations, filtering, sorting, duplication, and export functionality for test folders.
 */
public class TmsTestFolderControllerTest {

  private final long projectId = 1L;
  private final String projectKey = "test_project";

  @Mock
  private TmsTestFolderService tmsTestFolderService;
  @Mock
  private ProjectExtractor projectExtractor;
  @InjectMocks
  private TmsTestFolderController tmsTestFolderController;

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
    mockMvc = standaloneSetup(tmsTestFolderController)
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
            new FilterCriteriaResolver(),
            new HandlerMethodArgumentResolver() {
              @Override
              public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().equals(HttpServletResponse.class);
              }

              @Override
              public Object resolveArgument(MethodParameter parameter,
                  ModelAndViewContainer mavContainer,
                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return new MockHttpServletResponse();
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
  public void testCreateTestFolder_WithoutParent() throws Exception {
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .description("Root folder description")
        .name("Root Folder")
        .build();
    TmsTestFolderRS expectedResponse = TmsTestFolderRS.builder()
        .id(1L)
        .name("Root Folder")
        .description("Root folder description")
        .build();
    String jsonContent = objectMapper.writeValueAsString(request);

    given(tmsTestFolderService.create(projectId, request)).willReturn(expectedResponse);

    mockMvc.perform(post("/v1/project/{projectKey}/tms/folder", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).create(projectId, request);
  }

  @Test
  public void testCreateTestFolder_WithExistingParent() throws Exception {
    Long existingParentId = 5L;
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .description("Child folder description")
        .name("Child Folder")
        .parentTestFolderId(existingParentId)
        .build();
    TmsTestFolderRS expectedResponse = TmsTestFolderRS.builder()
        .id(2L)
        .name("Child Folder")
        .description("Child folder description")
        .parentFolderId(existingParentId)
        .build();
    String jsonContent = objectMapper.writeValueAsString(request);

    given(tmsTestFolderService.create(projectId, request)).willReturn(expectedResponse);

    mockMvc.perform(post("/v1/project/{projectKey}/tms/folder", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).create(projectId, request);
  }

  @Test
  public void testCreateTestFolder_WithNewParentFolder() throws Exception {
    NewTestFolderRQ parentFolder = NewTestFolderRQ.builder()
        .name("New Parent Folder")
        .build();
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .description("Child folder description")
        .name("Child Folder")
        .parentTestFolder(parentFolder)
        .build();
    TmsTestFolderRS expectedResponse = TmsTestFolderRS.builder()
        .id(3L)
        .name("Child Folder")
        .description("Child folder description")
        .build();
    String jsonContent = objectMapper.writeValueAsString(request);

    given(tmsTestFolderService.create(projectId, request)).willReturn(expectedResponse);

    mockMvc.perform(post("/v1/project/{projectKey}/tms/folder", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).create(projectId, request);
  }

  @Test
  public void testCreateTestFolder_WithNewParentFolderWithGrandparent() throws Exception {
    Long grandparentId = 10L;
    NewTestFolderRQ parentFolder = NewTestFolderRQ.builder()
        .name("New Parent Folder")
        .parentTestFolderId(grandparentId)
        .build();
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .description("Child folder description")
        .name("Child Folder")
        .parentTestFolder(parentFolder)
        .build();
    TmsTestFolderRS expectedResponse = TmsTestFolderRS.builder()
        .id(4L)
        .name("Child Folder")
        .description("Child folder description")
        .build();
    String jsonContent = objectMapper.writeValueAsString(request);

    given(tmsTestFolderService.create(projectId, request)).willReturn(expectedResponse);

    mockMvc.perform(post("/v1/project/{projectKey}/tms/folder", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).create(projectId, request);
  }

  @Test
  public void testUpdateTestFolder_WithoutParent() throws Exception {
    long folderId = 2L;
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .description("Updated description")
        .name("Updated Name")
        .build();
    TmsTestFolderRS expectedResponse = TmsTestFolderRS.builder()
        .id(folderId)
        .name("Updated Name")
        .description("Updated description")
        .build();
    String jsonContent = objectMapper.writeValueAsString(request);

    given(tmsTestFolderService.update(projectId, folderId, request))
        .willReturn(expectedResponse);

    mockMvc.perform(put("/v1/project/{projectKey}/tms/folder/{folderId}", projectKey, folderId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).update(projectId, folderId, request);
  }

  @Test
  public void testUpdateTestFolder_WithExistingParent() throws Exception {
    long folderId = 2L;
    Long existingParentId = 7L;
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .description("Updated description")
        .name("Updated Name")
        .parentTestFolderId(existingParentId)
        .build();
    TmsTestFolderRS expectedResponse = TmsTestFolderRS.builder()
        .id(folderId)
        .name("Updated Name")
        .description("Updated description")
        .parentFolderId(existingParentId)
        .build();
    String jsonContent = objectMapper.writeValueAsString(request);

    given(tmsTestFolderService.update(projectId, folderId, request))
        .willReturn(expectedResponse);

    mockMvc.perform(put("/v1/project/{projectKey}/tms/folder/{folderId}", projectKey, folderId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).update(projectId, folderId, request);
  }

  @Test
  public void testUpdateTestFolder_WithNewParentFolder() throws Exception {
    long folderId = 2L;
    NewTestFolderRQ parentFolder = NewTestFolderRQ.builder()
        .name("Updated Parent Folder")
        .build();
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .description("Updated description")
        .name("Updated Name")
        .parentTestFolder(parentFolder)
        .build();
    TmsTestFolderRS expectedResponse = TmsTestFolderRS.builder()
        .id(folderId)
        .name("Updated Name")
        .description("Updated description")
        .build();
    String jsonContent = objectMapper.writeValueAsString(request);

    given(tmsTestFolderService.update(projectId, folderId, request))
        .willReturn(expectedResponse);

    mockMvc.perform(put("/v1/project/{projectKey}/tms/folder/{folderId}", projectKey, folderId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).update(projectId, folderId, request);
  }

  @Test
  public void testPatchTestFolder_WithExistingParent() throws Exception {
    long folderId = 2L;
    Long existingParentId = 8L;
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .parentTestFolderId(existingParentId)
        .build();
    TmsTestFolderRS expectedResponse = TmsTestFolderRS.builder()
        .id(folderId)
        .name("Original Name")
        .description("Original description")
        .parentFolderId(existingParentId)
        .build();
    String jsonContent = objectMapper.writeValueAsString(request);

    given(tmsTestFolderService.patch(projectId, folderId, request))
        .willReturn(expectedResponse);

    mockMvc.perform(patch("/v1/project/{projectKey}/tms/folder/{folderId}", projectKey, folderId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).patch(projectId, folderId, request);
  }

  @Test
  public void testPatchTestFolder_WithNewParentFolder() throws Exception {
    long folderId = 2L;
    NewTestFolderRQ parentFolder = NewTestFolderRQ.builder()
        .name("Patched Parent Folder")
        .parentTestFolderId(15L)
        .build();
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .description("Patched description")
        .parentTestFolder(parentFolder)
        .build();
    TmsTestFolderRS expectedResponse = TmsTestFolderRS.builder()
        .id(folderId)
        .name("Original Name")
        .description("Patched description")
        .build();
    String jsonContent = objectMapper.writeValueAsString(request);

    given(tmsTestFolderService.patch(projectId, folderId, request))
        .willReturn(expectedResponse);

    mockMvc.perform(patch("/v1/project/{projectKey}/tms/folder/{folderId}", projectKey, folderId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).patch(projectId, folderId, request);
  }

  @Test
  public void testPatchTestFolder_OnlyName() throws Exception {
    long folderId = 2L;
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .name("Only Name Updated")
        .build();
    TmsTestFolderRS expectedResponse = TmsTestFolderRS.builder()
        .id(folderId)
        .name("Only Name Updated")
        .description("Original description")
        .build();
    String jsonContent = objectMapper.writeValueAsString(request);

    given(tmsTestFolderService.patch(projectId, folderId, request))
        .willReturn(expectedResponse);

    mockMvc.perform(patch("/v1/project/{projectKey}/tms/folder/{folderId}", projectKey, folderId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).patch(projectId, folderId, request);
  }

  @Test
  public void testGetTestFolderById() throws Exception {
    long folderId = 2L;
    TmsTestFolderRS expectedResponse = TmsTestFolderRS.builder()
        .id(1L)
        .name("name")
        .description("doc")
        .countOfTestCases(3L)
        .build();

    given(tmsTestFolderService.getById(projectId, folderId)).willReturn(expectedResponse);

    mockMvc.perform(get("/v1/project/{projectKey}/tms/folder/{folderId}", projectKey, folderId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("name"))
        .andExpect(jsonPath("$.description").value("doc"))
        .andExpect(jsonPath("$.countOfTestCases").value(3));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).getById(projectId, folderId);
  }

  @Test
  public void testGetFoldersByCriteria_WithoutFilters() throws Exception {
    TmsTestFolderRS folder1 = TmsTestFolderRS.builder()
        .id(1L)
        .name("name1")
        .description("doc1")
        .countOfTestCases(2L)
        .build();
    TmsTestFolderRS folder2 = TmsTestFolderRS.builder()
        .id(2L)
        .name("name2")
        .description("doc2")
        .countOfTestCases(3L)
        .build();

    Page<TmsTestFolderRS> expectedResponse = new Page<>(
        Arrays.asList(folder1, folder2),
        100L, // default size
        0L,   // number
        2L,   // totalElements
        1L    // totalPages
    );

    given(tmsTestFolderService.getFoldersByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class))).willReturn(expectedResponse);

    mockMvc.perform(get("/v1/project/{projectKey}/tms/folder", projectKey)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.content[0].name").value("name1"))
        .andExpect(jsonPath("$.content[0].countOfTestCases").value(2))
        .andExpect(jsonPath("$.content[1].id").value(2))
        .andExpect(jsonPath("$.content[1].name").value("name2"))
        .andExpect(jsonPath("$.content[1].countOfTestCases").value(3))
        .andExpect(jsonPath("$.page.totalElements").value(2));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).getFoldersByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class));
  }

  @Test
  public void testGetFoldersByCriteria_WithFilter() throws Exception {
    TmsTestFolderRS folder1 = TmsTestFolderRS.builder()
        .id(1L)
        .name("name1")
        .description("doc1")
        .countOfTestCases(1L)
        .build();

    Page<TmsTestFolderRS> expectedResponse = new Page<>(
        Collections.singletonList(folder1),
        100L, // default size
        0L,   // number
        1L,   // totalElements
        1L    // totalPages
    );

    given(tmsTestFolderService.getFoldersByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class))).willReturn(expectedResponse);

    mockMvc.perform(get("/v1/project/{projectKey}/tms/folder", projectKey)
            .param("filter.cnt.name", "name1")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.content[0].name").value("name1"))
        .andExpect(jsonPath("$.content[0].countOfTestCases").value(1))
        .andExpect(jsonPath("$.page.totalElements").value(1));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).getFoldersByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class));
  }

  @Test
  public void testGetFoldersByCriteria_WithPagination() throws Exception {
    TmsTestFolderRS folder1 = TmsTestFolderRS.builder()
        .id(1L)
        .name("name1")
        .description("doc1")
        .countOfTestCases(2L)
        .build();

    Page<TmsTestFolderRS> expectedResponse = new Page<>(
        Collections.singletonList(folder1),
        10L, // size
        1L,  // number (offset 10 / limit 10 = page 1)
        11L, // totalElements
        2L   // totalPages
    );

    given(tmsTestFolderService.getFoldersByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class))).willReturn(expectedResponse);

    mockMvc.perform(get("/v1/project/{projectKey}/tms/folder", projectKey)
            .param("offset", "10")
            .param("limit", "10")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.page.totalElements").value(11));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).getFoldersByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class));
  }

  @Test
  public void testGetFoldersByCriteria_WithFilterAndPagination() throws Exception {
    TmsTestFolderRS folder1 = TmsTestFolderRS.builder()
        .id(1L)
        .name("test folder")
        .description("test description")
        .countOfTestCases(5L)
        .build();

    Page<TmsTestFolderRS> expectedResponse = new Page<>(
        Collections.singletonList(folder1),
        20L, // size
        0L,  // number
        1L,  // totalElements
        1L   // totalPages
    );

    given(tmsTestFolderService.getFoldersByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class))).willReturn(expectedResponse);

    mockMvc.perform(get("/v1/project/{projectKey}/tms/folder", projectKey)
            .param("filter.cnt.name", "test")
            .param("offset", "0")
            .param("limit", "20")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.content[0].name").value("test folder"))
        .andExpect(jsonPath("$.page.totalElements").value(1));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).getFoldersByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class));
  }

  @Test
  public void testGetFoldersByCriteria_WithMultipleFilters() throws Exception {
    TmsTestFolderRS folder1 = TmsTestFolderRS.builder()
        .id(1L)
        .name("important folder")
        .description("critical description")
        .countOfTestCases(10L)
        .build();

    Page<TmsTestFolderRS> expectedResponse = new Page<>(
        Collections.singletonList(folder1),
        100L, // default size
        0L,   // number
        1L,   // totalElements
        1L    // totalPages
    );

    given(tmsTestFolderService.getFoldersByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class))).willReturn(expectedResponse);

    mockMvc.perform(get("/v1/project/{projectKey}/tms/folder", projectKey)
            .param("filter.cnt.name", "important")
            .param("filter.cnt.description", "critical")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].id").value(1))
        .andExpect(jsonPath("$.content[0].name").value("important folder"))
        .andExpect(jsonPath("$.page.totalElements").value(1));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).getFoldersByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class));
  }

  @Test
  public void testGetFoldersByCriteria_WithSort() throws Exception {
    TmsTestFolderRS folder1 = TmsTestFolderRS.builder()
        .id(1L)
        .name("A Folder")
        .description("doc1")
        .countOfTestCases(2L)
        .build();
    TmsTestFolderRS folder2 = TmsTestFolderRS.builder()
        .id(2L)
        .name("B Folder")
        .description("doc2")
        .countOfTestCases(3L)
        .build();

    Page<TmsTestFolderRS> expectedResponse = new Page<>(
        Arrays.asList(folder1, folder2),
        100L, // default size
        0L,   // number
        2L,   // totalElements
        1L    // totalPages
    );

    given(tmsTestFolderService.getFoldersByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class))).willReturn(expectedResponse);

    mockMvc.perform(get("/v1/project/{projectKey}/tms/folder", projectKey)
            .param("sort", "name,asc")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].name").value("A Folder"))
        .andExpect(jsonPath("$.content[1].name").value("B Folder"));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).getFoldersByCriteria(eq(projectId), any(Filter.class),
        any(OffsetRequest.class));
  }

  @Test
  public void testDeleteTestFolder() throws Exception {
    long folderId = 2L;

    doNothing().when(tmsTestFolderService).delete(projectId, folderId);

    mockMvc.perform(delete("/v1/project/{projectKey}/tms/folder/{folderId}", projectKey, folderId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).delete(projectId, folderId);
  }

  @Test
  public void testExportFolder_CSV() throws Exception {
    long folderId = 2L;
    TmsTestFolderExportFileType fileType = TmsTestFolderExportFileType.CSV;

    doNothing().when(tmsTestFolderService)
        .exportFolderById(eq(projectId), eq(folderId), eq(fileType),
            any(HttpServletResponse.class));

    mockMvc.perform(get("/v1/project/{projectKey}/tms/folder/{folderId}/export/{fileType}",
            projectKey, folderId, fileType)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).exportFolderById(eq(projectId), eq(folderId), eq(fileType),
        any(HttpServletResponse.class));
  }

  // ==================== DUPLICATION TESTS ====================

  @Test
  public void testDuplicateTestFolder_Success() throws Exception {
    long folderId = 2L;
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .name("Duplicated Folder")
        .description("Duplicated description")
        .build();

    BatchFolderOperationResultRS folderStatistics = BatchFolderOperationResultRS.builder()
        .totalCount(3)
        .successCount(3)
        .failureCount(0)
        .successFolderIds(Arrays.asList(10L, 11L, 12L))
        .errors(Collections.emptyList())
        .build();

    BatchTestCaseOperationResultRS testCaseStatistics = BatchTestCaseOperationResultRS.builder()
        .totalCount(5)
        .successCount(5)
        .failureCount(0)
        .successTestCaseIds(Arrays.asList(20L, 21L, 22L, 23L, 24L))
        .errors(Collections.emptyList())
        .build();

    DuplicateTmsTestFolderRS expectedResponse = DuplicateTmsTestFolderRS.builder()
        .id(10L)
        .name("Duplicated Folder")
        .description("Duplicated description")
        .countOfTestCases(5L)
        .folderDuplicationStatistic(folderStatistics)
        .testCaseDuplicationStatistic(testCaseStatistics)
        .build();

    String jsonContent = objectMapper.writeValueAsString(request);

    given(tmsTestFolderService.duplicateFolder(projectId, folderId, request))
        .willReturn(expectedResponse);

    mockMvc.perform(
            post("/v1/project/{projectKey}/tms/folder/{folderId}/duplicate", projectKey, folderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(10))
        .andExpect(jsonPath("$.name").value("Duplicated Folder"))
        .andExpect(jsonPath("$.description").value("Duplicated description"))
        .andExpect(jsonPath("$.countOfTestCases").value(5))
        .andExpect(jsonPath("$.folderDuplicationStatistic.totalCount").value(3))
        .andExpect(jsonPath("$.folderDuplicationStatistic.successCount").value(3))
        .andExpect(jsonPath("$.folderDuplicationStatistic.failureCount").value(0))
        .andExpect(jsonPath("$.testCaseDuplicationStatistic.totalCount").value(5))
        .andExpect(jsonPath("$.testCaseDuplicationStatistic.successCount").value(5))
        .andExpect(jsonPath("$.testCaseDuplicationStatistic.failureCount").value(0));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).duplicateFolder(projectId, folderId, request);
  }

  @Test
  public void testDuplicateTestFolder_WithExistingParentId() throws Exception {
    long folderId = 2L;
    Long parentFolderId = 50L;
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .name("Duplicated Folder")
        .parentTestFolderId(parentFolderId)
        .build();

    BatchFolderOperationResultRS folderStatistics = BatchFolderOperationResultRS.builder()
        .totalCount(1)
        .successCount(1)
        .failureCount(0)
        .successFolderIds(Collections.singletonList(10L))
        .errors(Collections.emptyList())
        .build();

    BatchTestCaseOperationResultRS testCaseStatistics = BatchTestCaseOperationResultRS.builder()
        .totalCount(2)
        .successCount(2)
        .failureCount(0)
        .successTestCaseIds(Arrays.asList(20L, 21L))
        .errors(Collections.emptyList())
        .build();

    DuplicateTmsTestFolderRS expectedResponse = DuplicateTmsTestFolderRS.builder()
        .id(10L)
        .name("Duplicated Folder")
        .parentFolderId(parentFolderId)
        .countOfTestCases(2L)
        .folderDuplicationStatistic(folderStatistics)
        .testCaseDuplicationStatistic(testCaseStatistics)
        .build();

    String jsonContent = objectMapper.writeValueAsString(request);

    given(tmsTestFolderService.duplicateFolder(projectId, folderId, request))
        .willReturn(expectedResponse);

    mockMvc.perform(
            post("/v1/project/{projectKey}/tms/folder/{folderId}/duplicate", projectKey, folderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(10))
        .andExpect(jsonPath("$.name").value("Duplicated Folder"))
        .andExpect(jsonPath("$.parentFolderId").value(parentFolderId))
        .andExpect(jsonPath("$.countOfTestCases").value(2));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).duplicateFolder(projectId, folderId, request);
  }

  @Test
  public void testDuplicateTestFolder_WithNewParentFolder() throws Exception {
    long folderId = 2L;
    NewTestFolderRQ parentFolder = NewTestFolderRQ.builder()
        .name("New Parent Folder")
        .build();
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .name("Duplicated Folder")
        .parentTestFolder(parentFolder)
        .build();

    BatchFolderOperationResultRS folderStatistics = BatchFolderOperationResultRS.builder()
        .totalCount(1)
        .successCount(1)
        .failureCount(0)
        .successFolderIds(Collections.singletonList(10L))
        .errors(Collections.emptyList())
        .build();

    BatchTestCaseOperationResultRS testCaseStatistics = BatchTestCaseOperationResultRS.builder()
        .totalCount(3)
        .successCount(3)
        .failureCount(0)
        .successTestCaseIds(Arrays.asList(20L, 21L, 22L))
        .errors(Collections.emptyList())
        .build();

    DuplicateTmsTestFolderRS expectedResponse = DuplicateTmsTestFolderRS.builder()
        .id(10L)
        .name("Duplicated Folder")
        .parentFolderId(100L)
        .countOfTestCases(3L)
        .folderDuplicationStatistic(folderStatistics)
        .testCaseDuplicationStatistic(testCaseStatistics)
        .build();

    String jsonContent = objectMapper.writeValueAsString(request);

    given(tmsTestFolderService.duplicateFolder(projectId, folderId, request))
        .willReturn(expectedResponse);

    mockMvc.perform(
            post("/v1/project/{projectKey}/tms/folder/{folderId}/duplicate", projectKey, folderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(10))
        .andExpect(jsonPath("$.name").value("Duplicated Folder"))
        .andExpect(jsonPath("$.parentFolderId").value(100))
        .andExpect(jsonPath("$.countOfTestCases").value(3));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).duplicateFolder(projectId, folderId, request);
  }

  @Test
  public void testDuplicateTestFolder_WithPartialFailures() throws Exception {
    long folderId = 2L;
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .name("Duplicated Folder")
        .build();

    BatchFolderOperationResultRS folderStatistics = BatchFolderOperationResultRS.builder()
        .totalCount(3)
        .successCount(2)
        .failureCount(1)
        .successFolderIds(Arrays.asList(10L, 11L))
        .errors(Collections.singletonList(
            new BatchFolderOperationError(
                12L, "Failed to duplicate folder")
        ))
        .build();

    BatchTestCaseOperationResultRS testCaseStatistics = BatchTestCaseOperationResultRS.builder()
        .totalCount(5)
        .successCount(4)
        .failureCount(1)
        .successTestCaseIds(Arrays.asList(20L, 21L, 22L, 23L))
        .errors(Collections.singletonList(
            new BatchTestCaseOperationError(
                24L, "Failed to duplicate test case")
        ))
        .build();

    DuplicateTmsTestFolderRS expectedResponse = DuplicateTmsTestFolderRS.builder()
        .id(10L)
        .name("Duplicated Folder")
        .countOfTestCases(4L)
        .folderDuplicationStatistic(folderStatistics)
        .testCaseDuplicationStatistic(testCaseStatistics)
        .build();

    String jsonContent = objectMapper.writeValueAsString(request);

    given(tmsTestFolderService.duplicateFolder(projectId, folderId, request))
        .willReturn(expectedResponse);

    mockMvc.perform(
            post("/v1/project/{projectKey}/tms/folder/{folderId}/duplicate", projectKey, folderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(10))
        .andExpect(jsonPath("$.name").value("Duplicated Folder"))
        .andExpect(jsonPath("$.countOfTestCases").value(4))
        .andExpect(jsonPath("$.folderDuplicationStatistic.totalCount").value(3))
        .andExpect(jsonPath("$.folderDuplicationStatistic.successCount").value(2))
        .andExpect(jsonPath("$.folderDuplicationStatistic.failureCount").value(1))
        .andExpect(jsonPath("$.folderDuplicationStatistic.errors[0].folderId").value(12))
        .andExpect(
            jsonPath("$.folderDuplicationStatistic.errors[0].errorMessage").value("Failed to duplicate folder"))
        .andExpect(jsonPath("$.testCaseDuplicationStatistic.totalCount").value(5))
        .andExpect(jsonPath("$.testCaseDuplicationStatistic.successCount").value(4))
        .andExpect(jsonPath("$.testCaseDuplicationStatistic.failureCount").value(1))
        .andExpect(jsonPath("$.testCaseDuplicationStatistic.errors[0].testCaseId").value(24))
        .andExpect(
            jsonPath("$.testCaseDuplicationStatistic.errors[0].errorMessage").value("Failed to duplicate test case"));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).duplicateFolder(projectId, folderId, request);
  }

  @Test
  public void testDuplicateTestFolder_EmptyFolder() throws Exception {
    long folderId = 2L;
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .name("Duplicated Empty Folder")
        .build();

    BatchFolderOperationResultRS folderStatistics = BatchFolderOperationResultRS.builder()
        .totalCount(1)
        .successCount(1)
        .failureCount(0)
        .successFolderIds(Collections.singletonList(10L))
        .errors(Collections.emptyList())
        .build();

    BatchTestCaseOperationResultRS testCaseStatistics = BatchTestCaseOperationResultRS.builder()
        .totalCount(0)
        .successCount(0)
        .failureCount(0)
        .successTestCaseIds(Collections.emptyList())
        .errors(Collections.emptyList())
        .build();

    DuplicateTmsTestFolderRS expectedResponse = DuplicateTmsTestFolderRS.builder()
        .id(10L)
        .name("Duplicated Empty Folder")
        .countOfTestCases(0L)
        .folderDuplicationStatistic(folderStatistics)
        .testCaseDuplicationStatistic(testCaseStatistics)
        .build();

    String jsonContent = objectMapper.writeValueAsString(request);

    given(tmsTestFolderService.duplicateFolder(projectId, folderId, request))
        .willReturn(expectedResponse);

    mockMvc.perform(
            post("/v1/project/{projectKey}/tms/folder/{folderId}/duplicate", projectKey, folderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(10))
        .andExpect(jsonPath("$.name").value("Duplicated Empty Folder"))
        .andExpect(jsonPath("$.countOfTestCases").value(0))
        .andExpect(jsonPath("$.folderDuplicationStatistic.totalCount").value(1))
        .andExpect(jsonPath("$.testCaseDuplicationStatistic.totalCount").value(0));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).duplicateFolder(projectId, folderId, request);
  }
}
