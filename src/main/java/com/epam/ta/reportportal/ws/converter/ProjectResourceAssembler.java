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

import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.ws.converter.converters.ExternalSystemConverter;
import com.epam.ta.reportportal.ws.converter.converters.ProjectConverter;
import com.epam.ta.reportportal.ws.model.project.ProjectResource;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Resource Assembler for the {@link Project} DB entity
 *
 * @author Andrei_Ramanchuk
 * @author Pavel_Bortnik
 */
@Service
public class ProjectResourceAssembler extends PagedResourcesAssembler<Project, ProjectResource> {

	@Override
	public ProjectResource toResource(Project entity) {
		ProjectResource resource = ProjectConverter.TO_RESOURCE.apply(entity);
		resource.getConfiguration().setExternalSystem(Lists.newArrayList());
		return resource;
	}

	public ProjectResource toResource(Project project, Iterable<ExternalSystem> systems) {
		ProjectResource resource = ProjectConverter.TO_RESOURCE.apply(project);
		resource.getConfiguration()
				.setExternalSystem(
						Lists.newArrayList(systems).stream().map(ExternalSystemConverter.TO_RESOURCE).collect(Collectors.toList()));
		return resource;
	}
}
