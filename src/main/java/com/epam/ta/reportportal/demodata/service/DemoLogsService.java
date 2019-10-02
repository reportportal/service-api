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
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.attachment.AttachmentMetaInfo;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.SplittableRandom;
import java.util.UUID;
import java.util.stream.IntStream;

import static com.epam.ta.reportportal.entity.enums.LogLevel.*;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.FAILED;
import static com.epam.ta.reportportal.util.MultipartFileUtils.getMultipartFile;
import static java.util.stream.Collectors.toList;

@Service
class DemoLogsService {
	private static final int MIN_LOGS_COUNT = 5;

	private static final int MAX_LOGS_COUNT = 30;

	private static final int BINARY_CONTENT_PROBABILITY = 7;

	private SplittableRandom random;

	private LogRepository logRepository;

	private LaunchRepository launchRepository;

	private TestItemRepository testItemRepository;

	private AttachmentBinaryDataService attachmentBinaryDataService;

	public DemoLogsService(LogRepository logRepository, LaunchRepository launchRepository, TestItemRepository testItemRepository,
			AttachmentBinaryDataService attachmentBinaryDataService) {
		this.random = new SplittableRandom();
		this.logRepository = logRepository;
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.attachmentBinaryDataService = attachmentBinaryDataService;
	}

	List<Log> generateDemoLogs(String itemUuid, StatusEnum status, Long projectId, String launchId) {
		TestItem testItem = testItemRepository.findByUuid(itemUuid)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR));
		int logsCount = random.nextInt(MIN_LOGS_COUNT, MAX_LOGS_COUNT);
		List<Log> logs = IntStream.range(1, logsCount).mapToObj(it -> {
			Log log = new Log();
			log.setLogLevel(infoLevel().toInt());
			log.setLogTime(LocalDateTime.now());
			log.setTestItem(testItem);
			log.setLogMessage(ContentUtils.getLogMessage());
			log.setUuid(UUID.randomUUID().toString());
			return log;
		}).collect(toList());
		if (FAILED.equals(status)) {
			List<String> errors = ContentUtils.getErrorLogs();
			logs.addAll(errors.stream().map(msg -> {
				Log log = new Log();
				log.setLogLevel(errorLevel().toInt());
				log.setLogTime(LocalDateTime.now());
				log.setTestItem(testItem);
				log.setLogMessage(msg);
				log.setUuid(UUID.randomUUID().toString());
				return log;
			}).collect(toList()));
		}
		logRepository.saveAll(logs);
		return logs;
	}

	void attachFiles(List<Log> logs, Long projectId, String itemUuid, String launchUuid) {
		Launch launch = launchRepository.findByUuid(launchUuid)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR));
		TestItem item = testItemRepository.findByUuid(itemUuid)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNCLASSIFIED_REPORT_PORTAL_ERROR));
		BooleanHolder binaryDataAttached = new BooleanHolder();
		logs.forEach(it -> {
			if (ERROR.toInt() >= it.getLogLevel()) {
				if (ContentUtils.getWithProbability(BINARY_CONTENT_PROBABILITY)) {
					attachFile(projectId, item.getItemId(), launch.getId(), it);
				}
			} else {
				if (!binaryDataAttached.getValue() && ContentUtils.getWithProbability(BINARY_CONTENT_PROBABILITY)) {
					attachFile(projectId, item.getItemId(), launch.getId(), it);
					binaryDataAttached.setValue(true);
				}
			}
		});
	}

	private void attachFile(Long projectId, Long testItemId, Long launchId, Log it) {
		Attachment attachment = Attachment.values()[random.nextInt(Attachment.values().length)];
		try {
			attachmentBinaryDataService.saveFileAndAttachToLog(
					getMultipartFile(attachment.getResource().getPath()),
					AttachmentMetaInfo.builder()
							.withProjectId(projectId)
							.withLaunchId(launchId)
							.withItemId(testItemId)
							.withLogId(it.getId())
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