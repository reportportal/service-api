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

package com.epam.ta.reportportal.core.dashboard.impl;

import com.epam.ta.reportportal.auth.acl.ShareableObjectsHandler;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.dashboard.GetDashboardHandler;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.DashboardUpdatedEvent;
import com.epam.ta.reportportal.core.widget.GetWidgetHandler;
import com.epam.ta.reportportal.dao.DashboardRepository;
import com.epam.ta.reportportal.dao.DashboardWidgetRepository;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.entity.dashboard.DashboardWidget;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.converter.builders.DashboardBuilder;
import com.epam.ta.reportportal.ws.converter.converters.WidgetConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.activity.DashboardActivityResource;
import com.epam.ta.reportportal.ws.model.dashboard.AddWidgetRq;
import com.epam.ta.reportportal.ws.model.dashboard.UpdateDashboardRQ;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.ws.converter.converters.DashboardConverter.TO_ACTIVITY_RESOURCE;

/**
 * @author Pavel Bortnik
 */
@Service
public class UpdateDashboardHandler implements com.epam.ta.reportportal.core.dashboard.UpdateDashboardHandler {

	@Autowired
	private DashboardWidgetRepository dashboardWidgetRepository;

	private DashboardRepository dashboardRepository;

	@Autowired
	private WidgetRepository widgetRepository;

	private MessageBus messageBus;

	private GetDashboardHandler getDashboardHandler;

	private GetWidgetHandler getWidgetHandler;

	private ShareableObjectsHandler aclHandler;

	@Autowired
	public UpdateDashboardHandler(DashboardRepository dashboardRepository, MessageBus messageBus, GetDashboardHandler getDashboardHandler,
			GetWidgetHandler getWidgetHandler, ShareableObjectsHandler aclHandler) {
		this.dashboardRepository = dashboardRepository;
		this.messageBus = messageBus;
		this.getDashboardHandler = getDashboardHandler;
		this.getWidgetHandler = getWidgetHandler;
		this.aclHandler = aclHandler;
	}

	@Override
	public OperationCompletionRS updateDashboard(ReportPortalUser.ProjectDetails projectDetails, UpdateDashboardRQ rq, Long dashboardId,
			ReportPortalUser user) {
		Dashboard dashboard = getDashboardHandler.getAdministrated(dashboardId, projectDetails);
		DashboardActivityResource before = TO_ACTIVITY_RESOURCE.apply(dashboard);

		dashboard = new DashboardBuilder(dashboard).addUpdateRq(rq).get();
		dashboardRepository.save(dashboard);

		if (before.isShared() != dashboard.isShared()) {
			aclHandler.updateAcl(dashboard, projectDetails.getProjectId(), dashboard.isShared());
		}

		messageBus.publishActivity(new DashboardUpdatedEvent(before, TO_ACTIVITY_RESOURCE.apply(dashboard), user.getUserId()));
		return new OperationCompletionRS("Dashboard with ID = '" + dashboard.getId() + "' successfully updated");
	}

	@Override
	public OperationCompletionRS addWidget(Long dashboardId, ReportPortalUser.ProjectDetails projectDetails, AddWidgetRq rq,
			ReportPortalUser user) {
		Dashboard dashboard = getDashboardHandler.getAdministrated(dashboardId, projectDetails);
		BusinessRule.expect(dashboard.getDashboardWidgets()
				.stream()
				.map(dw -> dw.getId().getWidgetId())
				.anyMatch(widgetId -> widgetId.equals(rq.getAddWidget().getWidgetId())), BooleanUtils::isFalse)
				.verify(ErrorType.DASHBOARD_UPDATE_ERROR, Suppliers.formattedSupplier(
						"Widget with ID = '{}' is already added to the dashboard with ID = '{}'",
						rq.getAddWidget().getWidgetId(),
						dashboard.getId()
				));
		Widget widget = getWidgetHandler.getPermitted(rq.getAddWidget().getWidgetId(), projectDetails);
		DashboardWidget dashboardWidget = WidgetConverter.toDashboardWidget(rq.getAddWidget(), dashboard, widget);
		dashboardWidgetRepository.save(dashboardWidget);
		return new OperationCompletionRS(
				"Widget with ID = '" + widget.getId() + "' was successfully added to the dashboard with ID = '" + dashboard.getId() + "'");

	}

	@Override
	public OperationCompletionRS removeWidget(Long widgetId, Long dashboardId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		Dashboard dashboard = getDashboardHandler.getPermitted(dashboardId, projectDetails);
		Widget widget = getWidgetHandler.getPermitted(widgetId, projectDetails);

		/*
		 *	if user is an owner of the widget - remove it from all dashboards
		 *	should be replaced with copy
		 */
		if (user.getUsername().equalsIgnoreCase(widget.getOwner())) {
			return deleteWidget(widget);
		}

		DashboardWidget toRemove = dashboard.getDashboardWidgets()
				.stream()
				.filter(dashboardWidget -> widget.getId().equals(dashboardWidget.getId().getWidgetId()))
				.findFirst()
				.orElseThrow(() -> new ReportPortalException(ErrorType.WIDGET_NOT_FOUND_IN_DASHBOARD, widgetId, dashboardId));

		dashboardWidgetRepository.delete(toRemove);

		return new OperationCompletionRS(
				"Widget with ID = '" + widget.getId() + "' was successfully removed from the dashboard with ID = '" + dashboard.getId()
						+ "'");
	}

	/**
	 * Totally remove the widget from all dashboards
	 *
	 * @param widget Widget
	 * @return OperationCompletionRS
	 */
	private OperationCompletionRS deleteWidget(Widget widget) {
		dashboardWidgetRepository.deleteAll(widget.getDashboardWidgets());
		widgetRepository.delete(widget);
		aclHandler.deleteAclForObject(widget);
		return new OperationCompletionRS("Widget with ID = '" + widget.getId() + "' was successfully deleted from the system.");
	}

}
