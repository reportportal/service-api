package com.epam.reportportal.base.core.aifactory.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.core.aifactory.PipelineTestData;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationRefRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageSummaryRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStepResultRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineUserRS;
import com.epam.reportportal.base.core.aifactory.enums.AttributeType;
import com.epam.reportportal.base.core.aifactory.enums.StageOwner;
import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.core.aifactory.model.PipelineExecution;
import com.epam.reportportal.base.core.aifactory.model.PipelineLink;
import com.epam.reportportal.base.core.aifactory.model.PipelineSection;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineAttributes;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineRawOutput;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineExecutionData;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineSections;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineSettings;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStageAttempt;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PipelineMapperTest {

  private final PipelineMapper mapper = new PipelineMapper();

  private Pipeline pipeline;
  private PipelineIteration iteration;

  @BeforeEach
  void setUp() {
    var project = new Project();
    project.setId(1L);

    pipeline = new Pipeline();
    pipeline.setId(2L);
    pipeline.setProject(project);
    pipeline.setName("factory-tc-gen");
    pipeline.setType("TEST_CASE_GENERATION");
    pipeline.setDescription("desc");
    pipeline.setSettings(new PipelineSettings(Map.of("autoReady", Map.of("enabled", true, "threshold", 90))));
    pipeline.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));

    iteration = new PipelineIteration();
    iteration.setId(7L);
    iteration.setPipeline(pipeline);
    iteration.setIterationNumber(3);
    iteration.setStatus("Completed");
    iteration.setStatusCategory(StatusCategory.PASSED);
    iteration.setRerun(false);
    iteration.setStartedAt(Instant.parse("2026-01-01T00:00:00Z"));
    iteration.setFinishedAt(Instant.parse("2026-01-01T00:05:00Z"));
    iteration.setCreatedBy(10L);
    iteration.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
  }

  @Test
  void toPipelineRS_ShouldMapAllFields() {
    pipeline.setAttributes(new PipelineAttributes(List.of(
        PipelineAttribute.builder().key("repo").value("org/repo").build())));

    var result = mapper.toPipelineRS(pipeline, 5L);

    assertEquals(pipeline.getId(), result.getId());
    assertEquals(pipeline.getName(), result.getName());
    assertEquals("TEST_CASE_GENERATION", result.getType());
    assertEquals(pipeline.getDescription(), result.getDescription());
    assertEquals("repo", result.getAttributes().get(0).getKey());
    assertEquals(Map.of("autoReady", Map.of("enabled", true, "threshold", 90)), result.getSettings());
    assertEquals(5L, result.getIterationsCount());
    assertEquals(pipeline.getCreatedAt(), result.getCreatedAt());
  }

  @Test
  void toIterationSummaryRS_ShouldMapHeaderFactsAndGivenAttributes() {
    var attributes = List.of(PipelineAttribute.builder().key("requirement").value("US-1")
        .type(AttributeType.META).build());
    var stages = List.of(PipelineStageSummaryRS.builder().id(50L).build(),
        PipelineStageSummaryRS.builder().id(51L).build());
    var startedBy = PipelineUserRS.builder().id(10L).login("jane").build();
    var rerunOf = PipelineIterationRefRS.builder().id(6L).iterationNumber(2).build();

    var result = mapper.toIterationSummaryRS(iteration, attributes, stages, startedBy, rerunOf);

    assertEquals(7L, result.getId());
    assertEquals(pipeline.getId(), result.getPipelineId());
    assertEquals(pipeline.getName(), result.getPipelineName());
    assertEquals("TEST_CASE_GENERATION", result.getPipelineType());
    assertEquals(3, result.getIterationNumber());
    assertEquals("Completed", result.getStatus());
    assertEquals(StatusCategory.PASSED, result.getStatusCategory());
    assertEquals(attributes, result.getAttributes());
    assertEquals(stages, result.getStages());
    assertEquals(300_000L, result.getDurationMillis());
    assertEquals(startedBy, result.getStartedBy());
    assertEquals(rerunOf, result.getRerunOf());
  }

  @Test
  void toIterationSummaryRS_WithoutFinishedAt_ShouldMapNullDuration() {
    iteration.setFinishedAt(null);

    var result = mapper.toIterationSummaryRS(iteration, null, List.of(), null, null);

    assertNull(result.getDurationMillis());
    assertNull(result.getAttributes());
  }

  @Test
  void toIterationDetailRS_ShouldMapFieldsAndEmbedStages() {
    var stages = List.of(com.epam.reportportal.base.core.aifactory.dto.PipelineStageRS.builder().id(50L).build());

    var result = mapper.toIterationDetailRS(iteration, null, stages, null, null);

    assertEquals(7L, result.getId());
    assertEquals(3, result.getIterationNumber());
    assertEquals(stages, result.getStages());
    assertEquals(300_000L, result.getDurationMillis());
    assertNull(result.getAttributes());
    assertNull(result.getStartedBy());
  }

  @Test
  void toUserRS_ShouldMapLoginAndFullName() {
    var user = new User();
    user.setId(10L);
    user.setLogin("jane");
    user.setFullName("Jane Doe");

    var result = mapper.toUserRS(user);

    assertEquals(10L, result.getId());
    assertEquals("jane", result.getLogin());
    assertEquals("Jane Doe", result.getFullName());
  }

  @Test
  void toStageSummaryRS_ShouldMapStatusAsReportedAndAttributesButNotHeavyDetails() {
    var stage = new PipelineStage();
    stage.setId(50L);
    stage.setStageKey("grade");
    stage.setName("Grade");
    stage.setSequence(2);
    stage.setParentStageId(40L);
    stage.setStatus("Queued · waiting for runner");
    stage.setStatusCategory(StatusCategory.UNKNOWN);
    stage.setOwnedBy(StageOwner.PIPELINE);
    stage.setAttributes(new PipelineAttributes(List.of(
        PipelineAttribute.builder().key("score").value("90").type(AttributeType.META).build())));
    stage.setSections(new PipelineSections(List.of(PipelineSection.builder().key("grades").build())));

    var result = mapper.toStageSummaryRS(stage);

    assertEquals(50L, result.getId());
    assertEquals("grade", result.getStageKey());
    assertEquals("Grade", result.getName());
    assertEquals(2, result.getSequence());
    assertEquals(40L, result.getParentStageId());
    assertEquals("Queued · waiting for runner", result.getStatus());
    assertEquals(StatusCategory.UNKNOWN, result.getStatusCategory());
    assertEquals("score", result.getAttributes().get(0).getKey());
  }

  @Test
  void toAttemptRS_ShouldMapAttemptWithDurationLinksResultAndDetails() {
    var attempt = new PipelineStageAttempt();
    attempt.setAttempt(1);
    attempt.setStatus("Failed");
    attempt.setStatusCategory(StatusCategory.FAILED);
    attempt.setStartedAt(Instant.parse("2026-01-01T10:00:00Z"));
    attempt.setFinishedAt(Instant.parse("2026-01-01T10:00:05Z"));
    attempt.setExecution(new PipelineExecutionData(
        PipelineExecution.builder().executorType("GITLAB_CI").externalRunId("job-1").runUrl("https://ci/jobs/1").build()));
    attempt.setResultRef("err-17");
    attempt.setAttributes(new PipelineAttributes(List.of(PipelineAttribute.builder().key("cost").value("0.1").build())));

    var rs = mapper.toAttemptRS(attempt);

    assertEquals(1, rs.getAttempt());
    assertEquals("Failed", rs.getStatus());
    assertEquals(StatusCategory.FAILED, rs.getStatusCategory());
    assertEquals(5_000L, rs.getDurationMillis());
    assertEquals(PipelineLink.RUN, rs.getLinks().get(0).getType());
    assertEquals("https://ci/jobs/1", rs.getLinks().get(0).getUrl());
    assertEquals("err-17", rs.getResult().getResultRef());
    assertEquals("cost", rs.getAttributes().get(0).getKey());
  }

  @Test
  void stageTiming_ShouldFlowFromRequestToEntityAndBothResponseShapes() {
    var started = Instant.parse("2026-01-01T10:00:00Z");
    var finished = Instant.parse("2026-01-01T10:00:42Z");
    var rq = PipelineStageRQ.builder().stageKey("grade").sequence(2).status("Passed")
        .startedAt(started).finishedAt(finished).build();

    var entity = mapper.toStageEntity(rq);
    var summary = mapper.toStageSummaryRS(entity);
    var full = mapper.toStageRS(entity);

    assertEquals(started, entity.getStartedAt());
    assertEquals(finished, entity.getFinishedAt());
    assertEquals(42_000L, summary.getDurationMillis());
    assertEquals(started, full.getStartedAt());
    assertEquals(finished, full.getFinishedAt());
    assertEquals(42_000L, full.getDurationMillis());
  }

  @Test
  void relatedEntities_ShouldBeCollectedFromTheCellTargetsOfTheSectionsAndReturnedOnTheStage() {
    var rq = PipelineStageRQ.builder().stageKey("upload").status("Passed")
        .sections(PipelineTestData.sectionLinking(List.of(
            new PipelineSection.EntityReference("TEST_CASE", "TC-12"),
            new PipelineSection.EntityReference("LAUNCH", "7"),
            new PipelineSection.EntityReference("TEST_CASE", "TC-12"))))
        .build();

    var entity = mapper.toStageEntity(rq);
    var full = mapper.toStageRS(entity);

    assertEquals(2, entity.getRelatedEntities().getItems().size());
    assertEquals("TEST_CASE", full.getRelatedEntities().get(0).getEntityType());
    assertEquals("TC-12", full.getRelatedEntities().get(0).getEntityId());
    assertEquals("LAUNCH", full.getRelatedEntities().get(1).getEntityType());
  }

  @Test
  void relatedEntities_WhenTheReportNoLongerLinksAnything_ShouldClearThem() {
    var stage = mapper.toStageEntity(PipelineStageRQ.builder().stageKey("upload").status("Running")
        .sections(PipelineTestData.sectionLinking(List.of(new PipelineSection.EntityReference("TEST_CASE", "TC-12"))))
        .build());

    mapper.applyStage(stage, PipelineStageRQ.builder().status("Passed").build());

    assertNull(stage.getRelatedEntities());
  }

  @Test
  void stageSummaryAndCountingFlag_ShouldFlowFromRequestToBothResponses() {
    var stage = mapper.toStageEntity(PipelineStageRQ.builder().stageKey("grade").status("Passed")
        .summary("79").affectsIterationStatus(false).build());

    assertEquals("79", mapper.toStageRS(stage).getSummary());
    assertEquals("79", mapper.toStageSummaryRS(stage).getSummary());
    assertFalse(mapper.toStageRS(stage).isAffectsIterationStatus());
    assertFalse(mapper.toStageSummaryRS(stage).isAffectsIterationStatus());
  }

  @Test
  void stageMessageAndLinks_ShouldFlowFromRequestToResponsesAndKeepTheDerivedRunLink() {
    var reported = PipelineLink.builder().type("ENTITY_LIST").label("Open queue")
        .target(new PipelineSection.EntityReference("TEST_CASE_LIST", "iteration=7")).build();
    var rq = PipelineStageRQ.builder().stageKey("upload").status("Failed")
        .message("Upload failed: folder not found").links(List.of(reported))
        .execution(PipelineExecution.builder().executorType("GITLAB_CI").runUrl("https://ci/run/1").externalRunId("9").build())
        .build();

    var entity = mapper.toStageEntity(rq);
    var summary = mapper.toStageSummaryRS(entity);
    var full = mapper.toStageRS(entity);

    assertEquals("Upload failed: folder not found", summary.getMessage());
    assertEquals("Upload failed: folder not found", full.getMessage());
    assertEquals(2, full.getLinks().size());
    assertEquals("ENTITY_LIST", full.getLinks().get(0).getType());
    assertEquals("iteration=7", full.getLinks().get(0).getTarget().getEntityId());
    assertEquals(PipelineLink.RUN, full.getLinks().get(1).getType());
  }

  @Test
  void stageTiming_WhenStillRunning_ShouldHaveNoDuration() {
    var stage = new PipelineStage();
    stage.setStartedAt(Instant.parse("2026-01-01T10:00:00Z"));

    assertNull(mapper.toStageRS(stage).getDurationMillis());
  }

  @Test
  void toStageRS_WithoutCiOrResult_ShouldLeaveThemNull() {
    var stage = new PipelineStage();
    stage.setId(50L);
    stage.setStageKey("stage-gen-tc");
    stage.setName("Review");
    stage.setSequence(2);
    stage.setStatus("In progress");
    stage.setStatusCategory(StatusCategory.WAITING);
    stage.setOwnedBy(StageOwner.REPORTPORTAL);

    var result = mapper.toStageRS(stage);

    assertEquals(50L, result.getId());
    assertEquals("stage-gen-tc", result.getStageKey());
    assertEquals("In progress", result.getStatus());
    assertEquals(StatusCategory.WAITING, result.getStatusCategory());
    assertEquals(StageOwner.REPORTPORTAL, result.getOwnedBy());
    assertNull(result.getLinks());
    assertNull(result.getResult());
    assertNull(result.getParentStageId());
    assertNull(result.getSections());
    assertNull(result.getRawOutput());
    assertFalse(result.isRetryable());
  }

  @Test
  void toStageRS_WithDetails_ShouldMapAttributesSectionsRawOutputResultAndLinks() {
    var stage = new PipelineStage();
    stage.setId(50L);
    stage.setStageKey("grade");
    stage.setSequence(2);
    stage.setParentStageId(40L);
    stage.setStatus("Passed");
    stage.setStatusCategory(StatusCategory.PASSED);
    stage.setExecution(new PipelineExecutionData(PipelineExecution.builder().executorType("GITHUB_ACTIONS")
        .runUrl("https://ci/runs/1").externalRunId("77").retryable(true).retryParameters(Map.of("repo", "org/repo")).build()));
    stage.setAttributes(new PipelineAttributes(List.of(PipelineAttribute.builder().key("agent").value("a@0.4").build())));
    stage.setSections(new PipelineSections(List.of(PipelineSection.builder().key("grades").title("Grades").build())));
    stage.setRawOutput(new PipelineRawOutput(Map.of("total_score", 90)));
    stage.setRawOutputSchemaName("grade.schema.json@1");
    stage.setResultType("LAUNCH");
    stage.setResultRef("12");

    var result = mapper.toStageRS(stage);

    assertEquals(40L, result.getParentStageId());
    assertTrue(result.isRetryable());
    assertEquals("agent", result.getAttributes().get(0).getKey());
    assertEquals("grades", result.getSections().get(0).getKey());
    assertEquals(Map.of("total_score", 90), result.getRawOutput());
    assertEquals("grade.schema.json@1", result.getRawOutputSchemaName());
    assertEquals("LAUNCH", result.getResult().getResultType());
    assertEquals("12", result.getResult().getResultRef());
    assertEquals(PipelineLink.RUN, result.getLinks().get(0).getType());
    assertEquals("#77", result.getLinks().get(0).getLabel());
  }

  @Test
  void toStageEntity_WithExecution_ShouldKeepItAsReportedAlongWithTheRest() {
    var rq = PipelineStageRQ.builder()
        .stageKey("stage-gen-tc")
        .name("MD -> Test cases")
        .sequence(2)
        .status("Passed")
        .affectsIterationStatus(false)
        .attributes(List.of(PipelineAttribute.builder().key("agent").value("create-test-cases@0.4").build()))
        .sections(List.of(PipelineSection.builder().key("rows").build()))
        .rawOutput(Map.of("k", "v"))
        .rawOutputSchemaName("s@1")
        .result(PipelineStepResultRQ.builder().resultType("LAUNCH").resultRef("12").build())
        .execution(PipelineExecution.builder()
            .executorType("GITHUB_ACTIONS")
            .externalRunId("78911")
            .runUrl("https://ci/runs/123456")
            .retryable(true)
            .retryParameters(Map.of("repo", "org/repo", "workflowRef", "ai-pipeline.yml"))
            .build())
        .build();

    var stage = mapper.toStageEntity(rq);

    assertEquals("stage-gen-tc", stage.getStageKey());
    assertEquals("MD -> Test cases", stage.getName());
    assertEquals(2, stage.getSequence());
    assertEquals("Passed", stage.getStatus());
    assertEquals(StatusCategory.PASSED, stage.getStatusCategory());
    assertEquals(StageOwner.PIPELINE, stage.getOwnedBy());
    assertFalse(stage.isAffectsIterationStatus());
    assertEquals("agent", stage.getAttributes().getItems().get(0).getKey());
    assertEquals("rows", stage.getSections().getItems().get(0).getKey());
    assertEquals(Map.of("k", "v"), stage.getRawOutput().getContent());
    assertEquals("s@1", stage.getRawOutputSchemaName());
    assertEquals("LAUNCH", stage.getResultType());
    assertEquals("12", stage.getResultRef());
    var execution = stage.getExecution().getContent();
    assertEquals("GITHUB_ACTIONS", execution.getExecutorType());
    assertEquals("78911", execution.getExternalRunId());
    assertEquals("https://ci/runs/123456", execution.getRunUrl());
    assertTrue(execution.isRetryable());
    assertEquals(Map.of("repo", "org/repo", "workflowRef", "ai-pipeline.yml"), execution.getRetryParameters());
  }

  @Test
  void toStageEntity_WithoutExecutionOrOptionalParts_ShouldLeaveThemUnsetAndCountTowardsIteration() {
    var rq = PipelineStageRQ.builder().stageKey("stage-reqs").sequence(1).status("Passed").build();

    var stage = mapper.toStageEntity(rq);

    assertNull(stage.getExecution());
    assertNull(stage.getAttributes());
    assertNull(stage.getSections());
    assertNull(stage.getRawOutput());
    assertNull(stage.getResultType());
    assertTrue(stage.isAffectsIterationStatus());
    assertEquals("stage-reqs", stage.getName());
  }

  @Test
  void toStageEntity_WithExplicitCategory_ShouldKeepItAndWithUnknownTextShouldBeUnknown() {
    var explicit = mapper.toStageEntity(PipelineStageRQ.builder().stageKey("a").sequence(1)
        .status("Queued · waiting for runner").statusCategory(StatusCategory.RUNNING).build());
    var unknown = mapper.toStageEntity(PipelineStageRQ.builder().stageKey("b").sequence(2)
        .status("Queued · waiting for runner").build());

    assertEquals(StatusCategory.RUNNING, explicit.getStatusCategory());
    assertEquals("Queued · waiting for runner", explicit.getStatus());
    assertEquals(StatusCategory.UNKNOWN, unknown.getStatusCategory());
  }

  @Test
  void applyStage_ShouldReplaceWhatWasReportedButKeepNameAndCountingFlagWhenAbsent() {
    var stage = mapper.toStageEntity(PipelineStageRQ.builder().stageKey("grade").name("Grade").sequence(2)
        .status("Running").affectsIterationStatus(false)
        .attributes(List.of(PipelineAttribute.builder().key("old").value("1").build())).build());

    mapper.applyStage(stage, PipelineStageRQ.builder().status("Passed").build());

    assertEquals("Grade", stage.getName());
    assertEquals(2, stage.getSequence());
    assertEquals("Passed", stage.getStatus());
    assertEquals(StatusCategory.PASSED, stage.getStatusCategory());
    assertFalse(stage.isAffectsIterationStatus());
    assertNull(stage.getAttributes());
  }

  @Test
  void attributeList_WhenEmptyOrNull_ShouldBeNull() {
    assertNull(mapper.attributeList(null));
    assertNull(mapper.attributeList(new PipelineAttributes(List.of())));
    assertNull(mapper.sectionList(null));
    assertNull(mapper.sectionList(new PipelineSections(List.of())));
  }
}
