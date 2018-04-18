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

package com.epam.ta.reportportal.core.bts.handler;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.bts.handler.impl.UpdateExternalSystemHandler;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.externalsystem.UpdateExternalSystemRQ;

/**
 * Basic interface for {@link UpdateExternalSystemHandler}
 *
 * @author Andrei_Ramanchuk
 * @author Pavel Bortnik
 */
public interface IUpdateExternalSystemHandler {

	/**
	 * Update method for {@link com.epam.ta.reportportal.store.database.entity.bts.BugTrackingSystem} entity
	 *
	 * @param request     Request Data
	 * @param projectName Project Name
	 * @param id          System ID
	 * @param user        Report portal user
	 * @return Operation result
	 */
	OperationCompletionRS updateExternalSystem(UpdateExternalSystemRQ request, String projectName, Long id, ReportPortalUser user);

	/**
	 * Validate connection of provided ExternalSystem configuration
	 *
	 * @param projectName Project
	 * @param systemId    External system id
	 * @param updateRQ    Request Data
	 * @param user        Report portal user
	 * @return Operation result
	 */
	OperationCompletionRS externalSystemConnect(UpdateExternalSystemRQ updateRQ, String projectName, Long systemId, ReportPortalUser user);

}