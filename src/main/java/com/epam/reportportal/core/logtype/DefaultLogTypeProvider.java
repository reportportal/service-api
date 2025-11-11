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

package com.epam.reportportal.core.logtype;

import com.epam.reportportal.infrastructure.persistence.entity.log.ProjectLogType;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Provides default log types with pre-configured settings.
 */
@Component
public class DefaultLogTypeProvider {

  private static final String DEFAULT_BACKGROUND_COLOR = "#FFFFFF";
  private static final String DEFAULT_TEXT_COLOR = "#464547";
  private static final String DEFAULT_TEXT_STYLE = "normal";

  /**
   * Provides a list of default log types for a given project.
   *
   * @return a list of `ProjectLogType` objects with pre-configured defaults.
   */
  public List<ProjectLogType> provideDefaultLogTypes(Long projectId) {
    return List.of(
        createLogType(projectId, "unknown", 60000, "#E3E7EC", false),
        createLogType(projectId, "fatal", 50000, "#8B0000", true),
        createLogType(projectId, "error", 40000, "#DC5959", true),
        createLogType(projectId, "warn", 30000, "#FFBC6C", true),
        createLogType(projectId, "info", 20000, "#23A6DE", true),
        createLogType(projectId, "debug", 10000, "#C1C7D0", true),
        createLogType(projectId, "trace", 5000, "#E3E7EC", true)
    );
  }

  private ProjectLogType createLogType(Long projectId, String name, int level,
      String labelColor, boolean filterable) {
    ProjectLogType logType = new ProjectLogType();
    logType.setProjectId(projectId);
    logType.setName(name);
    logType.setLevel(level);
    logType.setLabelColor(labelColor);
    logType.setBackgroundColor(DEFAULT_BACKGROUND_COLOR);
    logType.setTextColor(DEFAULT_TEXT_COLOR);
    logType.setTextStyle(DEFAULT_TEXT_STYLE);
    logType.setFilterable(filterable);
    logType.setSystem(true);
    return logType;
  }
}
