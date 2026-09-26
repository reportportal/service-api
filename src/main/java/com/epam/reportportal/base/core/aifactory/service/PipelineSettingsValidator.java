package com.epam.reportportal.base.core.aifactory.service;

import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import java.util.Map;

/**
 * Extension point that checks the settings of a pipeline before they are saved.
 * <p>
 * Example: the test case Auto-Ready validator (see {@code AutoReadySettingsValidator}) rejects
 * {@code {"autoReady": {"enabled": true}}} because a threshold between 0 and 100 is missing, and ignores
 * every setting that is not its own.
 * <p>
 * The pipeline core stores settings without interpreting them; validators are how the behaviour that reads
 * a setting keeps its values sane.
 */
public interface PipelineSettingsValidator {

  /**
   * @param pipeline the pipeline whose settings change
   * @param settings all settings the pipeline will have after the change, e.g.
   *                 {@code {"autoReady": {"enabled": true, "threshold": 90}}}
   * @throws com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException with a bad request
   *         error when a setting this validator owns has an unacceptable value
   */
  void validate(Pipeline pipeline, Map<String, Object> settings);
}
