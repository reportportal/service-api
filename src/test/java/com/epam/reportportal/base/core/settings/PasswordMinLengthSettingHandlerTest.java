package com.epam.reportportal.base.core.settings;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import org.junit.jupiter.api.Test;

class PasswordMinLengthSettingHandlerTest {

  private final PasswordMinLengthSettingHandler handler = new PasswordMinLengthSettingHandler();

  @Test
  void validateWhenValueIsWithinBoundsShouldPass() {
    // given
    String valueMin = "8";
    String valueMax = "256";

    // when + then
    assertDoesNotThrow(() -> handler.validate(valueMin));
    assertDoesNotThrow(() -> handler.validate(valueMax));
  }

  @Test
  void validateWhenValueBelowMinShouldThrow() {
    // given
    String belowMin = "7";

    // when + then
    assertThrows(ReportPortalException.class, () -> handler.validate(belowMin));
  }

  @Test
  void validateWhenValueAboveMaxShouldThrow() {
    // given
    String aboveMax = "257";

    // when + then
    assertThrows(ReportPortalException.class, () -> handler.validate(aboveMax));
  }

  @Test
  void validateWhenValueIsNotIntegerShouldThrow() {
    // given
    String notNumber = "abc";

    // when + then
    assertThrows(ReportPortalException.class, () -> handler.validate(notNumber));
  }
}
