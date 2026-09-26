package com.epam.reportportal.base.core.aifactory.controller.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.core.aifactory.connector.CiHttpClient;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineSettingsRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageCiRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRetryRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStepResultRQ;
import com.epam.reportportal.base.core.aifactory.enums.CiProvider;
import com.epam.reportportal.base.core.aifactory.enums.PipelineRunStatus;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for {@link com.epam.reportportal.base.core.aifactory.controller.PipelineController}.
 * {@link CiHttpClient} is mocked because it is the real outbound-network boundary
 * (GitHub Actions/GitLab CI) — everything else (controller, services, repositories,
 * mapper, connector) runs for real against the fixture below.
 */
@Sql("/db/aifactory/pipeline/pipeline-fill.sql")
@Sql(scripts = "/db/aifactory/pipeline/pipeline-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ExtendWith(MockitoExtension.class)
class PipelineIntegrationTest extends BaseMvcTest {

  private static final String SUPERADMIN_PROJECT_KEY = "superadmin_personal";

  @Autowired
  private PipelineRepository pipelineRepository;

  @Autowired
  private PipelineStageRepository pipelineStageRepository;

  @MockBean
  private CiHttpClient ciHttpClient;

  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

  @Test
  void getPipelines_ShouldReturnOnlyThisProjectsPipelinesWithLatestIterationSummary() throws Exception {
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/pipeline")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[?(@.name=='factory-tc-gen')].id").value(900))
        .andExpect(jsonPath("$.content[?(@.name=='factory-tc-gen')].iterationsCount").value(2))
        .andExpect(jsonPath("$.content[?(@.name=='factory-tc-gen')].latestIteration.iterationNumber").value(2))
        .andExpect(jsonPath("$.content[?(@.name=='factory-tc-gen')].latestIteration.status").value("NEEDS_HUMAN"))
        .andExpect(jsonPath("$.content[?(@.name=='other-project-pipeline')]").isEmpty());
  }

  @Test
  void updatePipelineSettings_WhenEnablingWithValidThreshold_ShouldPersistAndReturnUpdatedPipeline() throws Exception {
    var rq = PipelineSettingsRQ.builder().autoReadyEnabled(true).autoReadyThreshold(85).build();

    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/pipeline/900")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.autoReadyEnabled").value(true))
        .andExpect(jsonPath("$.autoReadyThreshold").value(85));

    var pipeline = pipelineRepository.findById(900L).orElseThrow();
    assertTrue(pipeline.isAutoReadyEnabled());
    assertEquals(85, pipeline.getAutoReadyThreshold());
  }

  @Test
  void updatePipelineSettings_WhenEnablingWithoutThreshold_ShouldReturnBadRequestAndNotPersist() throws Exception {
    var rq = PipelineSettingsRQ.builder().autoReadyEnabled(true).build();

    mockMvc.perform(patch("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/pipeline/900")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());

    var pipeline = pipelineRepository.findById(900L).orElseThrow();
    assertFalse(pipeline.isAutoReadyEnabled());
  }

  @Test
  void getIterations_ShouldReturnBothIterationsOfThePipelineOrderedByOffsetDefault() throws Exception {
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/pipeline/900/iteration")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.content[?(@.iterationNumber==1)].status").value("PASSED"))
        .andExpect(jsonPath("$.content[?(@.iterationNumber==2)].status").value("NEEDS_HUMAN"));
  }

  @Test
  void getIterations_ShouldExposeAttributesOnTheListRowNotJustDetail() throws Exception {
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/pipeline/900/iteration")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[?(@.iterationNumber==1)].attributes.env").value("beta5"))
        .andExpect(jsonPath("$.content[?(@.iterationNumber==1)].attributes.ci").value("#1284551"));
  }

  @Test
  void ingestIteration_ForNewPipelineName_ShouldCreatePipelineAndFirstIterationAndLinkTestCase() throws Exception {
    var rq = PipelineIterationRQ.builder()
        .pipelineName("brand-new-pipeline")
        .trigger("CI - smoke pack")
        .stages(List.of(PipelineStageRQ.builder()
            .stageKey("stage-gen-tc")
            .sequence(1)
            .status(PipelineRunStatus.PASSED)
            .attributes(Map.of("agent", "create-test-cases@0.4"))
            .testCaseIds(List.of("TC-900", "TC-901"))
            .build()))
        .build();

    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/pipeline/iteration")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.pipelineName").value("brand-new-pipeline"))
        .andExpect(jsonPath("$.iterationNumber").value(1))
        .andExpect(jsonPath("$.stages[0].testCaseIds", org.hamcrest.Matchers.containsInAnyOrder("TC-900", "TC-901")));

    var createdPipeline = pipelineRepository.findByProjectIdAndName(1L, "brand-new-pipeline");
    assertTrue(createdPipeline.isPresent());
    assertFalse(createdPipeline.get().isAutoReadyEnabled());
  }

  @Test
  void ingestIteration_ForExistingPipelineName_ShouldIncrementIterationNumber() throws Exception {
    var rq = PipelineIterationRQ.builder()
        .pipelineName("factory-tc-gen")
        .stages(List.of(PipelineStageRQ.builder()
            .stageKey("stage-gen-tc")
            .sequence(1)
            .status(PipelineRunStatus.PASSED)
            .build()))
        .build();

    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/pipeline/iteration")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.pipelineId").value(900))
        .andExpect(jsonPath("$.iterationNumber").value(3));
  }

  @Test
  void ingestIteration_WithNestedStageAndResultPointer_ShouldRoundTripParentStageIdPathAndResult() throws Exception {
    var rq = PipelineIterationRQ.builder()
        .pipelineName("brand-new-automation-pipeline")
        .stages(List.of(
            PipelineStageRQ.builder()
                .stageKey("automate")
                .sequence(1)
                .status(PipelineRunStatus.PASSED)
                .result(PipelineStepResultRQ.builder().resultType("LAUNCH").resultRef("42").build())
                .build(),
            PipelineStageRQ.builder()
                .stageKey("run-suite")
                .sequence(1)
                .parentStageKey("automate")
                .status(PipelineRunStatus.PASSED)
                .build()))
        .build();

    var response = mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/pipeline/iteration")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.stages[?(@.stageKey=='automate')].result.resultType").value("LAUNCH"))
        .andExpect(jsonPath("$.stages[?(@.stageKey=='automate')].result.resultRef").value("42"))
        .andExpect(jsonPath("$.stages[?(@.stageKey=='automate')].parentStageId").doesNotExist())
        .andExpect(jsonPath("$.stages[?(@.stageKey=='run-suite')].parentStageId").exists())
        .andReturn().getResponse().getContentAsString();

    var stagesNode = mapper.readTree(response).get("stages");
    com.fasterxml.jackson.databind.JsonNode automateNode = null;
    com.fasterxml.jackson.databind.JsonNode runSuiteNode = null;
    for (var stageNode : stagesNode) {
      if ("automate".equals(stageNode.get("stageKey").asText())) {
        automateNode = stageNode;
      } else if ("run-suite".equals(stageNode.get("stageKey").asText())) {
        runSuiteNode = stageNode;
      }
    }
    assertTrue(automateNode != null && runSuiteNode != null, "Expected both stages in the response");

    var parentId = automateNode.get("id").asLong();
    var childStageId = runSuiteNode.get("id").asLong();
    assertEquals(parentId, runSuiteNode.get("parentStageId").asLong());

    var childStage = pipelineStageRepository.findById(childStageId).orElseThrow();
    assertEquals(parentId + "." + childStageId, childStage.getPath());
  }

  @Test
  void getIterationDetail_ShouldReturnStagesWithLinkedTestCaseIds() throws Exception {
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/pipeline/iteration/900")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(900))
        .andExpect(jsonPath("$.stages[0].stageKey").value("stage-gen-tc"))
        .andExpect(jsonPath("$.stages[0].testCaseIds[0]").value("TC-900"));
  }

  @Test
  void getIterationDetail_ShouldExposeCiReferenceAndRetryableFlag() throws Exception {
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/pipeline/iteration/901")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stages[0].ci.provider").value("GITHUB_ACTIONS"))
        .andExpect(jsonPath("$.stages[0].ci.retryable").value(true))
        .andExpect(jsonPath("$.stages[1].ci").doesNotExist());
  }

  @Test
  void compareIterations_SamePipeline_ShouldReturnStageDeltasByStageKey() throws Exception {
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/pipeline/iteration/901/compare")
            .param("with", "900")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.current.id").value(901))
        .andExpect(jsonPath("$.previous.id").value(900))
        .andExpect(jsonPath("$.stageDeltas[?(@.stageKey=='stage-gen-tc')].current.status").value("NEEDS_HUMAN"))
        .andExpect(jsonPath("$.stageDeltas[?(@.stageKey=='stage-gen-tc')].previous.status").value("PASSED"))
        .andExpect(jsonPath("$.stageDeltas[?(@.stageKey=='stage-upload')].previous").doesNotExist());
  }

  @Test
  void compareIterations_DifferentPipelineDefinitions_ShouldReturnBadRequest() throws Exception {
    mockMvc.perform(get("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/pipeline/iteration/900/compare")
            .param("with", "920")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("same pipeline definition")));
  }

  /**
   * {@code PipelineStageRetryServiceImpl} deliberately does its DB reads/writes in two
   * independent {@code PROPAGATION_REQUIRES_NEW} transactions (see its class javadoc), so they
   * run on their own connections and commit independently of whatever wraps the request. That
   * collides with {@link BaseMvcTest}'s default test-managed transaction (rolled back, never
   * committed): a REQUIRES_NEW transaction opened on another connection cannot see fixture rows
   * inserted by a transaction that was never committed. Running these tests without the test's
   * own wrapping transaction lets the {@code @Sql} fixture commit for real, so the retry
   * service's transactions can see it; {@code pipeline-cleanup.sql} (AFTER_TEST_METHOD) removes
   * what would otherwise leak into later tests, since there is no rollback safety net here.
   */
  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void retryStage_WhenRetryable_ShouldTriggerConnectorAndSetStagePending() throws Exception {
    when(ciHttpClient.postForStatus(anyString(), anyMap(), anyString())).thenReturn("");
    var rq = PipelineStageRetryRQ.builder().comment("Retrying after prompt fix").build();

    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/pipeline/iteration/901/stage/901/retry")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.stageId").value(901))
        .andExpect(jsonPath("$.status").value("PENDING"))
        .andExpect(jsonPath("$.triggeredRunUrl").value("https://github.com/org/repo/actions/workflows/ai-pipeline.yml"));

    var stage = pipelineStageRepository.findById(901L).orElseThrow();
    assertEquals(PipelineRunStatus.PENDING, stage.getStatus());
    assertTrue(stage.getLastRetriedAt() != null);
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void retryStage_WhenStageNotRetryable_ShouldReturnBadRequestWithoutCallingConnector() throws Exception {
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/pipeline/iteration/900/stage/900/retry")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());

    org.mockito.Mockito.verifyNoInteractions(ciHttpClient);
    var stage = pipelineStageRepository.findById(900L).orElseThrow();
    assertEquals(PipelineRunStatus.PASSED, stage.getStatus());
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void retryStage_WhenStageBelongsToDifferentIteration_ShouldReturnNotFound() throws Exception {
    mockMvc.perform(post("/v1/project/" + SUPERADMIN_PROJECT_KEY + "/pipeline/iteration/900/stage/901/retry")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }
}
