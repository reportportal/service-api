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

import static java.util.stream.Collectors.toMap;

import com.epam.reportportal.extension.SensitiveIntegrationParam;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.stereotype.Component;

/**
 * Encrypts sensitive integration fields centrally before persisting. Fields listed in
 * {@link SensitiveIntegrationParam#ALL} are encrypted; null values and unknown keys are passed through unchanged.
 */
@Component
@RequiredArgsConstructor
public class IntegrationParamsEncryptor {

  private final BasicTextEncryptor basicTextEncryptor;

  public Map<String, Object> encryptSensitiveFields(Map<String, Object> params) {
    if (MapUtils.isEmpty(params)) {
      return params;
    }
    return params.entrySet().stream()
        .map(e -> SensitiveIntegrationParam.ALL.contains(e.getKey()) && e.getValue() != null
            ? Map.entry(e.getKey(), basicTextEncryptor.encrypt(String.valueOf(e.getValue())))
            : e)
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
