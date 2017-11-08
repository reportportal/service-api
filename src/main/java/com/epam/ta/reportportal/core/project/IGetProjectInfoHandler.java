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

package com.epam.ta.reportportal.core.project;

import com.epam.ta.reportportal.database.entity.project.info.InfoInterval;
import com.epam.ta.reportportal.database.entity.project.info.ProjectInfoWidget;
import com.epam.ta.reportportal.database.search.Filter;
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
public interface IGetProjectInfoHandler {

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
