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

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.preference.IGetPreferenceHandler;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.UserPreferenceRepository;
import com.epam.ta.reportportal.database.entity.UserPreference;
import com.epam.ta.reportportal.ws.converter.UserPreferenceResourceAssembler;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.preference.PreferenceResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Default implementation of
 * {@link com.epam.ta.reportportal.core.preference.IGetPreferenceHandler}
 *
 * @author Dzmitry_Kavalets
 */
@Service
public class GetPreferenceHandler implements IGetPreferenceHandler {

	@Autowired
	private UserPreferenceRepository userPreferenceRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private UserPreferenceResourceAssembler resourceAssembler;

	@Override
	public PreferenceResource getPreference(String projectName, String userName) {
		boolean assignedToProject = projectRepository.isAssignedToProject(projectName, userName);
		BusinessRule.expect(Boolean.TRUE, Predicates.equalTo(assignedToProject))
				.verify(ErrorType.PROJECT_DOESNT_CONTAIN_USER, projectName, userName);
		UserPreference preference = userPreferenceRepository.findByProjectAndUserName(projectName, userName);
		if (null == preference) {
			preference = new UserPreference();
			preference.setUserRef(userName);
			preference.setProjectRef(projectName);
		}
		return resourceAssembler.toResource(preference);
	}
}