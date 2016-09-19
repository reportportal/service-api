/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

import java.util.Date;

import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.project.EntryType;
import com.epam.ta.reportportal.ws.model.project.ProjectInfoResource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Builder for
 * {@link com.epam.ta.reportportal.ws.model.project.ProjectInfoResource}
 * 
 * @author Dzmitry_Kavalets
 */
@Service
@Scope("prototype")
public class ProjectInfoResourceBuilder extends ResourceBuilder<ProjectInfoResource> {

	public ProjectInfoResourceBuilder addProject(Project project) {
		getObject().setUsersQuantity(null != project.getUsers() ? project.getUsers().size() : 0);
		getObject().setProjectId(project.getId());
		getObject().setCreationDate(project.getCreationDate());
		String entryType = null == project.getConfiguration() ? EntryType.INTERNAL.name() : null == project.getConfiguration()
				.getEntryType() ? EntryType.INTERNAL.name() : project.getConfiguration().getEntryType().name();
		getObject().setEntryType(entryType);
		return this;
	}

	public ProjectInfoResourceBuilder addLastRun(Date lastRun) {
		getObject().setLastRun(lastRun);
		return this;
	}

	public ProjectInfoResourceBuilder addLaunchesQuantity(Long quantity) {
		getObject().setLaunchesQuantity(quantity);
		return this;
	}

	@Override
	protected ProjectInfoResource initObject() {
		return new ProjectInfoResource();
	}
}