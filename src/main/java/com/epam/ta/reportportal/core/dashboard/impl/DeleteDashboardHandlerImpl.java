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

package com.epam.ta.reportportal.core.dashboard.impl;

import com.epam.ta.reportportal.auth.acl.ShareableObjectsHandler;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.dashboard.DeleteDashboardHandler;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.DashboardDeletedEvent;
import com.epam.ta.reportportal.core.shareable.GetShareableEntityHandler;
import com.epam.ta.reportportal.core.widget.content.remover.WidgetContentRemover;
import com.epam.ta.reportportal.dao.DashboardRepository;
import com.epam.ta.reportportal.dao.DashboardWidgetRepository;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.entity.dashboard.DashboardWidget;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.ws.converter.converters.DashboardConverter.TO_ACTIVITY_RESOURCE;
import static java.util.stream.Collectors.toSet;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class DeleteDashboardHandlerImpl implements DeleteDashboardHandler {

	private final GetShareableEntityHandler<Dashboard> getShareableEntityHandler;
	private final DashboardRepository dashboardRepository;
	private final DashboardWidgetRepository dashboardWidgetRepository;
	private final WidgetRepository widgetRepository;
	private final ShareableObjectsHandler aclHandler;
	private final List<WidgetContentRemover> widgetContentRemovers;
	private final MessageBus messageBus;

	@Autowired
	public DeleteDashboardHandlerImpl(GetShareableEntityHandler<Dashboard> getShareableEntityHandler,
			DashboardRepository dashboardRepository, DashboardWidgetRepository dashboardWidgetRepository, WidgetRepository widgetRepository,
			ShareableObjectsHandler aclHandler, List<WidgetContentRemover> widgetContentRemovers, MessageBus messageBus) {
		this.getShareableEntityHandler = getShareableEntityHandler;
		this.dashboardRepository = dashboardRepository;
		this.dashboardWidgetRepository = dashboardWidgetRepository;
		this.widgetRepository = widgetRepository;
		this.aclHandler = aclHandler;
		this.widgetContentRemovers = widgetContentRemovers;
		this.messageBus = messageBus;
	}

	@Override
	public OperationCompletionRS deleteDashboard(Long dashboardId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		Dashboard dashboard = getShareableEntityHandler.getAdministrated(dashboardId, projectDetails);

		Set<DashboardWidget> dashboardWidgets = dashboard.getDashboardWidgets();
		List<Widget> widgets = dashboardWidgets.stream()
				.filter(DashboardWidget::isCreatedOn)
				.map(DashboardWidget::getWidget)
				.peek(aclHandler::deleteAclForObject)
				.peek(widget -> widgetContentRemovers.forEach(remover -> remover.removeContent(widget)))
				.collect(Collectors.toList());
		dashboardWidgets.addAll(widgets.stream().flatMap(w -> w.getDashboardWidgets().stream()).collect(toSet()));

		aclHandler.deleteAclForObject(dashboard);
		dashboardWidgetRepository.deleteAll(dashboardWidgets);
		dashboardRepository.delete(dashboard);
		widgetRepository.deleteAll(widgets);

		messageBus.publishActivity(new DashboardDeletedEvent(TO_ACTIVITY_RESOURCE.apply(dashboard), user.getUserId(), user.getUsername()));
		return new OperationCompletionRS("Dashboard with ID = '" + dashboardId + "' successfully deleted.");
	}
}
