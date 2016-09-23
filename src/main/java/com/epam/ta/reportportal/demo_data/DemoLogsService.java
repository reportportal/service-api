package com.epam.ta.reportportal.demo_data;

import static com.epam.ta.reportportal.database.entity.LogLevel.*;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
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

	public static final String CONTENT_TYPE = "image/png";
	public static final String IMAGE = "img.png";
	public static final String DEMO_LOGS = "demo_logs.txt";
	public static final String ERROR_LOGS = "error_logs.txt";
	private Random random;
	private LogRepository logRepository;
	private DataStorage dataStorage;

	@Autowired
	DemoLogsService(DataStorage dataStorage, LogRepository logRepository) {
		this.dataStorage = dataStorage;
		this.logRepository = logRepository;
		this.random = new Random();
	}

	List<Log> generateDemoLogs(String itemId, String status) {
		try {
			URL errorLogsUrl = this.getClass().getClassLoader().getResource(ERROR_LOGS);
			if (errorLogsUrl == null) {
				throw new ReportPortalException("Unable to find file with error logs");
			}
			List<String> errorLogs = Files.readAllLines(Paths.get(errorLogsUrl.toURI()));
			URL resource = this.getClass().getClassLoader().getResource(DEMO_LOGS);
			if (resource == null) {
				throw new ReportPortalException("Unable to find file with demo logs");
			}
			List<String> logMessages = Files.readAllLines(Paths.get(resource.toURI()));
			int t = random.nextInt(30);
			List<Log> logs = IntStream.range(1, t + 1).mapToObj(it -> {
				Log log = new Log();
				log.setLevel(logLevel());
				log.setLogTime(new Date());
				log.setTestItemRef(itemId);
				log.setLogMsg(logMessages.get(random.nextInt(logMessages.size())));
				return log;
			}).collect(toList());
			if ("FAILED".equals(status)) {
				File img = new File(this.getClass().getClassLoader().getResource(IMAGE).toURI());
				InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(IMAGE);
				String file = dataStorage.saveData(new BinaryData(CONTENT_TYPE, img.length(), resourceAsStream), "file");
				logs.addAll(errorLogs.stream().map(msg -> {
					Log log = new Log();
					log.setLevel(ERROR);
					log.setLogTime(new Date());
					log.setTestItemRef(itemId);
					log.setLogMsg(msg);
					log.setBinaryContent(new BinaryContent(file, file, CONTENT_TYPE));
					return log;
				}).collect(toList()));
			}
			return logRepository.save(logs);
		} catch (URISyntaxException | IOException e) {
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
