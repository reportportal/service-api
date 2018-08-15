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

package com.epam.ta.reportportal.core.log;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import org.springframework.data.domain.Pageable;

/**
 * GET operation for {@link Log} entity
 *
 * @author Andrei Varabyeu
 */
public interface IGetLogHandler {

	/**
	 * Returns logs for specified filter
	 *
	 * @param testStepId - parent step ID value
	 * @param filterable - filter definition
	 * @param pageable   - pageable definition
	 * @return Iterable<LogResource>
	 */
	Iterable<LogResource> getLogs(Long testStepId, ReportPortalUser.ProjectDetails projectDetails, Filter filterable, Pageable pageable);

	/**
	 * Returns log by ID
	 *
	 * @param logId          - target log ID value
	 * @param projectDetails Project details
	 * @param user           User
	 * @return LogResource
	 */
	LogResource getLog(Long logId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user);

	/**
	 * Calculates page number and returns entire page for specified log ID
	 *
	 * @param logId          ID of log to find
	 * @param projectDetails Project details
	 * @param filterable     Filter for paging
	 * @param pageable       Paging details
	 * @return Page Number
	 */
	long getPageNumber(Long logId, ReportPortalUser.ProjectDetails projectDetails, Filter filterable, Pageable pageable);
}
