package com.epam.reportportal.base.core.tms.pipeline;

import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import java.util.Map;
import java.util.Optional;

/**
 * The test case Auto-Ready setting of a pipeline: whether test cases an AI pipeline produced are marked Ready
 * on their own when their quality score reaches a threshold.
 * <p>
 * Stored in the pipeline's settings under the name {@value #SETTING_NAME}. Example:
 * {@code {"autoReady": {"enabled": true, "threshold": 90}}} promotes every produced test case that scored 90
 * or more; with {@code "enabled": false}, or without the setting, nothing is promoted.
 */
record AutoReadySettings(boolean enabled, Integer threshold) {

  static final String SETTING_NAME = "autoReady";

  /**
   * The Auto-Ready setting of the pipeline if it has one; a value of an unexpected shape reads as "not set".
   */
  static Optional<AutoReadySettings> of(Pipeline pipeline) {
    if (pipeline.getSettings() == null) {
      return Optional.empty();
    }
    return of(pipeline.getSettings().getValues().get(SETTING_NAME));
  }

  static Optional<AutoReadySettings> of(Object rawSetting) {
    if (!(rawSetting instanceof Map<?, ?> values)) {
      return Optional.empty();
    }
    var enabled = Boolean.TRUE.equals(values.get("enabled"));
    var threshold = values.get("threshold") instanceof Number number ? number.intValue() : null;
    return Optional.of(new AutoReadySettings(enabled, threshold));
  }

  /**
   * Whether test cases should be promoted: it is switched on and has a threshold.
   */
  boolean active() {
    return enabled && threshold != null;
  }
}
