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

import com.epam.ta.reportportal.core.imprt.impl.DateUtils;
import com.epam.ta.reportportal.core.imprt.impl.ImportStrategy;
import com.epam.ta.reportportal.core.launch.IFinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.IStartLaunchHandler;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class AsyncJunitImportStrategy implements ImportStrategy {

    @Autowired
    private Provider<JunitParseJob> xmlParseJobProvider;

    @Autowired
    private IStartLaunchHandler startLaunchHandler;

    @Autowired
    private IFinishLaunchHandler finishLaunchHandler;

    @Autowired
    private LaunchRepository launchRepository;

    private static final Date initalStartTime = new Date(0);

    private static final ExecutorService service = Executors.newFixedThreadPool(5);

    private static final String XML_REGEX = ".*xml";

    private static final String ZIP = ".zip";

    private static final Predicate<ZipEntry> isFile = zipEntry -> !zipEntry.isDirectory();

    private static final Predicate<ZipEntry> isXml = zipEntry -> zipEntry.getName().matches(XML_REGEX);

    @Override
    public String importLaunch(String projectId, String userName, MultipartFile file) {
        try {
            File tmp = File.createTempFile(file.getName(), ZIP);
            file.transferTo(tmp);
            String launchId = startLaunch(projectId, userName,
                    file.getOriginalFilename().substring(0, file.getOriginalFilename().indexOf(ZIP)));
            ParseResults results = processZipFile(tmp, projectId, userName, launchId);
            finishLaunch(launchId, projectId, userName, results);
            return launchId;
        } catch (IOException e) {
            throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, file.getOriginalFilename(), e);
        }
    }

    private ParseResults processZipFile(File zip, String projectId, String userName, String launchId) throws IOException {
        try (ZipFile zipFile = new ZipFile(zip)) {
            CompletableFuture[] futures = zipFile.stream()
                    .filter(isFile.and(isXml))
                    .map(zipEntry -> {
                        try {
                            JunitParseJob job = xmlParseJobProvider.get()
                                    .withParameters(projectId, launchId, userName, zipFile.getInputStream(zipEntry));
                            return CompletableFuture.supplyAsync(job::call, service);
                        } catch (IOException e) {
                            throw new ReportPortalException("There was a problem while parsing file : " + zipEntry.getName(), e);
                        }
                    }).toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(futures).get(5, TimeUnit.MINUTES);
            return processResults(futures);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "There are invalid xml files.", e);
        }
    }

    private ParseResults processResults(CompletableFuture[] futures) {
        ParseResults results = new ParseResults();
        Arrays.stream(futures).map(it -> (ParseResults) it.join()).forEach(res -> {
            results.checkAndSetStartLaunchTime(res.getStartTime());
            results.increaseDuration(res.getDuration());
        });
        return results;
    }

    private String startLaunch(String projectId, String userName, String launchName) {
        StartLaunchRQ startLaunchRQ = new StartLaunchRQ();
        startLaunchRQ.setStartTime(initalStartTime);
        startLaunchRQ.setName(launchName);
        startLaunchRQ.setMode(Mode.DEFAULT);
        return startLaunchHandler.startLaunch(userName, projectId, startLaunchRQ).getId();
    }

    private void finishLaunch(String launchId, String projectId, String userName, ParseResults results) {
        FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
        finishExecutionRQ.setEndTime(results.getEndTime());
        finishLaunchHandler.finishLaunch(launchId, finishExecutionRQ, projectId, userName);
        Launch launch = launchRepository.findOne(launchId);
        launch.setStartTime(DateUtils.toDate(results.getStartTime()));
        launchRepository.partialUpdate(launch);
    }
}
