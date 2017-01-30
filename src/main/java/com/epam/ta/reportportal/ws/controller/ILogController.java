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

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.database.entity.Log;
import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.BatchSaveOperatingRS;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.log.LogResource;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.Serializable;
import java.security.Principal;
import java.util.Map;

/**
 * @author Henadzi_Vrubleuski
 */
public interface ILogController {

	/**
	 * Creates new {@link Log} instance
	 *
	 * @param projectName
	 * @param createLogRQ
	 * @param principal
	 * @return
	 * @throws ReportPortalException
	 */
	EntryCreatedRS createLog(String projectName, SaveLogRQ createLogRQ, Principal principal);

	/**
	 * Updates {@link Log} instance
	 *
	 * @param projectName
	 * @param createLogRQs
	 * @param request
	 * @param principal
	 * @return
	 * @throws ReportPortalException
	 */
	ResponseEntity<BatchSaveOperatingRS> createLog(String projectName, @Valid SaveLogRQ[] createLogRQs, HttpServletRequest request,
			Principal principal);

	/**
	 * Deletes specified {@link Log} instance
	 *
	 * @param projectName
	 * @param logId
	 * @param principal
	 * @return
	 * @throws ReportPortalException
	 */
	OperationCompletionRS deleteLog(String projectName, String logId, Principal principal);

	/**
	 * Gets all Logs of specified test step
	 *
	 * @param projectName
	 * @param testId
	 * @param filter
	 * @param pageble
	 * @param principal
	 * @return
	 */
	Iterable<LogResource> getLogs(String projectName, String testId, Filter filter, Pageable pageble, Principal principal);

	/**
	 * Calculates page number of provided log
	 *
	 * @param projectName
	 * @param logId
	 * @param filter
	 * @param pageable
	 * @param principal
	 * @return
	 */
	Map<String, Serializable> getPageNumber(String projectName, String logId, Filter filter, Pageable pageable, Principal principal);

	/**
	 * Get by its ID
	 *
	 * @param projectName
	 * @param logId
	 * @param principal
	 * @return
	 */
	LogResource getLog(String projectName, String logId, Principal principal);
}