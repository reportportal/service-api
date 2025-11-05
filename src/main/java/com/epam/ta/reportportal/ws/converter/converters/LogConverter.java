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

import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.log.LogFull;
import com.epam.ta.reportportal.model.log.LogResource;
import com.epam.ta.reportportal.model.log.SearchLogRs;
import com.epam.ta.reportportal.service.LogTypeResolver;
import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Converts internal DB model to DTO
 *
 * @author Pavel Bortnik
 */
@Component
@RequiredArgsConstructor
public class LogConverter {

  private final LogTypeResolver logTypeResolver;

  public LogResource toResource(LogFull model) {
    Preconditions.checkNotNull(model);
    var resource = new LogResource();
    fillBaseFields(model, resource);
    resource.setLevel(getNameFromLogLevel(model));
    return resource;
  }

  public List<LogResource> toResources(Collection<LogFull> logs, Long projectId) {
    if (logs.isEmpty()) {
      return Collections.emptyList();
    }
    var logLevelMap = logTypeResolver.getLogLevelMapForProject(projectId);
    return logs.stream()
        .map(log -> fillLogResource(log, logLevelMap))
        .toList();
  }

  public SearchLogRs.LogEntry toLogEntry(LogFull log) {
    Preconditions.checkNotNull(log);
    var entry = new SearchLogRs.LogEntry();
    entry.setMessage(log.getLogMessage());
    entry.setLevel(getNameFromLogLevel(log));
    return entry;
  }

  public List<SearchLogRs.LogEntry> toLogEntries(Collection<LogFull> logs, Long projectId) {
    if (logs.isEmpty()) {
      return Collections.emptyList();
    }
    var logLevelMap = logTypeResolver.getLogLevelMapForProject(projectId);
    return logs.stream()
        .map(log -> fillLogEntry(log, logLevelMap))
        .toList();
  }

  public void fillWithLogContent(LogFull model, LogResource resource) {
    fillBaseFields(model, resource);
    resource.setLevel(getNameFromLogLevel(model));
  }

  public void fillWithLogContent(Map<Long, LogFull> logMap, List<? extends LogResource> resources,
      Long projectId) {
    if (logMap.isEmpty() || resources.isEmpty()) {
      return;
    }

    var logLevelMap = logTypeResolver.getLogLevelMapForProject(projectId);

    resources.forEach(resource ->
        ofNullable(logMap.get(resource.getId()))
            .ifPresent(model -> {
              fillBaseFields(model, resource);
              setLogLevel(model.getLogLevel(), logLevelMap, resource::setLevel);
            })
    );
  }

  private String getNameFromLogLevel(LogFull model) {
    return logTypeResolver.resolveNameFromLogLevel(model.getProjectId(), model.getLogLevel());
  }

  private LogResource fillLogResource(LogFull log, Map<Integer, String> logLevelMap) {
    var resource = new LogResource();
    fillBaseFields(log, resource);
    setLogLevel(log.getLogLevel(), logLevelMap, resource::setLevel);
    return resource;
  }

  private SearchLogRs.LogEntry fillLogEntry(LogFull log, Map<Integer, String> logLevelMap) {
    var entry = new SearchLogRs.LogEntry();
    entry.setMessage(log.getLogMessage());
    setLogLevel(log.getLogLevel(), logLevelMap, entry::setLevel);
    return entry;
  }

  private void setLogLevel(Integer level, Map<Integer, String> logLevelMap,
      Consumer<String> setter) {
    ofNullable(level).ifPresent(
        l -> setter.accept(logLevelMap.getOrDefault(l, LogLevel.UNKNOWN.toString())));
  }

  private void fillBaseFields(LogFull model, LogResource resource) {
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
  }

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
