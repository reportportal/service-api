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

import java.io.File;

/**
 * @author Anton Machulski
 */
public class XmlImportStrategy<T extends CallableImportJob> extends AbstractImportStrategy<T> {
	@Override
	public Long importLaunch(ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user, File file) {
		try {
			return processXmlFile(file, projectDetails, user);
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

	private Long processXmlFile(File xml, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		//copy of the launch's id to use it in catch block if something goes wrong
		//TODO: implement
//		Long savedLaunchId = null;
//		try (ZipFile zipFile = new ZipFile(zip)) {
//			Long launchId = startLaunch(projectDetails, user, zip.getName().substring(0, zip.getName().indexOf(ZIP_EXTENSION)));
//			savedLaunchId = launchId;
//			CompletableFuture[] futures = zipFile.stream().filter(isFile.and(isXml)).map(zipEntry -> {
//				CallableImportJob job = xmlParseJobProvider.get()
//						.withParameters(projectDetails, launchId, user, getEntryStream(zipFile, zipEntry));
//				return CompletableFuture.supplyAsync(job::call, service);
//			}).toArray(CompletableFuture[]::new);
//			ParseResults parseResults = processResults(futures);
//			finishLaunch(launchId, projectDetails, user, parseResults);
//			return launchId;
//		} catch (Exception e) {
//			updateBrokenLaunch(savedLaunchId);
//			throw new ReportPortalException(ErrorType.IMPORT_FILE_ERROR, cleanMessage(e));
//		}
		throw new UnsupportedOperationException("No implementation.");
	}

}
