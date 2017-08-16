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
import com.epam.ta.reportportal.ws.model.widget.WidgetRQ;
import com.epam.ta.reportportal.ws.model.widget.WidgetResource;

import java.security.Principal;
import java.util.List;
import java.util.Map;

/**
 * 
 * Report Portal WS Interface. Widget controller
 * 
 * @author Aliaksei_Makayed
 * 
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
     * Temporary solution for creating empty widgets
     *
     * @param projectName
     * @return EntryCreatedRS
     */
    EntryCreatedRS createEmptyWidget(String projectName, WidgetRQ createWidgetRq, Principal principal);

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
	 * Update widget with specified id
	 * 
	 * @param projectName
	 * @param principal
	 * @param widgetId
	 *            , updateRQ
	 * @param userRole
	 * @return {@link OperationCompletionRS}
	 * @throws ReportPortalException
	 */
	OperationCompletionRS updateWidget(String projectName, String widgetId, WidgetRQ updateRQ, UserRole userRole, Principal principal);

	/**
	 * Get shared widgets names
	 * 
	 * @param principal
	 * @param projectName
	 * @return
	 */
	Map<String, SharedEntity> getSharedWidgets(Principal principal, String projectName);
	
	/**
	 * Get list of shared widgets per project
	 * 
	 * @param principal
	 * @param projectName
	 * @return
	 */
	List<WidgetResource> getSharedWidgetsList(Principal principal, String projectName);

	/**
	 * Get list of widget names
	 * 
	 * @param project
	 * @param principal
	 * @return
	 */
	List<String> getWidgetNames(String project, Principal principal);
}