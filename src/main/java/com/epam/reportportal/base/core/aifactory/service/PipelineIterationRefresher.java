package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineIterationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.aifactory.PipelineStageRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Brings what ReportPortal derives for an iteration up to date: the stages owned by contributors and the
 * iteration's own status, which is the worst category among the stages that count towards it. Called
 * after every report and whenever something an owned stage depends on changes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PipelineIterationRefresher {

  private final PipelineIterationRepository iterationRepository;
  private final PipelineStageRepository stageRepository;
  private final List<PipelineStageContributor> contributors;

  @Transactional
  public PipelineIteration refresh(PipelineIteration iteration) {
    var pipeline = iteration.getPipeline();
    var stages = stageRepository.findByIterationIdOrderBySequenceAsc(iteration.getId());
    var applicable = contributors.stream().filter(contributor -> contributor.supports(pipeline)).toList();

    for (var contributor : applicable) {
      for (var contributed : contributor.contribute(iteration, stages)) {
        var isNew = contributed.getId() == null;
        var saved = stageRepository.save(contributed);
        if (isNew) {
          saved.setPath(String.valueOf(saved.getId()));
          saved = stageRepository.save(saved);
          stages = append(stages, saved);
        }
      }
    }

    var contributedAttributes = new java.util.ArrayList<PipelineAttribute>();
    for (var contributor : applicable) {
      contributedAttributes.addAll(contributor.iterationAttributes(iteration, stages));
    }
    if (!contributedAttributes.isEmpty()) {
      var merged = PipelineAttributeSupport.merge(iteration.getAttributes(), contributedAttributes, null);
      iteration.setAttributes(PipelineAttributeSupport.toColumn(merged));
      iteration.setSearchText(PipelineAttributeSupport.searchText(merged));
      iteration.setAttributeIndex(PipelineAttributeSupport.attributeIndex(merged));
    }

    var categories = stages.stream()
        .filter(PipelineStage::isAffectsIterationStatus)
        .map(PipelineStage::getStatusCategory)
        .filter(category -> category != StatusCategory.SKIPPED && category != StatusCategory.UNKNOWN)
        .collect(Collectors.toCollection(java.util.HashSet::new));
    // A stage that does not count (e.g. an evaluation) can never fail or hold up the iteration, but work
    // that is running is running: the iteration is not idle while it executes.
    if (stages.stream().anyMatch(stage -> !stage.isAffectsIterationStatus()
        && stage.getStatusCategory() == StatusCategory.RUNNING)) {
      categories.add(StatusCategory.RUNNING);
    }
    var category = categories.isEmpty() ? derivedWhenNothingCounts(stages) : StatusCategory.aggregate(categories);
    iteration.setStatusCategory(category);
    iteration.setStatus(applicable.stream()
        .map(contributor -> contributor.iterationStatusLabel(category))
        .flatMap(java.util.Optional::stream)
        .findFirst()
        .orElseGet(category::defaultLabel));
    return iterationRepository.save(iteration);
  }

  /**
   * Refreshes the given iterations in a transaction of their own, so it also works after the change that
   * caused it has been committed (for example: a test case was marked Ready, and the iteration that
   * produced it has to recount its review progress). A failing iteration is logged and does not stop
   * the others.
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void refreshIterations(Collection<Long> iterationIds) {
    for (var id : iterationIds) {
      try {
        iterationRepository.findByIdForUpdate(id).ifPresent(this::refresh);
      } catch (RuntimeException e) {
        log.warn("Could not refresh pipeline iteration {}", id, e);
      }
    }
  }

  /**
   * No stage counts (none reported yet, everything skipped, or only unknown statuses): an iteration
   * without stages is pending, one whose stages are all unknown is unknown, anything else passed.
   */
  private StatusCategory derivedWhenNothingCounts(List<PipelineStage> stages) {
    if (stages.isEmpty()) {
      return StatusCategory.PENDING;
    }
    var counting = stages.stream().filter(PipelineStage::isAffectsIterationStatus)
        .map(PipelineStage::getStatusCategory).collect(Collectors.toSet());
    if (counting.contains(StatusCategory.UNKNOWN)) {
      return StatusCategory.UNKNOWN;
    }
    return counting.isEmpty() ? StatusCategory.PENDING : StatusCategory.PASSED;
  }

  private List<PipelineStage> append(List<PipelineStage> stages, PipelineStage stage) {
    var all = new java.util.ArrayList<>(stages);
    all.add(stage);
    return all;
  }
}
