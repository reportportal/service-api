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

package com.epam.reportportal.ws.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.epam.reportportal.infrastructure.persistence.dao.LogTypeRepository;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.log.LogFull;
import com.epam.reportportal.infrastructure.persistence.service.LogTypeResolver;
import com.epam.reportportal.model.log.LogResource;
import com.epam.reportportal.ws.converter.converters.LogConverter;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class LogResourceAssemblerTest {

  @Mock
  private LogTypeRepository logTypeRepository;

  private LogResourceAssembler resourceAssembler;

  @BeforeEach
  void setUp() {
    resourceAssembler = new LogResourceAssembler(
        new LogConverter(new LogTypeResolver(logTypeRepository)));
  }

  @Test
  void toResource() {
    LogFull logFull = getFullLog();
    LogResource logResource = resourceAssembler.toResource(logFull);

    assertEquals(logResource.getId(), logFull.getId());
    assertEquals("ERROR", logResource.getLevel());
    assertEquals(logResource.getMessage(), logFull.getLogMessage());
    assertEquals(logResource.getItemId(), logFull.getTestItem().getItemId());
  }

  @Test
  void toResources() {
    List<LogResource> logResources = resourceAssembler.toResources(
        Collections.singleton(getFullLog()));

    assertEquals(1, logResources.size());
  }

  @Test
  void apply() {
    LogFull logFull = getFullLog();
    LogResource logResource = resourceAssembler.apply(logFull);

    assertEquals(logResource.getId(), logFull.getId());
    assertEquals("ERROR", logResource.getLevel());
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
    logFull.setProjectId(1L);
    TestItem testItem = new TestItem();
    testItem.setItemId(2L);
    logFull.setTestItem(testItem);

    return logFull;
  }

}
