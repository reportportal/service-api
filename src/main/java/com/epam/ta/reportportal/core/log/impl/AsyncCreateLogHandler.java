/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.log.impl;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.job.SaveBinaryDataJob;
import com.epam.ta.reportportal.ws.converter.builders.LogBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.inject.Provider;

/**
 * Asynchronous implementation of {@link CreateLogHandler}. Saves log and
 * returns response, also starts saving binary data in storage asynchronously to
 * decrease server response timesaveLogsTaskExecutor
 *
 * @author Andrei Varabyeu
 */
@Service("asyncCreateLogHandler")
public class AsyncCreateLogHandler extends CreateLogHandler {

	/**
	 * We are using {@link Provider} there because we need
	 * {@link SaveBinaryDataJob} with scope prototype. Since current class is in
	 * singleton scope, we have to find a way to get new instance of job for new
	 * execution
	 */
	@Autowired
	private Provider<SaveBinaryDataJob> saveBinaryDataJob;

	@Autowired
	@Qualifier("saveLogsTaskExecutor")
	private TaskExecutor taskExecutor;

	@Override
	@Nonnull
	//TODO check saving an attachment of the item of the project A in the project's B directory
	public EntryCreatedRS createLog(@Nonnull SaveLogRQ createLogRQ, MultipartFile file, ReportPortalUser.ProjectDetails projectDetails) {

		TestItem testItem = testItemRepository.findById(createLogRQ.getTestItemId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, createLogRQ.getTestItemId()));
		validate(testItem, createLogRQ);

		Log log = new LogBuilder().addSaveLogRq(createLogRQ).addTestItem(testItem).get();
		logRepository.save(log);

		if (null != file) {

			Long launchId = getLaunchId(testItem);

			taskExecutor.execute(saveBinaryDataJob.get()
					.withFile(file)
					.withProjectId(projectDetails.getProjectId())
					.withLaunchId(launchId)
					.withItemId(testItem.getItemId())
					.withLogId(log.getId()));

		}

		return new EntryCreatedRS(log.getId());
	}
}
