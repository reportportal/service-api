/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.launch;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.launch.UpdateLaunchRQ;

import java.util.List;

/**
 * Update launch operation handler
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 * @author Pavel Bortnik
 */
public interface UpdateLaunchHandler {

	/**
	 * Update specified by id launch.
	 *
	 * @param launchId    ID of Launch object
	 * @param projectName Related project name value
	 * @param user        Recipient user
	 * @param rq          Request Data
	 * @return OperationCompletionRS - Response Data
	 */
	OperationCompletionRS updateLaunch(Long launchId, String projectName, ReportPortalUser user, UpdateLaunchRQ rq);

	/**
	 * Start launch analyzer on demand
	 *
	 * @param projectName related project name value
	 * @param launchId    ID of launch object
	 * @return OperationCompletionRS - Response Data
	 */
	OperationCompletionRS startLaunchAnalyzer(String projectName, Long launchId);

	/**
	 * Bulk launch update.
	 *
	 * @param rq          Bulk request
	 * @param projectName Project name
	 * @param user        User
	 * @return OperationCompletionRS
	 */
	List<OperationCompletionRS> updateLaunch(BulkRQ<UpdateLaunchRQ> rq, String projectName, ReportPortalUser user);
}