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

package com.epam.ta.reportportal.core.widget;

import com.epam.ta.reportportal.exception.ReportPortalException;
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
public interface IGetWidgetHandler {

	/**
	 * Get widget by id
	 *
	 * @param widgetId
	 * @param userName
	 * @param project
	 * @return WidgetResource
	 * @throws ReportPortalException
	 */
	WidgetResource getWidget(String widgetId, String userName, String project);

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
	 * @param previewRQ   Widget parameters
	 * @param projectName Project name
	 * @param userName    Username
	 * @return Widget content
	 */
	Map<String, ?> getWidgetPreview(String projectName, String userName, WidgetPreviewRQ previewRQ);

	/**
	 * Get list of widgets that contains search criteria
	 *
	 * @param term        Search criteria
	 * @param projectName Project name
	 * @return List of widgets
	 */
	Iterable<WidgetResource> searchSharedWidgets(String term, String projectName, Pageable pageable);
}