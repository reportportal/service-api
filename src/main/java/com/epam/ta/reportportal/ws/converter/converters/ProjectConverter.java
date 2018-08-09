package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.model.project.ProjectResource;

import java.util.function.Function;

/**
 * @author Pavel Bortnik
 */
public final class ProjectConverter {

	private ProjectConverter() {
		//static only
	}

	public static final Function<Project, ProjectResource> TO_PROJECT_RESOURCE = project -> {
		if (project == null) {
			return null;
		}

		ProjectResource projectResource = new ProjectResource();

		return projectResource;
	};

}
