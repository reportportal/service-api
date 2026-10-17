package com.epam.reportportal.base.core.aifactory.controller.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.core.aifactory.connector.CiHttpClient;
import com.epam.reportportal.base.core.aifactory.PipelineTestData;
import com.epam.reportportal.base.core.aifactory.dto.PipelineDefinitionRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationPatchRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineSettingsRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRetryRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStepResultRQ;
import com.epam.reportportal.base.core.aifactory.enums.AttributeType;
import com.epam.reportportal.base.core.aifactory.enums.AttributeValueType;
import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.core.aifactory.model.PipelineExecution;
import com.epam.reportportal.base.core.aifactory.model.PipelineSection;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.enums.TmsTestCaseStatus;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.JsonNode;
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
  private static final String BASE = "/v1/project/" + SUPERADMIN_PROJECT_KEY + "/pipeline";

  @Autowired
  private PipelineRepository pipelineRepository;

  @Autowired
  private PipelineStageRepository pipelineStageRepository;

  @Autowired
  private TmsTestCaseRepository tmsTestCaseRepository;

  @MockBean
  private CiHttpClient ciHttpClient;

  private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

  private static String attribute(JsonNode owner, String key) {
    for (var attribute : owner.path("attributes")) {
      if (key.equals(attribute.path("key").asText())) {
        return attribute.path("value").asText();
      }
    }
    return null;
  }

  private static JsonNode stage(JsonNode iteration, String name) {
    for (var stage : iteration.path("stages")) {
      if (name.equals(stage.path("name").asText())) {
        return stage;
      }
    }
    return null;
  }

  private JsonNode body(org.springframework.test.web.servlet.ResultActions actions) throws Exception {
    return mapper.readTree(actions.andReturn().getResponse().getContentAsString());
  }

  private static PipelineAttribute main(String key, String value) {
    return PipelineAttribute.builder().key(key).value(value).type(AttributeType.META).build();
  }

  // ------------------------------------------------------------------ pipelines

  @Test
  void getPipelines_ShouldReturnOnlyThisProjectsPipelinesWithTypeAttributesAndLatestIterationSummary() throws Exception {
    mockMvc.perform(get(BASE).with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[?(@.name=='factory-tc-gen')].id").value(900))
        .andExpect(jsonPath("$.content[?(@.name=='factory-tc-gen')].type").value("TEST_CASE_GENERATION"))
        .andExpect(jsonPath("$.content[?(@.name=='factory-tc-gen')].iterationsCount").value(2))
        .andExpect(jsonPath("$.content[?(@.name=='factory-tc-gen')].latestIteration.iterationNumber").value(2))
        .andExpect(jsonPath("$.content[?(@.name=='factory-tc-gen')].latestIteration.status").value("In review"))
        .andExpect(jsonPath("$.content[?(@.name=='factory-tc-gen')].latestIteration.statusCategory").value("WAITING"))
        .andExpect(jsonPath("$.content[?(@.name=='other-project-pipeline')]").isEmpty());
  }

  @Test
  void updatePipelineSettings_WhenEnablingAutoReadyWithValidThreshold_ShouldPersistAndReturnSettings()
      throws Exception {
    var rq = PipelineSettingsRQ.builder()
        .settings(Map.of("autoReady", Map.of("enabled", true, "threshold", 85))).build();

    mockMvc.perform(patch(BASE + "/900")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.settings.autoReady.enabled").value(true))
        .andExpect(jsonPath("$.settings.autoReady.threshold").value(85));

    var pipeline = pipelineRepository.findById(900L).orElseThrow();
    assertEquals(Map.of("autoReady", Map.of("enabled", true, "threshold", 85)), pipeline.getSettings().getValues());
  }

  @Test
  void updatePipelineSettings_WhenEnablingAutoReadyWithoutThreshold_ShouldReturnBadRequestAndNotPersist()
      throws Exception {
    var rq = PipelineSettingsRQ.builder().settings(Map.of("autoReady", Map.of("enabled", true))).build();

    mockMvc.perform(patch(BASE + "/900")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());

    assertNull(pipelineRepository.findById(900L).orElseThrow().getSettings());
  }

  // ------------------------------------------------------------------ iteration list

  @Test
  void getIterations_ShouldReturnBothIterationsWithTypedAttributesAndCategories() throws Exception {
    var list = body(mockMvc.perform(get(BASE + "/900/iteration").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(2)));

    var newest = list.get("content").get(0);
    var oldest = list.get("content").get(1);
    assertEquals(2, newest.get("iterationNumber").asInt());
    assertEquals("WAITING", newest.get("statusCategory").asText());
    assertEquals("US-TMS-EXP-002", attribute(newest, "requirement"));
    assertEquals("beta5", attribute(oldest, "env"));
    assertEquals("META", oldest.get("attributes").get(0).get("type").asText());
    assertEquals("SCORE", oldest.get("attributes").get(2).get("valueType").asText());
    assertEquals("LINK", oldest.get("attributes").get(5).get("valueType").asText());
  }

  @Test
  void getIterations_WithFilters_ShouldNarrowTheList() throws Exception {
    var url = BASE + "/900/iteration";

    mockMvc.perform(get(url).param("search", "booking pack retry").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].iterationNumber").value(2));
    mockMvc.perform(get(url).param("search", "#1").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].iterationNumber").value(1));
    mockMvc.perform(get(url).param("search", "%").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(jsonPath("$.content.length()").value(0));
    mockMvc.perform(get(url).param("status", "WAITING").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].iterationNumber").value(2));
    mockMvc.perform(get(url).param("status", "WAITING,PASSED").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(jsonPath("$.content.length()").value(2));
    mockMvc.perform(get(url).param("attribute", "env:beta5").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].iterationNumber").value(1));
    mockMvc.perform(get(url).param("attribute", "requirement:us-tms-exp-002").param("attribute", "env:beta5")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(jsonPath("$.content.length()").value(1));
    mockMvc.perform(get(url).param("attribute", "env:beta").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(jsonPath("$.content.length()").value(0));
    mockMvc.perform(get(url).param("attribute", "env:beta5").param("status", "WAITING")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(jsonPath("$.page.totalElements").value(0));
  }

  @Test
  void getPipelines_WithSearch_ShouldMatchPipelineNameOrAnyOfItsIterations() throws Exception {
    mockMvc.perform(get(BASE).param("search", "EXP-002").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].name").value("factory-tc-gen"));
    mockMvc.perform(get(BASE).param("search", "TC-GEN").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(jsonPath("$.content[0].name").value("factory-tc-gen"));
    mockMvc.perform(get(BASE).param("search", "no-such-thing").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(jsonPath("$.content.length()").value(0));
  }

  // ------------------------------------------------------------------ reporting

  @Test
  void createIteration_ForNewPipelineName_ShouldCreatePipelineWithTypeAndLinkTestCases() throws Exception {
    var rq = PipelineIterationRQ.builder()
        .pipeline(PipelineDefinitionRQ.builder().name("brand-new-pipeline").type("TEST_AUTOMATION")
            .attributes(List.of(main("repo", "org/auto"))).build())
        .attributes(List.of(main("trigger", "CI - smoke pack")))
        .stages(List.of(PipelineStageRQ.builder()
            .stageKey("stage-gen-tc")
            .status("Passed")
            .attributes(List.of(PipelineAttribute.builder().key("agent").value("create-test-cases@0.4").build()))
            .sections(PipelineTestData.sectionLinking(List.of(new PipelineSection.EntityReference("TEST_CASE", "TC-900"), new PipelineSection.EntityReference("TEST_CASE", "TC-404"))))
            .build()))
        .build();

    var detail = body(mockMvc.perform(post(BASE + "/iteration")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.pipelineName").value("brand-new-pipeline"))
        .andExpect(jsonPath("$.pipelineType").value("TEST_AUTOMATION"))
        .andExpect(jsonPath("$.iterationNumber").value(1))
        .andExpect(jsonPath("$.statusCategory").value("PASSED"))
        .andExpect(jsonPath("$.warnings[0].code").value("RELATED_ENTITY_NOT_FOUND"))
        .andExpect(jsonPath("$.warnings[0].entityIds[0]").value("TC-404")));

    assertEquals("CI - smoke pack", attribute(detail, "trigger"));
    var createdPipeline = pipelineRepository.findByProjectIdAndName(1L, "brand-new-pipeline").orElseThrow();
    assertNull(createdPipeline.getSettings());
    assertEquals("TEST_AUTOMATION", createdPipeline.getType());
    assertEquals("repo", createdPipeline.getAttributes().getItems().get(0).getKey());
  }

  @Test
  void createIteration_WithADifferentPipelineTypeThanRecorded_ShouldBeBadRequest() throws Exception {
    var rq = PipelineIterationRQ.builder().pipeline(PipelineDefinitionRQ.builder().name("factory-tc-gen").type("TEST_AUTOMATION").build()).build();

    mockMvc.perform(post(BASE + "/iteration")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createIteration_WithIdempotencyKey_ShouldReplaySameBodyAndRejectDifferentBody() throws Exception {
    var rq = PipelineIterationRQ.builder()
        .pipeline(PipelineDefinitionRQ.named("factory-tc-gen"))
        .stages(List.of(PipelineStageRQ.builder().stageKey("stage-gen-tc").status("Passed").build()))
        .build();
    var other = PipelineIterationRQ.builder()
        .pipeline(PipelineDefinitionRQ.named("factory-tc-gen"))
        .stages(List.of(PipelineStageRQ.builder().stageKey("stage-gen-tc").status("Failed").build()))
        .build();
    var url = BASE + "/iteration";

    var first = mockMvc.perform(post(url)
            .header("Idempotency-Key", "ci-run-1001")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    var firstId = mapper.readTree(first).get("id").asLong();

    mockMvc.perform(post(url)
            .header("Idempotency-Key", "ci-run-1001")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(header().string("Idempotent-Replayed", "true"))
        .andExpect(jsonPath("$.id").value(firstId));

    mockMvc.perform(post(url)
            .header("Idempotency-Key", "ci-run-1001")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(other))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isConflict());
  }

  @Test
  void createIteration_ForExistingPipelineNameWithRerunOf_ShouldIncrementNumberAndLinkTheSource() throws Exception {
    var rq = PipelineIterationRQ.builder().pipeline(PipelineDefinitionRQ.named("factory-tc-gen")).rerunOfIterationId(901L).build();

    mockMvc.perform(post(BASE + "/iteration")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.pipelineId").value(900))
        .andExpect(jsonPath("$.iterationNumber").value(3))
        .andExpect(jsonPath("$.rerunOf.id").value(901))
        .andExpect(jsonPath("$.rerunOf.iterationNumber").value(2));
  }

  @Test
  void createIteration_WithNestedStageSectionsAndResultPointer_ShouldRoundTripTreeTablesAndResult() throws Exception {
    var rq = PipelineIterationRQ.builder()
        .pipeline(PipelineDefinitionRQ.named("brand-new-automation-pipeline"))
        .stages(List.of(
            PipelineStageRQ.builder()
                .stageKey("automate")
                .status("Passed")
                .result(PipelineStepResultRQ.builder().resultType("LAUNCH").resultRef("42").build())
                .sections(List.of(PipelineSection.builder().key("usage").title("Token usage")
                    .columns(List.of(PipelineSection.Column.builder().key("model").label("Model").build(),
                        PipelineSection.Column.builder().key("cost").label("Cost")
                            .valueType(AttributeValueType.MONEY).build()))
                    .rows(List.of(PipelineSection.Row.builder().key("auto")
                        .cells(Map.of("model", PipelineSection.Cell.builder().value("auto").build(),
                            "cost", PipelineSection.Cell.builder().value("0.31").build())).build()))
                    .build()))
                .build(),
            PipelineStageRQ.builder()
                .stageKey("run-suite")
                .parentStageKey("automate")
                .status("Passed")
                .build()))
        .build();

    var detail = body(mockMvc.perform(post(BASE + "/iteration")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated()));

    var automate = stage(detail, "automate");
    var runSuite = stage(detail, "run-suite");
    assertNotNull(automate);
    assertNotNull(runSuite);
    assertEquals("LAUNCH", automate.get("result").get("resultType").asText());
    assertEquals("0.31", automate.get("sections").get(0).get("rows").get(0).get("cells").get("cost").get("value").asText());
    assertFalse(automate.has("parentStageId"));
    assertEquals(automate.get("id").asLong(), runSuite.get("parentStageId").asLong());

    var childStage = pipelineStageRepository.findById(runSuite.get("id").asLong()).orElseThrow();
    assertEquals(automate.get("id").asLong() + "." + childStage.getId(), childStage.getPath());
  }

  @Test
  void createIteration_WithBareScalarCells_ShouldAcceptThem() throws Exception {
    var json = """
        {"pipeline": {"name": "scalar-cells"},
         "stages": [{"stageKey": "grade", "status": "Passed",
           "sections": [{"key": "grades", "columns": [{"key": "score", "valueType": "SCORE"}],
                         "rows": [{"key": "r1", "cells": {"score": 90}}]}]}]}
        """;

    var detail = body(mockMvc.perform(post(BASE + "/iteration")
            .contentType(APPLICATION_JSON)
            .content(json)
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated()));

    assertEquals("90", stage(detail, "grade").get("sections").get(0).get("rows").get(0).get("cells")
        .get("score").get("value").asText());
  }

  @Test
  void reportStage_ShouldReportAStageStepByStepAndKeepTheIterationStatusInStep() throws Exception {
    var created = body(mockMvc.perform(post(BASE + "/iteration")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(PipelineIterationRQ.builder().pipeline(PipelineDefinitionRQ.named("incremental")).build()))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.statusCategory").value("PENDING")));
    var iterationId = created.get("id").asLong();
    var url = BASE + "/iteration/" + iterationId + "/stage/create";

    mockMvc.perform(put(url).contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(PipelineStageRQ.builder().status("Running").build()))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCategory").value("RUNNING"))
        .andExpect(jsonPath("$.stages[0].name").value("create"));

    mockMvc.perform(put(url).contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(PipelineStageRQ.builder().status("Passed")
                .attributes(List.of(main("cases", "3"))).build()))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCategory").value("PASSED"))
        .andExpect(jsonPath("$.stages.length()").value(1))
        .andExpect(jsonPath("$.stages[0].attempt").value(1));
  }

  @Test
  void reportStage_OfAFinishedStageByAnotherJob_ShouldStartANewAttemptAndKeepTheFinishedOne() throws Exception {
    var created = body(mockMvc.perform(post(BASE + "/iteration")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(PipelineIterationRQ.builder().pipeline(PipelineDefinitionRQ.named("attempts")).build()))
            .with(token(oAuthHelper.getSuperadminToken()))));
    var url = BASE + "/iteration/" + created.get("id").asLong() + "/stage/upload";

    report(url, "Failed", "job-1");
    var detail = report(url, "Running", "job-2");

    var upload = stage(detail, "upload");
    assertEquals(2, upload.get("attempt").asInt());
    assertEquals(1, upload.get("attempts").size());
    assertEquals("Failed", upload.get("attempts").get(0).get("status").asText());
  }

  private JsonNode report(String url, String status, String jobId) throws Exception {
    var rq = PipelineStageRQ.builder().status(status)
        .execution(PipelineExecution.builder().executorType("GITLAB_CI").externalRunId(jobId)
            .runUrl("https://gitlab.example/jobs/" + jobId).build())
        .build();
    return body(mockMvc.perform(put(url).contentType(APPLICATION_JSON).content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk()));
  }

  @Test
  void reportStage_ForAStageWithAnUnknownStatusText_ShouldKeepTheTextAndShowItNeutrally() throws Exception {
    var created = body(mockMvc.perform(post(BASE + "/iteration")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(PipelineIterationRQ.builder().pipeline(PipelineDefinitionRQ.named("unknown-status")).build()))
            .with(token(oAuthHelper.getSuperadminToken()))));

    mockMvc.perform(put(BASE + "/iteration/" + created.get("id").asLong() + "/stage/grade")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(PipelineStageRQ.builder().status("Queued · waiting for runner").build()))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stages[0].status").value("Queued · waiting for runner"))
        .andExpect(jsonPath("$.stages[0].statusCategory").value(StatusCategory.UNKNOWN.name()));
  }

  @Test
  void generationPipeline_ShouldGetAReviewStageMaintainedByReportPortalAndFailedGradeShouldNotFailTheIteration()
      throws Exception {
    var draft = tmsTestCaseRepository.findById(901L).orElseThrow();
    draft.setStatus(TmsTestCaseStatus.DRAFT);
    tmsTestCaseRepository.saveAndFlush(draft);
    var rq = PipelineIterationRQ.builder()
        .pipeline(PipelineDefinitionRQ.named("factory-tc-gen"))
        .stages(List.of(
            PipelineStageRQ.builder().stageKey("create").status("Passed").build(),
            PipelineStageRQ.builder().stageKey("grade").status("Failed").affectsIterationStatus(false).build(),
            PipelineStageRQ.builder().stageKey("upload").status("Passed")
                .sections(PipelineTestData.sectionLinking(List.of(new PipelineSection.EntityReference("TEST_CASE", "TC-900"),
                    new PipelineSection.EntityReference("TEST_CASE", "TC-901")))).build()))
        .build();

    var detail = body(mockMvc.perform(post(BASE + "/iteration")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("In review"))
        .andExpect(jsonPath("$.statusCategory").value("WAITING")));

    var review = stage(detail, "Review");
    assertNotNull(review);
    assertEquals("REPORTPORTAL", review.get("ownedBy").asText());
    assertEquals("In progress", review.get("status").asText());
    assertEquals("1/2", attribute(review, "review"));
  }

  @Test
  void reportStage_ForTheReviewStageMaintainedByReportPortal_ShouldBeRejected() throws Exception {
    var created = body(mockMvc.perform(post(BASE + "/iteration")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(PipelineIterationRQ.builder().pipeline(PipelineDefinitionRQ.named("factory-tc-gen")).build()))
            .with(token(oAuthHelper.getSuperadminToken()))));

    mockMvc.perform(put(BASE + "/iteration/" + created.get("id").asLong() + "/stage/review")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(PipelineStageRQ.builder().status("Done").build()))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void patchIteration_ShouldAddAndRemoveAttributesAndBeFoundBySearchAfterwards() throws Exception {
    var rq = PipelineIterationPatchRQ.builder()
        .attributes(List.of(PipelineAttribute.builder().key("model").value("claude-opus").build()))
        .removeAttributes(List.of("env")).build();

    var detail = body(mockMvc.perform(patch(BASE + "/iteration/900")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk()));

    assertEquals("claude-opus", attribute(detail, "model"));
    assertEquals(null, attribute(detail, "env"));
    mockMvc.perform(get(BASE + "/900/iteration").param("search", "claude-opus")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].iterationNumber").value(1));
  }

  // ------------------------------------------------------------------ detail & compare

  @Test
  void getIterationDetail_ShouldReturnStagesWithSectionsAsReported() throws Exception {
    mockMvc.perform(get(BASE + "/iteration/900").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(900))
        .andExpect(jsonPath("$.pipelineType").value("TEST_CASE_GENERATION"))
        .andExpect(jsonPath("$.stages[0].name").value("MD -> Test cases"))
        .andExpect(jsonPath("$.stages[0].status").value("Passed"))
        .andExpect(jsonPath("$.stages[0].sections[0].key").value("grades"))
        .andExpect(jsonPath("$.stages[0].sections[0].rows[0].cells.case.target.entityId").value("TC-900"));
  }

  @Test
  void getIterationDetail_ShouldExposeLinksAndRetryableFlagButNotExecutionInternals() throws Exception {
    mockMvc.perform(get(BASE + "/iteration/901").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stages[0].links[0].type").value("RUN"))
        .andExpect(jsonPath("$.stages[0].links[0].url").value("https://github.com/org/repo/actions/runs/123456"))
        .andExpect(jsonPath("$.stages[0].retryable").value(true))
        .andExpect(jsonPath("$.stages[0].execution").doesNotExist())
        .andExpect(jsonPath("$.stages[1].links").doesNotExist());
  }

  @Test
  void compareIterations_SameRequirement_ShouldReturnMetricDeltasAndRowsSideBySide() throws Exception {
    var compare = body(mockMvc.perform(get(BASE + "/iteration/901/compare").param("with", "900")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.candidate.id").value(901))
        .andExpect(jsonPath("$.baseline.id").value(900))
        .andExpect(jsonPath("$.sameScope").value(true)));

    JsonNode suiteScore = null;
    JsonNode cost = null;
    for (var metric : compare.get("metrics")) {
      if ("suiteScore".equals(metric.get("key").asText())) {
        suiteScore = metric;
      } else if ("cost".equals(metric.get("key").asText())) {
        cost = metric;
      }
    }
    assertNotNull(suiteScore);
    assertEquals("-14", suiteScore.get("delta").asText());
    assertEquals("WORSE", suiteScore.get("verdict").asText());
    assertEquals("0.37", cost.get("delta").asText());
    assertEquals("WORSE", cost.get("verdict").asText());
  }

  @Test
  void compareIterations_DifferentPipelineDefinitions_ShouldReturnBadRequest() throws Exception {
    mockMvc.perform(get(BASE + "/iteration/900/compare").param("with", "920")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(org.hamcrest.Matchers.containsString("same pipeline definition")));
  }

  // ------------------------------------------------------------------ retry

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

    mockMvc.perform(post(BASE + "/iteration/901/stage/901/retry")
            .contentType(APPLICATION_JSON)
            .content(mapper.writeValueAsString(rq))
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.stageId").value(901))
        .andExpect(jsonPath("$.status").value("Pending"))
        .andExpect(jsonPath("$.statusCategory").value("PENDING"))
        .andExpect(jsonPath("$.triggeredRunUrl").value("https://github.com/org/repo/actions/workflows/ai-pipeline.yml"));

    var stage = pipelineStageRepository.findById(901L).orElseThrow();
    assertEquals(StatusCategory.PENDING, stage.getStatusCategory());
    assertTrue(stage.getLastRetriedAt() != null);
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void retryStage_WhenStageNotRetryable_ShouldReturnBadRequestWithoutCallingConnector() throws Exception {
    mockMvc.perform(post(BASE + "/iteration/900/stage/900/retry")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isBadRequest());

    org.mockito.Mockito.verifyNoInteractions(ciHttpClient);
    var stage = pipelineStageRepository.findById(900L).orElseThrow();
    assertEquals(StatusCategory.PASSED, stage.getStatusCategory());
  }

  @Test
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  void retryStage_WhenStageBelongsToDifferentIteration_ShouldReturnNotFound() throws Exception {
    mockMvc.perform(post(BASE + "/iteration/900/stage/901/retry")
            .with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isNotFound());
  }
}
