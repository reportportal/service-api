package com.epam.ta.reportportal.core.tms.controller.unit;

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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.tms.controller.TmsTestFolderController;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderExportFileType;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsTestFolderRS;
import com.epam.ta.reportportal.core.tms.service.TmsTestFolderService;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.model.Page;
import com.epam.ta.reportportal.util.ProjectExtractor;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class TmsTestFolderControllerTmsTest {

  private final long projectId = 1L;
  private final String projectKey = "test_project";
  private final Pageable pageable = PageRequest.of(0, 10);
  @Mock
  private TmsTestFolderService tmsTestFolderService;
  @Mock
  private ProjectExtractor projectExtractor;
  @InjectMocks
  private TmsTestFolderController tmsTestFolderController;
  private MockMvc mockMvc;
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
            new HandlerMethodArgumentResolver() {
              @Override
              public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().equals(Pageable.class);
              }

              @Override
              public Object resolveArgument(MethodParameter parameter,
                  ModelAndViewContainer mavContainer,
                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return pageable;
              }
            },
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
  public void testCreateTestFolder() throws Exception {
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .description("doc")
        .name("name")
        .build();
    TmsTestFolderRS expectedResponse = TmsTestFolderRS.builder()
        .id(1L)
        .name("name")
        .description("doc")
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    given(tmsTestFolderService.create(projectId, request)).willReturn(expectedResponse);

    mockMvc.perform(post("/v1/project/{projectKey}/tms/folder", projectKey)
            .contentType("application/json")
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).create(projectId, request);
  }

  @Test
  public void testUpdateTestFolder() throws Exception {
    long folderId = 2L;
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .description("doc")
        .name("name")
        .build();
    TmsTestFolderRS expectedResponse = TmsTestFolderRS.builder()
        .id(1L)
        .name("name")
        .description("doc")
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    given(tmsTestFolderService.update(projectId, folderId, request))
        .willReturn(expectedResponse);

    mockMvc.perform(put("/v1/project/{projectKey}/tms/folder/{folderId}", projectKey, folderId)
            .contentType("application/json")
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).update(projectId, folderId, request);
  }

  @Test
  public void testPatchTestFolder() throws Exception {
    long folderId = 2L;
    TmsTestFolderRQ request = TmsTestFolderRQ.builder()
        .description("updated doc")
        .name("updated name")
        .build();
    TmsTestFolderRS expectedResponse = TmsTestFolderRS.builder()
        .id(1L)
        .name("updated name")
        .description("updated doc")
        .build();
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    given(tmsTestFolderService.patch(projectId, folderId, request))
        .willReturn(expectedResponse);

    mockMvc.perform(patch("/v1/project/{projectKey}/tms/folder/{folderId}", projectKey, folderId)
            .contentType("application/json")
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

    mockMvc.perform(get("/v1/project/{projectKey}/tms/folder/{folderId}", projectKey, folderId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("name"))
        .andExpect(jsonPath("$.description").value("doc"))
        .andExpect(jsonPath("$.countOfTestCases").value(3));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).getById(projectId, folderId);
  }

  @Test
  public void testGetTestFolderByProjectId() throws Exception {
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
        10L, // size
        0L,  // number
        2L   // totalElements
    );

    given(tmsTestFolderService.getFoldersByProjectID(projectId, pageable)).willReturn(
        expectedResponse);

    mockMvc.perform(get("/v1/project/{projectKey}/tms/folder", projectKey))
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
    verify(tmsTestFolderService).getFoldersByProjectID(projectId, pageable);
  }

  @Test
  public void testGetSubfolders() throws Exception {
    long parentFolderId = 1L;
    TmsTestFolderRS subfolder1 = TmsTestFolderRS.builder()
        .id(2L)
        .name("subfolder1")
        .description("subfolder doc1")
        .countOfTestCases(0L)
        .build();
    TmsTestFolderRS subfolder2 = TmsTestFolderRS.builder()
        .id(3L)
        .name("subfolder2")
        .description("subfolder doc2")
        .countOfTestCases(1L)
        .build();


    Page<TmsTestFolderRS> expectedResponse = new Page<>(
        Arrays.asList(subfolder1, subfolder2),
        10L, // size
        0L,  // number
        2L   // totalElements
    );

    given(tmsTestFolderService.getSubFolders(projectId, parentFolderId, pageable))
        .willReturn(expectedResponse);

    mockMvc.perform(get("/v1/project/{projectKey}/tms/folder/{folderId}/sub-folder",
            projectKey, parentFolderId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[0].id").value(2))
        .andExpect(jsonPath("$.content[0].name").value("subfolder1"))
        .andExpect(jsonPath("$.content[1].id").value(3))
        .andExpect(jsonPath("$.content[1].name").value("subfolder2"))
        .andExpect(jsonPath("$.page.totalElements").value(2));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).getSubFolders(projectId, parentFolderId, pageable);
  }

  @Test
  public void testDeleteTestFolder() throws Exception {
    long folderId = 2L;

    doNothing().when(tmsTestFolderService).delete(projectId, folderId);

    mockMvc.perform(delete("/v1/project/{projectKey}/tms/folder/{folderId}", projectKey, folderId))
        .andExpect(status().isOk());

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).delete(projectId, folderId);
  }

  @Test
  public void testExportFolder_CSV() throws Exception {
    // Arrange
    long folderId = 2L;
    TmsTestFolderExportFileType fileType = TmsTestFolderExportFileType.CSV;

    doNothing().when(tmsTestFolderService)
        .exportFolderById(eq(projectId), eq(folderId), eq(fileType),
            any(HttpServletResponse.class));

    // Act & Assert
    mockMvc.perform(get("/v1/project/{projectKey}/tms/folder/{folderId}/export/{fileType}",
            projectKey, folderId, fileType))
        .andExpect(status().isOk());

    // Verify
    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(tmsTestFolderService).exportFolderById(eq(projectId), eq(folderId), eq(fileType),
        any(HttpServletResponse.class));
  }
}
