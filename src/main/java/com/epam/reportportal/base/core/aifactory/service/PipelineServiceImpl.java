package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.dto.PipelineRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineSettingsRQ;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageSummaryRS;
import com.epam.reportportal.base.core.aifactory.mapper.PipelineMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.OffsetRequest;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PipelineServiceImpl implements PipelineService {

  private final PipelineRepository pipelineRepository;
  private final PipelineIterationRepository pipelineIterationRepository;
  private final PipelineStageRepository pipelineStageRepository;
  private final PipelineMapper pipelineMapper;

  @Override
  @Transactional(readOnly = true)
  public Page<PipelineRS> getPipelines(Long projectId, OffsetRequest offsetRequest) {
    var page = pipelineRepository.findByProjectId(projectId, offsetRequest);
    var pipelineIds = page
        .getContent()
        .stream()
        .map(Pipeline::getId)
        .toList();
    if (pipelineIds.isEmpty()) {
      return page.map(this::toPipelineRS);
    }

    var iterationsCountByPipelineId = pipelineIterationRepository
        .countByPipelineIdIn(pipelineIds)
        .stream()
        .collect(Collectors.toMap(
            PipelineIterationRepository.PipelineIterationCount::getPipelineId,
            PipelineIterationRepository.PipelineIterationCount::getCount)
        );
    var latestIterationByPipelineId = pipelineIterationRepository
        .findLatestByPipelineIdIn(pipelineIds)
        .stream()
        .collect(Collectors.toMap(it -> it.getPipeline().getId(),
            Function.identity()));
    var iterationIds = latestIterationByPipelineId
        .values()
        .stream()
        .map(PipelineIteration::getId).toList();
    var stagesByIterationId = iterationIds.isEmpty() ? Map.<Long, List<PipelineStage>>of()
        : pipelineStageRepository
            .findByIterationIdInOrderByIterationIdAscSequenceAsc(iterationIds)
            .stream()
            .collect(Collectors.groupingBy(stage -> stage.getIteration().getId()));

    return page.map(pipeline -> {
      var iterationsCount = iterationsCountByPipelineId.getOrDefault(pipeline.getId(), 0L);
      var latestIteration = latestIterationByPipelineId.get(pipeline.getId());
      var latestIterationRS = latestIteration == null ? null
          : pipelineMapper.toIterationSummaryRS(latestIteration,
              toStageSummaries(stagesByIterationId.getOrDefault(latestIteration.getId(), List.of())));
      return pipelineMapper.toPipelineRS(pipeline, iterationsCount, latestIterationRS);
    });
  }

  private List<PipelineStageSummaryRS> toStageSummaries(List<PipelineStage> stages) {
    return stages.stream().map(pipelineMapper::toStageSummaryRS).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public PipelineRS patchPipelineSettings(Long projectId, Long pipelineId, PipelineSettingsRQ rq) {
    var pipeline = pipelineRepository
        .findById(pipelineId)
        .filter(p -> p.getProject().getId().equals(projectId))
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.NOT_FOUND, "Pipeline '" + pipelineId + "'"));

    if (Boolean.TRUE.equals(rq.getAutoReadyEnabled())
        && (rq.getAutoReadyThreshold() == null
        || rq.getAutoReadyThreshold() < 0 || rq.getAutoReadyThreshold() > 100)) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "'autoReadyThreshold' (0-100) is required when 'autoReadyEnabled' is true");
    }

    pipeline.setAutoReadyEnabled(rq.getAutoReadyEnabled());
    pipeline.setAutoReadyThreshold(rq.getAutoReadyThreshold());
    return toPipelineRS(pipelineRepository.save(pipeline));
  }

  private PipelineRS toPipelineRS(Pipeline pipeline) {
    var iterationsCount = pipelineIterationRepository.countByPipelineId(pipeline.getId());
    var latest = pipelineIterationRepository
        .findFirstByPipelineIdOrderByIterationNumberDesc(pipeline.getId())
        .map(it -> pipelineMapper.toIterationSummaryRS(it,
            toStageSummaries(pipelineStageRepository.findByIterationIdOrderBySequenceAsc(it.getId()))))
        .orElse(null);
    return pipelineMapper.toPipelineRS(pipeline, iterationsCount, latest);
  }
}
