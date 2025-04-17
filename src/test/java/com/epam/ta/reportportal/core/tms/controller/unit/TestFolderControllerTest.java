package com.epam.ta.reportportal.core.tms.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.tms.controller.TestFolderController;
import com.epam.ta.reportportal.core.tms.dto.TestFolderRQ;
import com.epam.ta.reportportal.core.tms.dto.TestFolderRS;
import com.epam.ta.reportportal.core.tms.service.TestFolderService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class TestFolderControllerTest {

  @Mock
  private TestFolderService testFolderService;

  @Mock
  private ProjectExtractor projectExtractor;

  @InjectMocks
  private TestFolderController testFolderController;

  private MockMvc mockMvc;
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
    mockMvc = standaloneSetup(testFolderController)
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
    MembershipDetails membershipDetails = MembershipDetails.builder()
        .withProjectId(projectId)
        .withProjectKey(projectKey)
        .build();
    given(projectExtractor.extractProjectDetailsAdmin(any(ReportPortalUser.class), anyString()))
        .willReturn(membershipDetails);
  }

  @Test
  public void testCreateTestFolder() throws Exception {
    TestFolderRQ request = new TestFolderRQ("name", "doc");
    TestFolderRS expectedResponse = new TestFolderRS(1L, "name", "doc");
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    given(testFolderService.createFolder(projectId, request)).willReturn(expectedResponse);

    mockMvc.perform(post("/project/{projectKey}/tms/folder", projectKey)
            .contentType("application/json")
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractProjectDetailsAdmin(any(ReportPortalUser.class),
                                                        eq(projectKey));
    verify(testFolderService).createFolder(projectId, request);
  }

  @Test
  public void testUpdateTestFolder() throws Exception {
    long folderId = 2L;
    TestFolderRQ request = new TestFolderRQ("name", "doc");
    TestFolderRS expectedResponse = new TestFolderRS(1L, "name", "doc");
    ObjectMapper mapper = new ObjectMapper();
    String jsonContent = mapper.writeValueAsString(request);

    given(testFolderService.updateFolder(projectId, folderId, request))
        .willReturn(expectedResponse);

    mockMvc.perform(put("/project/{projectKey}/tms/folder/{folderId}", projectKey, folderId)
            .contentType("application/json")
            .content(jsonContent))
        .andExpect(status().isOk());

    verify(projectExtractor).extractProjectDetailsAdmin(any(ReportPortalUser.class),
                                                        eq(projectKey));
    verify(testFolderService).updateFolder(projectId, folderId, request);
  }

  @Test
  public void testGetTestFolderById() throws Exception {
    long folderId = 2L;
    TestFolderRS expectedResponse = new TestFolderRS(1L, "name", "doc");

    given(testFolderService.getFolderById(folderId)).willReturn(expectedResponse);

    mockMvc.perform(get("/project/{projectKey}/tms/folder/{folderId}", projectKey, folderId))
        .andExpect(status().isOk());

    verify(projectExtractor).extractProjectDetailsAdmin(any(ReportPortalUser.class),
                                                        eq(projectKey));
    verify(testFolderService).getFolderById(folderId);
  }

  @Test
  public void testGetTestFolderByProjectId() throws Exception {
    TestFolderRS expectedResponse1 = new TestFolderRS(1L, "name", "doc");
    TestFolderRS expectedResponse2 = new TestFolderRS(1L, "name", "doc");
    List<TestFolderRS> expectedResponse = Arrays.asList(expectedResponse1, expectedResponse2);

    given(testFolderService.getFolderByProjectID(projectId)).willReturn(expectedResponse);

    mockMvc.perform(get("/project/{projectKey}/tms/folder", projectKey))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2));

    verify(projectExtractor).extractProjectDetailsAdmin(any(ReportPortalUser.class),
                                                        eq(projectKey));
    verify(testFolderService).getFolderByProjectID(projectId);
  }
}
