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

import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.settings.AnalyticsResource;
import com.epam.ta.reportportal.ws.model.settings.ServerEmailResource;
import com.epam.ta.reportportal.ws.model.settings.ServerSettingsResource;

import java.security.Principal;

/**
 * Controller interface for specific ADMIN ONLY features like server
 * configuration and monitoring
 *
 * @author Andrei_Ramanchuk
 */
public interface ISettingsController {

	/**
	 * Get server settings for specified profile name
	 *
	 * @param profileId - name of the profile ('default' till additional
	 *                  implementations)
	 * @param principal Name of logged-in user
	 * @return ServerSettingsResource
	 */
	ServerSettingsResource getServerSettings(String profileId, Principal principal);

	/**
	 * Update server settings for specified profile name
	 *
	 * @param profileId - name of the profile ('default' till additional
	 *                  implementations)
	 * @param request   Email settings update
	 * @param principal Name of logged-in user
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS saveEmailSettings(String profileId, ServerEmailResource request, Principal principal);

	/**
	 * Updates analytics settings for specified profile name
	 *
	 * @param profileId - name of the profile ('default' till additional
	 *                  implementations)
	 * @param request   Analytics settings
	 * @return OperationCompletionRS
	 */
	OperationCompletionRS saveAnalyticsSettings(String profileId, AnalyticsResource request);
}