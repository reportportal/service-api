/*
 * Copyright 2019 EPAM Systems
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

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.ws.model.widget.WidgetPreviewRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetResource;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import java.util.Map;

/**
 * @author Pavel Bortnik
 */
public interface GetWidgetHandler {

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
	 * Get specified widget level by id and attributes
	 *
	 * @param widgetId       Widget id
	 * @param projectDetails Project details
	 * @param attributes     Attributes
	 * @param params         Additional widget params
	 * @param user           User
	 * @return WidgetResource
	 */
	WidgetResource getWidget(Long widgetId, String[] attributes, MultiValueMap<String, String> params, ReportPortalUser.ProjectDetails projectDetails,
			ReportPortalUser user);

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
	 * Get widget names that belong to user
	 *
	 * @param projectDetails Project details
	 * @param pageable       Paging
	 * @param filter         Filter
	 * @param user           User
	 * @return List of widget names
	 */
	Iterable<Object> getOwnNames(ReportPortalUser.ProjectDetails projectDetails, Pageable pageable, Filter filter, ReportPortalUser user);

	/**
	 * Get shared widgets for user
	 *
	 * @param projectDetails Project details
	 * @param pageable       Paging
	 * @param filter         Filter
	 * @param user           User
	 * @return Page of shared widget resources
	 */
	Iterable<WidgetResource> getShared(ReportPortalUser.ProjectDetails projectDetails, Pageable pageable, Filter filter,
			ReportPortalUser user);

	/**
	 * Get shared widgets for user that contains a provided term
	 * in name, description or owner
	 *
	 * @param projectDetails Project details
	 * @param pageable       Paging
	 * @param filter         Filter
	 * @param user           User
	 * @param term           Search term
	 * @return Page of shared widget resources
	 */
	Iterable<WidgetResource> searchShared(ReportPortalUser.ProjectDetails projectDetails, Pageable pageable, Filter filter,
			ReportPortalUser user, String term);
}