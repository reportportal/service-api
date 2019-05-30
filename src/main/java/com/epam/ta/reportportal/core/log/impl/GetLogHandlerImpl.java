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
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.core.log.GetLogHandler;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.PagedResourcesAssembler;
import com.epam.ta.reportportal.ws.converter.converters.LogConverter;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.FORBIDDEN_OPERATION;
import static com.epam.ta.reportportal.ws.model.ErrorType.LOG_NOT_FOUND;
import static java.util.Optional.ofNullable;

/**
 * Implementation of GET log operations
 *
 * @author Andrei Varabyeu
 * @author Andrei_Ramanchuk
 */
@Service
public class GetLogHandlerImpl implements GetLogHandler {

	private final LogRepository logRepository;

	public GetLogHandlerImpl(LogRepository logRepository) {
		this.logRepository = logRepository;
	}

	@Override
	public Iterable<LogResource> getLogs(ReportPortalUser.ProjectDetails projectDetails, Filter filterable, Pageable pageable) {
		Page<Log> logPage = logRepository.findByFilter(filterable, pageable);
		return PagedResourcesAssembler.pageConverter(LogConverter.TO_RESOURCE).apply(logPage);
	}

	@Override
	public long getPageNumber(Long logId, ReportPortalUser.ProjectDetails projectDetails, Filter filterable, Pageable pageable) {
		return logRepository.getPageNumber(logId, filterable, pageable);
	}

	@Override
	public LogResource getLog(Long logId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		return LogConverter.TO_RESOURCE.apply(findAndValidate(logId, projectDetails, user));
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
		Log log = logRepository.findById(logId).orElseThrow(() -> new ReportPortalException(LOG_NOT_FOUND, logId));

		Long launchProjectId = ofNullable(log.getTestItem()).map(it -> it.getLaunch().getProjectId())
				.orElseGet(() -> log.getLaunch().getProjectId());

		expect(launchProjectId, equalTo(projectDetails.getProjectId())).verify(
				FORBIDDEN_OPERATION,
				formattedSupplier("Log '{}' not under specified '{}' project", logId, projectDetails.getProjectId())
		);

		return log;
	}
}
