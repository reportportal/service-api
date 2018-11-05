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
package com.epam.ta.reportportal.core.imprt.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.imprt.impl.junit.XunitParseJob;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Provider;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.epam.ta.reportportal.core.imprt.FileExtensionConstant.XML_EXTENSION;
import static com.epam.ta.reportportal.core.imprt.FileExtensionConstant.ZIP_EXTENSION;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class ZipImportStrategy extends AbstractImportStrategy {
	private static final Predicate<ZipEntry> isFile = zipEntry -> !zipEntry.isDirectory();
	private static final Predicate<ZipEntry> isXml = zipEntry -> zipEntry.getName().endsWith(XML_EXTENSION);

	@Autowired
	private Provider<XunitParseJob> xmlParseJobProvider;

	@Override
	public Long importLaunch(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, File file) {
		try {
			return processZipFile(file, projectDetails, user);
		} finally {
			try {
				ofNullable(file).ifPresent(File::delete);
			} catch (Exception e) {
				LOGGER.error("File '{}' was not successfully deleted.", file.getName(), e);
			}
		}
	}

	private Long processZipFile(File zip, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		//copy of the launch's id to use it in catch block if something goes wrong
		Long savedLaunchId = null;
		try (ZipFile zipFile = new ZipFile(zip)) {
			Long launchId = startLaunch(projectDetails, user, zip.getName().substring(0, zip.getName().indexOf(ZIP_EXTENSION)));
			savedLaunchId = launchId;
			CompletableFuture[] futures = zipFile.stream().filter(isFile.and(isXml)).map(zipEntry -> {
				XunitParseJob job = xmlParseJobProvider.get()
						.withParameters(projectDetails, launchId, user, getEntryStream(zipFile, zipEntry));
				return CompletableFuture.supplyAsync(job::call, service);
			}).toArray(CompletableFuture[]::new);
			ParseResults parseResults = processResults(futures);
			finishLaunch(launchId, projectDetails, user, parseResults);
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
}
