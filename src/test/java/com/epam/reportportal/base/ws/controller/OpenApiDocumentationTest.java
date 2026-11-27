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

package com.epam.reportportal.base.ws.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.infrastructure.rules.exception.ErrorRS;
import com.epam.reportportal.base.ws.BaseMvcTest;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Checks that the generated OpenAPI document describes the error responses the way they are sent.
 */
class OpenApiDocumentationTest extends BaseMvcTest {

  private static final String ERROR_RS_REF = "#/components/schemas/" + ErrorRS.class.getName();
  private static final Set<String> HTTP_METHODS = Set.of("get", "put", "post", "delete", "patch", "head", "options");

  @Autowired
  private JsonMapper objectMapper;

  private JsonNode apiDocs;

  @BeforeEach
  void readApiDocs() throws Exception {
    String body = mockMvc.perform(get("/api-docs"))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    apiDocs = objectMapper.readTree(body);
  }

  @Test
  void errorCodeIsDocumentedAsItIsSerialized() throws Exception {
    mockMvc.perform(get(DEFAULT_PROJECT_BASE_URL + "/launch/999999")
            .with(token(oAuthHelper.getDefaultToken())))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errorCode").value(4041));

    JsonNode errorCode = apiDocs.at("/components/schemas").get(ErrorRS.class.getName()).at("/properties/errorCode");
    assertEquals("integer", errorCode.path("type").asString());
    assertTrue(errorCode.path("enum").isMissingNode(), "Error code is a number, not a name of the error type");
  }

  @Test
  void unauthorizedResponseHasSingleSchema() {
    int checked = 0;
    for (JsonNode operation : operations()) {
      JsonNode content = operation.at("/responses/401/content");
      for (JsonNode mediaType : content) {
        assertFalse(mediaType.path("schema").has("oneOf"), "Unexpected 'oneOf' in: " + operation.path("operationId"));
        checked++;
      }
    }
    assertTrue(checked > 0);
    assertEquals(ERROR_RS_REF,
        launchOperation().at("/responses/401/content/application~1json/schema/$ref").asString());
  }

  @Test
  void errorResponseIsDeclaredForUndocumentedStatuses() {
    JsonNode launchResponses = launchOperation().path("responses");
    assertFalse(launchResponses.has("404"));
    assertEquals(ERROR_RS_REF, launchResponses.at("/default/content/application~1json/schema/$ref").asString());

    for (JsonNode operation : operations()) {
      JsonNode responses = operation.path("responses");
      if (responses.toString().contains("\"" + ERROR_RS_REF + "\"")) {
        assertTrue(responses.has("default"), "No default response in: " + operation.path("operationId"));
      }
    }
  }

  private JsonNode launchOperation() {
    return apiDocs.path("paths").path("/v1/{projectKey}/launch/{launchId}").path("get");
  }

  private Iterable<JsonNode> operations() {
    return apiDocs.path("paths").valueStream()
        .flatMap(pathItem -> pathItem.propertyStream()
            .filter(entry -> HTTP_METHODS.contains(entry.getKey()))
            .map(Map.Entry::getValue))
        .toList();
  }
}
