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

package com.epam.reportportal.base.core.tms.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class TmsMilestoneRQJsonTest {

  private final JsonMapper mapper = JsonMapper.builder().build();

  @Test
  void absentDates_areNotSpecified() {
    var rq = mapper.readValue("{\"name\":\"m\"}", TmsMilestoneRQ.class);

    assertFalse(rq.isStartDateSpecified());
    assertFalse(rq.isEndDateSpecified());
    assertNull(rq.getStartDate());
    assertNull(rq.getEndDate());
  }

  @Test
  void explicitNullDates_areSpecifiedAndNull() {
    var rq = mapper.readValue("{\"startDate\":null,\"endDate\":null}", TmsMilestoneRQ.class);

    assertTrue(rq.isStartDateSpecified());
    assertTrue(rq.isEndDateSpecified());
    assertNull(rq.getStartDate());
    assertNull(rq.getEndDate());
  }

  @Test
  void onlyOneDateNull_flagsAreIndependent() {
    var rq = mapper.readValue("{\"startDate\":null}", TmsMilestoneRQ.class);

    assertTrue(rq.isStartDateSpecified());
    assertFalse(rq.isEndDateSpecified());
  }

  @Test
  void dateValues_areParsedAndSpecified() {
    var rq = mapper.readValue(
        "{\"startDate\":\"2025-04-01T00:00:00Z\",\"endDate\":1751241599000}", TmsMilestoneRQ.class);

    assertTrue(rq.isStartDateSpecified());
    assertTrue(rq.isEndDateSpecified());
    assertEquals(Instant.parse("2025-04-01T00:00:00Z"), rq.getStartDate());
    assertEquals(Instant.ofEpochMilli(1751241599000L), rq.getEndDate());
  }

  @Test
  void builder_doesNotMarkDatesAsSpecified() {
    var rq = TmsMilestoneRQ.builder().name("m").startDate(Instant.now()).build();

    assertFalse(rq.isStartDateSpecified());
    assertFalse(rq.isEndDateSpecified());
  }

  @Test
  void flags_areNotSerialized() {
    var json = mapper.writeValueAsString(new TmsMilestoneRQ());

    assertFalse(json.contains("Specified"));
  }
}
