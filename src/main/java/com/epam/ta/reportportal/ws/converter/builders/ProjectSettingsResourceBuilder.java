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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.database.entity.ProjectSettings;
import com.epam.ta.reportportal.ws.model.project.config.IssueSubTypeResource;
import com.epam.ta.reportportal.ws.model.project.config.ProjectSettingsResource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link com.epam.ta.reportportal.ws.model.project.config.ProjectSettingsResource}
 * object builder
 *
 * @author Andrei_Ramanchuk
 */
@Service
@Scope("prototype")
public class ProjectSettingsResourceBuilder extends ResourceBuilder<ProjectSettingsResource> {

	public ProjectSettingsResourceBuilder addProjectSettings(ProjectSettings settings) {
		ProjectSettingsResource resource = getObject();
		resource.setProjectId(settings.getId());
		Map<String, List<IssueSubTypeResource>> result = settings.getSubTypes().entrySet().stream().collect(Collectors
				.toMap(entry -> entry.getKey().getValue(), entry -> entry.getValue().stream()
						.map(subType -> new IssueSubTypeResource(subType.getLocator(), subType.getTypeRef(), subType.getLongName(),
								subType.getShortName(), subType.getHexColor())).collect(Collectors.toList())));

		resource.setSubTypes(result);
		return this;
	}

	@Override
	protected ProjectSettingsResource initObject() {
		return new ProjectSettingsResource();
	}
}