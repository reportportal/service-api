/*
 * Copyright 2022 EPAM Systems
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

package com.epam.ta.reportportal.core.dashboard.impl;

import com.epam.ta.reportportal.auth.acl.ShareableObjectsHandler;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.core.dashboard.UpdateDashboardHandler;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.shareable.GetShareableEntityHandler;
import com.epam.ta.reportportal.core.widget.UpdateWidgetHandler;
import com.epam.ta.reportportal.core.widget.content.remover.WidgetContentRemover;
import com.epam.ta.reportportal.dao.DashboardRepository;
import com.epam.ta.reportportal.dao.DashboardWidgetRepository;
import com.epam.ta.reportportal.dao.WidgetRepository;
import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.entity.dashboard.DashboardWidget;
import com.epam.ta.reportportal.entity.dashboard.DashboardWidgetId;
import com.epam.ta.reportportal.entity.project.ProjectRole;
import com.epam.ta.reportportal.entity.user.UserRole;
import com.epam.ta.reportportal.entity.widget.Widget;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.Position;
import com.epam.ta.reportportal.ws.model.Size;
import com.epam.ta.reportportal.ws.model.dashboard.AddWidgetRq;
import com.epam.ta.reportportal.ws.model.dashboard.DashboardResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static com.epam.ta.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.ta.reportportal.util.TestProjectExtractor.extractProjectDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Andrei Piankouski
 */
@ExtendWith(MockitoExtension.class)
public class UpdateDashboardHandlerImplTest {

	@Mock
	private DashboardRepository dashboardRepository;

	@Mock
	private UpdateWidgetHandler updateWidgetHandler;

	@Mock
	private WidgetContentRemover widgetContentRemover;

	@Mock
	private WidgetRepository widgetRepository;

	@Mock
	private MessageBus messageBus;

	@Mock
	private ShareableObjectsHandler aclHandler;

	@Mock
	private GetShareableEntityHandler<Dashboard> getShareableDashboardHandler;

	@Mock
	private DashboardWidgetRepository dashboardWidgetRepository;

	@Mock
	private GetShareableEntityHandler<Widget> getShareableWidgetHandler;

	private UpdateDashboardHandlerImpl updateDashboardHandler;

	private final static int MAX_WIDGET_ON_DASHBOARD = 300;

	@BeforeEach
	void init() {
		updateDashboardHandler = new UpdateDashboardHandlerImpl(
				dashboardRepository,
				updateWidgetHandler,
				widgetContentRemover,
				messageBus,
				getShareableDashboardHandler,
				getShareableWidgetHandler,
				aclHandler,
				dashboardWidgetRepository,
				widgetRepository
		);
	}

	@Test
	void addWidgetExceedLimit() {
		final ReportPortalUser rpUser = getRpUser("owner", UserRole.USER, ProjectRole.MEMBER, 1L);

		AddWidgetRq addWidgetRq = new AddWidgetRq();
		addWidgetRq.setAddWidget(new DashboardResource.WidgetObjectModel("existed0", 10L, new Size(5, 5), new Position(0, 0)));
		Long dashboardId = 67L;
		ReportPortalUser.ProjectDetails projectDetails = extractProjectDetails(rpUser, "test_project");

		when(getShareableDashboardHandler.getAdministrated(dashboardId, projectDetails)).thenReturn(getDashboardWithExceedLimit());

		final ReportPortalException exception = assertThrows(ReportPortalException.class,
				() -> updateDashboardHandler.addWidget(67L, projectDetails, addWidgetRq, rpUser)
		);

		assertEquals(Suppliers.formattedSupplier(
				"Dashboard update request contains invalid data. The limit of {} dashboards has been reached. To create a new one you need to delete at least one created previously.",
				MAX_WIDGET_ON_DASHBOARD
		).get(), exception.getMessage());
	}

	@Test
	void addWidgetPositive() {
		final ReportPortalUser rpUser = getRpUser("owner", UserRole.USER, ProjectRole.MEMBER, 1L);

		AddWidgetRq addWidgetRq = new AddWidgetRq();
		Long widgetId = 10L;
		addWidgetRq.setAddWidget(new DashboardResource.WidgetObjectModel("existed0", widgetId, new Size(5, 5), new Position(0, 0)));
		Long dashboardId = 67L;
		ReportPortalUser.ProjectDetails projectDetails = extractProjectDetails(rpUser, "test_project");
		Widget widget = new Widget();
		widget.setName("testWidget");

		when(getShareableDashboardHandler.getAdministrated(dashboardId, projectDetails)).thenReturn(getDashboard());
		when(getShareableWidgetHandler.getPermitted(widgetId, projectDetails)).thenReturn(widget);

		updateDashboardHandler.addWidget(dashboardId, projectDetails, addWidgetRq, rpUser);

		verify(dashboardWidgetRepository).save(any(DashboardWidget.class));
	}

	private Dashboard getDashboardWithExceedLimit() {
		Dashboard dashboard = getDashboard();

		for (long i = 0; i < MAX_WIDGET_ON_DASHBOARD; i++) {
			DashboardWidget dashboardWidget = new DashboardWidget();
			dashboardWidget.setId(new DashboardWidgetId(67L, i));
			dashboardWidget.setWidgetName("existed" + i);
			dashboard.addWidget(dashboardWidget);
		}

		return dashboard;
	}

	private Dashboard getDashboard() {
		Dashboard dashboard = new Dashboard();
		dashboard.setName("testDashboard");
		dashboard.setDescription("Dashboard for test");
		dashboard.setCreationDate(LocalDateTime.now());

		return dashboard;
	}
}
