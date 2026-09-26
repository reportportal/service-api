package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.dto.PipelineCompareRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationDetailRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationSummaryRS;
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
import java.util.List;
import java.util.Map;
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
    Pipeline pipeline = pipelineRepository.findByProjectIdAndName(projectId, rq.getPipelineName())
        .orElseGet(() -> createPipeline(projectId, rq.getPipelineName()));

    PipelineIteration iteration;
    if (rq.isRerun() && rq.getRerunOfIterationId() != null) {
      iteration = pipelineIterationRepository.findById(rq.getRerunOfIterationId())
          .filter(it -> it.getPipeline().getId().equals(pipeline.getId()))
          .orElseThrow(() -> new ReportPortalException(ErrorType.NOT_FOUND,
              "Pipeline iteration '" + rq.getRerunOfIterationId() + "' for pipeline '" + pipeline.getName() + "'"));
      pipelineStageTestCaseRepository.deleteByStage_Iteration_Id(iteration.getId());
      pipelineStageRepository.deleteByIterationId(iteration.getId());
    } else {
      iteration = new PipelineIteration();
      iteration.setPipeline(pipeline);
      int nextNumber = pipelineIterationRepository.findFirstByPipelineIdOrderByIterationNumberDesc(pipeline.getId())
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

    List<PipelineStage> stages = rq.getStages().stream()
        .map(pipelineMapper::toStageEntity)
        .collect(Collectors.toList());
    stages.forEach(stage -> stage.setIteration(iteration));

    PipelineRunStatus aggregate = aggregateStatus(stages);
    iteration.setStatus(aggregate);
    iteration.setQualityGate(aggregate);

    iteration = pipelineIterationRepository.save(iteration);
    for (PipelineStage stage : stages) {
      stage.setIteration(iteration);
    }
    List<PipelineStage> savedStages = pipelineStageRepository.saveAll(stages);

    for (int i = 0; i < savedStages.size(); i++) {
      List<String> testCaseDisplayIds = rq.getStages().get(i).getTestCaseIds();
      if (testCaseDisplayIds != null && !testCaseDisplayIds.isEmpty()) {
        linkTestCases(projectId, savedStages.get(i), testCaseDisplayIds);
      }
    }

    eventPublisher.publishEvent(new PipelineIterationIngestedEvent(iteration.getId(), pipeline.getId(), projectId));

    return toDetailRS(iteration);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PipelineIterationSummaryRS> listIterations(Long projectId, Long pipelineId, OffsetRequest offsetRequest) {
    Pipeline pipeline = getPipelineOrThrow(projectId, pipelineId);
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
    PipelineIteration current = getIterationOrThrow(projectId, iterationId);
    PipelineIteration other = getIterationOrThrow(projectId, otherIterationId);
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

  private void linkTestCases(Long projectId, PipelineStage stage, List<String> displayIds) {
    List<TmsTestCase> matched = tmsTestCaseRepository.findByProjectIdAndDisplayIdIn(projectId, displayIds);
    List<PipelineStageTestCase> links = matched.stream()
        .map(testCase -> {
          var link = new PipelineStageTestCase();
          link.setStage(stage);
          link.setTestCase(testCase);
          return link;
        })
        .collect(Collectors.toList());
    pipelineStageTestCaseRepository.saveAll(links);
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
    List<PipelineStage> stages = pipelineStageRepository.findByIterationIdOrderBySequenceAsc(iteration.getId());
    Map<Long, List<String>> testCaseIdsByStage = pipelineStageTestCaseRepository.findByStage_Iteration_Id(iteration.getId())
        .stream()
        .collect(Collectors.groupingBy(link -> link.getStage().getId(),
            Collectors.mapping(link -> link.getTestCase().getDisplayId(), Collectors.toList())));
    List<PipelineStageRS> stageRS = stages.stream()
        .map(stage -> pipelineMapper.toStageRS(stage, testCaseIdsByStage.getOrDefault(stage.getId(), List.of())))
        .collect(Collectors.toList());
    return pipelineMapper.toIterationDetailRS(iteration, stageRS);
  }
}
