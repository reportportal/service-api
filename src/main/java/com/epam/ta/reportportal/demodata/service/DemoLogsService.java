/*
 * Copyright 2018 EPAM Systems
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

import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.commons.BinaryDataMetaInfo;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.SplittableRandom;
import java.util.stream.IntStream;

import static com.epam.ta.reportportal.entity.enums.LogLevel.*;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.FAILED;
import static java.util.stream.Collectors.toList;

@Service
class DemoLogsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DemoLogsService.class);

	private SplittableRandom random;

	private LogRepository logRepository;

	private DataStoreService dataStoreService;

	private static final int MIN_LOGS_COUNT = 5;

	private static final int MAX_LOGS_COUNT = 30;

	private static final int BINARY_CONTENT_PROBABILITY = 20;

	@Autowired
	public DemoLogsService(LogRepository logRepository, DataStoreService dataStoreService) {
		this.random = new SplittableRandom();
		this.logRepository = logRepository;
		this.dataStoreService = dataStoreService;
	}

	void generateDemoLogs(TestItem testItem, StatusEnum status, Long projectId) {
		int logsCount = random.nextInt(MIN_LOGS_COUNT, MAX_LOGS_COUNT);
		List<Log> logs = IntStream.range(1, logsCount).mapToObj(it -> {
			Log log = new Log();
			log.setLogLevel(logLevel().toInt());
			log.setLogTime(LocalDateTime.now());
			if (ContentUtils.getWithProbability(BINARY_CONTENT_PROBABILITY)) {
				attachFile(log, projectId);
			}
			log.setTestItem(testItem);
			log.setLogMessage(ContentUtils.getLogMessage());
			return log;
		}).collect(toList());
		if (FAILED.equals(status)) {
			List<String> errors = ContentUtils.getErrorLogs();
			logs.addAll(errors.stream().map(msg -> {
				Log log = new Log();
				log.setLogLevel(ERROR.toInt());
				log.setLogTime(LocalDateTime.now());
				log.setTestItem(testItem);
				log.setLogMessage(msg);
				attachFile(log, projectId);
				return log;
			}).collect(toList()));
		}
		logRepository.saveAll(logs);
	}

	private void attachFile(Log log, Long projectId) {
		saveAttachment(projectId).ifPresent(it -> {
			log.setAttachment(it.getFileId());
			log.setAttachmentThumbnail(it.getThumbnailFileId());
		});
	}

	private Optional<BinaryDataMetaInfo> saveAttachment(Long projectId) {
		Attachment attachment = Attachment.values()[random.nextInt(Attachment.values().length)];
		try {
			return dataStoreService.save(projectId, attachment.getResource().getFile());
		} catch (IOException e) {
			LOGGER.error("Cannot attach file: ", e);
		}
		return Optional.empty();
	}

	private LogLevel logLevel() {
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
}