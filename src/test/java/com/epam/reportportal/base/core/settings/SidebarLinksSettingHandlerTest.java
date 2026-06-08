package com.epam.reportportal.base.core.settings;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.settings.UpdateSettingsRq.SettingsKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SidebarLinksSettingHandlerTest {

  private final SidebarLinksSettingHandler handler = new SidebarLinksSettingHandler(new ObjectMapper());

  @ParameterizedTest
  @ValueSource(strings = {
      "https://slack.example.com/",
      "http://example.com/",
      "mailto:support@example.com"
  })
  void validateWhenSafeUrlSchemesShouldPass(String url) {
    // Given
    String value = "[{\"name\":\"Link\",\"url\":\"" + url + "\",\"order\":1}]";

    // When + Then
    assertDoesNotThrow(() -> handler.validate(value));
  }

  @Test
  void validateWhenMultipleSafeUrlsShouldPass() {
    // Given
    String value = """
        [
          {"name":"Slack","url":"https://slack.example.com/","order":1},
          {"name":"Contact","url":"mailto:support@example.com","order":2},
          {"name":"Docs","url":"http://docs.example.com/","order":3}
        ]
        """;

    // When + Then
    assertDoesNotThrow(() -> handler.validate(value));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "javascript:alert('XSS')",
      "JAVASCRIPT:alert('XSS')",
      "data:text/html,<script>alert(1)</script>",
      "vbscript:msgbox('XSS')",
      "ftp://unsupported.example.com"
  })
  void validateWhenUnsafeUrlSchemeShouldThrow(String unsafeUrl) {
    // Given
    String value = "[{\"name\":\"Evil\",\"url\":\"" + unsafeUrl + "\",\"order\":1}]";

    // When + Then
    assertThrows(ReportPortalException.class, () -> handler.validate(value));
  }

  @Test
  void validateWhenMixedSafeAndUnsafeLinksShouldThrow() {
    // Given
    String value = """
        [
          {"name":"Safe","url":"https://example.com","order":1},
          {"name":"Evil","url":"javascript:alert('XSS')","order":2}
        ]
        """;

    // When + Then
    assertThrows(ReportPortalException.class, () -> handler.validate(value));
  }

  @Test
  void validateWhenInvalidJsonShouldThrow() {
    // Given
    String value = "not-json";

    // When + Then
    assertThrows(ReportPortalException.class, () -> handler.validate(value));
  }

  @Test
  void validateWhenNotArrayJsonShouldThrow() {
    // Given
    String value = "{\"name\":\"Link\",\"url\":\"https://example.com\"}";

    // When + Then
    assertThrows(ReportPortalException.class, () -> handler.validate(value));
  }

  @Test
  void validateWhenUrlFieldMissingShouldPass() {
    // Given
    String value = "[{\"name\":\"No URL link\",\"order\":1}]";

    // When + Then
    assertDoesNotThrow(() -> handler.validate(value));
  }

  @Test
  void getKeyShouldReturnSidebarLinksKey() {
    assertEquals(SettingsKey.SERVER_SIDEBAR_LINKS.getName(), handler.getKey());
  }
}
