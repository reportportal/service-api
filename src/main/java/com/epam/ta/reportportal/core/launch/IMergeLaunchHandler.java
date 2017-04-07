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

import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.launch.DeepMergeLaunchesRQ;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;
import com.epam.ta.reportportal.ws.model.launch.MergeLaunchesRQ;

/**
 * Update launch operation handler
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 * @author Pavel_Bortnik
 */
public interface IMergeLaunchHandler {
    /**
     * Merge specified launches in common one
     *
     * @param projectName
     *            Related project name value
     * @param userName
     *            Recipient user name
     * @param mergeLaunchesRQ
     *            Request data
     * @return LaunchResource - Response Data
     */
    LaunchResource mergeLaunches(String projectName, String userName, MergeLaunchesRQ mergeLaunchesRQ);

    /**
     * Deep merge specified launches into selected one.
     * @param projectName
     * 			  Related project name value
     * @param launchTargetId
     * 			  Launch target id
     * @param userName
     * 			  Recipient user name
     * @param mergeLaunchesRQ
     * 			  Request data
     * @return LaunchResource - Response Data
     */
    OperationCompletionRS deepMergeLaunches(String projectName, String launchTargetId, String userName, DeepMergeLaunchesRQ mergeLaunchesRQ);
}
