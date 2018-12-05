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

package com.epam.ta.reportportal.core.widget;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.widget.WidgetPreviewRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetResource;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * Get Widget handler
 *
 * @author Aliaksei_Makayed
 */
public interface GetWidgetHandler {

	/**
	 * Get widget by id with permission check
	 *
	 * @param widgetId Widget id
	 * @return Allowed widget.
	 */
	Widget findById(Long widgetId);

	/**
	 * Get widget by id
	 *
	 * @param widgetId       Widget id
	 * @param projectDetails Project details
	 * @param user           User
	 * @return WidgetResource
	 */
	WidgetResource getWidget(Long widgetId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user);

	/**
	 * Get names of shared widgets for specified used(only shared not owned)
	 *
	 * @param userName
	 * @param projectName
	 * @return
	 */
	Iterable<SharedEntity> getSharedWidgetNames(String userName, String projectName, Pageable pageable);

	/**
	 * Get list of shared widget for specified project
	 *
	 * @param userName
	 * @param projectName
	 * @return
	 */
	Iterable<WidgetResource> getSharedWidgetsList(String userName, String projectName, Pageable pageable);

	/**
	 * Get list of widget names for specified user
	 *
	 * @param userName
	 * @param projectName
	 * @return
	 */
	List<String> getWidgetNames(String projectName, String userName);

	/**
	 * Get content for building preview while creating widget
	 *
	 * @param previewRQ      Widget parameters
	 * @param projectDetails Project name
	 * @param user           Username
	 * @return Widget content
	 */
	Map<String, ?> getWidgetPreview(WidgetPreviewRQ previewRQ, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user);

	/**
	 * Get list of widgets that contains search criteria
	 *
	 * @param term        Search criteria
	 * @param projectName Project name
	 * @return List of widgets
	 */
	Iterable<WidgetResource> searchSharedWidgets(String term, String username, String projectName, Pageable pageable);

	/**
	 * Get widget names that belong to user
	 *
	 * @param projectDetails Project details
	 * @param pageable       Paging
	 * @param filter         Filter
	 * @param user           User
	 * @return List of widget names
	 */
	List<String> getOwnWidgetNames(ReportPortalUser.ProjectDetails projectDetails, Pageable pageable, Filter filter, ReportPortalUser user);
}