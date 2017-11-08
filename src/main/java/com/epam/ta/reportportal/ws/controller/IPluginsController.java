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

import com.epam.ta.reportportal.database.search.Filter;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.SystemInfoRS;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

/**
 * External ReportPortal plug-ins controller interface.
 *
 * @author Andrei_Ramanchuk
 */
public interface IPluginsController {

	/**
	 * Get Launch resource by specified launch Name (for Jenkins)
	 *
	 * @param projectName
	 * @param request
	 * @param principal
	 * @return
	 */
	LaunchResource getLaunchByName(String projectName, Pageable pageble, Filter filter, Principal principal);

	/**
	 * Heartbeat of ReportPortal server status
	 *
	 * @param request
	 * @param principal
	 * @return
	 */
	OperationCompletionRS heartBeat(HttpServletRequest request, Principal principal);

	/**
	 * Get server system information (for dashing)
	 *
	 * @return
	 */
	SystemInfoRS getSystemStatus();
}