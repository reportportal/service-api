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

package com.epam.reportportal.ws.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.reportportal.reporting.FinishTestItemRQ;
import com.epam.reportportal.reporting.Issue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/**
 * @author Pavel Bortnik
 */
public class FinishTestItemSerializerTest {

  private final ObjectMapper om = getObjectMapper();

  private static final String FINISH_TEST_ITEM_RQ =
      "{\"attributes\":[],\"status\":\"PASSED\",\"description\":\"description\","
          + "\"issue\":{\"autoAnalyzed\":false,\"ignoreAnalyzer\":false},\"retry\":false}";

  @Test
  public void testSerializer() throws JsonProcessingException {
    String json = om.writeValueAsString(getFinishTestItem());
    assertEquals(FINISH_TEST_ITEM_RQ, json, "Incorrect serialization result");
  }

  @Test
  public void testDeserializer() throws IOException {
    FinishTestItemRQ rq = om.readValue(FINISH_TEST_ITEM_RQ.getBytes(StandardCharsets.UTF_8),
        FinishTestItemRQ.class);
    assertEquals(rq.getStatus(), "PASSED", "Incorrect deserialization result");
  }

  private FinishTestItemRQ getFinishTestItem() {
    FinishTestItemRQ finishTestItemRQ = new FinishTestItemRQ();
    finishTestItemRQ.setStatus("PASSED");
    finishTestItemRQ.setRetry(false);
    finishTestItemRQ.setDescription("description");
    finishTestItemRQ.setIssue(new Issue());
    finishTestItemRQ.setAttributes(Collections.emptySet());

    return finishTestItemRQ;
  }

  private ObjectMapper getObjectMapper() {
    ObjectMapper om = new ObjectMapper();
    om.configure(SerializationFeature.INDENT_OUTPUT, false);
    return om;
  }

}
