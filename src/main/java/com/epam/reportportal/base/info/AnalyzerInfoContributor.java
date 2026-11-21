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

package com.epam.reportportal.base.info;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Loads analyzer version and availability from the analyzer service health endpoint.
 *
 * @author Pavel Bortnik
 */
@Component
@Slf4j
public class AnalyzerInfoContributor implements ExtensionContributor {

  private static final String ANALYZERS_KEY = "analyzers";
  private static final String ANALYZER_KEY = "analyzer";
  private static final String ANALYZER_NAME = "analyzer";
  private static final String VERSION_KEY = "version";
  private static final String AVAILABLE_KEY = "available";
  private static final String STATUS_KEY = "status";
  private static final String HEALTHY_STATUS = "healthy";

  private final RestClient restClient;
  private final String analyzerBaseUrl;

  public AnalyzerInfoContributor(RestClient restClient,
      @Value("${rp.analyzer.baseUrl}") String analyzerBaseUrl) {
    this.restClient = restClient;
    this.analyzerBaseUrl = analyzerBaseUrl;
  }

  @Override
  public Map<String, ?> contribute() {
    try {
      Map<?, ?> analyzerRs = restClient.get()
          .uri(analyzerBaseUrl)
          .retrieve()
          .body(Map.class);
      if (analyzerRs == null) {
        log.error("Analyzer service returned an empty response");
        return Map.of(ANALYZERS_KEY, Set.of());
      }
      return Map.of(ANALYZERS_KEY, Set.of(toAnalyzerInfo(analyzerRs)));
    } catch (Exception e) {
      log.error("Analyzer service was not found: {}", e.getMessage());
      return Map.of(ANALYZERS_KEY, Set.of());
    }
  }

  private Map<String, Object> toAnalyzerInfo(Map<?, ?> response) {
    Map<String, Object> info = new HashMap<>();
    info.put(ANALYZER_KEY, ANALYZER_NAME);
    Object version = response.get(VERSION_KEY);
    if (version != null) {
      info.put(VERSION_KEY, version);
    }
    Object status = response.get(STATUS_KEY);
    info.put(AVAILABLE_KEY, HEALTHY_STATUS.equalsIgnoreCase(Objects.toString(status, "")));
    return info;
  }
}
