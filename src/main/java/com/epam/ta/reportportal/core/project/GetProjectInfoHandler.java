/*
 * Copyright (C) 2018 EPAM Systems
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

package com.epam.ta.reportportal.core.project;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.entity.enums.InfoInterval;
import com.epam.ta.reportportal.ws.model.project.ProjectInfoResource;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * Get {@link com.epam.ta.reportportal.ws.model.project.ProjectInfoResource}
 * request handler
 *
 * @author Dzmitry_Kavalets
 */
public interface GetProjectInfoHandler {

	/**
	 * Get all projects info
	 *
	 * @return
	 */
	Iterable<ProjectInfoResource> getAllProjectsInfo(Filter filter, Pageable pageable);

	/**
	 * Get project info
	 *
	 * @param projectDetails Project details
	 * @param interval       Interval
	 * @return Project info resource
	 */
	ProjectInfoResource getProjectInfo(ReportPortalUser.ProjectDetails projectDetails, String interval);

	/**
	 * Get widget data content for specified project by specified
	 * {@link InfoInterval} and {@link com.epam.ta.reportportal.entity.project.email.ProjectInfoWidget}
	 *
	 * @param projectDetails Project id
	 * @param interval       Interval
	 * @param widgetCode     Project Info Widget code
	 * @return
	 */
	Map<String, ?> getProjectInfoWidgetContent(ReportPortalUser.ProjectDetails projectDetails, String interval,
			String widgetCode);
}
