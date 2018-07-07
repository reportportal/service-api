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

import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;

import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Pavel Bortnik
 */
public final class DashboardConverter {

	private DashboardConverter() {
		//static only
	}

	public static final Function<Dashboard, DashboardResource> TO_RESOURCE = dashboard -> {
		DashboardResource resource = new DashboardResource();
		resource.setDashboardId(dashboard.getId());
		resource.setName(dashboard.getName());
		resource.setDescription(dashboard.getDescription());
		resource.setWidgets(dashboard.getDashboardWidgets().stream().map(WidgetConverter.TO_OBJECT_MODEL).collect(Collectors.toList()));
		return resource;
	};

}
