package com.epam.reportportal.base.core.tms.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.epam.reportportal.base.core.tms.controller.TmsManualLaunchController;
import com.epam.reportportal.base.core.tms.dto.AddTestCaseToLaunchRQ;
import com.epam.reportportal.base.core.tms.dto.CreateTmsManualLaunchRQ;
import com.epam.reportportal.base.core.tms.dto.CreateTmsManualLaunchRS;
import com.epam.reportportal.base.core.tms.dto.TmsManualLaunchRQ;
import com.epam.reportportal.base.core.tms.dto.TmsManualLaunchRS;
import com.epam.reportportal.base.core.tms.dto.TmsTestCaseExecutionCommentRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchDeleteTestCaseExecutionsRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchDeleteTestCaseExecutionsResultRS;
import com.epam.reportportal.base.core.tms.service.TmsManualLaunchService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.util.ProjectExtractor;
import com.epam.reportportal.base.ws.resolver.FilterCriteriaResolver;
import com.epam.reportportal.base.ws.resolver.OffsetArgumentResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class TmsManualLaunchControllerTest {

  private final long projectId = 1L;
  private final String projectKey = "test_project";
  private final long launchId = 100L;

  @Mock
  private TmsManualLaunchService tmsManualLaunchService;

  @Mock
  private ProjectExtractor projectExtractor;

  @InjectMocks
  private TmsManualLaunchController controller;

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private ReportPortalUser user;

  @BeforeEach
  void setUp() {
    // Correctly instantiate ReportPortalUser with a username
    user = ReportPortalUser.userBuilder()
        .withUserName("test_user")
        .withPassword("password")
        .withUserId(1L)
        .withActive(true)
        .withAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
        .build();

    mockMvc = standaloneSetup(controller)
        .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
          @Override
          public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterAnnotation(AuthenticationPrincipal.class) != null;
          }

          @Override
          public Object resolveArgument(MethodParameter parameter,
              ModelAndViewContainer mavContainer,
              NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return user;
          }
        }, new OffsetArgumentResolver(), new FilterCriteriaResolver())
        .build();

    // Use Builder for MembershipDetails
    MembershipDetails membershipDetails = MembershipDetails.builder()
        .withProjectId(projectId)
        .withProjectKey(projectKey)
        .build();

    given(projectExtractor.extractMembershipDetails(any(), eq(projectKey)))
        .willReturn(membershipDetails);
  }

  @Test
  void createManualLaunchTest() throws Exception {
    CreateTmsManualLaunchRQ request = new CreateTmsManualLaunchRQ();
    request.setName("Test Launch");
    CreateTmsManualLaunchRS response = new CreateTmsManualLaunchRS();
    response.setId(launchId);
  
    given(tmsManualLaunchService.create(eq(projectId), eq(user), any())).willReturn(response);
  
    mockMvc.perform(post("/v1/project/{projectKey}/launch/manual", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  
    verify(tmsManualLaunchService).create(eq(projectId), eq(user), any());
  }
  
  @Test
  void getManualLaunchesTest() throws Exception {
    var emptyPage = mock(com.epam.reportportal.base.model.Page.class);
  
    given(tmsManualLaunchService.getManualLaunches(eq(projectId), any(), any()))
        .willReturn(emptyPage);
  
    mockMvc.perform(get("/v1/project/{projectKey}/launch/manual", projectKey))
        .andExpect(status().isOk());
  
    verify(tmsManualLaunchService).getManualLaunches(eq(projectId), any(), any());
  }
  
  @Test
  void getManualLaunchByIdTest() throws Exception {
    given(tmsManualLaunchService.getById(projectId, launchId)).willReturn(new TmsManualLaunchRS());
  
    mockMvc.perform(get("/v1/project/{projectKey}/launch/manual/{launchId}", projectKey, launchId))
        .andExpect(status().isOk());
  
    verify(tmsManualLaunchService).getById(projectId, launchId);
  }
  
  @Test
  void deleteManualLaunchTest() throws Exception {
    mockMvc.perform(delete("/v1/project/{projectKey}/launch/manual/{launchId}", projectKey, launchId))
        .andExpect(status().isOk());
  
    verify(tmsManualLaunchService).delete(any(), eq(launchId), eq(user));
  }
  
  @Test
  void patchManualLaunchTest() throws Exception {
    TmsManualLaunchRQ request = new TmsManualLaunchRQ();
    request.setName("Patched Name");
  
    given(tmsManualLaunchService.patch(eq(projectId), eq(launchId), any())).willReturn(new TmsManualLaunchRS());
  
    mockMvc.perform(patch("/v1/project/{projectKey}/launch/manual/{launchId}", projectKey, launchId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  
    verify(tmsManualLaunchService).patch(eq(projectId), eq(launchId), any());
  }
  
  @Test
  void addTestCaseToLaunchTest() throws Exception {
    AddTestCaseToLaunchRQ request = new AddTestCaseToLaunchRQ();
    request.setTestCaseId(200L);
  
    mockMvc.perform(post("/v1/project/{projectKey}/launch/manual/{launchId}/test-case", projectKey, launchId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  
    verify(tmsManualLaunchService).addTestCaseToLaunch(eq(projectId), eq(launchId), any());
  }
  
  @Test
  void putTestCaseExecutionCommentTest() throws Exception {
    TmsTestCaseExecutionCommentRQ request = new TmsTestCaseExecutionCommentRQ();
    request.setComment("New comment");
  
    mockMvc.perform(put("/v1/project/{projectKey}/launch/manual/{launchId}/test-case/execution/{executionId}/comment", projectKey, launchId, 400L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  
    verify(tmsManualLaunchService).putTestCaseExecutionComment(eq(projectId), eq(launchId), eq(400L), any());
  }
  
  @Test
  void batchDeleteTestCaseExecutions() throws Exception {
    BatchDeleteTestCaseExecutionsRQ request = new BatchDeleteTestCaseExecutionsRQ(List.of(1L, 2L));
    BatchDeleteTestCaseExecutionsResultRS response = new BatchDeleteTestCaseExecutionsResultRS();

    given(tmsManualLaunchService.batchDeleteTestCaseExecutions(any(), any(), any())).willReturn(response);

    mockMvc.perform(delete("/v1/project/{projectKey}/launch/manual/{launchId}/test-case/execution", projectKey, launchId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    verify(tmsManualLaunchService).batchDeleteTestCaseExecutions(any(), any(), any());
  }

  @Test
  void deleteTestCaseExecutionCommentTest() throws Exception {
    mockMvc.perform(delete("/v1/project/{projectKey}/launch/manual/{launchId}/test-case/execution/{executionId}/comment", projectKey, launchId, 400L))
        .andExpect(status().isOk());

    verify(tmsManualLaunchService).deleteTestCaseExecutionComment(projectId, launchId, 400L);
  }
}
