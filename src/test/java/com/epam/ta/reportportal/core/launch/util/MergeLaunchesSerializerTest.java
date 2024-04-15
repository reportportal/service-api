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

package com.epam.ta.reportportal.core.launch.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.ta.reportportal.ws.BaseMvcTest;
import com.epam.ta.reportportal.ws.reporting.ItemAttributeResource;
import com.epam.ta.reportportal.ws.reporting.MergeLaunchesRQ;
import com.epam.ta.reportportal.ws.reporting.Mode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Pavel Bortnik
 */
public class MergeLaunchesSerializerTest extends BaseMvcTest {

  private static final String EXPECTED_JSON = "{\"name\":\"name\","
      + "\"description\":\"description\",\"attributes\":[{\"key\":\"key\",\"value\":\"value\"}],\"startTime\":\"1970-01-01T00:00:00Z\",\"mode\":\"DEFAULT\","
      + "\"launches\":[1],\"endTime\":\"1970-01-01T00:00:00.001Z\",\"mergeType\":\"BASIC\",\"extendSuitesDescription\":true}";

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  public void testSerializer() throws JsonProcessingException {
    MergeLaunchesRQ rq = getMergeLaunches();
    String json = objectMapper.writeValueAsString(rq);
    assertEquals(json, EXPECTED_JSON, "Incorrect serialization result");
  }

  @Test
  public void testDeserializer() throws IOException {
    MergeLaunchesRQ rq =
        objectMapper.readValue(EXPECTED_JSON.getBytes(StandardCharsets.UTF_8), MergeLaunchesRQ.class);
    assertEquals(getMergeLaunches().toString(), rq.toString(), "Incorrect deserialization result");
  }

  private MergeLaunchesRQ getMergeLaunches() {
    MergeLaunchesRQ rq = new MergeLaunchesRQ();
    rq.setName("name");
    rq.setDescription("description");
    rq.setMode(Mode.DEFAULT);
    rq.setStartTime(Instant.EPOCH);
    ItemAttributeResource itemAttributeResource = new ItemAttributeResource("key", "value");
    rq.setAttributes(Collections.singleton(itemAttributeResource));
    rq.setEndTime(Instant.EPOCH.plusMillis(1));
    rq.setExtendSuitesDescription(true);
    rq.setLaunches(Collections.singleton(1L));
    rq.setMergeStrategyType("BASIC");
    return rq;
  }

}
