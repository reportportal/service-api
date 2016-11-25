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

import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.util.LazyReference;
import com.epam.ta.reportportal.ws.controller.impl.ProjectController;
import com.epam.ta.reportportal.ws.converter.builders.ProjectInfoResourceBuilder;
import com.epam.ta.reportportal.ws.model.project.ProjectInfoResource;

/**
 * @author Dzmitry_Kavalets
 */
@Service
public class ProjectInfoResourceAssembler extends PagedResourcesAssember<Project, ProjectInfoResource> {

	@Autowired
	@Qualifier("projectInfoResourceBuilder.reference")
	private LazyReference<ProjectInfoResourceBuilder> builder;

	public ProjectInfoResourceAssembler() {
		super(ProjectController.class, ProjectInfoResource.class);
	}

	@Override
	public ProjectInfoResource toResource(Project entity) {
		return builder.get().addProject(entity)
				.addLink(ControllerLinkBuilder.linkTo(ProjectController.class).slash("info").slash(entity).withSelfRel())
				.addLink(ControllerLinkBuilder.linkTo(ProjectController.class).slash(entity).withRel("project")).build();
	}
}