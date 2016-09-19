/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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
import com.epam.ta.reportportal.util.LazyReference;
import com.epam.ta.reportportal.ws.controller.ITestItemController;
import com.epam.ta.reportportal.ws.controller.impl.ActivityController;
import com.epam.ta.reportportal.ws.converter.builders.ActivityResourceBuilder;
import com.epam.ta.reportportal.ws.model.ActivityResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Service;

/**
 * Resource assembler for the
 * {@link com.epam.ta.reportportal.database.entity.item.Activity} db entity
 * 
 * @author Dzmitry_Kavalets
 */
@Service
public class ActivityResourceAssembler extends ProjectRelatedResourceAssembler<Activity, ActivityResource> {

	@Autowired
	@Qualifier("activityResourceBuilder.reference")
	private LazyReference<ActivityResourceBuilder> builderLazyReference;

	public ActivityResourceAssembler() {
		super(ITestItemController.class, ActivityResource.class);
	}

	@Override
	public ActivityResource toResource(Activity entity) {
		return toResource(entity, null);
	}

	@Override
	public ActivityResource toResource(Activity element, String projectName) {
		String item = element.getLoggedObjectRef();
		Link link = ControllerLinkBuilder
				.linkTo(ActivityController.class, null == projectName ? element.getProjectRef() : projectName,
						item).slash(element).withSelfRel();
		return builderLazyReference.get().addActivity(element).addLink(link).build();
	}
}