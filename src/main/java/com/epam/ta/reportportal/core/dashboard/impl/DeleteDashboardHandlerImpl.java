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
import com.epam.ta.reportportal.auth.acl.ShareableObjectsHandler;
import com.epam.ta.reportportal.core.dashboard.DeleteDashboardHandler;
import com.epam.ta.reportportal.core.dashboard.GetDashboardHandler;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.DashboardDeletedEvent;
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

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class DeleteDashboardHandlerImpl implements DeleteDashboardHandler {

	@Autowired
	private GetDashboardHandler getDashboardHandler;

	@Autowired
	private DashboardRepository dashboardRepository;

	@Autowired
	private DashboardWidgetRepository dashboardWidgetRepository;

	@Autowired
	private WidgetRepository widgetRepository;

	@Autowired
	private ShareableObjectsHandler aclHandler;

	@Autowired
	private MessageBus messageBus;

	@Override
	public OperationCompletionRS deleteDashboard(Long dashboardId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		Dashboard dashboard = getDashboardHandler.getAdministrated(dashboardId);

		List<Widget> ownedWidgets = dashboard.getDashboardWidgets()
				.stream()
				.map(DashboardWidget::getWidget)
				.filter(widget -> widget.getOwner().equalsIgnoreCase(user.getUsername()))
				.collect(Collectors.toList());

		Set<DashboardWidget> dashboardWidgets = ownedWidgets.stream()
				.flatMap(it -> it.getDashboardWidgets().stream())
				.collect(Collectors.toSet());
		dashboardWidgets.addAll(dashboard.getDashboardWidgets());

		aclHandler.deleteAclForObject(dashboard);
		ownedWidgets.forEach(it -> aclHandler.deleteAclForObject(it));

		dashboardWidgetRepository.deleteAll(dashboardWidgets);
		dashboardRepository.delete(dashboard);
		widgetRepository.deleteAll(ownedWidgets);

		messageBus.publishActivity(new DashboardDeletedEvent(TO_ACTIVITY_RESOURCE.apply(dashboard), user.getUserId()));
		return new OperationCompletionRS("Dashboard with ID = '" + dashboardId + "' successfully deleted.");
	}
}
