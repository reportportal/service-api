package com.epam.reportportal.base.core.tms.pipeline;

import com.epam.reportportal.base.core.aifactory.service.PipelineSettingsValidator;
import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Checks the {@code autoReady} setting of a pipeline and ignores every other setting.
 * <p>
 * Example: {@code {"autoReady": {"enabled": true, "threshold": 90}}} is accepted;
 * {@code {"autoReady": {"enabled": true}}} and {@code {"autoReady": {"enabled": true, "threshold": 120}}} are
 * rejected, because switching Auto-Ready on needs a threshold between 0 and 100.
 */
@Component
public class AutoReadySettingsValidator implements PipelineSettingsValidator {

  @Override
  public void validate(Pipeline pipeline, Map<String, Object> settings) {
    var rawSetting = settings.get(AutoReadySettings.SETTING_NAME);
    if (rawSetting == null) {
      return;
    }
    var autoReady = AutoReadySettings.of(rawSetting).orElseThrow(() -> new ReportPortalException(
        ErrorType.BAD_REQUEST_ERROR, "'autoReady' must be an object like {\"enabled\": true, \"threshold\": 90}"));
    if (autoReady.enabled() && (autoReady.threshold() == null || autoReady.threshold() < 0
        || autoReady.threshold() > 100)) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "'autoReady.threshold' (0-100) is required when 'autoReady.enabled' is true");
    }
  }
}
