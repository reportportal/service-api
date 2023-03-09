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

package com.epam.ta.reportportal.demodata.service;

import com.epam.ta.reportportal.binary.AttachmentBinaryDataService;
import com.epam.ta.reportportal.core.log.LogService;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.attachment.AttachmentMetaInfo;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.log.LogFull;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.IntStream;

import static com.epam.ta.reportportal.entity.enums.LogLevel.*;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.FAILED;
import static com.epam.ta.reportportal.util.MultipartFileUtils.getMultipartFile;
import static com.epam.ta.reportportal.ws.converter.converters.LogConverter.LOG_FULL_TO_LOG;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

@Service
public class DemoLogsService {

	private final int attachmentProbability;

	private final SplittableRandom random;

	private final LogRepository logRepository;
	private final LaunchRepository launchRepository;
	private final TestItemRepository testItemRepository;
	private final LogService logService;

	private final AttachmentBinaryDataService attachmentBinaryDataService;

	public DemoLogsService(@Value("${rp.environment.variable.demo.attachment.probability}") int attachmentProbability,
						   LogRepository logRepository, LaunchRepository launchRepository, TestItemRepository testItemRepository,
						   LogService logService, AttachmentBinaryDataService attachmentBinaryDataService) {
		this.attachmentProbability = attachmentProbability;
		this.logService = logService;
		this.random = new SplittableRandom();
		this.logRepository = logRepository;
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.attachmentBinaryDataService = attachmentBinaryDataService;
	}

	public List<Log> generateLaunchLogs(int count, String launchUUid, StatusEnum status) {
		final Launch launch = launchRepository.findByUuid(launchUUid)
				.orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchUUid));
		final List<LogFull> logFulls = IntStream.range(0, count)
				.mapToObj(it -> getLogFull(launch, ContentUtils.getLogMessage(), infoLevel()))
				.collect(toList());
		if (FAILED.equals(status)) {
			List<String> errors = ContentUtils.getErrorLogs();
			logFulls.addAll(errors.stream().map(msg -> getLogFull(launch, msg, errorLevel())).collect(toList()));
		}
		List<Log> logs = logFulls.stream().map(LOG_FULL_TO_LOG).collect(toList());
		logRepository.saveAll(logs);
		fillIdByUuid(logFulls, logs);
		logService.saveLogMessageList(logFulls, launch.getId());

		return logs;
	}

	private LogFull getLogFull(Launch launch, String message, LogLevel logLevel) {
		LogFull logFull = new LogFull();
		logFull.setLogLevel(logLevel.toInt());
		logFull.setLogTime(LocalDateTime.now());
		logFull.setLaunch(launch);
		logFull.setProjectId(launch.getProjectId());
		logFull.setLogMessage(message);
		logFull.setUuid(UUID.randomUUID().toString());
		return logFull;
	}

	public List<Log> generateItemLogs(int count, Long projectId, String itemUuid, StatusEnum status) {
		final TestItem testItem = testItemRepository.findByUuid(itemUuid)
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, itemUuid));
		List<LogFull> logFulls = IntStream.range(0, count)
				.mapToObj(it -> getLogFull(projectId, testItem, infoLevel(), ContentUtils.getLogMessage()))
				.collect(toList());
		if (FAILED.equals(status)) {
			List<String> errors = ContentUtils.getErrorLogs();
			logFulls.addAll(errors.stream().map(msg -> getLogFull(projectId, testItem, errorLevel(), msg)).collect(toList()));
		}
		List<Log> logs = logFulls.stream().map(LOG_FULL_TO_LOG).collect(toList());
		logRepository.saveAll(logs);
		fillIdByUuid(logFulls, logs);
		logService.saveLogMessageList(logFulls, testItem.getLaunchId());

		return logs;
	}

	private void fillIdByUuid(List<LogFull> logFulls, List<Log> logs) {
		if (isNotEmpty(logFulls) && isNotEmpty(logs)) {
			Map<String, Long> logIdsMap = new HashMap<>(logs.size());
			for (Log log : logs) {
				logIdsMap.put(log.getUuid(), log.getId());
			}

			for (LogFull logFull : logFulls) {
				logFull.setId(logIdsMap.get(logFull.getUuid()));
			}
		}
	}

	private LogFull getLogFull(Long projectId, TestItem testItem, LogLevel logLevel, String logMessage) {
		LogFull logFull = new LogFull();
		logFull.setLogLevel(logLevel.toInt());
		logFull.setLogTime(LocalDateTime.now());
		logFull.setTestItem(testItem);
		logFull.setProjectId(projectId);
		logFull.setLogMessage(logMessage);
		logFull.setUuid(UUID.randomUUID().toString());
		return logFull;
	}

	public void attachFiles(List<Log> logs, Long projectId, String launchUuid) {
		Launch launch = launchRepository.findByUuid(launchUuid)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR));
		createAttachments(logs, projectId, launch.getId(), null, launchUuid);
	}

	public void attachFiles(List<Log> logs, Long projectId, String itemUuid, String launchUuid) {
		Launch launch = launchRepository.findByUuid(launchUuid)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR));
		TestItem item = testItemRepository.findByUuid(itemUuid)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR));
		createAttachments(logs, projectId, launch.getId(), item.getItemId(), launchUuid);
	}

	private void createAttachments(List<Log> logs, Long projectId, Long launchId, Long itemId, String launchUuid) {
		BooleanHolder binaryDataAttached = new BooleanHolder();
		logs.forEach(it -> {
			if (ERROR.toInt() >= it.getLogLevel()) {
				if (ContentUtils.getWithProbability(attachmentProbability)) {
					createAttachment(projectId, itemId, launchId, it, launchUuid);
				}
			} else {
				if (!binaryDataAttached.getValue() && ContentUtils.getWithProbability(attachmentProbability)) {
					createAttachment(projectId, itemId, launchId, it, launchUuid);
					binaryDataAttached.setValue(true);
				}
			}
		});
	}

	private void createAttachment(Long projectId, Long testItemId, Long launchId, Log it, String launchUuid) {
		Attachment attachment = Attachment.values()[random.nextInt(Attachment.values().length)];
		try {
			attachmentBinaryDataService.saveFileAndAttachToLog(getMultipartFile(attachment.getResource().getPath()),
					AttachmentMetaInfo.builder()
							.withProjectId(projectId)
							.withLaunchId(launchId)
							.withItemId(testItemId)
							.withLogId(it.getId())
							.withLaunchUuid(launchUuid)
							.withLogUuid(it.getUuid())
							.withCreationDate(LocalDateTime.now(ZoneOffset.UTC))
							.build()
			);
		} catch (IOException e) {
			throw new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR, "Error generating demo data.");
		}
	}

	private static class BooleanHolder {
		private boolean value;

		BooleanHolder() {
			value = false;
		}

		public boolean getValue() {
			return value;
		}

		public void setValue(boolean value) {
			this.value = value;
		}
	}

	private LogLevel infoLevel() {
		int i = random.nextInt(50);
		if (i < 10) {
			return DEBUG;
		} else if (i < 20) {
			return WARN;
		} else if (i < 30) {
			return TRACE;
		} else {
			return INFO;
		}
	}

	private LogLevel errorLevel() {
		return random.nextBoolean() ? ERROR : FATAL;
	}
}