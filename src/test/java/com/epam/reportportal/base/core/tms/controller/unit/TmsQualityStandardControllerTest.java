package com.epam.reportportal.base.core.tms.controller.unit;

import static com.epam.reportportal.base.util.StandaloneMockMvcSupport.standaloneJsonSetup;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.core.tms.controller.TmsQualityStandardController;
import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardCriterionRQ;
import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardRQ;
import com.epam.reportportal.base.core.tms.dto.TmsQualityStandardRS;
import com.epam.reportportal.base.core.tms.service.TmsQualityStandardService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.util.ProjectExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;
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

class TmsQualityStandardControllerTest {

  private final long projectId = 1L;
  private final String projectKey = "test_project";

  @Mock
  private TmsQualityStandardService tmsQualityStandardService;
  @Mock
  private ProjectExtractor projectExtractor;

  @InjectMocks
  private TmsQualityStandardController controller;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;
  private ReportPortalUser testUser;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    objectMapper = new ObjectMapper();

    testUser = ReportPortalUser.userBuilder()
        .withUserName("testUser")
        .withPassword("password")
        .withUserId(1L)
        .withActive(true)
        .withAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
        .build();

    mockMvc = standaloneJsonSetup(controller)
        .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
          @Override
          public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterAnnotation(AuthenticationPrincipal.class) != null;
          }

          @Override
          public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
              NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return testUser;
          }
        })
        .build();

    var membershipDetails = MembershipDetails.builder()
        .withProjectId(projectId)
        .withProjectKey(projectKey)
        .build();
    given(projectExtractor.extractMembershipDetails(eq(testUser), anyString())).willReturn(membershipDetails);
  }

  private TmsQualityStandardRQ validRq() {
    return TmsQualityStandardRQ.builder()
        .name("Default TC quality rubric")
        .description("6-criteria, sum/100")
        .criteria(List.of(TmsQualityStandardCriterionRQ.builder()
            .name("Scenario correctness").maxPoints(100).sequence(1).build()))
        .build();
  }

  @Test
  void getStandard_ShouldDelegateToServiceAndReturnIt() throws Exception {
    var rs = TmsQualityStandardRS.builder().id(5L).name("Default TC quality rubric").build();
    given(tmsQualityStandardService.getStandard(projectId)).willReturn(rs);

    mockMvc.perform(get("/v1/project/{projectKey}/tms/quality-standard", projectKey))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(5))
        .andExpect(jsonPath("$.name").value("Default TC quality rubric"));

    verify(tmsQualityStandardService).getStandard(projectId);
  }

  @Test
  void createStandard_ShouldDelegateToServiceAndReturnCreated() throws Exception {
    var rq = validRq();
    var rs = TmsQualityStandardRS.builder().id(5L).name(rq.getName()).build();
    given(tmsQualityStandardService.createStandard(eq(projectId), org.mockito.ArgumentMatchers.any())).willReturn(rs);

    mockMvc.perform(post("/v1/project/{projectKey}/tms/quality-standard", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(rq)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(5));

    verify(tmsQualityStandardService).createStandard(eq(projectId), org.mockito.ArgumentMatchers.any());
  }

  @Test
  void createStandard_WithCriteriaNotSummingToOneHundred_ShouldReturnBadRequestWithoutCallingService() throws Exception {
    var rq = TmsQualityStandardRQ.builder()
        .name("Bad rubric")
        .criteria(List.of(TmsQualityStandardCriterionRQ.builder().name("c1").maxPoints(50).sequence(1).build()))
        .build();

    mockMvc.perform(post("/v1/project/{projectKey}/tms/quality-standard", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(rq)))
        .andExpect(status().isBadRequest());

    org.mockito.Mockito.verifyNoInteractions(tmsQualityStandardService);
  }

  @Test
  void updateStandard_ShouldDelegateToServiceAndReturnUpdated() throws Exception {
    var rq = validRq();
    var rs = TmsQualityStandardRS.builder().id(5L).name(rq.getName()).build();
    given(tmsQualityStandardService.updateStandard(eq(projectId), org.mockito.ArgumentMatchers.any())).willReturn(rs);

    mockMvc.perform(put("/v1/project/{projectKey}/tms/quality-standard", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(rq)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(5));

    verify(tmsQualityStandardService).updateStandard(eq(projectId), org.mockito.ArgumentMatchers.any());
  }

  @Test
  void deleteStandard_ShouldDelegateToService() throws Exception {
    mockMvc.perform(delete("/v1/project/{projectKey}/tms/quality-standard", projectKey))
        .andExpect(status().isOk());

    verify(tmsQualityStandardService).deleteStandard(projectId);
  }
}
