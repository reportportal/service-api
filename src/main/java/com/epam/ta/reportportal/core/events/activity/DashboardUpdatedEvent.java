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

import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.ws.model.dashboard.UpdateDashboardRQ;

/**
 * @author Andrei Varabyeu
 */
public class DashboardUpdatedEvent {

	private final Dashboard dashboard;
	private final UpdateDashboardRQ updateRQ;
	private final String updatedBy;

	public DashboardUpdatedEvent(Dashboard dashboard, UpdateDashboardRQ updateRQ, String updatedBy) {
		this.dashboard = dashboard;
		this.updateRQ = updateRQ;
		this.updatedBy = updatedBy;
	}

	public Dashboard getDashboard() {
		return dashboard;
	}

	public UpdateDashboardRQ getUpdateRQ() {
		return updateRQ;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}
}
