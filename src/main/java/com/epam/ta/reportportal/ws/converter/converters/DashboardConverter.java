/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.ws.model.activity.DashboardActivityResource;
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

	public static final Function<Dashboard, DashboardActivityResource> TO_ACTIVITY_RESOURCE = dashboard -> {
		DashboardActivityResource resource = new DashboardActivityResource();
		resource.setId(dashboard.getId());
		resource.setName(dashboard.getName());
		resource.setProjectId(dashboard.getProjectId());
		resource.setDescription(dashboard.getDescription());
		//		resource.setShared(dashboard.getShared);
		return resource;
	};

}
