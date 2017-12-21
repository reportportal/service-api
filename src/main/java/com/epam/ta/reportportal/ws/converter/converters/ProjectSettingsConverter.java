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
import com.epam.ta.reportportal.ws.model.project.config.IssueSubTypeResource;
import com.epam.ta.reportportal.ws.model.project.config.ProjectSettingsResource;
import com.google.common.base.Preconditions;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Converts internal DB model to DTO
 *
 * @author Pavel Bortnik
 */
public final class ProjectSettingsConverter {

	private ProjectSettingsConverter() {
		//static only
	}

	public static final Function<Project, ProjectSettingsResource> TO_RESOURCE = project -> {
		Preconditions.checkNotNull(project);
		ProjectSettingsResource resource = new ProjectSettingsResource();
		resource.setProjectId(project.getId());
		Map<String, List<IssueSubTypeResource>> result = project.getConfiguration()
				.getSubTypes()
				.entrySet()
				.stream()
				.collect(Collectors.toMap(
						entry -> entry.getKey().getValue(),
						entry -> entry.getValue().stream().map(ProjectConverter.TO_SUBTYPE_RESOURCE).collect(Collectors.toList())
				));
		resource.setSubTypes(result);
		resource.setStatisticsStrategy(project.getConfiguration().getStatisticsCalculationStrategy().name());
		return resource;
	};

}
