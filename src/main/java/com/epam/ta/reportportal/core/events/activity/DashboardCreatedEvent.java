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
package com.epam.ta.reportportal.core.events.activity;

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
