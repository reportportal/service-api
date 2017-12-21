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

package com.epam.ta.reportportal.core.log.impl;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.log.ICreateLogHandler;
import com.epam.ta.reportportal.database.BinaryData;
import com.epam.ta.reportportal.database.DataStorage;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.BinaryContent;
import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.entity.LogLevel;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.util.RetryId;
import com.epam.ta.reportportal.ws.converter.builders.LogBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.inject.Provider;
import java.io.IOException;
import java.util.Optional;

/**
 * Create log handler. Save log and binary data related to it
 *
 * @author Henadzi Vrubleuski
 * @author Andrei Varabyeu
 */
public class CreateLogHandler implements ICreateLogHandler {

	protected TestItemRepository testItemRepository;

	protected LogRepository logRepository;

	private DataStorage dataStorage;

	protected Provider<LogBuilder> logBuilder;


	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Autowired
	public void setLogRepository(LogRepository logRepository) {
		this.logRepository = logRepository;
	}

	@Autowired
	public void setDataStorage(DataStorage dataStorage) {
		this.dataStorage = dataStorage;
	}

	@Autowired
	public void setLogBuilder(Provider<LogBuilder> logBuilder) {
		this.logBuilder = logBuilder;
	}

	@Override
	@Nonnull
	public EntryCreatedRS createLog(@Nonnull SaveLogRQ createLogRQ, MultipartFile file, String project) {
		Optional<TestItem> testItem = findTestItem(createLogRQ.getTestItemId());
		validate(testItem.orElse(null), createLogRQ);

		BinaryContent binaryContent = null;
		if (null != file) {
			try {
				String binaryDataId = dataStorage.saveData(new BinaryData(file.getContentType(), file.getSize(), file.getInputStream()),
						file.getOriginalFilename()
				);
				binaryContent = new BinaryContent(binaryDataId, null, file.getContentType());
			} catch (IOException e) {
				throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Unable to save log");
			}

		}
		Log log = logBuilder.get().addSaveLogRQ(createLogRQ).addBinaryContent(binaryContent).addTestItem(testItem.get()).build();
		try {
			logRepository.save(log);
		} catch (Exception exc) {
			throw new ReportPortalException("Error while Log instance creating.", exc);
		}
		return new EntryCreatedRS(log.getId());
	}

	/**
	 * Validates business rules related to test item of this log
	 *
	 * @param testItem
	 */
	protected void validate(TestItem testItem, SaveLogRQ saveLogRQ) {
		BusinessRule.expect(testItem, Predicates.notNull())
				.verify(ErrorType.LOGGING_IS_NOT_ALLOWED,
						Suppliers.formattedSupplier("Logging to test item '{}' is not allowed. Probably you try to log for Launch type.",
								saveLogRQ.getTestItemId()
						)
				);

		//removed as part of EPMRPP-23459
		//		BusinessRule.expect(testItem, Preconditions.IN_PROGRESS).verify(ErrorType.REPORTING_ITEM_ALREADY_FINISHED, testItem.getId());
		//		BusinessRule.expect(testItem.hasChilds(), Predicates.equalTo(Boolean.FALSE)).verify(ErrorType.LOGGING_IS_NOT_ALLOWED,
		//				Suppliers.formattedSupplier("Logging to item '{}' with descendants is not permitted", testItem.getId()));

		BusinessRule.expect(
				testItem.getStartTime().before(saveLogRQ.getLogTime()) || testItem.getStartTime().equals(saveLogRQ.getLogTime()),
				Predicates.equalTo(Boolean.TRUE)
		).verify(ErrorType.LOGGING_IS_NOT_ALLOWED,
				Suppliers.formattedSupplier("Log has incorrect log time. Log time should be after parent item's start time.")
		);

		BusinessRule.expect(LogLevel.toLevelOrUnknown(saveLogRQ.getLevel()), Predicates.notNull()).verify(ErrorType.BAD_SAVE_LOG_REQUEST,
				Suppliers.formattedSupplier("Cannot convert '{}' to valid 'LogLevel'", saveLogRQ.getLevel())
		);
	}

	protected Optional<TestItem> findTestItem(String id) {
		Optional<TestItem> testItem;
		if (RetryId.isRetry(id)) {
			testItem = testItemRepository.findRetry(id);
		} else {
			testItem = Optional.ofNullable(testItemRepository.findOne(id));
		}
		return testItem;
	}
}
