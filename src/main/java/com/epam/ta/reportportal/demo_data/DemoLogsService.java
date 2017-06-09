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
import java.util.Date;
import java.util.List;
import java.util.SplittableRandom;
import java.util.stream.IntStream;

import static com.epam.ta.reportportal.database.entity.LogLevel.*;
import static com.epam.ta.reportportal.database.entity.Status.FAILED;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.*;

@Service
class DemoLogsService {
    private SplittableRandom random;

    private LogRepository logRepository;

    private DataStorage dataStorage;

    @Autowired
    DemoLogsService(DataStorage dataStorage, LogRepository logRepository) {
        this.dataStorage = dataStorage;
        this.logRepository = logRepository;
        this.random = new SplittableRandom();
    }

    List<Log> generateDemoLogs(String itemId, String status) {
        int t = random.nextInt(30) + 5;
        List<Log> logs = IntStream.range(1, t + 1).mapToObj(it -> {
            Log log = new Log();
            log.setLevel(logLevel());
            log.setLogTime(new Date());
            if (random.nextInt(t) <= t / 8) {
                log.setBinaryContent(attachBinaryContent());
            }
            log.setTestItemRef(itemId);
            log.setLogMsg(ContentUtils.getLogMessage());
            return log;
        }).collect(toList());
        if (FAILED.name().equals(status)) {
            List<String> errors = ContentUtils.getErrorLogs(2);
            logs.addAll(errors.stream().map(msg -> {
                Log log = new Log();
                log.setLevel(ERROR);
                log.setLogTime(new Date());
                log.setTestItemRef(itemId);
                log.setLogMsg(msg);
                BinaryContent binaryContent = attachBinaryContent();
                log.setBinaryContent(binaryContent);
                return log;
            }).collect(toList()));
        }
        return logRepository.save(logs);
    }

    private BinaryContent attachBinaryContent() {
        ClassPathResource resource;
        String contentType;
        switch (random.nextInt(20)) {
            case 0:
                contentType = TEXT_PLAIN_VALUE;
                resource = new ClassPathResource("demo/attachments/Test.cmd");
                break;
            case 1:
                contentType = "text/css";
                resource = new ClassPathResource("demo/attachments/css.css");
                break;
            case 2:
                contentType = "text/csv";
                resource = new ClassPathResource("demo/attachments/Test.csv");
                break;
            case 3:
                contentType = TEXT_HTML_VALUE;
                resource = new ClassPathResource("demo/attachments/html.html");
                break;
            case 4:
                contentType = "application/javascript";
                resource = new ClassPathResource("demo/attachments/javascript.js");
                break;
            case 5:
                contentType = APPLICATION_PDF_VALUE;
                resource = new ClassPathResource("demo/attachments/test.pdf");
                break;
            case 6:
                contentType = "text/x-php";
                resource = new ClassPathResource("demo/attachments/php.php");
                break;
            case 7:
                contentType = TEXT_PLAIN_VALUE;
                resource = new ClassPathResource("demo/attachments/plain.txt");
                break;
            case 8:
                contentType = "application/zip";
                resource = new ClassPathResource("demo/attachments/demo.zip");
                break;
            case 9:
                contentType = APPLICATION_JSON_VALUE;
                resource = new ClassPathResource("demo/demo_widgets.json");
                break;
            default:
                contentType = IMAGE_PNG_VALUE;
                resource = new ClassPathResource("demo/attachments/img.png");
        }
        String file;
        try {
            file = saveResource(contentType, resource);
        } catch (IOException e) {
            throw new ReportPortalException("Unable to save binary data", e);
        }
        return new BinaryContent(file, file, contentType);
    }

    private String saveResource(String contentType, ClassPathResource resource) throws IOException {
        return dataStorage.saveData(new BinaryData(contentType,
                resource.contentLength(), resource.getInputStream()), "file");
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
