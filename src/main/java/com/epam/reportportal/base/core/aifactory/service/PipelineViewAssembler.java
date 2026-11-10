package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationDetailRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationRefRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineIterationSummaryRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineStageRS;
import com.epam.reportportal.base.core.aifactory.dto.PipelineUserRS;
import com.epam.reportportal.base.core.aifactory.mapper.PipelineMapper;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageAttemptRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStageAttempt;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Builds iteration views (summary for lists, detail for a page) from the stored entities, loading what
 * they need - stages, attempts, the users who started iterations, the iterations re-runs point to - in
 * batches. Shared by the pipeline and iteration services so a list and a page always agree.
 */
@Component
@RequiredArgsConstructor
public class PipelineViewAssembler {

  private final PipelineStageRepository stageRepository;
  private final PipelineStageAttemptRepository attemptRepository;
  private final PipelineIterationRepository iterationRepository;
  private final UserRepository userRepository;
  private final PipelineMapper mapper;

  /**
   * Summaries in the order of {@code iterations}.
   */
  public List<PipelineIterationSummaryRS> summaries(List<PipelineIteration> iterations) {
    if (iterations.isEmpty()) {
      return List.of();
    }
    var iterationIds = iterations.stream().map(PipelineIteration::getId).toList();
    var stagesByIteration = stageRepository.findByIterationIdInOrderByIterationIdAscSequenceAsc(iterationIds)
        .stream()
        .collect(Collectors.groupingBy(stage -> stage.getIteration().getId()));
    var users = users(iterations);
    var refs = rerunSources(iterations);
    return iterations.stream()
        .map(iteration -> {
          var stages = stagesByIteration.getOrDefault(iteration.getId(), List.of());
          return mapper.toIterationSummaryRS(iteration, effectiveAttributes(iteration, stages),
              stages.stream().map(mapper::toStageSummaryRS).collect(Collectors.toList()),
              lookup(users, iteration.getCreatedBy()), lookup(refs, iteration.getRerunOfIterationId()));
        })
        .toList();
  }

  public PipelineIterationSummaryRS summary(PipelineIteration iteration) {
    return summaries(List.of(iteration)).get(0);
  }

  public PipelineIterationDetailRS detail(PipelineIteration iteration) {
    var stages = stageRepository.findByIterationIdOrderBySequenceAsc(iteration.getId());
    var attemptsByStageKey = attemptRepository.findByIterationIdOrderByStageKeyAscAttemptAsc(iteration.getId())
        .stream()
        .collect(Collectors.groupingBy(PipelineStageAttempt::getStageKey));
    var stageRS = new ArrayList<PipelineStageRS>();
    for (var stage : stages) {
      var rs = mapper.toStageRS(stage);
      var attempts = attemptsByStageKey.get(stage.getStageKey());
      if (attempts != null) {
        rs.setAttempts(attempts.stream().map(mapper::toAttemptRS).collect(Collectors.toList()));
      }
      stageRS.add(rs);
    }
    var users = users(List.of(iteration));
    var refs = rerunSources(List.of(iteration));
    return mapper.toIterationDetailRS(iteration, effectiveAttributes(iteration, stages), stageRS,
        lookup(users, iteration.getCreatedBy()), lookup(refs, iteration.getRerunOfIterationId()));
  }

  /**
   * The attributes the iteration was reported with plus those summed up from its stages.
   */
  List<PipelineAttribute> effectiveAttributes(PipelineIteration iteration, List<PipelineStage> stages) {
    var own = iteration.getAttributes() == null ? List.<PipelineAttribute>of() : iteration.getAttributes().getItems();
    var reserved = own.stream().map(PipelineAttribute::getKey).collect(Collectors.toSet());
    var rolled = PipelineAttributeSupport.rollup(stages, reserved);
    if (own.isEmpty() && rolled.isEmpty()) {
      return null;
    }
    var all = new ArrayList<>(own);
    all.addAll(rolled);
    return all;
  }

  private static <V> V lookup(Map<Long, V> map, Long key) {
    return key == null ? null : map.get(key);
  }

  private Map<Long, PipelineUserRS> users(Collection<PipelineIteration> iterations) {
    var ids = iterations.stream().map(PipelineIteration::getCreatedBy).filter(Objects::nonNull)
        .collect(Collectors.toSet());
    if (ids.isEmpty()) {
      return Map.of();
    }
    return mapper.toUserMap(userRepository.findAllById(ids));
  }

  private Map<Long, PipelineIterationRefRS> rerunSources(Collection<PipelineIteration> iterations) {
    var ids = iterations.stream().map(PipelineIteration::getRerunOfIterationId).filter(Objects::nonNull)
        .collect(Collectors.toCollection(HashSet::new));
    if (ids.isEmpty()) {
      return Map.of();
    }
    var refs = new HashMap<Long, PipelineIterationRefRS>();
    iterationRepository.findAllById(ids).forEach(source -> refs.put(source.getId(),
        PipelineIterationRefRS.builder().id(source.getId()).iterationNumber(source.getIterationNumber()).build()));
    return refs;
  }
}
