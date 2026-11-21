/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.base.core.integration.util;

import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.extension.SensitiveIntegrationParam;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Prevents redirecting a stored integration secret to a different server: when a connection target
 * parameter changes, the update request must carry a new secret.
 */
public final class IntegrationTargetChangeValidator {

  /**
   * Lower-cased names of parameters that decide where credentials are sent.
   */
  static final Set<String> TARGET_PARAMS =
      Set.of("host", "url", "port", "protocol", "btsurl", "baseurl", "serverurl", "endpoint");

  private static final Set<String> SENSITIVE_LOWER = SensitiveIntegrationParam.ALL.stream()
      .map(k -> k.toLowerCase(Locale.ROOT)).collect(Collectors.toSet());

  private IntegrationTargetChangeValidator() {
  }

  /**
   * Rejects the update if a connection target changes while a stored secret exists and the request
   * does not supply a new secret.
   *
   * @param integration   persisted integration (before update)
   * @param requestParams parameters from the update request
   */
  public static void validate(Integration integration, Map<String, Object> requestParams) {
    if (requestParams == null || requestParams.isEmpty() || integration.getParams() == null
        || integration.getParams().getParams() == null) {
      return;
    }
    Map<String, Object> stored = integration.getParams().getParams();
    if (!hasSecret(stored)) {
      return;
    }
    boolean targetChanged = requestParams.entrySet().stream()
        .filter(e -> e.getKey() != null && TARGET_PARAMS.contains(e.getKey().toLowerCase(Locale.ROOT)))
        .anyMatch(e -> !sameValue(e.getValue(), findIgnoreCase(stored, e.getKey())));
    if (targetChanged && !hasSecret(requestParams)) {
      throw new ReportPortalException(ErrorType.INCORRECT_REQUEST,
          "Connection target (host, url or port) cannot be changed without providing "
              + "the secret (password or token) again");
    }
  }

  private static boolean hasSecret(Map<String, Object> params) {
    return params.entrySet().stream()
        .anyMatch(e -> e.getKey() != null && SENSITIVE_LOWER.contains(e.getKey().toLowerCase(Locale.ROOT))
            && e.getValue() != null && !e.getValue().toString().isBlank());
  }

  private static Object findIgnoreCase(Map<String, Object> params, String key) {
    return params.entrySet().stream()
        .filter(e -> e.getKey() != null && e.getKey().equalsIgnoreCase(key))
        .map(Map.Entry::getValue)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  private static boolean sameValue(Object requested, Object current) {
    String a = requested == null ? "" : requested.toString().trim();
    String b = current == null ? "" : current.toString().trim();
    return a.equals(b);
  }
}
