package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationSummaryRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineRS;
import com.epam.reportportal.base.core.aifactory.mapper.PipelineMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.util.OffsetRequest;
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
  public Page<PipelineRS> listPipelines(Long projectId, OffsetRequest offsetRequest) {
    return pipelineRepository.findByProjectId(projectId, offsetRequest)
        .map(pipeline -> {
          long iterationsCount = pipelineIterationRepository.countByPipelineId(pipeline.getId());
          PipelineIterationSummaryRS latest = pipelineIterationRepository
              .findFirstByPipelineIdOrderByIterationNumberDesc(pipeline.getId())
              .map(it -> pipelineMapper.toIterationSummaryRS(it, (int) pipelineStageRepository.countByIterationId(it.getId())))
              .orElse(null);
          return pipelineMapper.toPipelineRS(pipeline, iterationsCount, latest);
        });
  }
}
