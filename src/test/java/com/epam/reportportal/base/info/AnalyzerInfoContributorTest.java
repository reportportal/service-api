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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class AnalyzerInfoContributorTest {

  private static final String ANALYZER_URL = "http://analyzer:5001";

  private MockRestServiceServer server;
  private AnalyzerInfoContributor contributor;

  @BeforeEach
  void setUp() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    contributor = new AnalyzerInfoContributor(builder.build(), ANALYZER_URL);
  }

  @Test
  void contributeShouldExposeVersionWhenAnalyzerIsHealthy() {
    server.expect(requestTo(ANALYZER_URL))
        .andRespond(withSuccess("""
            {"status":"healthy","version":"6.0.0"}""", MediaType.APPLICATION_JSON));

    Set<Map<String, Object>> analyzers = analyzers();

    assertEquals(1, analyzers.size());
    Map<String, Object> analyzer = analyzers.iterator().next();
    assertEquals("analyzer", analyzer.get("analyzer"));
    assertEquals("6.0.0", analyzer.get("version"));
    assertEquals(true, analyzer.get("available"));
  }

  @Test
  void contributeShouldMarkAnalyzerUnavailableWhenStatusIsNotHealthy() {
    server.expect(requestTo(ANALYZER_URL))
        .andRespond(withSuccess("""
            {"status":"unhealthy","version":"6.0.0"}""", MediaType.APPLICATION_JSON));

    Set<Map<String, Object>> analyzers = analyzers();

    assertEquals(1, analyzers.size());
    Map<String, Object> analyzer = analyzers.iterator().next();
    assertEquals("6.0.0", analyzer.get("version"));
    assertEquals(false, analyzer.get("available"));
  }

  @Test
  void contributeShouldReturnEmptyAnalyzersWhenAnalyzerIsUnreachable() {
    server.expect(requestTo(ANALYZER_URL))
        .andRespond(withServerError());

    Set<Map<String, Object>> analyzers = analyzers();

    assertTrue(analyzers.isEmpty());
  }

  @SuppressWarnings("unchecked")
  private Set<Map<String, Object>> analyzers() {
    return (Set<Map<String, Object>>) contributor.contribute().get("analyzers");
  }
}
