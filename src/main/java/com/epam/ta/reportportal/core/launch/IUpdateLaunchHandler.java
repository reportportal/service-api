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

package com.epam.ta.reportportal.core.launch;

import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.launch.UpdateLaunchRQ;

import java.util.List;

/**
 * Update launch operation handler
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
public interface IUpdateLaunchHandler {

	/**
	 * Update specified by id launch.
	 *
	 * @param launchId    ID of Launch object
	 * @param projectName Related project name value
	 * @param userName    Recipient user name
	 * @param rq          Request Data
	 * @return OperationCompletionRS - Response Data
	 */
	OperationCompletionRS updateLaunch(String launchId, String projectName, String userName, UpdateLaunchRQ rq);

	/**
	 * Start launch analyzer on demand
	 *
	 * @param projectName related project name value
	 * @param launchId    ID of launch object
	 * @param mode        Analyze mode
	 * @return OperationCompletionRS - Response Data
	 */
	OperationCompletionRS startLaunchAnalyzer(String projectName, String launchId, String mode);

	List<OperationCompletionRS> updateLaunch(BulkRQ<UpdateLaunchRQ> rq, String projectName, String userName);
}