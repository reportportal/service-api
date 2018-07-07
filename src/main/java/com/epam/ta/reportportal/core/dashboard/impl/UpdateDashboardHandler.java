/*
 * Copyright 2017 EPAM Systems
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
