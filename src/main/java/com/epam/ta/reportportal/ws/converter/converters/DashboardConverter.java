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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Converts internal DB model to DTO
 *
 * @author Pavel Bortnik
 */
public final class DashboardConverter {

	private DashboardConverter() {
		//static only
	}

	public static final Function<Dashboard, DashboardResource> TO_RESOURCE = dashboard -> {
		Preconditions.checkNotNull(dashboard);
		DashboardResource resource = new DashboardResource();
		resource.setDashboardId(dashboard.getId());
		resource.setName(dashboard.getName());
		resource.setDescription(dashboard.getDescription());
		resource.setWidgets(Optional.ofNullable(dashboard.getWidgets())
				.orElseGet(Collections::emptyList)
				.stream()
				.map(DashboardConverter.TO_WIDGET_RESOURCE)
				.collect(Collectors.toList()));
		Optional.ofNullable(dashboard.getAcl()).ifPresent(acl -> {
			resource.setOwner(acl.getOwnerUserId());
			resource.setShare(!acl.getEntries().isEmpty());
		});
		return resource;
	};

	private static final Function<Dashboard.WidgetObject, DashboardResource.WidgetObjectModel> TO_WIDGET_RESOURCE = widgetObject -> {
		DashboardResource.WidgetObjectModel resource = new DashboardResource.WidgetObjectModel();
		resource.setWidgetId(widgetObject.getWidgetId());
		resource.setWidgetPosition(widgetObject.getWidgetPosition());
		resource.setWidgetSize(widgetObject.getWidgetSize());
		return resource;
	};

}
