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

package com.epam.reportportal.base.core.log;

import static com.epam.reportportal.base.ws.converter.converters.LogConverter.LOG_FULL_TO_LOG;

import com.epam.reportportal.base.infrastructure.persistence.dao.LogRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.Log;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.LogFull;
import com.epam.reportportal.base.infrastructure.persistence.service.LogTypeResolver;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Generic helper that persists a system log (attached either to a launch or to a test item) and
 * forwards it to the search-engine indexer. The log level is resolved from the supplied log type
 * name via {@link LogTypeResolver}.
 */
@Service
@RequiredArgsConstructor
public class SystemLogService {

  private final LogRepository logRepository;
  private final LogTypeResolver logTypeResolver;

  /**
   * Persists a system log attached to the given launch (item_id is left null).
   *
   * @param launch      Target launch
   * @param logTypeName Project log type name resolved to the log level
   * @param message     Log message
   * @return Generated id of the persisted {@link Log} row
   */
  public Long writeLaunchLog(Launch launch, String logTypeName, String message) {
    LogFull logFull = buildLogFull(launch.getProjectId(), logTypeName, message);
    logFull.setLaunch(launch);
    Log saved = logRepository.saveAndFlush(LOG_FULL_TO_LOG.apply(logFull));
    return saved.getId();
  }

  /**
   * Persists a system log attached to the given test item (launch_id on the log row is left null).
   * The {@code launch} parameter is only used to provide projectId and launchId for search-engine
   * indexing.
   *
   * @param testItem    Target test item
   * @param launch      Launch the test item belongs to
   * @param logTypeName Project log type name resolved to the log level
   * @param message     Log message
   * @return Generated id of the persisted {@link Log} row
   */
  public Long writeTestItemLog(TestItem testItem, Launch launch, String logTypeName,
      String message) {
    LogFull logFull = buildLogFull(launch.getProjectId(), logTypeName, message);
    logFull.setTestItem(testItem);
    Log saved = logRepository.saveAndFlush(LOG_FULL_TO_LOG.apply(logFull));
    return saved.getId();
  }

  private LogFull buildLogFull(Long projectId, String logTypeName, String message) {
    Integer level = logTypeResolver.resolveLogLevelFromName(projectId, logTypeName);
    LogFull logFull = new LogFull();
    logFull.setUuid(UUID.randomUUID().toString());
    logFull.setLogTime(Instant.now());
    logFull.setLogMessage(message);
    logFull.setLogLevel(level);
    logFull.setProjectId(projectId);
    return logFull;
  }
}
