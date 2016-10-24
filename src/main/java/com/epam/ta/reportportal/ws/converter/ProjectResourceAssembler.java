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

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.util.LazyReference;
import com.epam.ta.reportportal.ws.controller.impl.ExternalSystemController;
import com.epam.ta.reportportal.ws.controller.impl.ProjectController;
import com.epam.ta.reportportal.ws.converter.builders.ProjectResourceBuilder;
import com.epam.ta.reportportal.ws.model.project.ProjectResource;

/**
 * Resource Assembler for the {@link Project} DB entity
 * 
 * @author Andrei_Ramanchuk
 */
@Service
public class ProjectResourceAssembler extends PagedResourcesAssember<Project, ProjectResource> {

	public static final String REL = "related";

	@Autowired
	@Qualifier("projectResourceBuilder.reference")
	private LazyReference<ProjectResourceBuilder> builder;

	public ProjectResourceAssembler() {
		super(ProjectController.class, ProjectResource.class);
	}

	public ProjectResource toResource(Project project, Iterable<ExternalSystem> systems) {
		Link selfLink = ControllerLinkBuilder.linkTo(ProjectController.class).slash(project).withSelfRel();

		ProjectResourceBuilder resourceBuilder = builder.get();
		resourceBuilder.addProject(project, systems).addLink(selfLink);

		for (String systemId : project.getConfiguration().getExternalSystem()) {
			Link sysLink = ControllerLinkBuilder.linkTo(ExternalSystemController.class, project.getId()).slash(systemId).withRel(REL);
			resourceBuilder.addLink(sysLink);
		}

		return resourceBuilder.build();
	}

	@Override
	public ProjectResource toResource(Project entity) {
		Link selfLink = ControllerLinkBuilder.linkTo(ProjectController.class).slash(entity).withSelfRel();
		ProjectResourceBuilder resourceBuilder = builder.get();
		resourceBuilder.addProject(entity, new ArrayList<>()).addLink(selfLink);

		for (String systemId : entity.getConfiguration().getExternalSystem()) {
			Link widgetLink = ControllerLinkBuilder.linkTo(ExternalSystemController.class, entity.getId()).slash(systemId).withRel(REL);
			resourceBuilder.addLink(widgetLink);
		}

		return resourceBuilder.build();
	}
}