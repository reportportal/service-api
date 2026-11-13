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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Loads analyzer version and availability from the analyzer service health endpoint.
 *
 * @author Pavel Bortnik
 */
@Component
public class AnalyzerInfoContributor implements ExtensionContributor {

  private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzerInfoContributor.class);

  private static final String ANALYZERS_KEY = "analyzers";
  private static final String ANALYZER_KEY = "analyzer";
  private static final String ANALYZER_NAME = "analyzer";
  private static final String VERSION_KEY = "version";
  private static final String AVAILABLE_KEY = "available";
  private static final String STATUS_KEY = "status";
  private static final String HEALTHY_STATUS = "healthy";

  private final RestTemplate restTemplate;
  private final String analyzerBaseUrl;

  public AnalyzerInfoContributor(RestTemplate restTemplate,
      @Value("${rp.analyzer.baseUrl}") String analyzerBaseUrl) {
    this.restTemplate = restTemplate;
    this.analyzerBaseUrl = analyzerBaseUrl;
  }

  @Override
  public Map<String, ?> contribute() {
    try {
      Map<?, ?> analyzerRs = restTemplate.getForObject(analyzerBaseUrl, Map.class);
      if (analyzerRs == null) {
        LOGGER.error("Analyzer service returned an empty response");
        return Map.of(ANALYZERS_KEY, Set.of());
      }
      return Map.of(ANALYZERS_KEY, Set.of(toAnalyzerInfo(analyzerRs)));
    } catch (Exception e) {
      LOGGER.error("Analyzer service was not found: {}", e.getMessage());
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
