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
package com.epam.ta.reportportal.events;

import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.ws.model.project.UpdateProjectRQ;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Being triggered on after project update
 *
 * @author Andrei Varabyeu
 */
public class ProjectUpdatedEvent extends AroundEvent<Project> {

	private final String updatedBy;
	private final UpdateProjectRQ updateProjectRQ;

	/**
	 * Create a new ApplicationEvent.
	 *
	 * @param before Project before update
	 * @param after  Project after update
	 */
	public ProjectUpdatedEvent(Project before, Project after, String updatedBy, UpdateProjectRQ updateProjectRQ) {
		super(before, after);
		Preconditions.checkArgument(!Strings.isNullOrEmpty(updatedBy));
		this.updatedBy = updatedBy;
		this.updateProjectRQ = updateProjectRQ;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public UpdateProjectRQ getUpdateProjectRQ() {
		return updateProjectRQ;
	}
}
