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
 
package com.epam.ta.reportportal.ws.converter.builders;

import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.entity.UserPreference;
import com.epam.ta.reportportal.ws.model.preference.PreferenceResource;

/**
 * Builder for
 * {@link com.epam.ta.reportportal.ws.model.preference.PreferenceResource}
 * 
 * @author Dzmitry_Kavalets
 */
@Service
public class PreferenceResourceBuilder extends ResourceBuilder<PreferenceResource> {
	@Override
	protected PreferenceResource initObject() {
		return new PreferenceResource();
	}

	public PreferenceResourceBuilder addPreference(UserPreference userPreference) {
		PreferenceResource preferenceResource = getObject();
		if (null != userPreference) {
			preferenceResource.setUserRef(userPreference.getUserRef());
			preferenceResource.setProjectRef(userPreference.getProjectRef());
			UserPreference.LaunchTabs preference = userPreference.getLaunchTabs();
			if (null != preference) {
				preferenceResource.setActive(preference.getActive());
				preferenceResource.setFilters(preference.getFilters());
			}
		}
		return this;
	}
}