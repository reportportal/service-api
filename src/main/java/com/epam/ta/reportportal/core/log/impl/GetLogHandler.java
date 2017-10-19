/*
 * Copyright 2016 EPAM Systems
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

import com.epam.ta.reportportal.core.log.IGetLogHandler;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.converter.LogResourceAssembler;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.notNull;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.FORBIDDEN_OPERATION;
import static com.epam.ta.reportportal.ws.model.ErrorType.LOG_NOT_FOUND;

/**
 * Implementation of GET log operations
 *
 * @author Andrei Varabyeu
 * @author Andrei_Ramanchuk
 */
@Service
public class GetLogHandler implements IGetLogHandler {

	private LogRepository logRepository;

	private LogResourceAssembler logResourceAssembler;

	private TestItemRepository testItemRepository;

	private LaunchRepository launchRepository;

	@Autowired
	public void setLogRepository(LogRepository logRepository) {
		this.logRepository = logRepository;
	}

	@Autowired
	public void setLogResourceAssembler(LogResourceAssembler logResourceAssembler) {
		this.logResourceAssembler = logResourceAssembler;
	}

	@Autowired
	public void setTestItemRepository(TestItemRepository testItemRepository) {
		this.testItemRepository = testItemRepository;
	}

	@Autowired
	public void setLaunchRepository(LaunchRepository launchRepository) {
		this.launchRepository = launchRepository;
	}

	@Override
	public Iterable<LogResource> getLogs(String testStepId, String project, Filter filterable, Pageable pageable) {
		// DO we need filter for project here?
		Page<Log> logs = logRepository.findByFilter(filterable, pageable);
		return logResourceAssembler.toPagedResources(logs);
	}

	@Override
	public long getPageNumber(String logId, String project, Filter filterable, Pageable pageable) {
		return logRepository.getPageNumber(logId, filterable, pageable);
	}

	@Override
	public LogResource getLog(String logId, String projectName) {
		Log log = findAndValidate(logId, projectName);
		return logResourceAssembler.toResource(log);
	}

	/**
	 * Validate log item on existence, availability under specified project,
	 * etc.
	 *
	 * @param id          - log ID
	 * @param projectName - project name value
	 * @return Log - validate Log item in accordance with specified ID
	 */
	private Log findAndValidate(String id, String projectName) {
		Log log = logRepository.findOne(id);
		expect(log, notNull()).verify(LOG_NOT_FOUND, id);

		final TestItem testItem = testItemRepository.findOne(log.getTestItemRef());
		String project = launchRepository.findOne(testItem.getLaunchRef()).getProjectRef();
		expect(project, equalTo(projectName)).verify(FORBIDDEN_OPERATION,
				formattedSupplier("Log '{}' is not under specified project '{}'", id, projectName)
		);
		return log;
	}
}
