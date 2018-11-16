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

package com.epam.ta.reportportal.core.log.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.commons.BinaryDataMetaInfo;
import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.log.ICreateLogHandler;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.LogBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import java.util.Optional;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;

/**
 * Create log handler. Save log and binary data related to it
 *
 * @author Henadzi Vrubleuski
 * @author Andrei Varabyeu
 */
@Service("createLogHandler")
public class CreateLogHandler implements ICreateLogHandler {

	protected TestItemRepository testItemRepository;

	protected LogRepository logRepository;

	private DataStoreService dataStoreService;

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Autowired
	public void setLogRepository(LogRepository logRepository) {
		this.logRepository = logRepository;
	}

	@Autowired
	public void setDataStoreService(DataStoreService dataStoreService) {
		this.dataStoreService = dataStoreService;
	}

	@Override
	@Nonnull
	public EntryCreatedRS createLog(@Nonnull SaveLogRQ createLogRQ, MultipartFile file, ReportPortalUser.ProjectDetails projectDetails) {

		TestItem testItem = testItemRepository.findById(createLogRQ.getTestItemId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, createLogRQ.getTestItemId()));

		validate(testItem, createLogRQ);

		Log log = new LogBuilder().addSaveLogRq(createLogRQ).addTestItem(testItem).get();

		Optional<BinaryDataMetaInfo> maybeBinaryDataMetaInfo = dataStoreService.save(projectDetails.getProjectId(), file);
		maybeBinaryDataMetaInfo.ifPresent(binaryDataMetaInfo -> {

			log.setAttachment(maybeBinaryDataMetaInfo.get().getFileId());
			log.setContentType(file.getContentType());
		});

		logRepository.save(log);

		return new EntryCreatedRS(log.getId());
	}

	/**
	 * Validates business rules related to test item of this log
	 *
	 * @param testItem  Test item
	 * @param saveLogRQ Save log request
	 */
	protected void validate(TestItem testItem, SaveLogRQ saveLogRQ) {
		expect(saveLogRQ.getLogTime(), Preconditions.sameTimeOrLater(testItem.getStartTime())).verify(
				ErrorType.LOGGING_IS_NOT_ALLOWED,
				Suppliers.formattedSupplier("Log has incorrect log time. Log time should be after parent item's start time.")
		);
		expect(LogLevel.toLevelOrUnknown(saveLogRQ.getLevel()), Predicates.notNull()).verify(
				ErrorType.BAD_SAVE_LOG_REQUEST,
				Suppliers.formattedSupplier("Cannot convert '{}' to valid 'LogLevel'", saveLogRQ.getLevel())
		);
	}
}
