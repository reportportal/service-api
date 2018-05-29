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

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.store.commons.EntityUtils;
import com.epam.ta.reportportal.store.database.dao.LaunchRepository;
import com.epam.ta.reportportal.store.database.dao.LogRepository;
import com.epam.ta.reportportal.store.database.dao.TestItemRepository;
import com.epam.ta.reportportal.store.database.entity.item.TestItem;
import com.epam.ta.reportportal.store.database.entity.launch.Launch;
import com.epam.ta.reportportal.store.database.entity.log.Log;
import com.epam.ta.reportportal.ws.converter.LogResourceAssembler;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.store.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.store.commons.Predicates.notNull;
import static com.epam.ta.reportportal.ws.model.ErrorType.FORBIDDEN_OPERATION;
import static com.epam.ta.reportportal.ws.model.ErrorType.LOG_NOT_FOUND;

/**
 * Implementation of GET log operations
 *
 * @author Andrei Varabyeu
 * @author Andrei_Ramanchuk
 */
@Service
public class GetLogHandler {

	private final LogRepository logRepository;

	private final LogResourceAssembler logResourceAssembler;

	private final TestItemRepository testItemRepository;

	private final LaunchRepository launchRepository;

	public GetLogHandler(LogRepository logRepository, LogResourceAssembler logResourceAssembler, TestItemRepository testItemRepository,
			LaunchRepository launchRepository) {
		this.logRepository = logRepository;
		this.logResourceAssembler = logResourceAssembler;
		this.testItemRepository = testItemRepository;
		this.launchRepository = launchRepository;
	}

	//		public Iterable<LogResource> getLogs(String testStepId, String project, Filter filterable, Pageable pageable) {
	//			// DO we need filter for project here?
	//			Page<Log> logs = logRepository.findByFilter(filterable, pageable);
	//			return logResourceAssembler.toPagedResources(logs);
	//		}
	//
	//	@Override
	//	public long getPageNumber(String logId, String project, Filter filterable, Pageable pageable) {
	//		return logRepository.getPageNumber(logId, filterable, pageable);
	//	}
	//
	public LogResource getLog(String logId, String projectName, ReportPortalUser user) {

		Log log = findAndValidate(logId, projectName, user);

		return logResourceAssembler.toResource(log);
	}

	/**
	 * Validate log item on existence, availability under specified project,
	 * etc.
	 *
	 * @param logId       - log ID
	 * @param projectName - project name value
	 * @return Log - validate Log item in accordance with specified ID
	 */
	private Log findAndValidate(String logId, String projectName, ReportPortalUser user) {

		Log log = logRepository.findById(Long.valueOf(logId)).orElse(null);
		expect(log, notNull()).verify(LOG_NOT_FOUND, logId);

		final TestItem testItem = log.getTestItem();
		Launch launch = testItem.getLaunch();

		ReportPortalUser.ProjectDetails projectDetails = EntityUtils.takeProjectDetails(user, projectName);
		expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(FORBIDDEN_OPERATION,
				formattedSupplier("Log '{}' not under specified '{}' project", logId, projectName)
		);

		return log;
	}
}
