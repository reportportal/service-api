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
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.database.entity.Dashboard.WidgetObject;
import com.epam.ta.reportportal.ws.controller.impl.DashboardController;
import com.epam.ta.reportportal.ws.controller.impl.WidgetController;
import com.epam.ta.reportportal.ws.converter.builders.DashboardResourceBuilder;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;

import javax.inject.Provider;

/**
 * Resource Assembler for the {@link Dashboard} DB entity.
 * 
 * @author Aliaksei_Makayed
 * 
 */
@Service
public class DashboardResourceAssembler extends
	ResourceAssemblerSupport<Dashboard, DashboardResource> {
	
	public static final String REL = "related";

	@Autowired
	private Provider<DashboardResourceBuilder> builderLazyReference;

	public DashboardResourceAssembler() {
		super(DashboardController.class, DashboardResource.class);
	}

	@Override
	public DashboardResource toResource(Dashboard entity) {

		Link selfLink = ControllerLinkBuilder
				.linkTo(DashboardController.class,
						entity.getProjectName()).slash(entity)
				.withSelfRel();
		DashboardResourceBuilder resourceBuilder = builderLazyReference.get();
		
		resourceBuilder.addDashboard(entity).addLink(selfLink);

		for (WidgetObject widget : entity.getWidgets()) {
			Link widgetLink = ControllerLinkBuilder
					.linkTo(WidgetController.class,
							entity.getProjectName()).slash(widget.getWidgetId()).withRel(REL);
			resourceBuilder.addLink(widgetLink);
		}

		return resourceBuilder.build();
	}

}