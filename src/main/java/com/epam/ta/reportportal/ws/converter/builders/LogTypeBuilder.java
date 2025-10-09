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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.reportportal.api.model.LogTypeStyle;
import com.epam.ta.reportportal.entity.log.ProjectLogType;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class LogTypeBuilder implements Supplier<ProjectLogType> {

  private static final String DEFAULT_LABEL_COLOR = "#4DB6AC";
  private static final String DEFAULT_BACKGROUND_COLOR = "#FFFFFF";
  private static final String DEFAULT_TEXT_COLOR = "#445A47";
  private static final String DEFAULT_TEXT_STYLE = "normal";

  private final ProjectLogType logType;

  public LogTypeBuilder() {
    this.logType = new ProjectLogType();
  }

  /**
   * Sets the project ID.
   *
   * @param projectId The ID of the associated project.
   * @return The builder instance.
   */
  public LogTypeBuilder addProjectId(Long projectId) {
    logType.setProjectId(projectId);
    return this;
  }

  /**
   * Sets the name of the log type.
   *
   * @param name The name of the log type.
   * @return The builder instance.
   */
  public LogTypeBuilder addName(String name) {
    logType.setName(name);
    return this;
  }

  /**
   * Sets the numeric level of the log type.
   *
   * @param level The numeric level of the log type.
   * @return The builder instance.
   */
  public LogTypeBuilder addLevel(Integer level) {
    logType.setLevel(level);
    return this;
  }

  /**
   * Adds the entire style object to the LogType, setting default values when the style is null.
   *
   * @param style The `LogTypeStyle` object.
   * @return The builder instance.
   */
  public LogTypeBuilder addStyle(LogTypeStyle style) {
    if (Objects.isNull(style)) {
      logType.setLabelColor(DEFAULT_LABEL_COLOR);
      logType.setBackgroundColor(DEFAULT_BACKGROUND_COLOR);
      logType.setTextColor(DEFAULT_TEXT_COLOR);
      logType.setTextStyle(DEFAULT_TEXT_STYLE);
      return this;
    }

    logType.setLabelColor(style.getLabelColor());
    logType.setBackgroundColor(style.getBackgroundColor());
    logType.setTextColor(style.getTextColor());
    logType.setTextStyle(style.getTextStyle().getValue());
    return this;
  }

  /**
   * Sets the `isFilterable` property of the log type.
   *
   * @param isFilterable True if the log type is filterable; otherwise false.
   * @return The builder instance.
   */
  public LogTypeBuilder addIsFilterable(Boolean isFilterable) {
    logType.setFilterable(Optional.ofNullable(isFilterable).orElse(false));
    return this;
  }

  @Override
  public ProjectLogType get() {
    return logType;
  }
}
