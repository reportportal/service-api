package com.epam.reportportal.base.core.tms.controller.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import com.epam.reportportal.base.core.tms.controller.TmsManualLaunchController;
import com.epam.reportportal.base.core.tms.dto.batch.BatchDeleteTestCaseExecutionsRQ;
import com.epam.reportportal.base.core.tms.dto.batch.BatchDeleteTestCaseExecutionsResultRS;
import com.epam.reportportal.base.core.tms.service.TmsManualLaunchService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.util.ProjectExtractor;
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
  private ObjectMapper objectMapper = new ObjectMapper();
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
        })
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
}
