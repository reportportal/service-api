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

package com.epam.ta.reportportal.ws.converter.builders;

import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.database.entity.Dashboard.WidgetObject;
import com.epam.ta.reportportal.ws.model.dashboard.CreateDashboardRQ;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Builder for {@link Dashboard} persistence layer object.
 *
 * @author Aliaksei_Makayed
 */

@Service
@Scope("prototype")
public class DashboardBuilder extends ShareableEntityBuilder<Dashboard> {

	public DashboardBuilder addCreateDashboardRQ(CreateDashboardRQ request) {
		if (request != null) {
			getObject().setName(request.getName().trim());
		}
		return this;
	}

	public DashboardBuilder addProject(String projectName) {
		getObject().setProjectName(projectName);
		return this;
	}

	public DashboardBuilder addWidgets(List<WidgetObject> widgets) {
		getObject().setWidgets(widgets);
		return this;
	}

	public DashboardBuilder addSharing(String owner, String project, String description, boolean isShare) {
		super.addAcl(owner, project, description, isShare);
		return this;
	}

	@Override
	protected Dashboard initObject() {
		return new Dashboard();
	}
}