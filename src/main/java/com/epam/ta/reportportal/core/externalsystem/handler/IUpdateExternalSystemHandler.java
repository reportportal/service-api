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

package com.epam.ta.reportportal.core.externalsystem.handler;

import com.epam.ta.reportportal.core.externalsystem.handler.impl.UpdateExternalSystemHandler;
import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.externalsystem.UpdateExternalSystemRQ;

/**
 * Basic interface for {@link UpdateExternalSystemHandler}
 *
 * @author Andrei_Ramanchuk
 */
public interface IUpdateExternalSystemHandler {

	/**
	 * Update method for {@link ExternalSystem} entity
	 *
	 * @param request       Request Data
	 * @param projectName   Project Name
	 * @param id            System ID
	 * @param principalName Login
	 * @return Operation result
	 */
	OperationCompletionRS updateExternalSystem(UpdateExternalSystemRQ request, String projectName, String id, String principalName);

	/**
	 * Validate connection of provided ExternalSystem configuration
	 *
	 * @param projectName   Project Name
	 * @param updateRQ      Request Data
	 * @param principalName Login
	 * @return Operation result
	 */
	OperationCompletionRS externalSystemConnect(String projectName, UpdateExternalSystemRQ updateRQ, String principalName);

}