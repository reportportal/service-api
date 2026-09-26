package com.epam.reportportal.base.core.tms.pipeline;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.reportportal.base.infrastructure.persistence.entity.aifactory.Pipeline;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AutoReadySettingsValidatorTest {

  private final AutoReadySettingsValidator validator = new AutoReadySettingsValidator();
  private final Pipeline pipeline = new Pipeline();

  @Test
  void validate_WithEnabledAndAThresholdInRange_ShouldAccept() {
    assertDoesNotThrow(() -> validator.validate(pipeline,
        Map.of("autoReady", Map.of("enabled", true, "threshold", 90))));
    assertDoesNotThrow(() -> validator.validate(pipeline,
        Map.of("autoReady", Map.of("enabled", true, "threshold", 0))));
    assertDoesNotThrow(() -> validator.validate(pipeline,
        Map.of("autoReady", Map.of("enabled", true, "threshold", 100))));
  }

  @Test
  void validate_WhenDisabledOrAbsent_ShouldNotRequireAThreshold() {
    assertDoesNotThrow(() -> validator.validate(pipeline, Map.of("autoReady", Map.of("enabled", false))));
    assertDoesNotThrow(() -> validator.validate(pipeline, Map.of()));
  }

  @Test
  void validate_WhenEnabledWithoutAThresholdOrOutOfRange_ShouldReject() {
    assertThrows(ReportPortalException.class,
        () -> validator.validate(pipeline, Map.of("autoReady", Map.of("enabled", true))));
    assertThrows(ReportPortalException.class, () -> validator.validate(pipeline,
        Map.of("autoReady", Map.of("enabled", true, "threshold", 101))));
    assertThrows(ReportPortalException.class, () -> validator.validate(pipeline,
        Map.of("autoReady", Map.of("enabled", true, "threshold", -1))));
  }

  @Test
  void validate_WhenTheSettingIsNotAnObject_ShouldReject() {
    assertThrows(ReportPortalException.class, () -> validator.validate(pipeline, Map.of("autoReady", "yes")));
  }

  @Test
  void validate_ShouldIgnoreSettingsThatAreNotItsOwn() {
    assertDoesNotThrow(() -> validator.validate(pipeline, Map.of("somethingElse", "anything")));
  }
}
