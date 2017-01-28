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

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource.WidgetObjectModel;

/**
 * Builder for {@link DashboardResource} domain model object
 * 
 * @author Aliaksei_Makayed
 * 
 */
@Service
@Scope("prototype")
public class DashboardResourceBuilder extends Builder<DashboardResource> {

	public DashboardResourceBuilder addDashboard(Dashboard dashboard) {
		if (dashboard != null) {
			getObject().setDashboardId(dashboard.getId());
			getObject().setName(dashboard.getName());
			getObject().setDescription(dashboard.getDescription());
			if (null != dashboard.getWidgets()) {
				List<WidgetObjectModel> models = dashboard.getWidgets().stream()
						.map(widgetObject -> new WidgetObjectModel(widgetObject.getWidgetId(), widgetObject.getWidgetSize(),
								widgetObject.getWidgetPosition()))
						.collect(Collectors.toList());
				getObject().setWidgets(models);
			}
			if (null != dashboard.getAcl()) {
				getObject().setOwner(dashboard.getAcl().getOwnerUserId());
				getObject().setIsShared(!dashboard.getAcl().getEntries().isEmpty());
			}
		}
		return this;
	}

	@Override
	protected DashboardResource initObject() {
		return new DashboardResource();
	}

}
