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

package com.epam.reportportal.ws.converter.builders;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.api.model.LogTypeRequest;
import com.epam.reportportal.api.model.LogTypeStyle;
import com.epam.reportportal.api.model.LogTypeStyle.TextStyleEnum;
import com.epam.reportportal.infrastructure.persistence.entity.log.ProjectLogType;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class LogTypeBuilder implements Supplier<ProjectLogType> {

  private static final String DEFAULT_IF_UNSPECIFIED_LABEL_COLOR = "#4DB6AC";
  private static final String DEFAULT_IF_UNSPECIFIED_BACKGROUND_COLOR = "#FFFFFF";
  private static final String DEFAULT_IF_UNSPECIFIED_TEXT_COLOR = "#445A47";
  private static final String DEFAULT_IF_UNSPECIFIED_TEXT_STYLE = "normal";

  private final ProjectLogType logType;

  public LogTypeBuilder() {
    this.logType = new ProjectLogType();
  }

  /**
   * Creates a new {@code LogTypeBuilder} for an existing {@link ProjectLogType} instance. This allows updating or
   * augmenting an already populated log type.
   *
   * @param logType the log type instance to wrap
   */
  public LogTypeBuilder(ProjectLogType logType) {
    this.logType = logType;
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
   * Adds the entire style object to the LogType, setting default values for missing fields.
   *
   * @param style The `LogTypeStyle` object.
   * @return The builder instance.
   */
  public LogTypeBuilder addStyle(LogTypeStyle style) {
    if (Objects.isNull(style)) {
      logType.setLabelColor(DEFAULT_IF_UNSPECIFIED_LABEL_COLOR);
      logType.setBackgroundColor(DEFAULT_IF_UNSPECIFIED_BACKGROUND_COLOR);
      logType.setTextColor(DEFAULT_IF_UNSPECIFIED_TEXT_COLOR);
      logType.setTextStyle(DEFAULT_IF_UNSPECIFIED_TEXT_STYLE);
      return this;
    }

    logType.setLabelColor(ofNullable(style.getLabelColor())
        .orElse(DEFAULT_IF_UNSPECIFIED_LABEL_COLOR));
    logType.setBackgroundColor(ofNullable(style.getBackgroundColor())
        .orElse(DEFAULT_IF_UNSPECIFIED_BACKGROUND_COLOR));
    logType.setTextColor(ofNullable(style.getTextColor())
        .orElse(DEFAULT_IF_UNSPECIFIED_TEXT_COLOR));
    logType.setTextStyle(ofNullable(style.getTextStyle())
        .map(TextStyleEnum::getValue)
        .orElse(DEFAULT_IF_UNSPECIFIED_TEXT_STYLE));

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

  /**
   * Updates the log type with fields from the update request following PUT semantics. All required fields must be
   * provided in the request. Missing optional fields will be set to defaults.
   *
   * @param updateRq The update request containing the complete log type representation.
   * @return The builder instance.
   */
  public LogTypeBuilder addUpdateRq(LogTypeRequest updateRq) {
    logType.setName(updateRq.getName());
    logType.setLevel(updateRq.getLevel());
    logType.setFilterable(updateRq.getIsFilterable());

    addStyle(updateRq.getStyle());
    return this;
  }

  @Override
  public ProjectLogType get() {
    return logType;
  }
}
