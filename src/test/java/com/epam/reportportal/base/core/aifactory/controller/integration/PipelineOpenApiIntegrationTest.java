package com.epam.reportportal.base.core.aifactory.controller.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epam.reportportal.base.ws.BaseMvcTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Guards the published OpenAPI contract of the pipeline and quality standard endpoints that the UI
 * generates its client from.
 */
class PipelineOpenApiIntegrationTest extends BaseMvcTest {

  private static final String LIST_PATH = "/v1/project/{projectKey}/pipeline";

  private JsonNode api;

  @BeforeEach
  void loadApiDocs() throws Exception {
    var body = mockMvc.perform(get("/api-docs").with(token(oAuthHelper.getSuperadminToken())))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    api = new ObjectMapper().readTree(body);
  }

  @Test
  void pipelineList_ShouldBeDocumentedAsPageWithOffsetAndLimit() {
    var op = api.at("/paths").get(LIST_PATH).get("get");

    var schema = op.at("/responses/200/content").elements().next().get("schema");
    assertTrue(schema.toString().contains("Page"), "expected a Page schema but was " + schema);
    assertTrue(!"array".equals(schema.path("type").asText()), "must not be a bare array: " + schema);

    var paramNames = new java.util.ArrayList<String>();
    op.get("parameters").forEach(p -> paramNames.add(p.get("name").asText()));
    assertEquals(1, paramNames.stream().filter("offset"::equals).count(), paramNames.toString());
    assertEquals(1, paramNames.stream().filter("limit"::equals).count(), paramNames.toString());
  }

  @Test
  void identityAndStatusFields_ShouldBeRequired() {
    assertRequired("PipelineRS", "id", "name", "iterationsCount");
    assertRequired("PipelineIterationSummaryRS", "id", "pipelineId", "iterationNumber", "status", "statusCategory", "stages");
    assertRequired("PipelineIterationDetailRS", "id", "pipelineId", "iterationNumber", "status", "statusCategory", "stages");
    assertRequired("PipelineStageRS", "id", "name", "sequence", "status", "statusCategory", "origin");
    assertRequired("PipelineStageRetryRS", "stageId", "status", "statusCategory");
    assertRequired("TmsQualityStandardRS", "id", "name", "criteria");
    assertRequired("TmsQualityStandardCriterionRS", "id", "name", "maxPoints", "sequence");
  }


  private void assertRequired(String schemaName, String... fields) {
    // schema component names are fully-qualified class names in this API
    JsonNode schema = null;
    var names = api.at("/components/schemas").fieldNames();
    while (names.hasNext()) {
      var name = names.next();
      if (name.matches("com\\.epam\\.reportportal\\.base\\.core\\.[a-z]+\\.dto\\." + schemaName)) {
        schema = api.at("/components/schemas").get(name);
      }
    }
    assertTrue(schema != null, "schema not found: " + schemaName);
    var actual = new java.util.ArrayList<String>();
    schema.path("required").forEach(n -> actual.add(n.asText()));
    assertTrue(actual.containsAll(List.of(fields)), schemaName + " required=" + actual);
  }
}
