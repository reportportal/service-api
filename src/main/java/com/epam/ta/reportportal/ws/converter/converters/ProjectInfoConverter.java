/*
 * Copyright 2017 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.project.EntryType;
import com.epam.ta.reportportal.ws.model.project.ProjectInfoResource;
import com.google.common.base.Preconditions;

import java.util.Optional;
import java.util.function.Function;

/**
 * Converts internal DB model to DTO
 *
 * @author Pavel Bortnik
 */
public final class ProjectInfoConverter {

	private ProjectInfoConverter() {
		//static only
	}

	public static final Function<Project, ProjectInfoResource> TO_RESOURCE = project -> {
		Preconditions.checkNotNull(project);
		ProjectInfoResource resource = new ProjectInfoResource();
		resource.setUsersQuantity(null != project.getUsers() ? project.getUsers().size() : 0);
		resource.setProjectId(project.getId());
		resource.setCreationDate(project.getCreationDate());
		String entryType = Optional.ofNullable(project.getConfiguration())
				.map(Project.Configuration::getEntryType)
				.map(Enum::name)
				.orElse(EntryType.INTERNAL.name());
		resource.setEntryType(entryType);
		return resource;
	};
}
