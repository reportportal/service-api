package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationSummaryRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineSettingsRQ;
import com.epam.reportportal.base.core.aifactory.mapper.PipelineMapper;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineSpecifications;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineSettings;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.util.OffsetRequest;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PipelineServiceImpl implements PipelineService {

  private final PipelineRepository pipelineRepository;
  private final PipelineIterationRepository pipelineIterationRepository;
  private final PipelineViewAssembler viewAssembler;
  private final PipelineMapper pipelineMapper;
  private final List<PipelineSettingsValidator> settingsValidators;

  @Override
  @Transactional(readOnly = true)
  public Page<PipelineRS> getPipelines(Long projectId, String search, OffsetRequest offsetRequest) {
    var page = pipelineRepository.findAll(PipelineSpecifications.pipelinesOfProject(projectId, search),
        PipelinePaging.capLimit(offsetRequest, Sort.by("name", "id")));
    var pipelineIds = page
        .getContent()
        .stream()
        .map(Pipeline::getId)
        .toList();
    if (pipelineIds.isEmpty()) {
      return page.map(pipeline -> pipelineMapper.toPipelineRS(pipeline, 0, null));
    }

    var iterationsCountByPipelineId = pipelineIterationRepository
        .countByPipelineIdIn(pipelineIds)
        .stream()
        .collect(Collectors.toMap(
            PipelineIterationRepository.PipelineIterationCount::getPipelineId,
            PipelineIterationRepository.PipelineIterationCount::getCount)
        );
    var latestIterations = pipelineIterationRepository.findLatestByPipelineIdIn(pipelineIds);
    var latestSummaryByPipelineId = viewAssembler.summaries(latestIterations)
        .stream()
        .collect(Collectors.toMap(PipelineIterationSummaryRS::getPipelineId, Function.identity()));

    return page.map(pipeline -> pipelineMapper.toPipelineRS(pipeline,
        iterationsCountByPipelineId.getOrDefault(pipeline.getId(), 0L),
        latestSummaryByPipelineId.get(pipeline.getId())));
  }

  @Override
  @Transactional
  public PipelineRS patchPipelineSettings(Long projectId, Long pipelineId, PipelineSettingsRQ rq) {
    var pipeline = pipelineRepository
        .findById(pipelineId)
        .filter(p -> p.getProject().getId().equals(projectId))
        .orElseThrow(
            () -> new ReportPortalException(ErrorType.NOT_FOUND, "Pipeline '" + pipelineId + "'"));

    var settings = new LinkedHashMap<String, Object>(
        pipeline.getSettings() == null ? Map.of() : pipeline.getSettings().getValues());
    rq.getSettings().forEach((name, value) -> {
      if (value == null) {
        settings.remove(name);
      } else {
        settings.put(name, value);
      }
    });
    settingsValidators.forEach(validator -> validator.validate(pipeline, settings));

    pipeline.setSettings(settings.isEmpty() ? null : new PipelineSettings(settings));
    return toPipelineRS(pipelineRepository.save(pipeline));
  }

  private PipelineRS toPipelineRS(Pipeline pipeline) {
    var iterationsCount = pipelineIterationRepository.countByPipelineId(pipeline.getId());
    var latest = pipelineIterationRepository
        .findFirstByPipelineIdOrderByIterationNumberDesc(pipeline.getId())
        .map(viewAssembler::summary)
        .orElse(null);
    return pipelineMapper.toPipelineRS(pipeline, iterationsCount, latest);
  }
}
