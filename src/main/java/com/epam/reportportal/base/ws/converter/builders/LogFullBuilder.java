/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.ws.converter.builders;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.LogFull;
import com.epam.reportportal.base.reporting.SaveLogRQ;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Builds full log representations including nested data for API responses.
 *
 * @author Pavel Bortnik
 */
public class LogFullBuilder implements Supplier<LogFull> {

  private final LogFull logFull;

  public LogFullBuilder() {
    logFull = new LogFull();
  }

  public LogFullBuilder addSaveLogRq(SaveLogRQ createLogRQ) {
    logFull.setLogMessage(ofNullable(createLogRQ.getMessage()).orElse("NULL"));
    logFull.setLogTime(createLogRQ.getLogTime());
    logFull.setUuid(ofNullable(createLogRQ.getUuid()).orElse(UUID.randomUUID().toString()));
    return this;
  }

  public LogFullBuilder addTestItem(TestItem testItem) {
    logFull.setTestItem(testItem);
    return this;
  }

  public LogFullBuilder addLaunch(Launch launch) {
    logFull.setLaunch(launch);
    return this;
  }

  public LogFullBuilder addProjectId(Long projectId) {
    logFull.setProjectId(projectId);
    return this;
  }

  public LogFullBuilder addLevel(Integer level) {
    logFull.setLogLevel(level);
    return this;
  }

  @Override
  public LogFull get() {
    return logFull;
  }

}
