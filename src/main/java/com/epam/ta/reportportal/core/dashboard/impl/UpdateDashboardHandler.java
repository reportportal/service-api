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
import com.epam.ta.reportportal.store.database.dao.DashboardRepository;
import com.epam.ta.reportportal.store.database.dao.WidgetRepository;
import com.epam.ta.reportportal.store.database.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.store.database.entity.dashboard.DashboardWidget;
import com.epam.ta.reportportal.store.database.entity.dashboard.DashboardWidgetId;
import com.epam.ta.reportportal.store.database.entity.widget.Widget;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.dashboard.AddWidgetRq;
import com.epam.ta.reportportal.ws.model.dashboard.UpdateDashboardRQ;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * @author Pavel Bortnik
 */
public class UpdateDashboardHandler implements IUpdateDashboardHandler {

	private DashboardRepository dashboardRepository;

	private WidgetRepository widgetRepository;

	@Autowired
	public void setDashboardRepository(DashboardRepository dashboardRepository) {
		this.dashboardRepository = dashboardRepository;
	}

	@Override
	public OperationCompletionRS updateDashboard(ReportPortalUser.ProjectDetails projectDetails, UpdateDashboardRQ rq, Long dashboardId,
			ReportPortalUser user) {

		Dashboard dashboard = dashboardRepository.findById(dashboardId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.DASHBOARD_NOT_FOUND));

		Optional.ofNullable(rq.getWidgets()).ifPresent(widgets -> {
			for (DashboardWidget dashboardWidget : dashboard.getDashboardWidgets()) {
				widgets.stream()
						.filter(updWidget -> Objects.equals(dashboardWidget.getWidget().getId(), updWidget.getWidgetId()))
						.forEach(updWidget -> {
							ofNullable(updWidget.getWidgetPosition()).ifPresent(position -> {
								dashboardWidget.setPositionX(position.getX());
								dashboardWidget.setPositionY(position.getY());
							});
							ofNullable(updWidget.getWidgetSize()).ifPresent(size -> {
								dashboardWidget.setWidth(size.getWidth());
								dashboardWidget.setHeight(size.getHeight());
							});
						});
			}
		});
		dashboardRepository.save(dashboard);
		return new OperationCompletionRS("ok");
	}

	@Override
	public OperationCompletionRS addWidget(ReportPortalUser.ProjectDetails projectDetails, AddWidgetRq rq, ReportPortalUser user) {

		Dashboard dashboard = dashboardRepository.findById(rq.getDashboardId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.DASHBOARD_NOT_FOUND));

		Widget widget = widgetRepository.findById(rq.getWidgetId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.DASHBOARD_NOT_FOUND));

		DashboardWidget dashboardWidget = new DashboardWidget();
		dashboardWidget.setId(new DashboardWidgetId(dashboard.getId(), widget.getId()));

		dashboard.addWidget(dashboardWidget);
		widget.getDashboardWidgets().add(dashboardWidget);

		dashboardRepository.save(dashboard);

		return new OperationCompletionRS("ok");

	}

	@Override
	public OperationCompletionRS removeWidget(Long widgetId, Long dashboardId) {
		return null;
	}
}
