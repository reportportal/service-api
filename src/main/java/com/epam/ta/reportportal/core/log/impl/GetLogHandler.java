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
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.log.IGetLogHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.util.ProjectUtils;
import com.epam.ta.reportportal.ws.converter.converters.LogConverter;
import com.epam.ta.reportportal.ws.model.log.LogResource;
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

	private final LogRepository logRepository;

	private final TestItemRepository testItemRepository;

	private final LaunchRepository launchRepository;

	public GetLogHandler(LogRepository logRepository, TestItemRepository testItemRepository, LaunchRepository launchRepository) {
		this.logRepository = logRepository;
		this.testItemRepository = testItemRepository;
		this.launchRepository = launchRepository;
	}

	@Override
	public Iterable<LogResource> getLogs(Long testStepId, ReportPortalUser.ProjectDetails projectDetails, Filter filterable,
			Pageable pageable) {
		// DO we need filter for project here?
		//		Page<Log> logs = logRepository.findByFilter(filterable, pageable);
		//		return logResourceAssembler.toPagedResources(logs);
		throw new UnsupportedOperationException("No implementation");
	}

	@Override
	public long getPageNumber(Long logId, ReportPortalUser.ProjectDetails projectDetails, Filter filterable, Pageable pageable) {
		//		return logRepository.getPageNumber(logId, filterable, pageable);
		throw new UnsupportedOperationException("No implementation");
	}

	@Override
	public LogResource getLog(Long logId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {

		Log log = findAndValidate(logId, projectDetails, user);

		return LogConverter.TO_RESOURCE.apply(log);
	}

	/**
	 * Validate log item on existence, availability under specified project,
	 * etc.
	 *
	 * @param logId          - log ID
	 * @param projectDetails Project details
	 * @return Log - validate Log item in accordance with specified ID
	 */
	private Log findAndValidate(Long logId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {

		Log log = logRepository.findById(logId).orElse(null);
		expect(log, notNull()).verify(LOG_NOT_FOUND, logId);

		final TestItem testItem = log.getTestItem();
		Launch launch = testItem.getLaunch();

		expect(launch.getProjectId(), equalTo(projectDetails.getProjectId())).verify(FORBIDDEN_OPERATION,
				formattedSupplier("Log '{}' not under specified '{}' project", logId, projectDetails.getProjectId())
		);

		return log;
	}
}
