package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.core.aifactory.model.PipelineAttribute;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.PipelineStage;
import java.util.List;

/**
 * Extension point that reacts to a stage report once it is saved, for behaviour that belongs to one kind of
 * pipeline and not to the core.
 * <p>
 * Example: when the Upload stage of a test case generation pipeline passes and Auto-Ready is on for the
 * pipeline, the Auto-Ready handler (see {@code AutoReadyOnStagePassed}) promotes the stage's test cases and
 * answers with the attribute {@code autoReadyPromoted = 1}, which the core adds to the stage and which sums up
 * into the iteration's totals. A plain CI/CD pipeline has no such handler, so nothing happens.
 */
public interface PipelineStageReportedHandler {

  /**
   * Called after each stage report, with the stage already saved and its related entities resolved.
   *
   * @return attributes to add to the stage, replacing ones with the same key, or an empty list
   */
  List<PipelineAttribute> onStageReported(StageReport report);

  /**
   * What a handler is told about a stage report.
   *
   * @param projectId   the project the pipeline belongs to
   * @param pipeline    the pipeline the stage belongs to, including its settings
   * @param stage       the stage as saved after this report
   * @param stagePassed whether this report is the one that made the stage pass, so a handler can act once
   *                    and not on every repeated report of an already passed stage
   */
  record StageReport(Long projectId, Pipeline pipeline, PipelineStage stage, boolean stagePassed) {
  }
}
