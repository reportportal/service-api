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
package com.epam.ta.reportportal.demo_data;

import com.epam.ta.reportportal.database.dao.DashboardRepository;
import com.epam.ta.reportportal.database.dao.UserFilterRepository;
import com.epam.ta.reportportal.database.dao.WidgetRepository;
import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.widget.Widget;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.URL;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DemoDashboardsServiceTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	private final String filter = "filter";
	private final String user = "user";
	private final String project = "project";
	private final String dashboard = "dashboard";
	private final String widget = "widget";

	@Test
	public void demoFilterNameAlreadyExists() {
		final UserFilterRepository userFilterRepository = mock(UserFilterRepository.class);

		when(userFilterRepository.findOneByName(user, filter, project)).thenReturn(new UserFilter());
		DemoDashboardsService demoDashboardsService = new DemoDashboardsService(userFilterRepository, mock(DashboardRepository.class),
				mock(WidgetRepository.class), mock(ObjectMapper.class)
		);
		expectedException.expect(ReportPortalException.class);
		expectedException.expectMessage("Resource '" + filter + "' already exists. You couldn't create the duplicate.");
		demoDashboardsService.createDemoFilter(filter, user, project);
	}

	@Test
	public void demoDashboardNameAlreadyExists() {
		final DashboardRepository dashboardRepository = mock(DashboardRepository.class);
		when(dashboardRepository.findOneByUserProject(user, project, dashboard)).thenReturn(new Dashboard());
		final DemoDashboardsService demoDashboardsService = new DemoDashboardsService(mock(UserFilterRepository.class), dashboardRepository,
				mock(WidgetRepository.class), mock(ObjectMapper.class)
		);
		expectedException.expect(ReportPortalException.class);
		expectedException.expectMessage("Resource '" + dashboard + "' already exists. You couldn't create the duplicate");
		demoDashboardsService.createDemoDashboard(emptyList(), user, project, dashboard);
	}

	@Test
	public void widgetNameAlreadyExists() throws IOException {
		final String postfix = "postfix";
		final ObjectMapper objectMapper = mock(ObjectMapper.class);
		final Widget toSave = new Widget();
		toSave.setName(widget);
		final Resource resource = mock(Resource.class);
		when(objectMapper.readValue(any(URL.class), any(TypeReference.class))).thenReturn(singletonList(toSave));
		final WidgetRepository widgetRepository = mock(WidgetRepository.class);
		final Widget existing = new Widget();
		existing.setName(widget + "#" + postfix);
		when(widgetRepository.findByProjectAndUser(project, user)).thenReturn(singletonList(existing));
		final DemoDashboardsService dashboardsService = new DemoDashboardsService(mock(UserFilterRepository.class),
				mock(DashboardRepository.class), widgetRepository, objectMapper
		);

		dashboardsService.setResource(resource);
		expectedException.expect(ReportPortalException.class);
		expectedException.expectMessage("Resource '" + widget + "#" + postfix + "' already exists. You couldn't create the duplicate");
		dashboardsService.createWidgets(postfix, user, project, widget);
	}
}