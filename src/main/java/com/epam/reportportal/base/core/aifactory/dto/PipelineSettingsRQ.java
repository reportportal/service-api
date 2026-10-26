package com.epam.reportportal.base.core.aifactory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Changes to a pipeline's settings. Each top-level entry replaces the entry of the same name; an entry set to
 * {@code null} is removed; entries not mentioned stay as they are.
 * <p>
 * Example: {@code {"settings": {"autoReady": {"enabled": true, "threshold": 90}}}} switches on Auto-Ready for
 * the test cases the pipeline produces, and {@code {"settings": {"autoReady": null}}} switches the setting off
 * again by removing it.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineSettingsRQ {

  @NotNull
  @Schema(description = "Settings to set, by name. What a name means is up to the behaviour plugged into the "
      + "pipeline, e.g. \"autoReady\" is read by test case Auto-Ready; a value the plugged-in behaviour does "
      + "not accept is rejected", requiredMode = Schema.RequiredMode.REQUIRED)
  private Map<String, Object> settings;
}
