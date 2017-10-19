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
package com.epam.ta.reportportal.events;

import com.epam.ta.reportportal.ws.model.dashboard.CreateDashboardRQ;

/**
 * @author pavel_bortnik
 */
public class DashboardCreatedEvent {

	private final CreateDashboardRQ createDashboardRQ;
	private final String createdBy;
	private final String projectRef;
	private final String dashboardId;

	public DashboardCreatedEvent(CreateDashboardRQ createDashboardRQ, String createdBy, String projectRef, String dashboardId) {
		this.createDashboardRQ = createDashboardRQ;
		this.createdBy = createdBy;
		this.projectRef = projectRef;
		this.dashboardId = dashboardId;
	}

	public CreateDashboardRQ getCreateDashboardRQ() {
		return createDashboardRQ;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public String getProjectRef() {
		return projectRef;
	}

	public String getDashboardId() {
		return dashboardId;
	}
}
