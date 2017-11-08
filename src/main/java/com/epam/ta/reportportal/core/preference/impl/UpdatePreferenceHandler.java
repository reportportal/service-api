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

package com.epam.ta.reportportal.core.preference.impl;

import com.epam.ta.reportportal.core.preference.IUpdatePreferenceHandler;
import com.epam.ta.reportportal.database.dao.UserPreferenceRepository;
import com.epam.ta.reportportal.database.entity.UserPreference;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.preference.UpdatePreferenceRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Default implementation of
 * {@link com.epam.ta.reportportal.core.preference.IUpdatePreferenceHandler}
 *
 * @author Dzmitry_Kavalets
 */
@Service
public class UpdatePreferenceHandler implements IUpdatePreferenceHandler {

	@Autowired
	private UserPreferenceRepository userPreferenceRepository;

	@Override
	public OperationCompletionRS updatePreference(String userName, String projectName, UpdatePreferenceRQ rq) {

		UserPreference preference = userPreferenceRepository.findByProjectAndUserName(projectName, userName);

		if (null == preference) {
			preference = new UserPreference();
			preference.setLaunchTabs(new UserPreference.LaunchTabs());
			preference.setUserRef(userName);
			preference.setProjectRef(projectName);
		}
		if (null != rq.getActive()) {
			preference.getLaunchTabs().setActive(rq.getActive());
		}
		preference.getLaunchTabs().setFilters(rq.getFilters());
		OperationCompletionRS operationCompletionRS = new OperationCompletionRS();
		userPreferenceRepository.save(preference);
		operationCompletionRS.setResultMessage("Filter tabs for '" + userName + "' user have been updated");
		return operationCompletionRS;
	}
}