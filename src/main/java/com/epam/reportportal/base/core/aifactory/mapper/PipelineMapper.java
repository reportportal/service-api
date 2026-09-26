package com.epam.reportportal.base.core.aifactory.mapper;

import com.epam.reportportal.base.core.aifactory.dto.PipelineCompareRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineGraderRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineGraderRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationDetailRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationSummaryRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageCiRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageDeltaRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRS;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineGrader;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineGraders;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineMetrics;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
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

  public PipelineIterationSummaryRS toIterationSummaryRS(PipelineIteration iteration, int stagesCount) {
    return PipelineIterationSummaryRS.builder()
        .id(iteration.getId())
        .pipelineId(iteration.getPipeline().getId())
        .pipelineName(iteration.getPipeline().getName())
        .iterationNumber(iteration.getIterationNumber())
        .status(iteration.getStatus())
        .qualityGate(iteration.getQualityGate())
        .metrics(metricsMap(iteration.getMetrics()))
        .trigger(iteration.getTrigger())
        .rerun(iteration.isRerun())
        .rerunOfIterationId(iteration.getRerunOfIterationId())
        .stagesCount(stagesCount)
        .startedAt(iteration.getStartedAt())
        .finishedAt(iteration.getFinishedAt())
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
        .qualityGate(iteration.getQualityGate())
        .metrics(metricsMap(iteration.getMetrics()))
        .trigger(iteration.getTrigger())
        .rerun(iteration.isRerun())
        .rerunOfIterationId(iteration.getRerunOfIterationId())
        .startedAt(iteration.getStartedAt())
        .finishedAt(iteration.getFinishedAt())
        .createdBy(iteration.getCreatedBy())
        .createdAt(iteration.getCreatedAt())
        .stages(stages)
        .build();
  }

  public PipelineStageRS toStageRS(PipelineStage stage, List<String> testCaseIds) {
    return PipelineStageRS.builder()
        .id(stage.getId())
        .stageKey(stage.getStageKey())
        .name(stage.getName())
        .shortName(stage.getShortName())
        .sequence(stage.getSequence())
        .agent(stage.getAgent())
        .status(stage.getStatus())
        .metrics(metricsMap(stage.getMetrics()))
        .note(stage.getNote())
        .graders(gradersToRS(stage.getGraders()))
        .testCaseIds(testCaseIds)
        .ci(toStageCiRS(stage))
        .lastRetriedAt(stage.getLastRetriedAt())
        .lastRetriedBy(stage.getLastRetriedBy())
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
   * {@code iteration}; test case links are resolved and persisted separately by
   * the service (displayId strings need a repository lookup).
   */
  public PipelineStage toStageEntity(PipelineStageRQ rq) {
    var stage = new PipelineStage();
    stage.setStageKey(rq.getStageKey());
    stage.setName(rq.getName());
    stage.setShortName(rq.getShortName());
    stage.setSequence(rq.getSequence());
    stage.setAgent(rq.getAgent());
    stage.setStatus(rq.getStatus());
    stage.setMetrics(toMetrics(rq.getMetrics()));
    stage.setGraders(gradersToEntity(rq.getGraders()));
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

  private List<PipelineGraderRS> gradersToRS(PipelineGraders graders) {
    if (graders == null || graders.getGraders() == null) {
      return null;
    }
    return graders.getGraders().stream()
        .map(g -> PipelineGraderRS.builder().name(g.getName()).type(g.getType()).result(g.getResult()).pass(g.isPass()).build())
        .collect(Collectors.toList());
  }

  private PipelineGraders gradersToEntity(List<PipelineGraderRQ> graders) {
    if (graders == null) {
      return null;
    }
    var mapped = graders.stream()
        .map(g -> PipelineGrader.builder().name(g.getName()).type(g.getType()).result(g.getResult()).pass(g.isPass()).build())
        .collect(Collectors.toList());
    return PipelineGraders.builder().graders(mapped).build();
  }
}
