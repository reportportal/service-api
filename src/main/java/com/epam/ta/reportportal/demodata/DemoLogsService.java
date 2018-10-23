/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.demodata;

import com.epam.reportportal.commons.Thumbnailator;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.filesystem.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.IntStream;

import static com.epam.ta.reportportal.entity.enums.LogLevel.*;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.FAILED;
import static java.util.stream.Collectors.toList;

@Service
class DemoLogsService {
	private SplittableRandom random;

	private LogRepository logRepository;

	private DataStore dataStore;

	private Thumbnailator thumbnailator;

	private static final int MIN_LOGS_COUNT = 5;

	private static final int MAX_LOGS_COUNT = 30;

	private static final int BINARY_CONTENT_PROBABILITY = 20;

	@Autowired
	public DemoLogsService(LogRepository logRepository, DataStore dataStore, Thumbnailator thumbnailator) {
		this.random = new SplittableRandom();
		this.logRepository = logRepository;
		this.dataStore = dataStore;
		this.thumbnailator = thumbnailator;
	}

	List<Log> generateDemoLogs(TestItem testItem, String status) {
		int logsCount = random.nextInt(MIN_LOGS_COUNT, MAX_LOGS_COUNT);
		List<Log> logs = IntStream.range(1, logsCount).mapToObj(it -> {
			Log log = new Log();
			log.setLogLevel(logLevel().toInt());
			log.setLogTime(LocalDateTime.now());
			if (ContentUtils.getWithProbability(BINARY_CONTENT_PROBABILITY)) {
				attachFile(log);
			}
			log.setTestItem(testItem);
			log.setLogMessage(ContentUtils.getLogMessage());
			return log;
		}).collect(toList());
		if (FAILED.name().equals(status)) {
			List<String> errors = ContentUtils.getErrorLogs();
			logs.addAll(errors.stream().map(msg -> {
				Log log = new Log();
				log.setLogLevel(ERROR.toInt());
				log.setLogTime(LocalDateTime.now());
				log.setTestItem(testItem);
				log.setLogMessage(msg);
				attachFile(log);
				return log;
			}).collect(toList()));
		}
		return logRepository.saveAll(logs);
	}

	private void attachFile(Log log) {
		AttachmentMetaData attachmentMetaData = saveAttachment();
		log.setAttachment(attachmentMetaData.getPath());
		if (attachmentMetaData.isImage()) {
			log.setAttachmentThumbnail(attachmentMetaData.getThumbnailPath());
		}
	}

	private AttachmentMetaData saveAttachment() {
		try {
			Attachment attachment = randomAttachment();
			String filePath = saveResource(attachment.getResource());
			if (attachment.equals(Attachment.PNG)) {
				InputStream thumbnail = thumbnailator.createThumbnail(attachment.getResource().getInputStream());
				String thumbnailFileName = AttachmentMetaData.THUMBNAIL_PREFIX + "-" + attachment.getResource().getFilename();
				String thumbnailPath = dataStore.save(thumbnailFileName, thumbnail);
				return AttachmentMetaData.of(filePath, thumbnailPath);
			}
			return AttachmentMetaData.of(filePath);
		} catch (IOException e) {
			throw new ReportPortalException("Unable to save binary data", e);
		}
	}

	private String saveResource(ClassPathResource resource) throws IOException {
		return dataStore.save(resource.getFilename(), resource.getInputStream());
	}

	private Attachment randomAttachment() {
		return Attachment.values()[random.nextInt(Attachment.values().length)];
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