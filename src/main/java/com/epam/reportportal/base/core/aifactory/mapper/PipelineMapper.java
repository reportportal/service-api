package com.epam.reportportal.base.core.aifactory.mapper;

import com.epam.reportportal.base.core.aifactory.dto.PipelineCompareRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationDetailRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationSummaryRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageCiRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageDeltaRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageSummaryRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStepResultRS;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineAttributes;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineMetrics;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Structural mapping between the {@code core.aifactory} entities and their DTOs.
 * Deliberately hand-written rather than MapStruct: most conversions here unwrap a
 * JSONB value object into a plain map/list or flatten the {@code ci} sub-object
 * onto entity columns, which reads more clearly as plain code. Anything that
 * needs a repository lookup (resolving a {@code Pipeline} by name, a test case by
 * {@code displayId}) is deliberately left to the service layer, not this mapper.
 */
@Component
public class PipelineMapper {

  public PipelineRS toPipelineRS(Pipeline pipeline, long iterationsCount, PipelineIterationSummaryRS latestIteration) {
    return PipelineRS.builder()
        .id(pipeline.getId())
        .name(pipeline.getName())
        .description(pipeline.getDescription())
        .autoReadyEnabled(pipeline.isAutoReadyEnabled())
        .autoReadyThreshold(pipeline.getAutoReadyThreshold())
        .iterationsCount(iterationsCount)
        .latestIteration(latestIteration)
        .createdAt(pipeline.getCreatedAt())
        .build();
  }

  public PipelineIterationSummaryRS toIterationSummaryRS(PipelineIteration iteration,
      List<PipelineStageSummaryRS> stages) {
    return PipelineIterationSummaryRS.builder()
        .id(iteration.getId())
        .pipelineId(iteration.getPipeline().getId())
        .pipelineName(iteration.getPipeline().getName())
        .iterationNumber(iteration.getIterationNumber())
        .status(iteration.getStatus())
        .metrics(metricsMap(iteration.getMetrics()))
        .attributes(attributesMap(iteration.getAttributes()))
        .trigger(iteration.getTrigger())
        .rerun(iteration.isRerun())
        .rerunOfIterationId(iteration.getRerunOfIterationId())
        .stagesCount(stages.size())
        .stages(stages)
        .startedAt(iteration.getStartedAt())
        .finishedAt(iteration.getFinishedAt())
        .durationMillis(durationMillis(iteration))
        .createdBy(iteration.getCreatedBy())
        .createdAt(iteration.getCreatedAt())
        .build();
  }

  public PipelineIterationDetailRS toIterationDetailRS(PipelineIteration iteration, List<PipelineStageRS> stages) {
    return PipelineIterationDetailRS.builder()
        .id(iteration.getId())
        .pipelineId(iteration.getPipeline().getId())
        .pipelineName(iteration.getPipeline().getName())
        .iterationNumber(iteration.getIterationNumber())
        .status(iteration.getStatus())
        .metrics(metricsMap(iteration.getMetrics()))
        .trigger(iteration.getTrigger())
        .rerun(iteration.isRerun())
        .rerunOfIterationId(iteration.getRerunOfIterationId())
        .startedAt(iteration.getStartedAt())
        .finishedAt(iteration.getFinishedAt())
        .durationMillis(durationMillis(iteration))
        .createdBy(iteration.getCreatedBy())
        .createdAt(iteration.getCreatedAt())
        .attributes(attributesMap(iteration.getAttributes()))
        .stages(stages)
        .build();
  }

  private Long durationMillis(PipelineIteration iteration) {
    if (iteration.getStartedAt() == null || iteration.getFinishedAt() == null) {
      return null;
    }
    return Duration.between(iteration.getStartedAt(), iteration.getFinishedAt()).toMillis();
  }

  public PipelineStageSummaryRS toStageSummaryRS(PipelineStage stage) {
    return PipelineStageSummaryRS.builder()
        .id(stage.getId())
        .stageKey(stage.getStageKey())
        .name(stage.getName())
        .shortName(stage.getShortName())
        .sequence(stage.getSequence())
        .parentStageId(stage.getParentStageId())
        .status(stage.getStatus())
        .metrics(metricsMap(stage.getMetrics()))
        .build();
  }

  public PipelineStageRS toStageRS(PipelineStage stage, List<String> testCaseIds) {
    return PipelineStageRS.builder()
        .id(stage.getId())
        .stageKey(stage.getStageKey())
        .name(stage.getName())
        .shortName(stage.getShortName())
        .sequence(stage.getSequence())
        .parentStageId(stage.getParentStageId())
        .status(stage.getStatus())
        .metrics(metricsMap(stage.getMetrics()))
        .attributes(attributesMap(stage.getAttributes()))
        .result(toStepResultRS(stage))
        .testCaseIds(testCaseIds)
        .ci(toStageCiRS(stage))
        .lastRetriedAt(stage.getLastRetriedAt())
        .lastRetriedBy(stage.getLastRetriedBy())
        .build();
  }

  private PipelineStepResultRS toStepResultRS(PipelineStage stage) {
    if (stage.getResultType() == null && stage.getResultRef() == null) {
      return null;
    }
    return PipelineStepResultRS.builder()
        .resultType(stage.getResultType())
        .resultRef(stage.getResultRef())
        .build();
  }

  private PipelineStageCiRS toStageCiRS(PipelineStage stage) {
    if (stage.getCiProvider() == null) {
      return null;
    }
    return PipelineStageCiRS.builder()
        .provider(stage.getCiProvider())
        .repo(stage.getCiRepo())
        .workflowRef(stage.getCiWorkflowRef())
        .runId(stage.getCiRunId())
        .jobId(stage.getCiJobId())
        .runUrl(stage.getCiRunUrl())
        .retryable(stage.isRetryable())
        .build();
  }

  /**
   * Builds a new (unsaved) stage entity from its ingest payload. The caller sets
   * {@code iteration} and, once ids exist, {@code parentStageId}/{@code path}
   * (resolved from {@code parentStageKey} against the rest of the same ingest
   * batch — see {@code PipelineIterationServiceImpl}); test case links are
   * resolved and persisted separately by the service (displayId strings need a
   * repository lookup).
   */
  public PipelineStage toStageEntity(PipelineStageRQ rq) {
    var stage = new PipelineStage();
    stage.setStageKey(rq.getStageKey());
    stage.setName(rq.getName());
    stage.setShortName(rq.getShortName());
    stage.setSequence(rq.getSequence());
    stage.setStatus(rq.getStatus());
    stage.setMetrics(toMetrics(rq.getMetrics()));
    stage.setAttributes(rq.getAttributes() == null ? null : new PipelineAttributes(rq.getAttributes()));
    if (rq.getResult() != null) {
      stage.setResultType(rq.getResult().getResultType());
      stage.setResultRef(rq.getResult().getResultRef());
    }
    if (rq.getCi() != null) {
      stage.setCiProvider(rq.getCi().getProvider());
      stage.setCiRepo(rq.getCi().getRepo());
      stage.setCiWorkflowRef(rq.getCi().getWorkflowRef());
      stage.setCiRunId(rq.getCi().getRunId());
      stage.setCiJobId(rq.getCi().getJobId());
      stage.setCiRunUrl(rq.getCi().getRunUrl());
      stage.setRetryable(rq.getCi().isRetryable());
    }
    return stage;
  }

  public PipelineCompareRS toCompareRS(PipelineIterationDetailRS current, PipelineIterationDetailRS previous) {
    var previousByKey = previous.getStages().stream()
        .collect(Collectors.toMap(PipelineStageRS::getStageKey, s -> s, (a, b) -> a));
    var deltas = new ArrayList<PipelineStageDeltaRS>();
    var seen = new HashSet<String>();
    for (var currentStage : current.getStages()) {
      seen.add(currentStage.getStageKey());
      var previousStage = previousByKey.get(currentStage.getStageKey());
      deltas.add(PipelineStageDeltaRS.builder()
          .stageKey(currentStage.getStageKey())
          .current(toDeltaEntry(currentStage))
          .previous(previousStage == null ? null : toDeltaEntry(previousStage))
          .build());
    }
    for (var previousStage : previous.getStages()) {
      if (!seen.contains(previousStage.getStageKey())) {
        deltas.add(PipelineStageDeltaRS.builder()
            .stageKey(previousStage.getStageKey())
            .current(null)
            .previous(toDeltaEntry(previousStage))
            .build());
      }
    }
    return PipelineCompareRS.builder()
        .current(current)
        .previous(previous)
        .stageDeltas(deltas)
        .build();
  }

  private PipelineStageDeltaRS.Entry toDeltaEntry(PipelineStageRS stage) {
    return PipelineStageDeltaRS.Entry.builder()
        .status(stage.getStatus())
        .metrics(stage.getMetrics())
        .build();
  }

  private Map<String, Object> metricsMap(PipelineMetrics metrics) {
    return metrics == null ? null : metrics.getMetrics();
  }

  private PipelineMetrics toMetrics(Map<String, Object> map) {
    return map == null ? null : new PipelineMetrics(map);
  }

  private Map<String, String> attributesMap(PipelineAttributes attributes) {
    return attributes == null ? null : attributes.getAttributes();
  }
}
