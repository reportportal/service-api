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

import com.epam.reportportal.extension.SensitiveIntegrationParam;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.stereotype.Component;

/**
 * Encrypts sensitive integration fields centrally before persisting. Fields listed in
 * {@link SensitiveIntegrationParam#ALL} are encrypted; null values and unknown keys are passed through unchanged.
 * Nested maps and lists are traversed recursively.
 */
@Component
@RequiredArgsConstructor
public class IntegrationParamsEncryptor {

  private final BasicTextEncryptor basicTextEncryptor;

  public Map<String, Object> encryptSensitiveFields(Map<String, Object> params) {
    if (MapUtils.isEmpty(params)) {
      return params;
    }
    return encryptMap(params);
  }

  private Map<String, Object> encryptMap(Map<String, Object> params) {
    Map<String, Object> result = HashMap.newHashMap(params.size());
    params.forEach((key, value) -> result.put(key, encryptEntry(key, value)));
    return result;
  }

  private Object encryptEntry(String key, Object value) {
    if (value != null && SensitiveIntegrationParam.ALL.contains(key)) {
      return basicTextEncryptor.encrypt(value.toString());
    }
    return traverse(value);
  }

  private Object traverse(Object value) {
    return switch (value) {
      case Map<?, ?> map -> encryptMap((Map<String, Object>) map);
      case List<?> list -> list.stream()
          .map(this::traverse)
          .toList();
      case null, default -> value;
    };
  }

}
