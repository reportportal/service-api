package com.epam.ta.reportportal.core.tms.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.tms.controller.TmsDatasetController;
import com.epam.ta.reportportal.core.tms.dto.TmsDatasetRQ;
import com.epam.ta.reportportal.core.tms.dto.TmsDatasetRS;
import com.epam.ta.reportportal.core.tms.service.TmsDatasetService;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@ExtendWith(MockitoExtension.class)
class TmsDatasetControllerTest {

  @Mock
  private TmsDatasetService tmsDatasetService;

  @Mock
  private ProjectExtractor projectExtractor;

  @InjectMocks
  private TmsDatasetController tmsDatasetController;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;
  private final long projectId = 1L;
  private final String projectKey = "test_project";
  private ReportPortalUser testUser;

  @BeforeEach
  void setUp() {
    // Create a test user
    testUser = ReportPortalUser.userBuilder()
        .withUserName("testUser")
        .withPassword("password")
        .withUserId(1L)
        .withActive(true)
        .withAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
        .build();

    // Configure MockMvc with a custom argument resolver for @AuthenticationPrincipal
    mockMvc = standaloneSetup(tmsDatasetController)
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

    objectMapper = new ObjectMapper();

    // Setup the project extractor mock to return a MembershipDetails with the projectId
    MembershipDetails membershipDetails = MembershipDetails.builder()
        .withProjectId(projectId)
        .withProjectKey(projectKey)
        .build();
    given(projectExtractor.extractProjectDetailsAdmin(anyString()))
        .willReturn(membershipDetails);
  }

  @Test
  void shouldCreateDataset() throws Exception {
    var datasetRQ = TmsDatasetRQ.builder().name("Dataset1").attributes(List.of()).build();
    var expectedResponse = TmsDatasetRS.builder().id(1L).data(List.of()).build();

    var jsonContent = objectMapper.writeValueAsString(datasetRQ);

    given(tmsDatasetService.create(projectId, datasetRQ)).willReturn(expectedResponse);

    mockMvc
        .perform(post("/project/{projectKey}/tms/dataset", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(expectedResponse.getId()))
        .andExpect(jsonPath("$.name").value(expectedResponse.getName()));

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
    verify(tmsDatasetService).create(projectId, datasetRQ);
  }

  @Test
  void shouldUploadDatasetFromFile() throws Exception {
    var dataset1 = TmsDatasetRS.builder().id(1L).name("Dataset1").data(List.of()).build();
    var dataset2 = TmsDatasetRS.builder().id(2L).name("Dataset2").data(List.of()).build();
    var mockResponse = Arrays.asList(dataset1, dataset2);

    given(tmsDatasetService.uploadFromFile(eq(projectId), any())).willReturn(mockResponse);

    mockMvc
        .perform(multipart("/project/{projectKey}/tms/dataset/upload", projectKey)
            .file("file", "mockFileContent".getBytes())
            .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
    verify(tmsDatasetService).uploadFromFile(eq(projectId), any());
  }

  @Test
  void shouldGetDatasetsByProjectId() throws Exception {
    var dataset1 = TmsDatasetRS.builder().id(1L).name("Dataset1").data(List.of()).build();
    var dataset2 = TmsDatasetRS.builder().id(2L).name("Dataset2").data(List.of()).build();
    var mockResponse = Arrays.asList(dataset1, dataset2);

    given(tmsDatasetService.getByProjectId(projectId)).willReturn(mockResponse);

    mockMvc
        .perform(get("/project/{projectKey}/tms/dataset", projectKey))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2));

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
    verify(tmsDatasetService).getByProjectId(projectId);
  }

  @Test
  void shouldGetDatasetById() throws Exception {
    long datasetId = 100L;
    var mockResponse = TmsDatasetRS.builder().id(100L).name("Dataset100").build();

    given(tmsDatasetService.getById(projectId, datasetId)).willReturn(mockResponse);

    mockMvc
        .perform(get("/project/{projectKey}/tms/dataset/{datasetId}", projectKey,
                                                                                 datasetId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(mockResponse.getId()))
        .andExpect(jsonPath("$.name").value(mockResponse.getName()));

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
    verify(tmsDatasetService).getById(projectId, datasetId);
  }

  @Test
  void shouldUpdateDataset() throws Exception {
    long datasetId = 100L;
    var requestData = TmsDatasetRQ.builder().name("UpdatedDataset").build();
    var expectedResponse = TmsDatasetRS.builder().id(100L).name("UpdatedDataset").build();

    var jsonContent = objectMapper.writeValueAsString(requestData);

    given(tmsDatasetService.update(eq(projectId), eq(datasetId), eq(requestData)))
        .willReturn(expectedResponse);

    mockMvc
        .perform(put("/project/{projectKey}/tms/dataset/{datasetId}", projectKey, datasetId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(expectedResponse.getId()))
        .andExpect(jsonPath("$.name").value(expectedResponse.getName()));

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
    verify(tmsDatasetService).update(projectId, datasetId, requestData);
  }

  @Test
  void shouldPatchDataset() throws Exception {
    long datasetId = 100L;
    var requestData = TmsDatasetRQ.builder().name("UpdatedDataset").build();
    var expectedResponse = TmsDatasetRS.builder().id(100L).name("PatchedDataset").build();

    var jsonContent = objectMapper.writeValueAsString(requestData);

    given(tmsDatasetService.patch(eq(projectId), eq(datasetId), eq(requestData)))
        .willReturn(expectedResponse);

    mockMvc
        .perform(patch("/project/{projectKey}/tms/dataset/{datasetId}", projectKey, datasetId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonContent))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(expectedResponse.getId()))
        .andExpect(jsonPath("$.name").value(expectedResponse.getName()));

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
    verify(tmsDatasetService).patch(projectId, datasetId, requestData);
  }

  @Test
  void shouldDeleteDataset() throws Exception {
    long datasetId = 100L;

    mockMvc
        .perform(
          delete("/project/{projectKey}/tms/dataset/{datasetId}", projectKey, datasetId))
        .andExpect(status().isOk());

    verify(projectExtractor).extractProjectDetailsAdmin(eq(projectKey));
    verify(tmsDatasetService).delete(projectId, datasetId);
  }
}
