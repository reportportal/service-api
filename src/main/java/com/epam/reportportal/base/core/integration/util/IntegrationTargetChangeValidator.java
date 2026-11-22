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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Prevents redirecting a stored integration secret to a different server.
 *
 * <p>When an update changes a connection target parameter (at any depth), every secret stored
 * before the update is removed from the integration, so the only secrets the updated integration
 * can hold are the ones the update request itself supplies. Must be called before the request
 * parameters are merged into the stored ones.
 *
 * <p>A request that changes the target and does not name any secret parameter at all (neither a new
 * value nor an explicit blank to clear it) is rejected, so a forgotten secret is reported instead of
 * silently producing an integration without credentials.
 */
public final class IntegrationTargetChangeValidator {

  /**
   * Lower-cased names of parameters that decide where credentials are sent.
   */
  static final Set<String> TARGET_PARAMS =
      Set.of("host", "url", "port", "protocol", "btsurl", "baseurl", "serverurl", "endpoint");

  private IntegrationTargetChangeValidator() {
  }

  /**
   * Rejects or neutralises an update that changes a connection target of an integration holding a
   * stored secret: the request must name a secret parameter, and all stored secrets are dropped
   * from {@code integration} so none of them survives the following merge unless re-supplied.
   *
   * @param integration   persisted integration (before update); its params may be replaced
   * @param requestParams parameters from the update request
   */
  public static void validate(Integration integration, Map<String, Object> requestParams) {
    if (requestParams == null || requestParams.isEmpty() || integration.getParams() == null
        || integration.getParams().getParams() == null) {
      return;
    }
    Map<String, Object> stored = integration.getParams().getParams();
    if (!hasStoredSecret(stored) || !targetChanged(requestParams, stored)) {
      return;
    }
    if (!namesSecret(requestParams)) {
      throw new ReportPortalException(ErrorType.INCORRECT_REQUEST,
          "Connection target (host, url, port or protocol) cannot be changed without providing "
              + "the secret (password or token) again");
    }
    integration.getParams().setParams(withoutSecrets(stored));
  }

  private static boolean hasStoredSecret(Object value) {
    return switch (value) {
      case Map<?, ?> map -> map.entrySet().stream().anyMatch(e ->
          (IntegrationParamsEncryptor.isSensitiveKey(e.getKey()) && e.getValue() != null
              && !e.getValue().toString().isBlank()) || hasStoredSecret(e.getValue()));
      case List<?> list -> list.stream().anyMatch(IntegrationTargetChangeValidator::hasStoredSecret);
      case null, default -> false;
    };
  }

  private static boolean namesSecret(Object value) {
    return switch (value) {
      case Map<?, ?> map -> map.entrySet().stream().anyMatch(e ->
          IntegrationParamsEncryptor.isSensitiveKey(e.getKey()) || namesSecret(e.getValue()));
      case List<?> list -> list.stream().anyMatch(IntegrationTargetChangeValidator::namesSecret);
      case null, default -> false;
    };
  }

  /**
   * Walks the request and compares every target parameter with the stored value at the same path
   * (keys matched case-insensitively). A target absent from the stored params counts as changed.
   */
  private static boolean targetChanged(Map<?, ?> request, Map<?, ?> stored) {
    for (Map.Entry<?, ?> entry : request.entrySet()) {
      if (!(entry.getKey() instanceof String key)) {
        continue;
      }
      Object current = stored == null ? null : findIgnoreCase(stored, key);
      Object requested = entry.getValue();
      if (TARGET_PARAMS.contains(key.toLowerCase(Locale.ROOT)) && !sameValue(requested, current)) {
        return true;
      }
      if (nestedTargetChanged(requested, current)) {
        return true;
      }
    }
    return false;
  }

  private static boolean nestedTargetChanged(Object requested, Object current) {
    if (requested instanceof Map<?, ?> map) {
      return targetChanged(map, current instanceof Map<?, ?> storedMap ? storedMap : null);
    }
    if (requested instanceof List<?> list) {
      List<?> storedList = current instanceof List<?> l ? l : List.of();
      for (int i = 0; i < list.size(); i++) {
        if (nestedTargetChanged(list.get(i), i < storedList.size() ? storedList.get(i) : null)) {
          return true;
        }
      }
    }
    return false;
  }

  private static Object findIgnoreCase(Map<?, ?> params, String key) {
    return params.entrySet().stream()
        .filter(e -> e.getKey() instanceof String k && k.equalsIgnoreCase(key))
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

  private static Map<String, Object> withoutSecrets(Map<?, ?> params) {
    Map<String, Object> result = HashMap.newHashMap(params.size());
    params.forEach((key, value) -> {
      if (!IntegrationParamsEncryptor.isSensitiveKey(key)) {
        result.put(key == null ? null : key.toString(), stripValue(value));
      }
    });
    return result;
  }

  private static Object stripValue(Object value) {
    return switch (value) {
      case Map<?, ?> map -> withoutSecrets(map);
      case List<?> list -> {
        List<Object> copy = new ArrayList<>(list.size());
        list.forEach(item -> copy.add(stripValue(item)));
        yield copy;
      }
      case null, default -> value;
    };
  }
}
