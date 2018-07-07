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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.entity.dashboard.DashboardWidget;
import com.epam.ta.reportportal.ws.model.dashboard.CreateDashboardRQ;
import com.epam.ta.reportportal.ws.model.dashboard.UpdateDashboardRQ;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;

/**
 * @author Pavel Bortnik
 */
public class DashboardBuilder implements Supplier<Dashboard> {

	private Dashboard dashboard;

	public DashboardBuilder() {
		dashboard = new Dashboard();
	}

	public DashboardBuilder(Dashboard dashboard) {
		this.dashboard = dashboard;
	}

	public DashboardBuilder addDashboardRq(CreateDashboardRQ rq) {
		dashboard.setName(rq.getName());
		dashboard.setDescription(rq.getDescription());
		return this;
	}

	public DashboardBuilder addProject(Long projectId) {
		dashboard.setProjectId(projectId);
		return this;
	}

	public DashboardBuilder addUpdateRq(UpdateDashboardRQ rq) {
		Optional.ofNullable(rq.getName()).ifPresent(name -> dashboard.setName(name));
		Optional.ofNullable(rq.getDescription()).ifPresent(description -> dashboard.setDescription(description));
		Optional.ofNullable(rq.getWidgets()).ifPresent(widgets -> {
			for (DashboardWidget dashboardWidget : dashboard.getDashboardWidgets()) {
				widgets.stream()
						.filter(updWidget -> Objects.equals(dashboardWidget.getId().getWidgetId(), updWidget.getWidgetId()))
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
		return this;
	}

	@Override
	public Dashboard get() {
		return dashboard;
	}
}
