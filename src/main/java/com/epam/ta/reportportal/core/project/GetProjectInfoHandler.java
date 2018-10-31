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

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.ws.model.project.ProjectInfoResource;
import com.epam.ta.reportportal.ws.model.widget.ChartObject;
import org.springframework.data.domain.Pageable;

import java.util.List;
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
	 * @param projectId
	 * @return
	 */
	ProjectInfoResource getProjectInfo(String projectId, String interval);

	/**
	 * Get widget data content for specified project by specified
	 * {@link InfoInterval} and {@link ProjectInfoWidget}
	 *
	 * @param projectId
	 * @param interval
	 * @param widgetId
	 * @return
	 */
	Map<String, List<ChartObject>> getProjectInfoWidgetContent(String projectId, String interval, String widgetId);
}
