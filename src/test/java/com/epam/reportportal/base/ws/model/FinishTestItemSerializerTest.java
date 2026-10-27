/*
 * Copyright 2019 EPAM Systems
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

package com.epam.reportportal.base.ws.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.reportportal.base.reporting.FinishTestItemRQ;
import com.epam.reportportal.base.reporting.Issue;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.SerializationFeature;

/**
 * @author Pavel Bortnik
 */
public class FinishTestItemSerializerTest {

  private final JsonMapper om = getObjectMapper();

  private static final String FINISH_TEST_ITEM_RQ =
      "{\"attributes\":[],\"status\":\"PASSED\",\"description\":\"description\","
          + "\"issue\":{\"autoAnalyzed\":false,\"ignoreAnalyzer\":false},\"retry\":false}";

  @Test
  public void testSerializer() {
    String json = om.writeValueAsString(getFinishTestItem());
    assertEquals(FINISH_TEST_ITEM_RQ, json, "Incorrect serialization result");
  }

  @Test
  public void testDeserializer() {
    FinishTestItemRQ rq = om.readValue(FINISH_TEST_ITEM_RQ.getBytes(StandardCharsets.UTF_8),
        FinishTestItemRQ.class);
    assertEquals("PASSED", rq.getStatus(), "Incorrect deserialization result");
  }

  private FinishTestItemRQ getFinishTestItem() {
    FinishTestItemRQ finishTestItemRq = new FinishTestItemRQ();
    finishTestItemRq.setStatus("PASSED");
    finishTestItemRq.setRetry(false);
    finishTestItemRq.setDescription("description");
    finishTestItemRq.setIssue(new Issue());
    finishTestItemRq.setAttributes(Collections.emptySet());

    return finishTestItemRq;
  }

  private JsonMapper getObjectMapper() {
    return JsonMapper.builder()
        .disable(SerializationFeature.INDENT_OUTPUT)
        .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        .build();
  }

}
