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
