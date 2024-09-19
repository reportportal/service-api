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

package com.epam.ta.reportportal.ws.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.log.LogFull;
import com.epam.ta.reportportal.model.log.LogResource;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class LogResourceAssemblerTest {

  @Test
  void toResource() {
    LogResourceAssembler resourceAssembler = new LogResourceAssembler();
    LogFull logFull = getFullLog();
    LogResource logResource = resourceAssembler.toResource(logFull);

    assertEquals(logResource.getId(), logFull.getId());
    assertEquals(logResource.getLevel(), LogLevel.toLevel(logFull.getLogLevel()).name());
    assertEquals(logResource.getMessage(), logFull.getLogMessage());
    assertEquals(logResource.getItemId(), logFull.getTestItem().getItemId());
  }

  @Test
  void toResources() {
    LogResourceAssembler resourceAssembler = new LogResourceAssembler();
    List<LogResource> logResources = resourceAssembler.toResources(
        Collections.singleton(getFullLog()));

    assertEquals(1, logResources.size());
  }

  @Test
  void apply() {
    LogResourceAssembler resourceAssembler = new LogResourceAssembler();
    LogFull logFull = getFullLog();
    LogResource logResource = resourceAssembler.apply(logFull);

    assertEquals(logResource.getId(), logFull.getId());
    assertEquals(logResource.getLevel(), LogLevel.toLevel(logFull.getLogLevel()).name());
    assertEquals(logResource.getMessage(), logFull.getLogMessage());
    assertEquals(logResource.getItemId(), logFull.getTestItem().getItemId());
  }

  private LogFull getFullLog() {
    LogFull logFull = new LogFull();
    logFull.setId(1L);
    logFull.setLogTime(Instant.now());
    logFull.setLastModified(Instant.now());
    logFull.setLogMessage("message");
    logFull.setLogLevel(40000);
    TestItem testItem = new TestItem();
    testItem.setItemId(2L);
    logFull.setTestItem(testItem);

    return logFull;
  }
}
