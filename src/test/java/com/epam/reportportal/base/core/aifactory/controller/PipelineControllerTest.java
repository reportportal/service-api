package com.epam.reportportal.base.core.aifactory.controller;

import static com.epam.reportportal.base.util.StandaloneMockMvcSupport.standaloneJsonSetup;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.core.aifactory.dto.IterationRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineCompareRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationDetailRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationPatchRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationSummaryRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineSettingsRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRetryRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRetryRS;
import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.core.aifactory.service.PipelineIterationFilter;
import com.epam.reportportal.base.core.aifactory.service.PipelineIterationIngestResult;
import com.epam.reportportal.base.core.aifactory.service.PipelineIterationService;
import com.epam.reportportal.base.core.aifactory.service.PipelineService;
import com.epam.reportportal.base.core.aifactory.service.PipelineStageRetryService;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.util.OffsetRequest;
import com.epam.reportportal.base.util.ProjectExtractor;
import com.epam.reportportal.base.ws.resolver.OffsetArgumentResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

class PipelineControllerTest {

  private final long projectId = 1L;
  private final String projectKey = "test_project";

  @Mock
  private PipelineService pipelineService;
  @Mock
  private PipelineIterationService pipelineIterationService;
  @Mock
  private PipelineStageRetryService pipelineStageRetryService;
  @Mock
  private ProjectExtractor projectExtractor;

  @InjectMocks
  private PipelineController pipelineController;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;
  private ReportPortalUser testUser;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    objectMapper = new ObjectMapper().findAndRegisterModules();

    testUser = ReportPortalUser.userBuilder()
        .withUserName("testUser")
        .withPassword("password")
        .withUserId(9L)
        .withActive(true)
        .withAuthorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
        .build();

    mockMvc = standaloneJsonSetup(pipelineController)
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
            new OffsetArgumentResolver())
        .build();

    var membershipDetails = MembershipDetails.builder()
        .withProjectId(projectId)
        .withProjectKey(projectKey)
        .build();
    given(projectExtractor.extractMembershipDetails(eq(testUser), anyString())).willReturn(membershipDetails);
  }

  @Test
  void getPipelines_ShouldDelegateToServiceAndReturnPagedContent() throws Exception {
    Page<PipelineRS> page = new PageImpl<>(List.of(PipelineRS.builder().id(2L).name("factory-tc-gen").build()));
    given(pipelineService.getPipelines(eq(projectId), isNull(), isNull(), any(OffsetRequest.class))).willReturn(page);

    mockMvc.perform(get("/v1/project/{projectKey}/pipeline", projectKey))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(2))
        .andExpect(jsonPath("$.content[0].name").value("factory-tc-gen"));

    verify(projectExtractor).extractMembershipDetails(eq(testUser), anyString());
    verify(pipelineService).getPipelines(eq(projectId), isNull(), isNull(), any(OffsetRequest.class));
  }

  @Test
  void getPipelines_WithNameParameter_ShouldPassItToTheServiceApartFromSearch() throws Exception {
    given(pipelineService.getPipelines(eq(projectId), eq("EXP-002"), eq("gener"), any(OffsetRequest.class)))
        .willReturn(new PageImpl<PipelineRS>(List.of()));

    mockMvc.perform(get("/v1/project/{projectKey}/pipeline", projectKey)
            .param("search", "EXP-002")
            .param("name", "gener"))
        .andExpect(status().isOk());

    verify(pipelineService).getPipelines(eq(projectId), eq("EXP-002"), eq("gener"), any(OffsetRequest.class));
  }

  @Test
  void getPipeline_ShouldDelegateToServiceAndReturnTheSettings() throws Exception {
    given(pipelineService.getPipeline(projectId, 2L)).willReturn(PipelineRS.builder().id(2L).name("factory-tc-gen")
        .settings(java.util.Map.of("autoReady", java.util.Map.of("enabled", true, "threshold", 90))).build());

    mockMvc.perform(get("/v1/project/{projectKey}/pipeline/{pipelineId}", projectKey, 2L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(2))
        .andExpect(jsonPath("$.settings.autoReady.threshold").value(90));

    verify(pipelineService).getPipeline(projectId, 2L);
  }

  @Test
  void updatePipelineSettings_ShouldDelegateToServiceAndReturnUpdatedPipeline() throws Exception {
    var settings = Map.<String, Object>of("autoReady", Map.of("enabled", true, "threshold", 90));
    var rq = PipelineSettingsRQ.builder().settings(settings).build();
    var rs = PipelineRS.builder().id(2L).settings(settings).build();
    given(pipelineService.patchPipelineSettings(projectId, 2L, rq)).willReturn(rs);

    mockMvc.perform(patch("/v1/project/{projectKey}/pipeline/{pipelineId}", projectKey, 2L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(rq)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.settings.autoReady.enabled").value(true))
        .andExpect(jsonPath("$.settings.autoReady.threshold").value(90));

    verify(pipelineService).patchPipelineSettings(projectId, 2L, rq);
  }

  @Test
  void getIterations_ShouldDelegateToServiceAndReturnPagedContent() throws Exception {
    Page<PipelineIterationSummaryRS> page = new PageImpl<>(
        List.of(PipelineIterationSummaryRS.builder().id(7L).iterationNumber(3).build()));
    given(pipelineIterationService.getPipelineIterations(eq(projectId), eq(2L), any(PipelineIterationFilter.class), any(OffsetRequest.class))).willReturn(page);

    mockMvc.perform(get("/v1/project/{projectKey}/pipeline/{pipelineId}/iteration", projectKey, 2L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(7))
        .andExpect(jsonPath("$.content[0].iterationNumber").value(3));

    verify(pipelineIterationService).getPipelineIterations(eq(projectId), eq(2L), any(PipelineIterationFilter.class), any(OffsetRequest.class));
  }

  @Test
  void ingestIteration_ShouldDelegateToServiceWithUserIdAndReturnCreated() throws Exception {
    var rq = PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().stages(List.of(PipelineStageRQ.builder()
            .stageKey("stage-gen-tc").sequence(1).status("Passed").build())).build()).build();
    var rs = PipelineIterationDetailRS.builder().id(7L).pipelineName("factory-tc-gen").build();
    given(pipelineIterationService.createPipelineIteration(projectId, 9L, rq, null))
        .willReturn(new PipelineIterationIngestResult(rs, false));

    mockMvc.perform(post("/v1/project/{projectKey}/pipeline/iteration", projectKey)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(rq)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(7))
        .andExpect(jsonPath("$.pipelineName").value("factory-tc-gen"));

    verify(pipelineIterationService).createPipelineIteration(projectId, 9L, rq, null);
  }

  @Test
  void ingestIteration_WithIdempotencyKey_ShouldPassKeyAndReturn200WithReplayHeaderOnReplay() throws Exception {
    var rq = PipelineIterationRQ.builder().name("factory-tc-gen").iteration(IterationRQ.builder().stages(List.of(PipelineStageRQ.builder()
            .stageKey("stage-gen-tc").sequence(1).status("Passed").build())).build()).build();
    var rs = PipelineIterationDetailRS.builder().id(7L).build();
    given(pipelineIterationService.createPipelineIteration(projectId, 9L, rq, "run-42"))
        .willReturn(new PipelineIterationIngestResult(rs, true));

    mockMvc.perform(post("/v1/project/{projectKey}/pipeline/iteration", projectKey)
            .header("Idempotency-Key", "run-42")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(rq)))
        .andExpect(status().isOk())
        .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
            .header().string("Idempotent-Replayed", "true"))
        .andExpect(jsonPath("$.id").value(7));
  }

  @Test
  void getIteration_ShouldDelegateToServiceAndReturnDetail() throws Exception {
    var rs = PipelineIterationDetailRS.builder().id(7L).build();
    given(pipelineIterationService.getPipelineIteration(projectId, 7L)).willReturn(rs);

    mockMvc.perform(get("/v1/project/{projectKey}/pipeline/iteration/{iterationId}", projectKey, 7L))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(7));

    verify(pipelineIterationService).getPipelineIteration(projectId, 7L);
  }

  @Test
  void compareIterations_ShouldDelegateToServiceWithBothIterationIds() throws Exception {
    var rs = PipelineCompareRS.builder()
        .candidate(PipelineIterationDetailRS.builder().id(7L).build())
        .baseline(PipelineIterationDetailRS.builder().id(6L).build())
        .build();
    given(pipelineIterationService.comparePipelineIterations(projectId, 7L, 6L)).willReturn(rs);

    mockMvc.perform(get("/v1/project/{projectKey}/pipeline/iteration/{iterationId}/compare", projectKey, 7L)
            .param("with", "6"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.candidate.id").value(7))
        .andExpect(jsonPath("$.baseline.id").value(6));

    verify(pipelineIterationService).comparePipelineIterations(projectId, 7L, 6L);
  }

  @Test
  void retryStage_ShouldDelegateToServiceWithUserIdAndReturnAccepted() throws Exception {
    var rq = PipelineStageRetryRQ.builder().comment("Retrying after prompt fix").build();
    var rs = PipelineStageRetryRS.builder().stageId(501L).status("Pending").statusCategory(com.epam.reportportal.base.core.aifactory.enums.StatusCategory.PENDING)
        .triggeredRunUrl("https://ci/runs/999999").build();
    given(pipelineStageRetryService.retryPipelineIterationStage(projectId, 9L, 7L, 501L, rq)).willReturn(rs);

    mockMvc.perform(post("/v1/project/{projectKey}/pipeline/iteration/{iterationId}/stage/{stageId}/retry",
            projectKey, 7L, 501L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(rq)))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.stageId").value(501))
        .andExpect(jsonPath("$.status").value("Pending"))
        .andExpect(jsonPath("$.triggeredRunUrl").value("https://ci/runs/999999"));

    verify(pipelineStageRetryService).retryPipelineIterationStage(projectId, 9L, 7L, 501L, rq);
  }

  @Test
  void reportStage_ShouldDelegateWithTheStageKeyFromThePathAndReturnTheIteration() throws Exception {
    var rq = PipelineStageRQ.builder().status("Running").build();
    var rs = PipelineIterationDetailRS.builder().id(7L).build();
    given(pipelineIterationService.reportStage(projectId, 7L, "grade", rq)).willReturn(rs);

    mockMvc.perform(put("/v1/project/{projectKey}/pipeline/iteration/{iterationId}/stage/{stageKey}",
            projectKey, 7L, "grade")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(rq)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(7));

    verify(pipelineIterationService).reportStage(projectId, 7L, "grade", rq);
  }

  @Test
  void reportStage_WithoutStatus_ShouldBeRejectedByValidation() throws Exception {
    mockMvc.perform(put("/v1/project/{projectKey}/pipeline/iteration/{iterationId}/stage/{stageKey}",
            projectKey, 7L, "grade")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void patchIteration_ShouldDelegateAndReturnTheUpdatedIteration() throws Exception {
    var rq = PipelineIterationPatchRQ.builder()
        .attributes(List.of(PipelineAttribute.builder().key("env").value("beta5").build()))
        .removeAttributes(List.of("jira")).build();
    var rs = PipelineIterationDetailRS.builder().id(7L).build();
    given(pipelineIterationService.patchPipelineIteration(projectId, 7L, rq)).willReturn(rs);

    mockMvc.perform(patch("/v1/project/{projectKey}/pipeline/iteration/{iterationId}", projectKey, 7L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(rq)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(7));

    verify(pipelineIterationService).patchPipelineIteration(projectId, 7L, rq);
  }

  @Test
  void getIterations_ShouldParseStatusAndAttributeFiltersIntoTheFilter() throws Exception {
    given(pipelineIterationService.getPipelineIterations(eq(projectId), eq(2L), any(PipelineIterationFilter.class),
        any(OffsetRequest.class))).willReturn(new PageImpl<PipelineIterationSummaryRS>(List.of()));

    mockMvc.perform(get("/v1/project/{projectKey}/pipeline/{pipelineId}/iteration", projectKey, 2L)
            .param("search", "#4")
            .param("status", "FAILED,RUNNING")
            .param("attribute", "requirement:US-TMS-EXP-002", "ci:#1301188"))
        .andExpect(status().isOk());

    var captor = org.mockito.ArgumentCaptor.forClass(PipelineIterationFilter.class);
    verify(pipelineIterationService).getPipelineIterations(eq(projectId), eq(2L), captor.capture(),
        any(OffsetRequest.class));
    assertEquals("#4", captor.getValue().search());
    assertEquals(java.util.Set.of(StatusCategory.FAILED, StatusCategory.RUNNING), captor.getValue().statuses());
    assertEquals("US-TMS-EXP-002", captor.getValue().attributes().get("requirement"));
    assertEquals("#1301188", captor.getValue().attributes().get("ci"));
  }

  @Test
  void getIterations_WithMalformedAttributeFilter_ShouldBeRejectedAsBadRequest() {
    var thrown = org.junit.jupiter.api.Assertions.assertThrows(Exception.class,
        () -> mockMvc.perform(get("/v1/project/{projectKey}/pipeline/{pipelineId}/iteration", projectKey, 2L)
            .param("attribute", "no-separator")));

    var cause = thrown.getCause() != null ? thrown.getCause() : thrown;
    org.junit.jupiter.api.Assertions.assertInstanceOf(
        com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException.class, cause);
  }

  @Test
  void retryStage_WithoutBody_ShouldPassNullRequestToService() throws Exception {
    var rs = PipelineStageRetryRS.builder().stageId(501L).status("Pending").statusCategory(com.epam.reportportal.base.core.aifactory.enums.StatusCategory.PENDING).build();
    given(pipelineStageRetryService.retryPipelineIterationStage(eq(projectId), eq(9L), eq(7L), eq(501L), isNull())).willReturn(rs);

    mockMvc.perform(post("/v1/project/{projectKey}/pipeline/iteration/{iterationId}/stage/{stageId}/retry",
            projectKey, 7L, 501L))
        .andExpect(status().isAccepted());

    verify(pipelineStageRetryService).retryPipelineIterationStage(eq(projectId), eq(9L), eq(7L), eq(501L), isNull());
  }
}
