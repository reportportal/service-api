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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.epam.reportportal.base.infrastructure.persistence.entity.tms.TmsTestCaseExecution;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;

class TmsTestCaseExecutionMapperTest {

  @Test
  void convert_whenTestItemWasDeleted_shouldNotFailAndKeepSnapshotData() {
    var mapper = new TmsTestCaseExecutionMapperImpl();
    ReflectionTestUtils.setField(mapper, "objectMapper", JsonMapper.builder().build());

    var execution = new TmsTestCaseExecution();
    execution.setId(1L);
    execution.setTestCaseId(2L);
    execution.setTestItem(null);
    execution.setTestCaseSnapshot("{\"id\": 2, \"name\": \"Case\", \"testFolder\": {\"id\": 5}}");

    var result = mapper.convert(execution);

    assertNotNull(result);
    assertEquals("Case", result.getTestCaseName());
    assertNull(result.getTestItemId());
    assertNotNull(result.getTestFolder());
    assertEquals(5L, result.getTestFolder().getId());
    assertNull(result.getTestFolder().getTestItemId());
  }
}
