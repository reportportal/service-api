/*
 * Copyright 2025 EPAM Systems
 * ... (license header unchanged)
 */

package com.epam.reportportal.base.core.settings;

import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.settings.UpdateSettingsRq.SettingsKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Validates the {@code server.sidebar.links} setting to prevent stored XSS via malicious URL schemes. Only
 * {@code https://}, {@code http://}, and {@code mailto:} are permitted.
 */
@Service
@RequiredArgsConstructor
public class SidebarLinksSettingHandler implements ServerSettingHandler {

  private static final String SETTING_KEY = SettingsKey.SERVER_SIDEBAR_LINKS.getName();
  private static final String URL_FIELD = "url";
  private static final Set<String> ALLOWED_SCHEMES = Set.of("https://", "http://", "mailto:");

  private final ObjectMapper objectMapper;

  @Override
  public String getKey() {
    return SETTING_KEY;
  }

  @Override
  public void validate(String value) {
    JsonNode root = parseJson(value);

    if (!root.isArray()) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Value for '%s' must be a JSON array.".formatted(SETTING_KEY));
    }

    root.forEach(link -> {
      JsonNode urlNode = link.get(URL_FIELD);
      if (urlNode != null && urlNode.isTextual()) {
        validateUrl(urlNode.asText());
      }
    });
  }

  private JsonNode parseJson(String value) {
    try {
      return objectMapper.readTree(value);
    } catch (JsonProcessingException _) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Invalid JSON format for '%s'.".formatted(SETTING_KEY));
    }
  }

  private static void validateUrl(String url) {
    String normalized = url.strip().toLowerCase();
    boolean safe = ALLOWED_SCHEMES.stream().anyMatch(normalized::startsWith);
    if (!safe) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
          "Unsafe URL scheme in sidebar link: '%s'. Only https://, http://, and mailto: are permitted."
              .formatted(url));
    }
  }

}
