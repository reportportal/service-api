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

package com.epam.reportportal.base.infrastructure.persistence.service;

import com.epam.reportportal.base.infrastructure.persistence.dao.LogTypeRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LogLevel;
import com.epam.reportportal.base.infrastructure.persistence.entity.log.ProjectLogType;
import com.google.common.base.Strings;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * A service for resolving log levels and log names based on predefined enums or custom repository-backed mappings.
 *
 */
@Component
@RequiredArgsConstructor
public class LogTypeResolver {

  private final LogTypeRepository logTypeRepository;

  /**
   * Resolves log level (integer) from level name (string). Searches first in the predefined enum and falls back to the
   * repository for custom log levels.
   *
   * @param projectId the project ID used for repository lookup
   * @param levelName the log level name as a string
   * @return resolved log level as an integer or UNKNOWN_INT if not found
   */
  public int resolveLogLevelFromName(Long projectId, String levelName) {
    if (Strings.isNullOrEmpty(levelName)) {
      return LogLevel.UNKNOWN_INT;
    }

    return LogLevel.toCustomLogLevel(levelName)
        .or(() -> logTypeRepository.findLevelByProjectIdAndNameIgnoreCase(projectId, levelName))
        .orElse(LogLevel.UNKNOWN_INT);
  }

  /**
   * Resolves level name (string) from log level (integer). Searches first in the predefined enum and falls back to the
   * repository for custom log levels.
   *
   * @param projectId the project ID used for repository lookup
   * @param logLevel  the log level as an integer
   * @return resolved log level name as a string or "UNKNOWN" if not found
   */
  public String resolveNameFromLogLevel(Long projectId, int logLevel) {
    return LogLevel.toLevel(logLevel)
        .or(() -> Optional.ofNullable(
            logTypeRepository.findNameByProjectIdAndLevel(projectId, logLevel)))
        .orElse(LogLevel.UNKNOWN.toString());
  }

  /**
   * Retrieves a map of all log levels for a given project. Optimized for batch operations where multiple log level
   * resolutions are needed.
   *
   * @param projectId the project ID
   * @return a map where keys are log level integers and values are log level names
   */
  public Map<Integer, String> getLogLevelMapForProject(Long projectId) {
    return logTypeRepository.findByProjectId(projectId).stream()
        .collect(Collectors.toMap(ProjectLogType::getLevel, ProjectLogType::getName));
  }
}
