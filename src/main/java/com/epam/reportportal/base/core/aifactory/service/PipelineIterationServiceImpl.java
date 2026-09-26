package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.dto.PipelineCompareRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationDetailRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationSummaryRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRS;
import com.epam.reportportal.base.core.aifactory.enums.PipelineRunStatus;
import com.epam.reportportal.base.core.aifactory.event.PipelineIterationIngestedEvent;
import com.epam.reportportal.base.core.aifactory.mapper.PipelineMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.tms.TmsTestCaseRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineMetrics;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStageTestCase;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCase;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.OffsetRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PipelineIterationServiceImpl implements PipelineIterationService {

  private final PipelineRepository pipelineRepository;
  private final PipelineIterationRepository pipelineIterationRepository;
  private final PipelineStageRepository pipelineStageRepository;
  private final PipelineStageTestCaseRepository pipelineStageTestCaseRepository;
  private final TmsTestCaseRepository tmsTestCaseRepository;
  private final ProjectRepository projectRepository;
  private final PipelineMapper pipelineMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public PipelineIterationDetailRS ingestIteration(Long projectId, Long userId, PipelineIterationRQ rq) {
    if (rq.isRerun() && rq.getRerunOfIterationId() == null) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "'rerunOfIterationId' is required when 'rerun' is true");
    }

    var pipeline = pipelineRepository.findByProjectIdAndName(projectId, rq.getPipelineName())
        .orElseGet(() -> createPipeline(projectId, rq.getPipelineName()));

    PipelineIteration iteration;
    if (rq.isRerun()) {
      iteration = pipelineIterationRepository.findById(rq.getRerunOfIterationId())
          .filter(it -> it.getPipeline().getId().equals(pipeline.getId()))
          .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND,
              "Pipeline iteration '" + rq.getRerunOfIterationId() + "' for pipeline '" + pipeline.getName() + "'"));
      // pipeline_stage_test_case.stage_id and pipeline_stage.iteration_id both cascade on delete,
      // so removing the stages alone also removes their test-case links.
      pipelineStageRepository.deleteByIterationId(iteration.getId());
    } else {
      // Locks the pipeline row for the rest of this transaction so two concurrent ingests for the
      // same pipeline can't compute the same next iteration number.
      var lockedPipeline = pipelineRepository.findByIdForUpdate(pipeline.getId())
          .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND, "Pipeline '" + pipeline.getId() + "'"));
      iteration = new PipelineIteration();
      iteration.setPipeline(lockedPipeline);
      var nextNumber = pipelineIterationRepository.findFirstByPipelineIdOrderByIterationNumberDesc(lockedPipeline.getId())
          .map(PipelineIteration::getIterationNumber)
          .orElse(0) + 1;
      iteration.setIterationNumber(nextNumber);
      iteration.setCreatedBy(userId);
    }

    iteration.setTrigger(rq.getTrigger());
    iteration.setStartedAt(rq.getStartedAt());
    iteration.setFinishedAt(rq.getFinishedAt());
    iteration.setRerun(rq.isRerun());
    iteration.setRerunOfIterationId(rq.getRerunOfIterationId());
    iteration.setMetrics(rq.getMetrics() == null ? null : new PipelineMetrics(rq.getMetrics()));

    var stages = rq.getStages().stream()
        .map(pipelineMapper::toStageEntity)
        .collect(Collectors.toList());
    stages.forEach(stage -> stage.setIteration(iteration));

    var aggregate = aggregateStatus(stages);
    iteration.setStatus(aggregate);
    iteration.setQualityGate(aggregate);

    var savedIteration = pipelineIterationRepository.save(iteration);
    var savedStages = pipelineStageRepository.saveAll(stages);

    var stageDisplayIds = rq.getStages().stream()
        .map(PipelineStageRQ::getTestCaseIds)
        .collect(Collectors.toList());
    linkTestCases(projectId, savedStages, stageDisplayIds);

    eventPublisher.publishEvent(
        new PipelineIterationIngestedEvent(savedIteration.getId(), pipeline.getId(), projectId));

    return toDetailRS(savedIteration);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PipelineIterationSummaryRS> listIterations(Long projectId, Long pipelineId, OffsetRequest offsetRequest) {
    var pipeline = getPipelineOrThrow(projectId, pipelineId);
    return pipelineIterationRepository.findByPipelineId(pipeline.getId(), offsetRequest)
        .map(it -> pipelineMapper.toIterationSummaryRS(it, (int) pipelineStageRepository.countByIterationId(it.getId())));
  }

  @Override
  @Transactional(readOnly = true)
  public PipelineIterationDetailRS getIterationDetail(Long projectId, Long iterationId) {
    return toDetailRS(getIterationOrThrow(projectId, iterationId));
  }

  @Override
  @Transactional(readOnly = true)
  public PipelineCompareRS compareIterations(Long projectId, Long iterationId, Long otherIterationId) {
    var current = getIterationOrThrow(projectId, iterationId);
    var other = getIterationOrThrow(projectId, otherIterationId);
    if (!current.getPipeline().getId().equals(other.getPipeline().getId())) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Iterations must belong to the same pipeline definition to be compared");
    }
    return pipelineMapper.toCompareRS(toDetailRS(current), toDetailRS(other));
  }

  private Pipeline createPipeline(Long projectId, String name) {
    var pipeline = new Pipeline();
    pipeline.setProject(projectRepository.getReferenceById(projectId));
    pipeline.setName(name);
    pipeline.setAutoReadyEnabled(false);
    return pipelineRepository.save(pipeline);
  }

  private Pipeline getPipelineOrThrow(Long projectId, Long pipelineId) {
    return pipelineRepository.findById(pipelineId)
        .filter(p -> p.getProject().getId().equals(projectId))
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND, "Pipeline '" + pipelineId + "'"));
  }

  private PipelineIteration getIterationOrThrow(Long projectId, Long iterationId) {
    return pipelineIterationRepository.findById(iterationId)
        .filter(it -> it.getPipeline().getProject().getId().equals(projectId))
        .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND, "Pipeline iteration '" + iterationId + "'"));
  }

  private void linkTestCases(Long projectId, List<PipelineStage> stages, List<List<String>> displayIdsByStage) {
    var allDisplayIds = displayIdsByStage.stream()
        .filter(Objects::nonNull)
        .flatMap(List::stream)
        .distinct()
        .collect(Collectors.toList());
    if (allDisplayIds.isEmpty()) {
      return;
    }

    var testCasesByDisplayId = tmsTestCaseRepository.findByProjectIdAndDisplayIdIn(projectId, allDisplayIds).stream()
        .collect(Collectors.toMap(TmsTestCase::getDisplayId, Function.identity()));

    var links = new ArrayList<PipelineStageTestCase>();
    for (int i = 0; i < stages.size(); i++) {
      var displayIds = displayIdsByStage.get(i);
      if (displayIds == null || displayIds.isEmpty()) {
        continue;
      }
      var stage = stages.get(i);
      for (var displayId : displayIds) {
        var testCase = testCasesByDisplayId.get(displayId);
        if (testCase != null) {
          var link = new PipelineStageTestCase();
          link.setStage(stage);
          link.setTestCase(testCase);
          links.add(link);
        }
      }
    }
    if (!links.isEmpty()) {
      pipelineStageTestCaseRepository.saveAll(links);
    }
  }

  private PipelineRunStatus aggregateStatus(List<PipelineStage> stages) {
    if (stages.stream().anyMatch(s -> s.getStatus() == PipelineRunStatus.FAILED)) {
      return PipelineRunStatus.FAILED;
    }
    if (stages.stream().anyMatch(s -> s.getStatus() == PipelineRunStatus.NEEDS_HUMAN)) {
      return PipelineRunStatus.NEEDS_HUMAN;
    }
    if (stages.stream().anyMatch(s -> s.getStatus() == PipelineRunStatus.PENDING)) {
      return PipelineRunStatus.PENDING;
    }
    return PipelineRunStatus.PASSED;
  }

  private PipelineIterationDetailRS toDetailRS(PipelineIteration iteration) {
    var stages = pipelineStageRepository.findByIterationIdOrderBySequenceAsc(iteration.getId());
    var testCaseIdsByStage = pipelineStageTestCaseRepository.findByStage_Iteration_Id(iteration.getId())
        .stream()
        .collect(Collectors.groupingBy(link -> link.getStage().getId(),
            Collectors.mapping(link -> link.getTestCase().getDisplayId(), Collectors.toList())));
    var stageRS = stages.stream()
        .map(stage -> pipelineMapper.toStageRS(stage, testCaseIdsByStage.getOrDefault(stage.getId(), List.of())))
        .collect(Collectors.toList());
    return pipelineMapper.toIterationDetailRS(iteration, stageRS);
  }
}
