/*
 * Copyright 2016 EPAM Systems
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
package com.epam.ta.reportportal.demo_data;

import com.epam.ta.reportportal.database.BinaryData;
import com.epam.ta.reportportal.database.DataStorage;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.entity.BinaryContent;
import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.entity.LogLevel;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.IntStream;

import static com.epam.ta.reportportal.database.entity.LogLevel.*;
import static com.epam.ta.reportportal.database.entity.Status.FAILED;
import static java.util.stream.Collectors.toList;

@Service
class DemoLogsService {
	private SplittableRandom random;

	private LogRepository logRepository;

	private DataStorage dataStorage;

	private static final int MIN_LOGS_COUNT = 5;

	private static final int MAX_LOGS_COUNT = 30;

	private static final int BINARY_CONTENT_PROBABILITY = 20;

	@Autowired
	DemoLogsService(DataStorage dataStorage, LogRepository logRepository) {
		this.dataStorage = dataStorage;
		this.logRepository = logRepository;
		this.random = new SplittableRandom();
	}

	List<Log> generateDemoLogs(String itemId, String status, String projectName) {
		int logsCount = random.nextInt(MIN_LOGS_COUNT, MAX_LOGS_COUNT);
		List<Log> logs = IntStream.range(1, logsCount).mapToObj(it -> {
			Log log = new Log();
			log.setLevel(logLevel());
			log.setLogTime(new Date());
			if (ContentUtils.getWithProbability(BINARY_CONTENT_PROBABILITY)) {
				log.setBinaryContent(attachBinaryContent(projectName));
			}
			log.setTestItemRef(itemId);
			log.setLogMsg(ContentUtils.getLogMessage());
			return log;
		}).collect(toList());
		if (FAILED.name().equals(status)) {
			List<String> errors = ContentUtils.getErrorLogs();
			logs.addAll(errors.stream().map(msg -> {
				Log log = new Log();
				log.setLevel(ERROR);
				log.setLogTime(new Date());
				log.setTestItemRef(itemId);
				log.setLogMsg(msg);
				BinaryContent binaryContent = attachBinaryContent(projectName);
				log.setBinaryContent(binaryContent);
				return log;
			}).collect(toList()));
		}
		return logRepository.save(logs);
	}

	private BinaryContent attachBinaryContent(String projectName) {
		try {
			Attachment attachment = randomAttachment();
			String file = saveResource(attachment.getContentType(), attachment.getResource(), projectName);
			if (attachment.equals(Attachment.PNG)) {
				String thumbnail = saveResource(attachment.getContentType(), new ClassPathResource("demo/attachments/img_tn.png"),
						projectName
				);
				return new BinaryContent(file, thumbnail, attachment.getContentType());
			}
			return new BinaryContent(file, file, attachment.getContentType());
		} catch (IOException e) {
			throw new ReportPortalException("Unable to save binary data", e);
		}
	}

	private String saveResource(String contentType, ClassPathResource resource, String projectName) throws IOException {
		return dataStorage.saveData(new BinaryData(contentType, resource.contentLength(), resource.getInputStream()),
				resource.getFilename(), Collections.singletonMap("project", projectName)
		);
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
