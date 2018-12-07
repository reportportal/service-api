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

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.auth.ReportPortalUser.ProjectDetails;
import com.epam.ta.reportportal.auth.acl.ReportPortalAclHandler;
import com.epam.ta.reportportal.core.dashboard.GetDashboardHandler;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.DashboardUpdatedEvent;
import com.epam.ta.reportportal.core.widget.GetWidgetHandler;
import com.epam.ta.reportportal.dao.DashboardRepository;
import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.entity.dashboard.DashboardWidget;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.ws.converter.builders.DashboardBuilder;
import com.epam.ta.reportportal.ws.converter.converters.WidgetConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.activity.DashboardActivityResource;
import com.epam.ta.reportportal.ws.model.dashboard.AddWidgetRq;
import com.epam.ta.reportportal.ws.model.dashboard.UpdateDashboardRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.ws.converter.converters.DashboardConverter.TO_ACTIVITY_RESOURCE;

/**
 * @author Pavel Bortnik
 */
@Service
public class UpdateDashboardHandler implements com.epam.ta.reportportal.core.dashboard.UpdateDashboardHandler {

	private DashboardRepository dashboardRepository;

	private MessageBus messageBus;

	private GetDashboardHandler getDashboardHandler;

	private GetWidgetHandler getWidgetHandler;

	private ReportPortalAclHandler aclHandler;

	@Autowired
	public void setDashboardRepository(DashboardRepository dashboardRepository, GetDashboardHandler getDashboardHandler) {
		this.dashboardRepository = dashboardRepository;
		this.getDashboardHandler = getDashboardHandler;
	}

	@Autowired
	public void setMessageBus(MessageBus messageBus) {
		this.messageBus = messageBus;
	}

	@Override
	public OperationCompletionRS updateDashboard(ReportPortalUser.ProjectDetails projectDetails, UpdateDashboardRQ rq, Long dashboardId,
			ReportPortalUser user) {
		Dashboard dashboard = getDashboardHandler.getAdministrated(dashboardId);
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
		Dashboard dashboard = getDashboardHandler.getAdministrated(dashboardId);
		Widget widget = getWidgetHandler.getPermitted(rq.getObjectModel().getWidgetId());

		DashboardWidget dashboardWidget = WidgetConverter.toDashboardWidget(rq.getObjectModel(), dashboard, widget);
		dashboard.addWidget(dashboardWidget);
		dashboardRepository.save(dashboard);
		return new OperationCompletionRS(
				"Widget with ID = '" + widget.getId() + "' was successfully added to the dashboard with ID = '" + dashboard.getId() + "'");

	}

	@Override
	public OperationCompletionRS removeWidget(Long widgetId, Long dashboardId, ProjectDetails projectDetails, ReportPortalUser user) {
		Dashboard dashboard = getDashboardHandler.getPermitted(dashboardId);
		Widget widget = getWidgetHandler.getPermitted(widgetId);

		boolean isRemoved = dashboard.getDashboardWidgets()
				.removeIf(dashboardWidget -> widget.getId().equals(dashboardWidget.getId().getWidgetId()));

		expect(isRemoved, Predicate.isEqual(true)).verify(ErrorType.WIDGET_NOT_FOUND_IN_DASHBOARD, widgetId);

		widget.getDashboardWidgets().removeIf(dashboardWidget -> dashboard.getId().equals(dashboardWidget.getId().getDashboardId()));
		dashboardRepository.save(dashboard);
		return new OperationCompletionRS(
				"Widget with ID = '" + widget.getId() + "' was successfully removed to the dashboard with ID = '" + dashboard.getId()
						+ "'");
	}

}
