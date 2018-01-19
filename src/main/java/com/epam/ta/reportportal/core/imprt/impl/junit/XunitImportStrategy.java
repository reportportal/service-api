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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class XunitImportStrategy implements ImportStrategy {

	private static final Logger LOGGER = LoggerFactory.getLogger(XunitImportStrategy.class);

	private static final Date initialStartTime = new Date(0);
	private static final ExecutorService service = Executors.newFixedThreadPool(5);
	private static final String XML_REGEX = ".*xml";
	private static final Predicate<ZipEntry> isFile = zipEntry -> !zipEntry.isDirectory();
	private static final Predicate<ZipEntry> isXml = zipEntry -> zipEntry.getName().matches(XML_REGEX);

	@Autowired
	private Provider<XunitParseJob> xmlParseJobProvider;

	@Autowired
	private IStartLaunchHandler startLaunchHandler;

	@Autowired
	private IFinishLaunchHandler finishLaunchHandler;

	@Autowired
	private LaunchRepository launchRepository;

	@Override
	public String importLaunch(String projectId, String userName, File file) {
		try {
			return processZipFile(file, projectId, userName);
		} finally {
			try {
				if (null != file) {
					file.delete();
				}
			} catch (Exception e) {
				LOGGER.error("File '{}' was not successfully deleted.", file.getName(), e);
			}
		}
	}

	private String processZipFile(File zip, String projectId, String userName) {
		//copy of the launch's id to use it in catch block if something goes wrong
		String savedLaunchId = null;
		try (ZipFile zipFile = new ZipFile(zip)) {
			String launchId = startLaunch(projectId, userName, zip.getName().substring(0, zip.getName().indexOf(".zip")));
			savedLaunchId = launchId;
			CompletableFuture[] futures = zipFile.stream().filter(isFile.and(isXml)).map(zipEntry -> {
				XunitParseJob job = xmlParseJobProvider.get()
						.withParameters(projectId, launchId, userName, getEntryStream(zipFile, zipEntry));
				return CompletableFuture.supplyAsync(job::call, service);
			}).toArray(CompletableFuture[]::new);
			ParseResults parseResults = processResults(futures);
			finishLaunch(launchId, projectId, userName, parseResults);
			return launchId;
		} catch (Exception e) {
			updateBrokenLaunch(savedLaunchId);
			throw new ReportPortalException(ErrorType.IMPORT_FILE_ERROR, cleanMessage(e));
		}
	}

	private InputStream getEntryStream(ZipFile file, ZipEntry zipEntry) {
		try {
			return file.getInputStream(zipEntry);
		} catch (IOException e) {
			throw new ReportPortalException(ErrorType.IMPORT_FILE_ERROR, e.getMessage());
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
		startLaunchRQ.setStartTime(initialStartTime);
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

	/**
	 * Got a cause exception message if it has any.
	 *
	 * @param e Exception
	 * @return Clean exception message
	 */
	private String cleanMessage(Exception e) {
		if (e.getCause() != null) {
			return e.getCause().getMessage();
		}
		return e.getMessage();
	}

	/*
	 * if the importing results do not contain initial timestamp a launch gets
	 * a default date if the launch is broken, time should be updated to not to broke
	 * the statistics
	 */
	private void updateBrokenLaunch(String savedLaunchId) {
		if (savedLaunchId != null) {
			Launch launch = new Launch();
			launch.setId(savedLaunchId);
			launch.setStatistics(null);
			launch.setStartTime(Calendar.getInstance().getTime());
			launchRepository.partialUpdate(launch);
		}
	}
}
