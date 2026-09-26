package com.epam.reportportal.base.core.aifactory.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationDetailRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationSummaryRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageCiRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageSummaryRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStepResultRQ;
import com.epam.reportportal.base.core.aifactory.enums.CiProvider;
import com.epam.reportportal.base.core.aifactory.enums.PipelineRunStatus;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineAttributes;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineMetrics;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
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
    pipeline.setDescription("desc");
    pipeline.setAutoReadyEnabled(true);
    pipeline.setAutoReadyThreshold(90);
    pipeline.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));

    iteration = new PipelineIteration();
    iteration.setId(7L);
    iteration.setPipeline(pipeline);
    iteration.setIterationNumber(3);
    iteration.setStatus(PipelineRunStatus.PASSED);
    iteration.setTrigger("CI · booking pack");
    iteration.setRerun(false);
    iteration.setStartedAt(Instant.parse("2026-01-01T00:00:00Z"));
    iteration.setFinishedAt(Instant.parse("2026-01-01T00:05:00Z"));
    iteration.setCreatedBy(10L);
    iteration.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
  }

  @Test
  void toPipelineRS_ShouldMapAllFields() {
    var summary = PipelineIterationSummaryRS.builder().id(7L).build();

    var result = mapper.toPipelineRS(pipeline, 5L, summary);

    assertEquals(pipeline.getId(), result.getId());
    assertEquals(pipeline.getName(), result.getName());
    assertEquals(pipeline.getDescription(), result.getDescription());
    assertTrue(result.isAutoReadyEnabled());
    assertEquals(90, result.getAutoReadyThreshold());
    assertEquals(5L, result.getIterationsCount());
    assertEquals(summary, result.getLatestIteration());
    assertEquals(pipeline.getCreatedAt(), result.getCreatedAt());
  }

  @Test
  void toIterationSummaryRS_WithNullMetrics_ShouldMapWithNullMetricsMap() {
    iteration.setMetrics(null);
    var stages = List.of(PipelineStageSummaryRS.builder().id(50L).build(),
        PipelineStageSummaryRS.builder().id(51L).build());

    var result = mapper.toIterationSummaryRS(iteration, stages);

    assertEquals(7L, result.getId());
    assertEquals(pipeline.getId(), result.getPipelineId());
    assertEquals(pipeline.getName(), result.getPipelineName());
    assertEquals(3, result.getIterationNumber());
    assertEquals(PipelineRunStatus.PASSED, result.getStatus());
    assertNull(result.getMetrics());
    assertEquals(2, result.getStagesCount());
    assertEquals(stages, result.getStages());
    assertEquals(300_000L, result.getDurationMillis());
    assertEquals(10L, result.getCreatedBy());
  }

  @Test
  void toIterationSummaryRS_WithMetrics_ShouldUnwrapJsonbValueObjectToPlainMap() {
    iteration.setMetrics(new PipelineMetrics(Map.of("avgScore", 86, "costUsd", 0.61)));

    var result = mapper.toIterationSummaryRS(iteration, List.of());

    assertEquals(Map.of("avgScore", 86, "costUsd", 0.61), result.getMetrics());
  }

  @Test
  void toIterationSummaryRS_WithoutFinishedAt_ShouldMapNullDuration() {
    iteration.setFinishedAt(null);

    var result = mapper.toIterationSummaryRS(iteration, List.of());

    assertNull(result.getDurationMillis());
  }

  @Test
  void toIterationSummaryRS_WithAttributes_ShouldUnwrapJsonbValueObjectToPlainMap() {
    iteration.setAttributes(new PipelineAttributes(Map.of("env", "beta5", "ci", "#1284551")));

    var result = mapper.toIterationSummaryRS(iteration, List.of());

    assertEquals(Map.of("env", "beta5", "ci", "#1284551"), result.getAttributes());
  }

  @Test
  void toIterationSummaryRS_WithoutAttributes_ShouldMapNull() {
    var result = mapper.toIterationSummaryRS(iteration, List.of());

    assertNull(result.getAttributes());
  }

  @Test
  void toStageSummaryRS_ShouldMapFieldsWithoutHeavyStageDetails() {
    var stage = new PipelineStage();
    stage.setId(50L);
    stage.setStageKey("stage-gen-tc");
    stage.setName("MD -> Test cases");
    stage.setShortName("Gen TC");
    stage.setSequence(2);
    stage.setParentStageId(40L);
    stage.setStatus(PipelineRunStatus.PASSED);
    stage.setMetrics(new PipelineMetrics(Map.of("softPct", 86)));

    var result = mapper.toStageSummaryRS(stage);

    assertEquals(50L, result.getId());
    assertEquals("stage-gen-tc", result.getStageKey());
    assertEquals("MD -> Test cases", result.getName());
    assertEquals("Gen TC", result.getShortName());
    assertEquals(2, result.getSequence());
    assertEquals(40L, result.getParentStageId());
    assertEquals(PipelineRunStatus.PASSED, result.getStatus());
    assertEquals(Map.of("softPct", 86), result.getMetrics());
  }

  @Test
  void toIterationDetailRS_ShouldMapFieldsAndEmbedStages() {
    var stages = List.of(PipelineStageRS.builder().id(50L).build());

    PipelineIterationDetailRS result = mapper.toIterationDetailRS(iteration, stages);

    assertEquals(7L, result.getId());
    assertEquals(pipeline.getId(), result.getPipelineId());
    assertEquals(3, result.getIterationNumber());
    assertEquals(stages, result.getStages());
    assertEquals(300_000L, result.getDurationMillis());
    assertNull(result.getAttributes());
  }

  @Test
  void toIterationDetailRS_WithAttributes_ShouldUnwrapJsonbValueObjectToPlainMap() {
    iteration.setAttributes(new PipelineAttributes(Map.of("skill", "create-test-cases@0.4")));

    var result = mapper.toIterationDetailRS(iteration, List.of());

    assertEquals(Map.of("skill", "create-test-cases@0.4"), result.getAttributes());
  }

  @Test
  void toStageRS_WithoutCiOrResult_ShouldLeaveThemNull() {
    var stage = new PipelineStage();
    stage.setId(50L);
    stage.setStageKey("stage-gen-tc");
    stage.setName("MD -> Test cases");
    stage.setSequence(2);
    stage.setStatus(PipelineRunStatus.NEEDS_HUMAN);

    var result = mapper.toStageRS(stage, List.of("TC1", "TC2"));

    assertEquals(50L, result.getId());
    assertEquals("stage-gen-tc", result.getStageKey());
    assertEquals(PipelineRunStatus.NEEDS_HUMAN, result.getStatus());
    assertEquals(List.of("TC1", "TC2"), result.getTestCaseIds());
    assertNull(result.getCi());
    assertNull(result.getResult());
    assertNull(result.getParentStageId());
  }

  @Test
  void toStageRS_WithCiAttributesAndResult_ShouldMapAll() {
    var stage = new PipelineStage();
    stage.setId(50L);
    stage.setStageKey("stage-gen-tc");
    stage.setSequence(2);
    stage.setParentStageId(40L);
    stage.setStatus(PipelineRunStatus.PASSED);
    stage.setCiProvider(CiProvider.GITHUB_ACTIONS);
    stage.setCiRepo("org/repo");
    stage.setCiRunUrl("https://ci/runs/1");
    stage.setRetryable(true);
    stage.setAttributes(new PipelineAttributes(Map.of("agent", "create-test-cases@0.4")));
    stage.setResultType("LAUNCH");
    stage.setResultRef("12");

    var result = mapper.toStageRS(stage, List.of());

    assertEquals(CiProvider.GITHUB_ACTIONS, result.getCi().getProvider());
    assertEquals("org/repo", result.getCi().getRepo());
    assertEquals("https://ci/runs/1", result.getCi().getRunUrl());
    assertTrue(result.getCi().isRetryable());
    assertEquals(40L, result.getParentStageId());
    assertEquals(Map.of("agent", "create-test-cases@0.4"), result.getAttributes());
    assertEquals("LAUNCH", result.getResult().getResultType());
    assertEquals("12", result.getResult().getResultRef());
  }

  @Test
  void toStageEntity_WithCi_ShouldFlattenCiOntoStageColumns() {
    var rq = PipelineStageRQ.builder()
        .stageKey("stage-gen-tc")
        .name("MD -> Test cases")
        .shortName("Gen TC")
        .sequence(2)
        .status(PipelineRunStatus.PASSED)
        .metrics(Map.of("costUsd", 0.98))
        .attributes(Map.of("agent", "create-test-cases@0.4"))
        .result(PipelineStepResultRQ.builder().resultType("LAUNCH").resultRef("12").build())
        .ci(PipelineStageCiRQ.builder()
            .provider(CiProvider.GITHUB_ACTIONS)
            .repo("org/repo")
            .workflowRef("ai-pipeline.yml")
            .runId("123456")
            .jobId("78911")
            .runUrl("https://ci/runs/123456")
            .retryable(true)
            .build())
        .build();

    var stage = mapper.toStageEntity(rq);

    assertEquals("stage-gen-tc", stage.getStageKey());
    assertEquals("MD -> Test cases", stage.getName());
    assertEquals("Gen TC", stage.getShortName());
    assertEquals(2, stage.getSequence());
    assertEquals(PipelineRunStatus.PASSED, stage.getStatus());
    assertEquals(Map.of("costUsd", 0.98), stage.getMetrics().getMetrics());
    assertEquals(Map.of("agent", "create-test-cases@0.4"), stage.getAttributes().getAttributes());
    assertEquals("LAUNCH", stage.getResultType());
    assertEquals("12", stage.getResultRef());
    assertEquals(CiProvider.GITHUB_ACTIONS, stage.getCiProvider());
    assertEquals("org/repo", stage.getCiRepo());
    assertEquals("ai-pipeline.yml", stage.getCiWorkflowRef());
    assertEquals("123456", stage.getCiRunId());
    assertEquals("78911", stage.getCiJobId());
    assertEquals("https://ci/runs/123456", stage.getCiRunUrl());
    assertTrue(stage.isRetryable());
  }

  @Test
  void toStageEntity_WithoutCi_ShouldLeaveCiColumnsUnsetAndNotRetryable() {
    var rq = PipelineStageRQ.builder()
        .stageKey("stage-reqs")
        .sequence(1)
        .status(PipelineRunStatus.PASSED)
        .build();

    var stage = mapper.toStageEntity(rq);

    assertNull(stage.getCiProvider());
    assertFalse(stage.isRetryable());
    assertNull(stage.getMetrics());
    assertNull(stage.getAttributes());
    assertNull(stage.getResultType());
  }

  @Test
  void toCompareRS_ShouldPairStagesByKeyAndIncludeUnmatchedOnBothSides() {
    var currentCommon = PipelineStageRS.builder().stageKey("stage-gen-tc")
        .status(PipelineRunStatus.PASSED).metrics(Map.of("softPct", 86)).build();
    var currentOnly = PipelineStageRS.builder().stageKey("stage-new")
        .status(PipelineRunStatus.PASSED).build();
    var previousCommon = PipelineStageRS.builder().stageKey("stage-gen-tc")
        .status(PipelineRunStatus.NEEDS_HUMAN).metrics(Map.of("softPct", 72)).build();
    var previousOnly = PipelineStageRS.builder().stageKey("stage-removed")
        .status(PipelineRunStatus.FAILED).build();

    var current = PipelineIterationDetailRS.builder().id(7L).stages(List.of(currentCommon, currentOnly)).build();
    var previous = PipelineIterationDetailRS.builder().id(6L).stages(List.of(previousCommon, previousOnly)).build();

    var result = mapper.toCompareRS(current, previous);

    assertEquals(current, result.getCurrent());
    assertEquals(previous, result.getPrevious());
    assertEquals(3, result.getStageDeltas().size());

    var commonDelta = result.getStageDeltas().stream()
        .filter(d -> d.getStageKey().equals("stage-gen-tc")).findFirst().orElseThrow();
    assertEquals(PipelineRunStatus.PASSED, commonDelta.getCurrent().getStatus());
    assertEquals(Map.of("softPct", 86), commonDelta.getCurrent().getMetrics());
    assertEquals(PipelineRunStatus.NEEDS_HUMAN, commonDelta.getPrevious().getStatus());
    assertEquals(Map.of("softPct", 72), commonDelta.getPrevious().getMetrics());

    var currentOnlyDelta = result.getStageDeltas().stream()
        .filter(d -> d.getStageKey().equals("stage-new")).findFirst().orElseThrow();
    assertNull(currentOnlyDelta.getPrevious());

    var previousOnlyDelta = result.getStageDeltas().stream()
        .filter(d -> d.getStageKey().equals("stage-removed")).findFirst().orElseThrow();
    assertNull(previousOnlyDelta.getCurrent());
  }
}
