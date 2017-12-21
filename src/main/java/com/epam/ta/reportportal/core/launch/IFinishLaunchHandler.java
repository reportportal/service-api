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

import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;

import java.util.List;

/**
 * {@link FinishExecutionRQ} request handler
 *
 * @author Andrei_Kliashchonak
 */

public interface IFinishLaunchHandler {

	/**
	 * Updates {@link Launch} instance
	 *
	 * @param launchId       ID of launch
	 * @param finishLaunchRQ Request data
	 * @param projectName    Project name
	 * @param username       Username
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS finishLaunch(String launchId, FinishExecutionRQ finishLaunchRQ, String projectName, String username);

	/**
	 * Stop Launch instance by user
	 *
	 * @param launchId       ID of launch
	 * @param finishLaunchRQ Request data
	 * @param projectName    Project ID
	 * @param userName       Username
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS stopLaunch(String launchId, FinishExecutionRQ finishLaunchRQ, String projectName, String userName);

	List<OperationCompletionRS> stopLaunch(BulkRQ<FinishExecutionRQ> bulkRQ, String projectName, String userName);
}