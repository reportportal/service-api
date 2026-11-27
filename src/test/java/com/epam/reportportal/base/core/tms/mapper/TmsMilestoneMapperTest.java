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

package com.epam.reportportal.base.core.tms.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.epam.reportportal.base.core.tms.dto.TmsMilestoneRQ;
import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsMilestone;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class TmsMilestoneMapperTest {

  private static final LocalDateTime START = LocalDateTime.of(2025, 4, 1, 0, 0);
  private static final LocalDateTime END = LocalDateTime.of(2025, 6, 30, 0, 0);

  private final JsonMapper jsonMapper = JsonMapper.builder().build();
  private final TmsMilestoneMapperImpl mapper = new TmsMilestoneMapperImpl();

  private TmsMilestone milestone() {
    var milestone = new TmsMilestone();
    milestone.setName("old");
    milestone.setStartDate(START);
    milestone.setEndDate(END);
    return milestone;
  }

  private TmsMilestone patch(String json) {
    var milestone = milestone();
    mapper.patchEntity(1L, jsonMapper.readValue(json, TmsMilestoneRQ.class), milestone);
    return milestone;
  }

  @Test
  void patchEntity_explicitNullStartDate_clearsOnlyStartDate() {
    var milestone = patch("{\"startDate\":null}");

    assertNull(milestone.getStartDate());
    assertEquals(END, milestone.getEndDate());
  }

  @Test
  void patchEntity_explicitNullEndDate_clearsOnlyEndDate() {
    var milestone = patch("{\"endDate\":null}");

    assertEquals(START, milestone.getStartDate());
    assertNull(milestone.getEndDate());
  }

  @Test
  void patchEntity_bothNull_clearsBoth() {
    var milestone = patch("{\"startDate\":null,\"endDate\":null}");

    assertNull(milestone.getStartDate());
    assertNull(milestone.getEndDate());
  }

  @Test
  void patchEntity_absentDates_keepsDates() {
    var milestone = patch("{\"name\":\"new\"}");

    assertEquals("new", milestone.getName());
    assertEquals(START, milestone.getStartDate());
    assertEquals(END, milestone.getEndDate());
  }

  @Test
  void patchEntity_dateValue_setsDate() {
    var milestone = patch("{\"startDate\":\"2026-01-01T00:00:00Z\"}");

    assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0), milestone.getStartDate());
    assertEquals(END, milestone.getEndDate());
  }

  @Test
  void patchEntity_builtRequestWithoutFlags_keepsDates() {
    var milestone = milestone();

    mapper.patchEntity(1L, TmsMilestoneRQ.builder().name("new").build(), milestone);

    assertEquals(START, milestone.getStartDate());
    assertEquals(END, milestone.getEndDate());
  }
}
