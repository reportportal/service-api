/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.ws.model.activity.ProjectAttributesActivityResource;

import java.util.function.Function;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
public final class ProjectActivityConverter {

	private ProjectActivityConverter() {
		//static only
	}

	public static final Function<Project, ProjectAttributesActivityResource> TO_ACTIVITY_RESOURCE = project -> {
		ProjectAttributesActivityResource resource = new ProjectAttributesActivityResource();
		resource.setProjectId(project.getId());
		resource.setProjectName(project.getName());
		resource.setConfig(ProjectUtils.getConfigParameters(project.getProjectAttributes()));
		return resource;
	};

}
