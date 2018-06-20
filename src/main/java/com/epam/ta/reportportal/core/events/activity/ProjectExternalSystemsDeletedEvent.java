/*
 * Copyright 2016 EPAM Systems
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
package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.store.database.entity.integration.Integration;

/**
 * @author Andrei Varabyeu
 */
public class ProjectExternalSystemsDeletedEvent {

	private final Iterable<Integration> integrations;
	private final String project;
	private final String deletedBy;

	public ProjectExternalSystemsDeletedEvent(Iterable<Integration> integrations, String project, String deletedBy) {
		this.integrations = integrations;
		this.project = project;
		this.deletedBy = deletedBy;
	}

	public Iterable<Integration> getIntegrations() {
		return integrations;
	}

	public String getProject() {
		return project;
	}

	public String getDeletedBy() {
		return deletedBy;
	}
}