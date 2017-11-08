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

package com.epam.ta.reportportal.core.log;

import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.search.Filter;
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
	Iterable<LogResource> getLogs(String testStepId, String project, Filter filterable, Pageable pageable);

	/**
	 * Returns log by ID
	 *
	 * @param logId       - target log ID value
	 * @param projectName - specified project name value
	 * @return LogResource
	 */
	LogResource getLog(String logId, String projectName);

	/**
	 * Calculates page number and returns entire page for specified log ID
	 *
	 * @param logId      ID of log to find
	 * @param project    Project name
	 * @param filterable Filter for paging
	 * @param pageable   Paging details
	 * @return Page Number
	 */
	long getPageNumber(String logId, String project, Filter filterable, Pageable pageable);
}
