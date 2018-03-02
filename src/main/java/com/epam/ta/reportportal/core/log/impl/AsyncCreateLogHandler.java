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

import org.springframework.stereotype.Service;

/**
 * Asynchronous implementation of {@link ICreateLogHandler}. Saves log and
 * returns response, also starts saving binary data in storage asynchronously to
 * decrease server response time
 *
 * @author Andrei Varabyeu
 */
@Service
public class AsyncCreateLogHandler extends CreateLogHandler /*implements ICreateLogHandler*/ {
	//
	//	/**
	//	 * We are using {@link Provider} there because we need
	//	 * {@link SaveBinaryDataJob} with scope prototype. Since current class is in
	//	 * singleton scope, we have to find a way to get new instance of job for new
	//	 * execution
	//	 */
	//	@Autowired
	//	private Provider<SaveBinaryDataJob> saveBinaryDataJob;
	//
	//	@Autowired
	//	@Qualifier("saveLogsTaskExecutor")
	//	private TaskExecutor taskExecutor;
	//
	//	@Autowired
	//	private ILogIndexer logIndexer;
	//
	//	@Override
	//	@Nonnull
	//	public EntryCreatedRS createLog(@Nonnull SaveLogRQ createLogRQ, MultipartFile file, String projectName) {
	//		Optional<TestItem> testItem = findTestItem(createLogRQ.getTestItemId());
	//		validate(testItem.orElse(null), createLogRQ);
	//
	//		Log log = logBuilder.get().addSaveLogRQ(createLogRQ).addTestItem(testItem.get()).build();
	//		try {
	//			logRepository.save(log);
	//		} catch (Exception exc) {
	//			throw new ReportPortalException("Error while Log instance creating.", exc);
	//		}
	//		if (null != file) {
	//			taskExecutor.execute(saveBinaryDataJob.get().withProject(projectName).withFile(file).withLog(log));
	//		}
	//		return new EntryCreatedRS(log.getId());
	//	}
}
