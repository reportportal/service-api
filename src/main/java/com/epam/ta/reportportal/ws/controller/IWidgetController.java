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

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.database.entity.user.UserRole;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.SharedEntity;
import com.epam.ta.reportportal.ws.model.widget.WidgetPreviewRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetResource;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * Report Portal WS Interface. Widget controller
 *
 * @author Aliaksei_Makayed
 */
public interface IWidgetController {

	/**
	 * Create new widget
	 *
	 * @param createWidgetRQ
	 * @param projectName
	 * @param principal
	 * @return EntryCreatedRS
	 * @throws ReportPortalException
	 */
	EntryCreatedRS createWidget(String projectName, WidgetRQ createWidgetRQ, Principal principal);

	/**
	 * Get widget by id
	 *
	 * @param widgetId
	 * @param principal
	 * @param projectName
	 * @return WidgetResource
	 * @throws ReportPortalException
	 */
	WidgetResource getWidget(String projectName, String widgetId, Principal principal);

	/**
	 * Get content for building preview while creating widget
	 *
	 * @param previewRQ   Widget parameters
	 * @param projectName Project name
	 * @return Content
	 */
	Map<String, ?> getWidgetPreview(String projectName, WidgetPreviewRQ previewRQ, Principal principal);

	/**
	 * Update widget with specified id
	 *
	 * @param projectName Project name
	 * @param principal   Principal
	 * @param widgetId    Widget id
	 * @param updateRQ    Update request
	 * @param userRole    User role
	 * @return {@link OperationCompletionRS}
	 * @throws ReportPortalException
	 */
	OperationCompletionRS updateWidget(String projectName, String widgetId, WidgetRQ updateRQ, UserRole userRole, Principal principal);

	/**
	 * Get shared widgets names
	 *
	 * @param principal   Principal
	 * @param projectName Project name
	 * @return Page of Shared entities
	 */
	Iterable<SharedEntity> getSharedWidgets(Principal principal, String projectName, Pageable pageable);

	/**
	 * Get list of widget names
	 *
	 * @param project   Project name
	 * @param principal Principal
	 * @return Widget names
	 */
	List<String> getWidgetNames(String project, Principal principal);

	/**
	 * Get list of shared widgets per project
	 *
	 * @param principal   Principal
	 * @param projectName Project name
	 * @param pageable    Paging
	 * @return
	 */
	Iterable<WidgetResource> getSharedWidgetsList(Principal principal, String projectName, Pageable pageable);

	/**
	 * Get list of shared widgets by term
	 *
	 * @param term Part of widget name
	 * @return List of widgets
	 */
	Iterable<WidgetResource> searchSharedWidgets(String term, String projectName, Pageable pageable);
}