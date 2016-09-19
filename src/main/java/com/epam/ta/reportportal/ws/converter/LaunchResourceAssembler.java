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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.util.LazyReference;
import com.epam.ta.reportportal.ws.controller.impl.LaunchController;
import com.epam.ta.reportportal.ws.converter.builders.LaunchResourceBuilder;
import com.epam.ta.reportportal.ws.model.launch.LaunchResource;

/**
 * Resource Assembler for the {@link Launch} DB entity.
 * 
 * @author Andrei Varabyeu
 * 
 */
@Service
public class LaunchResourceAssembler extends PagedResourcesAssember<Launch, LaunchResource> {

	@Autowired
	@Qualifier("launchResourceBuilder.reference")
	private LazyReference<LaunchResourceBuilder> launchRecordBuilder;

	public LaunchResourceAssembler() {
		super(LaunchController.class, LaunchResource.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.hateoas.ResourceAssembler#toResource(java.lang.Object
	 * )
	 */
	@Override
	public LaunchResource toResource(Launch launch) {
		return launchRecordBuilder.get().addLaunch(launch)
				.addLink(ControllerLinkBuilder.linkTo(LaunchController.class, launch.getProjectRef()).slash(launch).withSelfRel()).build();
	}

}