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

import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.log.ICreateLogHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.commons.Preconditions;
import com.epam.ta.reportportal.store.commons.Predicates;
import com.epam.ta.reportportal.store.database.dao.LogRepository;
import com.epam.ta.reportportal.store.database.dao.TestItemRepository;
import com.epam.ta.reportportal.store.database.entity.enums.LogLevel;
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.store.database.entity.log.Log;
import com.epam.ta.reportportal.ws.converter.builders.LogBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;

/**
 * Create log handler. Save log and binary data related to it
 *
 * @author Henadzi Vrubleuski
 * @author Andrei Varabyeu
 */
public class CreateLogHandler implements ICreateLogHandler {

	protected TestItemRepository testItemRepository;

	protected LogRepository logRepository;

	//private DataStorage dataStorage;

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Autowired
	public void setLogRepository(LogRepository logRepository) {
		this.logRepository = logRepository;
	}

	//	@Autowired
	//	public void setDataStorage(DataStorage dataStorage) {
	//		this.dataStorage = dataStorage;
	//	}

	@Override
	@Nonnull
	public EntryCreatedRS createLog(@Nonnull SaveLogRQ createLogRQ, MultipartFile file, String project) {
		TestItem testItem = testItemRepository.findById(createLogRQ.getTestItemId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, createLogRQ.getTestItemId()));
		validate(testItem, createLogRQ);

		//TODO implement save binary content
		//		BinaryContent binaryContent = null;
		//		if (null != file) {
		//			try {
		//				String binaryDataId = dataStorage.saveData(new BinaryData(file.getContentType(), file.getSize(), file.getInputStream()),
		//						file.getOriginalFilename()
		//				);
		//				binaryContent = new BinaryContent(binaryDataId, null, file.getContentType());
		//			} catch (IOException e) {
		//				throw new ReportPortalException(ErrorType.INCORRECT_REQUEST, "Unable to save log");
		//			}
		//		}
		Log log = new LogBuilder().addSaveLogRq(createLogRQ).get();
		log.setTestItem(testItem);
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
