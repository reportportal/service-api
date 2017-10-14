/*
 * Copyright 2017 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.ta.reportportal.core.imprt.impl.junit;

import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.core.log.ICreateLogHandler;
import com.epam.ta.reportportal.database.entity.LogLevel;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.TestItemType;
import com.epam.ta.reportportal.ws.model.FinishTestItemRQ;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

import static com.epam.ta.reportportal.core.imprt.impl.DateUtils.toDate;
import static com.epam.ta.reportportal.core.imprt.impl.DateUtils.toMillis;

public class XunitImportHandler extends DefaultHandler {

    @Autowired
    private StartTestItemHandler startTestItemHandler;

    @Autowired
    private FinishTestItemHandler finishTestItemHandler;

    @Autowired
    private ICreateLogHandler createLogHandler;

    //initial info
    private String projectId;
    private String userName;
    private String launchId;

    //need to know item's id to attach System.out/System.err logs
    private String currentId;

    private LocalDateTime startSuiteTime;

    private long commonDuration;
    private long currentDuration;

    //items structure ids
    private Deque<String> itemsIds;
    private Status status;
    private StringBuilder message;
    private LocalDateTime startItemTime;

    @Override
    public void startDocument() throws SAXException {
        itemsIds = new ArrayDeque<>();
        message = new StringBuilder();
        startSuiteTime = LocalDateTime.now();
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (XunitReportTag.fromString(qName)) {
            case TESTSUITE:
                startRootItem(attributes.getValue(XunitReportTag.ATTR_NAME.getValue()),
                        attributes.getValue(XunitReportTag.TIMESTAMP.getValue()));
                break;
            case TESTCASE:
                startTestItem(attributes.getValue(XunitReportTag.ATTR_NAME.getValue()),
                        attributes.getValue(XunitReportTag.ATTR_TIME.getValue()));
                break;
            case FAILURE:
                message = new StringBuilder();
                status = Status.FAILED;
                break;
            case SKIPPED:
                message = new StringBuilder();
                status = Status.SKIPPED;
                break;
            case SYSTEM_OUT:
            case SYSTEM_ERR:
                message = new StringBuilder();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (XunitReportTag.fromString(qName)) {
            case TESTSUITE:
                finishRootItem();
                break;
            case TESTCASE:
                finishTestItem();
                break;
            case SKIPPED:
                attachLog(LogLevel.ERROR);
                break;
            case FAILURE:
                attachLog(LogLevel.ERROR);
                break;
            case SYSTEM_OUT:
            case SYSTEM_ERR:
                attachDebugLog(LogLevel.DEBUG);
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String msg = new String(ch, start, length);
        if (!msg.isEmpty()) {
            message.append(new String(ch, start, length));
        }
    }

    private void startRootItem(String name, String timestamp) {
        if (null != timestamp) {
            startItemTime = parseTimeStamp(timestamp);
            if (startSuiteTime.isAfter(startItemTime)) {
                startSuiteTime = LocalDateTime.of(startItemTime.toLocalDate(), startItemTime.toLocalTime());
            }
        } else {
            startItemTime = LocalDateTime.now();
        }
        StartTestItemRQ rq = new StartTestItemRQ();
        rq.setLaunchId(launchId);
        rq.setStartTime(toDate(startItemTime));
        rq.setType(TestItemType.TEST.name());
        rq.setName(name);
        String id = startTestItemHandler.startRootItem(projectId, rq).getId();
        itemsIds.push(id);
    }

    private LocalDateTime parseTimeStamp(String timestamp) {
        LocalDateTime localDateTime = null;
        try {
            long l = Long.parseLong(timestamp);
			System.out.println(l);
			localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(timestamp)), ZoneId.systemDefault());
        }catch (NumberFormatException e) {
            //ignored
        }
        if (null == localDateTime) {
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .appendOptional(DateTimeFormatter.RFC_1123_DATE_TIME)
                    .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .optionalStart().appendZoneId().optionalEnd()
                    .toFormatter();
            localDateTime = LocalDateTime.parse(timestamp, formatter);
        }
        return localDateTime;
    }

    private void startTestItem(String name, String duration) {
        StartTestItemRQ rq = new StartTestItemRQ();
        rq.setLaunchId(launchId);
        rq.setStartTime(toDate(startItemTime));
        rq.setType(TestItemType.STEP.name());
        rq.setName(name);
        String id = startTestItemHandler.startChildItem(projectId, rq, itemsIds.peekLast()).getId();
        currentDuration = toMillis(duration);
        currentId = id;
        itemsIds.push(id);
    }

    private void finishRootItem() {
        FinishTestItemRQ rq = new FinishTestItemRQ();
        rq.setEndTime(toDate(startItemTime));
        rq.setStatus(Optional.ofNullable(status).orElse(Status.PASSED).name());
        finishTestItemHandler.finishTestItem(itemsIds.poll(), rq, userName);
        status = null;
    }

    private void finishTestItem() {
        FinishTestItemRQ rq = new FinishTestItemRQ();
        startItemTime = startItemTime.plus(currentDuration, ChronoUnit.MILLIS);
        commonDuration += currentDuration;
        rq.setEndTime(toDate(startItemTime));
        rq.setStatus(Optional.ofNullable(status).orElse(Status.PASSED).name());
        currentId = itemsIds.poll();
        finishTestItemHandler.finishTestItem(currentId, rq, userName);
        status = null;
    }

    private void attachDebugLog(LogLevel logLevel) {
        if (null != message && message.length() != 0) {
            SaveLogRQ saveLogRQ = new SaveLogRQ();
            saveLogRQ.setLevel(logLevel.name());
            saveLogRQ.setLogTime(toDate(startItemTime));
            saveLogRQ.setMessage(message.toString());
            saveLogRQ.setTestItemId(currentId);
            createLogHandler.createLog(saveLogRQ, null, projectId);
        }
    }

    private void attachLog(LogLevel logLevel) {
        if (null != message && message.length() != 0) {
            SaveLogRQ saveLogRQ = new SaveLogRQ();
            saveLogRQ.setLevel(logLevel.name());
            saveLogRQ.setLogTime(toDate(startItemTime));
            saveLogRQ.setMessage(message.toString());
            saveLogRQ.setTestItemId(itemsIds.getFirst());
            createLogHandler.createLog(saveLogRQ, null, projectId);
        }
    }

    XunitImportHandler withParameters(String projectId, String launchId, String user) {
        this.projectId = projectId;
        this.launchId = launchId;
        this.userName = user;
        return this;
    }

    LocalDateTime getStartSuiteTime() {
        return startSuiteTime;
    }

    long getCommonDuration() {
        return commonDuration;
    }
}
