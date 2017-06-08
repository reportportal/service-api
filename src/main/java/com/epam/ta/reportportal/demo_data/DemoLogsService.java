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
import com.google.common.io.CharStreams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static com.epam.ta.reportportal.database.entity.LogLevel.*;
import static com.epam.ta.reportportal.database.entity.Status.FAILED;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

@Service
class DemoLogsService {

	private final Random random;
	private final LogRepository logRepository;
	private final DataStorage dataStorage;
	@Value("classpath:demo/img.png")
	private Resource img;
	@Value("classpath:demo/demo_logs.txt")
	private Resource demoLogs;
	@Value("classpath:demo/error_logs.txt")
	private Resource errorLogsResource;

	@Autowired
	DemoLogsService(DataStorage dataStorage, LogRepository logRepository) {
		this.dataStorage = dataStorage;
		this.logRepository = logRepository;
		this.random = new Random();
	}

	List<Log> generateDemoLogs(String itemId, String status) {
		try (BufferedReader errorsBufferedReader = new BufferedReader(new InputStreamReader(errorLogsResource.getInputStream(), UTF_8));
				BufferedReader demoLogsBufferedReader = new BufferedReader(new InputStreamReader(demoLogs.getInputStream(), UTF_8))) {
            List<String> errorLogs = Arrays.stream(CharStreams.toString(errorsBufferedReader).split("\r\n\r\n")).collect(toList());
            List<String> logMessages = demoLogsBufferedReader.lines().collect(toList());
			int t = random.nextInt(30);
			List<Log> logs = IntStream.range(1, t + 1).mapToObj(it -> {
				Log log = new Log();
				log.setLevel(logLevel());
				log.setLogTime(new Date());
				log.setTestItemRef(itemId);
				log.setLogMsg(logMessages.get(random.nextInt(logMessages.size())));
				return log;
			}).collect(toList());
			if (FAILED.name().equals(status)) {
				String file = dataStorage.saveData(new BinaryData(IMAGE_PNG_VALUE, img.contentLength(), img.getInputStream()), "file");
                logs.addAll(errorLogs.stream().map(msg -> {
                    Log log = new Log();
                    log.setLevel(ERROR);
                    log.setLogTime(new Date());
                    log.setTestItemRef(itemId);
                    log.setLogMsg(msg);
                    final BinaryContent binaryContent = new BinaryContent(file, file, IMAGE_PNG_VALUE);
                    log.setBinaryContent(binaryContent);
                    return log;
                }).collect(toList()));
			}
			return logRepository.save(logs);
		} catch (IOException e) {
			throw new ReportPortalException("Unable to generate demo logs", e);
		}
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
