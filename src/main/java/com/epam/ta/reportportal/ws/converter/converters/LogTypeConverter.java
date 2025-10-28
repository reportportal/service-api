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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.reportportal.api.model.LogTypeResponse;
import com.epam.reportportal.api.model.LogTypeStyle;
import com.epam.ta.reportportal.entity.log.ProjectLogType;
import com.epam.ta.reportportal.model.activity.LogTypeActivityResource;
import java.util.function.Function;

/**
 * Converter for {@link ProjectLogType} to various representations.
 */
public final class LogTypeConverter {

  private LogTypeConverter() {
    // static only
  }

  public static final Function<ProjectLogType, LogTypeActivityResource> TO_ACTIVITY_RESOURCE =
      logType -> {
        LogTypeActivityResource resource = new LogTypeActivityResource();
        resource.setId(logType.getId());
        resource.setProjectId(logType.getProjectId());
        resource.setName(logType.getName());
        resource.setLevel(logType.getLevel());
        resource.setLabelColor(logType.getLabelColor());
        resource.setBackgroundColor(logType.getBackgroundColor());
        resource.setTextColor(logType.getTextColor());
        resource.setTextStyle(logType.getTextStyle());
        resource.setIsFilterable(logType.isFilterable());
        return resource;
      };

  public static final Function<ProjectLogType, LogTypeResponse> TO_RESOURCE = entity -> {
    LogTypeResponse logType = new LogTypeResponse();
    logType.setId(entity.getId());
    logType.setName(entity.getName());
    logType.setLevel(entity.getLevel());
    logType.setIsFilterable(entity.isFilterable());
    logType.setIsSystem(entity.isSystem());

    LogTypeStyle style = new LogTypeStyle();
    style.setLabelColor(entity.getLabelColor());
    style.setBackgroundColor(entity.getBackgroundColor());
    style.setTextColor(entity.getTextColor());
    style.setTextStyle(LogTypeStyle.TextStyleEnum.fromValue(entity.getTextStyle()));

    logType.setStyle(style);
    return logType;
  };
}

