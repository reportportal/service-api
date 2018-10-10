/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.core.dashboard.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.core.dashboard.IUpdateDashboardHandler;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.dao.DashboardRepository;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.entity.dashboard.DashboardWidget;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.ws.converter.builders.DashboardBuilder;
import com.epam.ta.reportportal.ws.converter.converters.WidgetConverter;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.dashboard.AddWidgetRq;
import com.epam.ta.reportportal.ws.model.dashboard.UpdateDashboardRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Predicate;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;

/**
 * @author Pavel Bortnik
 */
@Service
public class UpdateDashboardHandler implements IUpdateDashboardHandler {

	private DashboardRepository dashboardRepository;

	private WidgetRepository widgetRepository;

	@Autowired
	public void setDashboardRepository(DashboardRepository dashboardRepository) {
		this.dashboardRepository = dashboardRepository;
	}

	@Autowired
	public void setWidgetRepository(WidgetRepository widgetRepository) {
		this.widgetRepository = widgetRepository;
	}

	@Override
	public OperationCompletionRS updateDashboard(ReportPortalUser.ProjectDetails projectDetails, UpdateDashboardRQ rq, Long dashboardId,
			ReportPortalUser user) {

		Dashboard dashboard = dashboardRepository.findById(dashboardId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.DASHBOARD_NOT_FOUND));

		dashboard = new DashboardBuilder(dashboard).addUpdateRq(rq).get();
		dashboardRepository.save(dashboard);

		return new OperationCompletionRS("Dashboard with ID = '" + dashboard.getId() + "' successfully updated");
	}

	@Override
	public OperationCompletionRS addWidget(Long dashboardId, ReportPortalUser.ProjectDetails projectDetails, AddWidgetRq rq,
			ReportPortalUser user) {

		Dashboard dashboard = dashboardRepository.findById(dashboardId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.DASHBOARD_NOT_FOUND));

		Widget widget = widgetRepository.findById(rq.getObjectModel().getWidgetId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.DASHBOARD_NOT_FOUND));

		DashboardWidget dashboardWidget = WidgetConverter.toDashboardWidget(rq.getObjectModel(), dashboard, widget);

		dashboard.addWidget(dashboardWidget);
		dashboardRepository.save(dashboard);

		return new OperationCompletionRS(
				"Widget with ID = '" + widget.getId() + "' was successfully added to the dashboard with ID = '" + dashboard.getId() + "'");

	}

	@Override
	public OperationCompletionRS removeWidget(Long widgetId, Long dashboardId, ReportPortalUser.ProjectDetails projectDetails) {

		Dashboard dashboard = dashboardRepository.findById(dashboardId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.DASHBOARD_NOT_FOUND));

		Widget widget = widgetRepository.findById(widgetId).orElseThrow(() -> new ReportPortalException(ErrorType.WIDGET_NOT_FOUND));

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
