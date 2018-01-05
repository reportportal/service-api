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

package com.epam.ta.reportportal.core.admin;

import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.settings.AnalyticsResource;
import com.epam.ta.reportportal.ws.model.settings.ServerEmailResource;
import com.epam.ta.reportportal.ws.model.settings.ServerSettingsResource;

/**
 * Server settings administration interface
 *
 * @author Andrei_Ramanchuk
 */
public interface ServerAdminHandler {

	/**
	 * Get server settings for specified profile
	 *
	 * @param profileId Profile ID
	 * @return Settings
	 */
	ServerSettingsResource getServerSettings(String profileId);

	/**
	 * Updates email settings for specified profile
	 *
	 * @param profileId Profile ID
	 * @param request   Update data
	 * @return Operation results
	 */
	OperationCompletionRS saveEmailSettings(String profileId, ServerEmailResource request);

	/**
	 * Deletes email settings for specified profile
	 *
	 * @param profileId Profile ID
	 * @return Operation results
	 */
	OperationCompletionRS deleteEmailSettings(String profileId);

	/**
	 * Updates analytics settings for specified profile
	 *
	 * @param profileId Profile ID
	 * @param analyticsResource   Analytics settings
	 * @return Operation results
	 */
	OperationCompletionRS saveAnalyticsSettings(String profileId, AnalyticsResource analyticsResource);
}
