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
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.Attributes;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(XunitImportHandler.class);

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
	public void startDocument() {
		itemsIds = new ArrayDeque<>();
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
				if (itemsIds.isEmpty()) {
					startRootItem(attributes.getValue(XunitReportTag.ATTR_NAME.getValue()),
							attributes.getValue(XunitReportTag.TIMESTAMP.getValue())
					);
				} else {
					startTestItem(attributes.getValue(XunitReportTag.ATTR_NAME.getValue()));
				}
				break;
			case TESTCASE:
				startStepItem(attributes.getValue(XunitReportTag.ATTR_NAME.getValue()),
						attributes.getValue(XunitReportTag.ATTR_TIME.getValue())
				);
				break;
			case ERROR:
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
			case WARNING:
				message = new StringBuilder();
				break;
			case UNKNOWN:
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
				attachLog(LogLevel.ERROR);
				break;
			case SYSTEM_OUT:
				attachLog(LogLevel.INFO);
				break;
			case SYSTEM_ERR:
				attachLog(LogLevel.ERROR);
				break;
			case WARNING:
				attachLog(LogLevel.WARN);
				break;
			case UNKNOWN:
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

	private void startRootItem(String name, String timestamp) {
		if (null != timestamp) {
			startItemTime = parseTimeStamp(timestamp);
			if (startSuiteTime.isAfter(startItemTime)) {
				startSuiteTime = LocalDateTime.of(startItemTime.toLocalDate(), startItemTime.toLocalTime());
			}
		} else {
			startItemTime = LocalDateTime.now();
		}
		StartTestItemRQ rq = buildStartTestRq(name);
		String id = startTestItemHandler.startRootItem(projectId, rq).getId();
		itemsIds.push(id);
	}

	private LocalDateTime parseTimeStamp(String timestamp) {
		LocalDateTime localDateTime = null;
		try {
			localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(timestamp)), ZoneId.systemDefault());
		} catch (NumberFormatException ignored) {
			//ignored
		}
		if (null == localDateTime) {
			DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendOptional(DateTimeFormatter.RFC_1123_DATE_TIME)
					.appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
					.optionalStart()
					.appendZoneId()
					.optionalEnd()
					.toFormatter();
			localDateTime = LocalDateTime.parse(timestamp, formatter);
		}
		return localDateTime;
	}

	private void startTestItem(String name) {
		StartTestItemRQ rq = buildStartTestRq(name);
		String id = startTestItemHandler.startChildItem(projectId, rq, itemsIds.peek()).getId();
		itemsIds.push(id);
	}

	private void startStepItem(String name, String duration) {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchId(launchId);
		rq.setStartTime(toDate(startItemTime));
		rq.setType(TestItemType.STEP.name());
		rq.setName(name);
		String id = startTestItemHandler.startChildItem(projectId, rq, itemsIds.peek()).getId();
		currentDuration = toMillis(duration);
		currentId = id;
		itemsIds.push(id);
	}

	private void finishRootItem() {
		FinishTestItemRQ rq = new FinishTestItemRQ();
		rq.setEndTime(toDate(startItemTime));
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

	private void attachLog(LogLevel logLevel) {
		if (null != message && message.length() != 0) {
			SaveLogRQ saveLogRQ = new SaveLogRQ();
			saveLogRQ.setLevel(logLevel.name());
			saveLogRQ.setLogTime(toDate(startItemTime));
			saveLogRQ.setMessage(message.toString().trim());
			saveLogRQ.setTestItemId(currentId);
			createLogHandler.createLog(saveLogRQ, null, projectId);
		}
	}

	XunitImportHandler withParameters(String projectId, String launchId, String user) {
		this.projectId = projectId;
		this.launchId = launchId;
		this.userName = user;
		return this;
	}

	private StartTestItemRQ buildStartTestRq(String name) {
		StartTestItemRQ rq = new StartTestItemRQ();
		rq.setLaunchId(launchId);
		rq.setStartTime(toDate(startItemTime));
		rq.setType(TestItemType.TEST.name());
		rq.setName(Strings.isNullOrEmpty(name) ? "no_name" : name);
		return rq;
	}

	LocalDateTime getStartSuiteTime() {
		return startSuiteTime;
	}

	long getCommonDuration() {
		return commonDuration;
	}
}
