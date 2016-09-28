package com.epam.ta.reportportal.demo_data;

import static com.epam.ta.reportportal.database.entity.LogLevel.*;
import static com.epam.ta.reportportal.database.entity.Status.FAILED;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.BinaryData;
import com.epam.ta.reportportal.database.DataStorage;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.entity.BinaryContent;
import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.entity.LogLevel;
import com.epam.ta.reportportal.exception.ReportPortalException;

@Service
class DemoLogsService {

	private Random random;
	private LogRepository logRepository;
	private DataStorage dataStorage;
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
		try {
			List<String> errorLogs = Files.readAllLines(Paths.get(errorLogsResource.getURI()));
			List<String> logMessages = Files.readAllLines(Paths.get(demoLogs.getURI()));
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
				String file = dataStorage.saveData(new BinaryData(IMAGE_PNG_VALUE, img.getFile().length(), img.getInputStream()), "file");
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
