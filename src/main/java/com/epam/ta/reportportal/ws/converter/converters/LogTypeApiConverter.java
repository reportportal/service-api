package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.reportportal.api.model.LogTypeResponse;
import com.epam.reportportal.api.model.LogTypeStyle;
import com.epam.ta.reportportal.entity.log.ProjectLogType;
import java.util.function.Function;

/**
 * Converts DB entity LogType to OpenAPI model LogType.
 */
public final class LogTypeApiConverter {

  private LogTypeApiConverter() {
    // static only
  }

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
