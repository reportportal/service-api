package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.enums.StatusCategory;
import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineIteration;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import java.util.List;
import java.util.Optional;

/**
 * Extension point for stages ReportPortal itself owns, e.g. a human review that is not a CI job but is
 * derived from the lifecycle of entities the iteration produced. The refresher asks every contributor
 * that supports a pipeline to bring its stages up to date whenever the iteration is reported into or
 * something it depends on changes.
 */
public interface PipelineStageContributor {

  boolean supports(Pipeline pipeline);

  /**
   * Creates or updates the stages this contributor owns for the iteration.
   *
   * @param stages all current stages of the iteration, ordered by sequence
   * @return the stages to save: new ones (already bound to the iteration) and changed existing ones
   */
  List<PipelineStage> contribute(PipelineIteration iteration, List<PipelineStage> stages);

  /**
   * Wording for the derived iteration status when the pipeline kind has its own; the category's default
   * label is used when empty.
   */
  default Optional<String> iterationStatusLabel(StatusCategory category) {
    return Optional.empty();
  }

  /**
   * Facts about the whole iteration that follow from the stages this contributor owns (e.g. how many of
   * its test cases are Ready); the refresher adds them to the iteration's attributes by key.
   */
  default List<PipelineAttribute> iterationAttributes(PipelineIteration iteration, List<PipelineStage> stages) {
    return List.of();
  }
}
