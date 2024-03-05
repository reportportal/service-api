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

package com.epam.ta.reportportal.core.imprt.impl.junit;

import static com.epam.ta.reportportal.core.imprt.impl.DateUtils.toMillis;
import static com.epam.ta.reportportal.entity.enums.TestItemIssueGroup.NOT_ISSUE_FLAG;

import com.epam.ta.reportportal.commons.EntityUtils;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.core.log.CreateLogHandler;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.enums.TestItemTypeEnum;
import com.epam.ta.reportportal.ws.reporting.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.reporting.Issue;
import com.epam.ta.reportportal.ws.reporting.SaveLogRQ;
import com.epam.ta.reportportal.ws.reporting.StartTestItemRQ;
import com.google.common.base.Strings;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class XunitImportHandler extends DefaultHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(XunitImportHandler.class);

  private final StartTestItemHandler startTestItemHandler;

  private final FinishTestItemHandler finishTestItemHandler;

  private final CreateLogHandler createLogHandler;

  private static final int MAX_LAUNCH_NAME_LENGTH = 256;

  @Autowired
  public XunitImportHandler(StartTestItemHandler startTestItemHandler,
      FinishTestItemHandler finishTestItemHandler, CreateLogHandler createLogHandler) {
    this.startTestItemHandler = startTestItemHandler;
    this.finishTestItemHandler = finishTestItemHandler;
    this.createLogHandler = createLogHandler;
  }

  //initial info
  private ReportPortalUser.ProjectDetails projectDetails;
  private ReportPortalUser user;
  private String launchUuid;
  private boolean isSkippedNotIssue = false;

  //need to know item's id to attach System.out/System.err logs
  private String currentItemUuid;

  private LocalDateTime startSuiteTime;

  private long commonDuration;
  private long currentDuration;

  private long currentSuiteDuration;

  //items structure ids
  private Deque<String> itemUuids;
  private StatusEnum status;
  private StringBuilder message;
  private LocalDateTime startItemTime;

  @Override
  public void startDocument() {
    itemUuids = new ArrayDeque<>();
    message = new StringBuilder();
    startSuiteTime = LocalDateTime.now();
  }

  @Override
  public void endDocument() {
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) {
    switch (XunitReportTag.fromString(qName)) {
      case TESTSUITE:
        if (itemUuids.isEmpty()) {
          startRootItem(attributes.getValue(XunitReportTag.ATTR_NAME.getValue()),
              attributes.getValue(XunitReportTag.START_TIME.getValue()),
              attributes.getValue(XunitReportTag.TIMESTAMP.getValue()),
              attributes.getValue(XunitReportTag.ATTR_TIME.getValue())
          );
        } else {
          startTestItem(attributes.getValue(XunitReportTag.ATTR_NAME.getValue()));
        }
        break;
      case TESTCASE:
        startStepItem(attributes.getValue(XunitReportTag.ATTR_NAME.getValue()),
            attributes.getValue(XunitReportTag.START_TIME.getValue()),
            attributes.getValue(XunitReportTag.TIMESTAMP.getValue()),
            attributes.getValue(XunitReportTag.ATTR_TIME.getValue())
        );
        break;
      case ERROR:
      case FAILURE:
        message = new StringBuilder();
        status = StatusEnum.FAILED;
        break;
      case SKIPPED:
        message = new StringBuilder();
        status = StatusEnum.SKIPPED;
        break;
      case SYSTEM_OUT:
      case SYSTEM_ERR:
      case WARNING:
        message = new StringBuilder();
        break;
      case UNKNOWN:
      default:
        LOGGER.warn("Unknown tag: {}", qName);
        break;
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) {
    switch (XunitReportTag.fromString(qName)) {
      case TESTSUITE:
        finishRootItem();
        break;
      case TESTCASE:
        finishTestItem();
        break;
      case SKIPPED:
      case ERROR:
      case FAILURE:
      case SYSTEM_ERR:
        attachLog(LogLevel.ERROR);
        break;
      case SYSTEM_OUT:
        attachLog(LogLevel.INFO);
        break;
      case WARNING:
        attachLog(LogLevel.WARN);
        break;
      case UNKNOWN:
      default:
        LOGGER.warn("Unknown tag: {}", qName);
        break;
    }
  }

  @Override
  public void characters(char[] ch, int start, int length) {
    String msg = new String(ch, start, length);
    if (!msg.isEmpty()) {
      message.append(msg);
    }
  }

  private void startRootItem(String name, String startTime, String timestamp, String duration) {
    if (null != timestamp) {
      startItemTime = parseTimeStamp(timestamp);
      if (startSuiteTime.isAfter(startItemTime)) {
        startSuiteTime = LocalDateTime.of(startItemTime.toLocalDate(), startItemTime.toLocalTime());
      }
    } else if (null != startTime) {
      startItemTime = parseTimeStamp(startTime);
      if (startSuiteTime.isAfter(startItemTime)) {
        startSuiteTime = LocalDateTime.of(startItemTime.toLocalDate(), startItemTime.toLocalTime());
      }
    } else {
      startItemTime = LocalDateTime.now();
      startSuiteTime = LocalDateTime.now();
    }
    currentSuiteDuration = toMillis(duration);
    StartTestItemRQ rq = buildStartTestRq(name);
    String id = startTestItemHandler.startRootItem(user, projectDetails, rq).getId();
    itemUuids.push(id);
  }

  private LocalDateTime parseTimeStamp(String timestamp) {
    // try to parse datetime as Long, otherwise parse as timestamp
    try {
      return LocalDateTime.ofInstant(
          Instant.ofEpochMilli(Long.parseLong(timestamp)), ZoneOffset.UTC);
    } catch (NumberFormatException ignored) {
      DateTimeFormatter formatter =
          new DateTimeFormatterBuilder().appendOptional(DateTimeFormatter.RFC_1123_DATE_TIME)
              .appendOptional(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
              .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME).optionalStart()
              .appendOffsetId().appendZoneId().optionalEnd().optionalStart().appendLiteral(' ')
              .parseCaseSensitive().appendZoneId().optionalEnd().toFormatter();

      TemporalAccessor temporalAccessor = formatter.parse(timestamp);
      if (isParsedTimeStampHasOffset(temporalAccessor)) {
        return ZonedDateTime.from(temporalAccessor).withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDateTime();
      } else {
        return LocalDateTime.from(temporalAccessor);
      }
    }

  }

  private void startTestItem(String name) {
    StartTestItemRQ rq = buildStartTestRq(name);
    String id =
        startTestItemHandler.startChildItem(user, projectDetails, rq, itemUuids.peek()).getId();
    itemUuids.push(id);
  }

  private void startStepItem(String name, String startTime, String timestamp, String duration) {
    StartTestItemRQ rq = new StartTestItemRQ();
    rq.setLaunchUuid(launchUuid);
    rq.setType(TestItemTypeEnum.STEP.name());
    rq.setName(StringUtils.abbreviate(name, MAX_LAUNCH_NAME_LENGTH));

    if (null != timestamp) {
      startItemTime = parseTimeStamp(timestamp);
    } else if (null != startTime) {
      startItemTime = parseTimeStamp(startTime);
    } else {
      startItemTime = startSuiteTime;
    }

    rq.setStartTime(EntityUtils.TO_DATE.apply(startItemTime));

    String id =
        startTestItemHandler.startChildItem(user, projectDetails, rq, itemUuids.peek()).getId();
    currentDuration = toMillis(duration);
    currentItemUuid = id;
    itemUuids.push(id);
  }

  private void finishRootItem() {
    FinishTestItemRQ rq = new FinishTestItemRQ();
    markAsNotIssue(rq);
    rq.setEndTime(
        EntityUtils.TO_DATE.apply(startSuiteTime.plus(currentSuiteDuration, ChronoUnit.MILLIS)));
    finishTestItemHandler.finishTestItem(user, projectDetails, itemUuids.poll(), rq);
    status = null;
  }

  private void finishTestItem() {
    FinishTestItemRQ rq = new FinishTestItemRQ();
    markAsNotIssue(rq);
    LocalDateTime endTime = startItemTime.plus(currentDuration, ChronoUnit.MILLIS);
    commonDuration += currentDuration;
    rq.setEndTime(EntityUtils.TO_DATE.apply(endTime));
    rq.setStatus(Optional.ofNullable(status).orElse(StatusEnum.PASSED).name());
    currentItemUuid = itemUuids.poll();
    finishTestItemHandler.finishTestItem(user, projectDetails, currentItemUuid, rq);
    status = null;
  }

  private void markAsNotIssue(FinishTestItemRQ rq) {
    if (StatusEnum.SKIPPED.equals(status) && isSkippedNotIssue) {
      Issue issue = new Issue();
      issue.setIssueType(NOT_ISSUE_FLAG.getValue());
      rq.setIssue(issue);
    }
  }

  private void attachLog(LogLevel logLevel) {
    if (null != message && message.length() != 0) {
      SaveLogRQ saveLogRQ = new SaveLogRQ();
      saveLogRQ.setLevel(logLevel.name());
      saveLogRQ.setLogTime(EntityUtils.TO_DATE.apply(startItemTime));
      saveLogRQ.setMessage(message.toString().trim());
      saveLogRQ.setItemUuid(currentItemUuid);
      createLogHandler.createLog(saveLogRQ, null, projectDetails);
    }
  }

  XunitImportHandler withParameters(ReportPortalUser.ProjectDetails projectDetails, String launchId,
      ReportPortalUser user, boolean isSkippedNotIssue) {
    this.projectDetails = projectDetails;
    this.launchUuid = launchId;
    this.user = user;
    this.isSkippedNotIssue = isSkippedNotIssue;
    return this;
  }

  private StartTestItemRQ buildStartTestRq(String name) {
    StartTestItemRQ rq = new StartTestItemRQ();
    rq.setLaunchUuid(launchUuid);
    rq.setStartTime(EntityUtils.TO_DATE.apply(startItemTime));
    rq.setType(TestItemTypeEnum.TEST.name());
    rq.setName(Strings.isNullOrEmpty(name) ? "no_name" : name);
    return rq;
  }

  LocalDateTime getStartSuiteTime() {
    return startSuiteTime;
  }

  long getCommonDuration() {
    return commonDuration;
  }

  private boolean isParsedTimeStampHasOffset(TemporalAccessor temporalAccessor) {
    return temporalAccessor.query(TemporalQueries.offset()) != null;
  }

}
