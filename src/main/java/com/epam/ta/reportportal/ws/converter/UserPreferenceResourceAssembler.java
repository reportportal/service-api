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
 
package com.epam.ta.reportportal.ws.converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.entity.UserPreference;
import com.epam.ta.reportportal.util.LazyReference;
import com.epam.ta.reportportal.ws.controller.impl.ProjectController;
import com.epam.ta.reportportal.ws.converter.builders.PreferenceResourceBuilder;
import com.epam.ta.reportportal.ws.model.preference.PreferenceResource;

/**
 * Resource assembler for
 * {@link com.epam.ta.reportportal.database.entity.UserPreference} db entity
 * 
 * @author Dzmitry_Kavalets
 */
@Service
public class UserPreferenceResourceAssembler extends PagedResourcesAssember<UserPreference, PreferenceResource> {

	private static final String PREFERENCE = "preference";

	@Autowired
	@Qualifier("preferenceResourceBuilder.reference")
	private LazyReference<PreferenceResourceBuilder> builder;

	public UserPreferenceResourceAssembler() {
		super(ProjectController.class, PreferenceResource.class);
	}

	@Override
	public PreferenceResource toResource(UserPreference entity) {
		return builder
				.get()
				.addPreference(entity)
				.addLink(
						ControllerLinkBuilder.linkTo(ProjectController.class).slash(entity.getProjectRef()).slash(PREFERENCE)
								.slash(entity.getUserRef()).withSelfRel()).build();
	}
}