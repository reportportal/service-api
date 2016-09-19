/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
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
import com.epam.ta.reportportal.util.LazyReference;
import com.epam.ta.reportportal.ws.converter.builders.LogBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;

import javax.annotation.Nonnull;

/**
 * Create log handler. Save log and binary data related to it
 * 
 * @author Henadzi Vrubleuski
 * @author Andrei Varabyeu
 * 
 */
public class CreateLogHandler implements ICreateLogHandler {

	protected TestItemRepository testItemRepository;

	protected LogRepository logRepository;

	private DataStorage dataStorage;

	protected LazyReference<LogBuilder> logBuilder;

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
	@Qualifier("logBuilder.reference")
	public void setLogBuilder(LazyReference<LogBuilder> logBuilder) {
		this.logBuilder = logBuilder;
	}

	@Override
	public EntryCreatedRS createLog(SaveLogRQ createLogRQ, BinaryData binaryData, String filename, String project) {
		TestItem testItem = testItemRepository.findOne(createLogRQ.getTestItemId());
		validate(testItem, createLogRQ);

		BinaryContent binaryContent = null;
		if (null != binaryData) {
			String binaryDataId	= dataStorage.saveData(binaryData, filename);
			binaryContent = new BinaryContent(binaryDataId, null, binaryData.getContentType());
		}

		Log log = logBuilder.get().addSaveLogRQ(createLogRQ).addBinaryContent(binaryContent).addTestItem(testItem).build();

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
		BusinessRule.expect(testItem, Predicates.notNull()).verify(ErrorType.LOGGING_IS_NOT_ALLOWED, Suppliers.formattedSupplier(
				"Logging to test item '{}' is not allowed. Probably you try to log for Launch type.", saveLogRQ.getTestItemId()));

		BusinessRule.expect(testItem, Preconditions.IN_PROGRESS).verify(ErrorType.REPORTING_ITEM_ALREADY_FINISHED, testItem.getId());

		BusinessRule.expect(testItem.hasChilds(), Predicates.equalTo(Boolean.FALSE)).verify(ErrorType.LOGGING_IS_NOT_ALLOWED,
				Suppliers.formattedSupplier("Logging to item '{}' with descendants is not permitted", testItem.getId()));

		BusinessRule.expect(testItem.getStartTime().before(saveLogRQ.getLogTime()), Predicates.equalTo(Boolean.TRUE)).verify(
				ErrorType.LOGGING_IS_NOT_ALLOWED,
				Suppliers.formattedSupplier("Log has incorrect log time. Log time should be after parent item's start time."));

		BusinessRule.expect(LogLevel.toLevelOrUnknown(saveLogRQ.getLevel()), Predicates.notNull()).verify(ErrorType.BAD_SAVE_LOG_REQUEST,
				Suppliers.formattedSupplier("Cannot convert '{}' to valid 'LogLevel'", saveLogRQ.getLevel()));
	}
}
