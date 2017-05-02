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

import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.ws.converter.builders.ActivityResourceBuilder;
import com.epam.ta.reportportal.ws.model.ActivityResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Provider;

/**
 * Resource assembler for the
 * {@link com.epam.ta.reportportal.database.entity.item.Activity} db entity
 * 
 * @author Dzmitry_Kavalets
 */
@Service
public class ActivityResourceAssembler extends PagedResourcesAssembler<Activity, ActivityResource> {

	@Autowired
	private Provider<ActivityResourceBuilder> builderLazyReference;

	@Override
	public ActivityResource toResource(Activity element) {
		return builderLazyReference.get().addActivity(element).build();
	}
}
