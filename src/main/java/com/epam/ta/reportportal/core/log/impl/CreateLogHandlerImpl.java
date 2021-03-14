/*
 * Copyright 2019 EPAM Systems
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
import com.epam.ta.reportportal.core.item.TestItemService;
import com.epam.ta.reportportal.core.log.CreateLogHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.attachment.AttachmentMetaInfo;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.LogBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedAsyncRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.inject.Provider;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static java.util.Optional.ofNullable;

/**
 * Create log handler. Save log and binary data related to it
 *
 * @author Henadzi Vrubleuski
 * @author Andrei Varabyeu
 */
@Service
@Primary
@Transactional
public class CreateLogHandlerImpl implements CreateLogHandler {

	@Autowired
	TestItemRepository testItemRepository;

	@Autowired
	TestItemService testItemService;

	@Autowired
	LaunchRepository launchRepository;

	@Autowired
	LogRepository logRepository;

	/**
	 * We are using {@link Provider} there because we need
	 * {@link SaveLogBinaryDataTask} with scope prototype. Since current class is in
	 * singleton scope, we have to find a way to get new instance of job for new
	 * execution
	 */
	@Autowired
	private Provider<SaveLogBinaryDataTask> saveLogBinaryDataTask;

	@Autowired
	@Qualifier("saveLogsTaskExecutor")
	private TaskExecutor taskExecutor;

	@Override
	@Nonnull
	//TODO check saving an attachment of the item of the project A in the project's B directory
	public EntryCreatedAsyncRS createLog(@Nonnull SaveLogRQ request, MultipartFile file, ReportPortalUser.ProjectDetails projectDetails) {
		validate(request);

		final LogBuilder logBuilder = new LogBuilder().addSaveLogRq(request).addProjectId(projectDetails.getProjectId());

		final Launch launch = testItemRepository.findByUuid(request.getItemUuid()).map(item -> {
			logBuilder.addTestItem(item);
			return testItemService.getEffectiveLaunch(item);
		}).orElseGet(() -> launchRepository.findByUuid(request.getLaunchUuid()).map(l -> {
			logBuilder.addLaunch(l);
			return l;
		}).orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, request.getLaunchUuid())));

		final Log log = logBuilder.get();
		logRepository.save(log);

		ofNullable(file).ifPresent(f -> saveBinaryData(f, launch, log));

		return new EntryCreatedAsyncRS(log.getUuid());

	}

	private void saveBinaryData(MultipartFile file, Launch launch, Log log) {

		final AttachmentMetaInfo.AttachmentMetaInfoBuilder metaInfoBuilder = AttachmentMetaInfo.builder()
				.withProjectId(launch.getProjectId())
				.withLaunchId(launch.getId())
				.withLaunchUuid(launch.getUuid())
				.withLogId(log.getId())
				.withLogUuid(log.getUuid())
				.withCreationDate(LocalDateTime.now(ZoneOffset.UTC));
		ofNullable(log.getTestItem()).map(TestItem::getItemId).ifPresent(metaInfoBuilder::withItemId);

		SaveLogBinaryDataTask saveLogBinaryDataTask = this.saveLogBinaryDataTask.get()
				.withFile(file)
				.withAttachmentMetaInfo(metaInfoBuilder.build());

		taskExecutor.execute(saveLogBinaryDataTask);

	}

}
