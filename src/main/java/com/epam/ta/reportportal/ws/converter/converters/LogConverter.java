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

package com.epam.ta.reportportal.ws.converter.converters;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import com.epam.ta.reportportal.core.log.impl.PagedLogResource;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.log.LogFull;
import com.epam.ta.reportportal.model.log.LogResource;
import com.epam.ta.reportportal.model.log.SearchLogRs;
import com.google.common.base.Preconditions;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Converts internal DB model to DTO
 *
 * @author Pavel Bortnik
 */
public final class LogConverter {

  private LogConverter() {
    //static only
  }

  public static final Function<LogFull, LogResource> TO_RESOURCE = model -> {
    Preconditions.checkNotNull(model);
    LogResource resource = new LogResource();
    fillWithLogContent(model, resource);
    return resource;
  };

  private static void fillWithLogContent(LogFull model, LogResource resource) {
    resource.setId(model.getId());
    resource.setUuid(model.getUuid());
    resource.setMessage(ofNullable(model.getLogMessage()).orElse("NULL"));
    resource.setLogTime(model.getLogTime());

    if (isBinaryDataExists(model)) {

      LogResource.BinaryContent binaryContent = new LogResource.BinaryContent();

      binaryContent.setBinaryDataId(String.valueOf(model.getAttachment().getId()));
      binaryContent.setContentType(model.getAttachment().getContentType());
      binaryContent.setThumbnailId(model.getAttachment().getThumbnailId());
      resource.setBinaryContent(binaryContent);
    }

    ofNullable(model.getTestItem()).ifPresent(testItem -> resource.setItemId(testItem.getItemId()));
    ofNullable(model.getLaunch()).ifPresent(launch -> resource.setLaunchId(launch.getId()));
    ofNullable(model.getLogLevel()).ifPresent(
        level -> resource.setLevel(LogLevel.toLevel(level).toString()));
  }

  public static final BiFunction<LogFull, PagedLogResource, PagedLogResource> FILL_WITH_LOG_CONTENT = (model, pagedLog) -> {
    fillWithLogContent(model, pagedLog);
    return pagedLog;
  };

  public static final Function<LogFull, SearchLogRs.LogEntry> TO_LOG_ENTRY = log -> {
    SearchLogRs.LogEntry logEntry = new SearchLogRs.LogEntry();
    logEntry.setMessage(log.getLogMessage());
    logEntry.setLevel(LogLevel.toLevel(log.getLogLevel()).name());
    return logEntry;
  };

  public static final Function<LogFull, Log> LOG_FULL_TO_LOG = logFull -> {
    Log log = new Log();
    log.setAttachment(logFull.getAttachment());
    log.setClusterId(logFull.getClusterId());
    log.setId(logFull.getId());
    log.setLastModified(logFull.getLastModified());
    log.setLaunch(logFull.getLaunch());
    log.setLogLevel(logFull.getLogLevel());
    log.setLogMessage(logFull.getLogMessage());
    log.setLogTime(logFull.getLogTime());
    log.setProjectId(logFull.getProjectId());
    log.setTestItem(logFull.getTestItem());
    log.setUuid(logFull.getUuid());

    return log;
  };

  public static final Function<Log, LogFull> LOG_TO_LOG_FULL = log -> {
    LogFull logFull = new LogFull();
    logFull.setAttachment(log.getAttachment());
    logFull.setClusterId(log.getClusterId());
    logFull.setId(log.getId());
    logFull.setLastModified(log.getLastModified());
    logFull.setLaunch(log.getLaunch());
    logFull.setLogLevel(log.getLogLevel());
    logFull.setLogMessage(log.getLogMessage());
    logFull.setLogTime(log.getLogTime());
    logFull.setProjectId(log.getProjectId());
    logFull.setTestItem(log.getTestItem());
    logFull.setUuid(log.getUuid());

    return logFull;
  };

  private static boolean isBinaryDataExists(LogFull log) {
    return ofNullable(log.getAttachment()).map(
        a -> isNotEmpty(a.getContentType()) || isNotEmpty(a.getThumbnailId())
            || isNotEmpty(a.getFileId())).orElse(false);
  }

}
