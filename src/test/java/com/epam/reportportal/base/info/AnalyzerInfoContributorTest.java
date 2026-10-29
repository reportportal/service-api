/*
 * Copyright 2026 EPAM Systems
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class AnalyzerInfoContributorTest {

  private static final String ANALYZER_URL = "http://analyzer:5001";

  @Mock
  private RestTemplate restTemplate;

  private AnalyzerInfoContributor contributor;

  @BeforeEach
  void setUp() {
    contributor = new AnalyzerInfoContributor(restTemplate, ANALYZER_URL);
  }

  @Test
  void contributeShouldExposeVersionWhenAnalyzerIsHealthy() {
    when(restTemplate.getForObject(ANALYZER_URL, Map.class)).thenReturn(Map.of(
        "status", "healthy",
        "version", "6.0.0"
    ));

    Set<Map<String, Object>> analyzers = analyzers();

    assertEquals(1, analyzers.size());
    Map<String, Object> analyzer = analyzers.iterator().next();
    assertEquals("analyzer", analyzer.get("analyzer"));
    assertEquals("6.0.0", analyzer.get("version"));
    assertEquals(true, analyzer.get("available"));
  }

  @Test
  void contributeShouldMarkAnalyzerUnavailableWhenStatusIsNotHealthy() {
    when(restTemplate.getForObject(ANALYZER_URL, Map.class)).thenReturn(Map.of(
        "status", "unhealthy",
        "version", "6.0.0"
    ));

    Set<Map<String, Object>> analyzers = analyzers();

    assertEquals(1, analyzers.size());
    Map<String, Object> analyzer = analyzers.iterator().next();
    assertEquals("6.0.0", analyzer.get("version"));
    assertEquals(false, analyzer.get("available"));
  }

  @Test
  void contributeShouldReturnEmptyAnalyzersWhenAnalyzerIsUnreachable() {
    when(restTemplate.getForObject(ANALYZER_URL, Map.class))
        .thenThrow(new RestClientException("connection refused"));

    Set<Map<String, Object>> analyzers = analyzers();

    assertTrue(analyzers.isEmpty());
  }

  @SuppressWarnings("unchecked")
  private Set<Map<String, Object>> analyzers() {
    return (Set<Map<String, Object>>) contributor.contribute().get("analyzers");
  }
}
