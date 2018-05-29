/*
 * Copyright 2018 EPAM Systems
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

package com.epam.ta.reportportal.ws.controller;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.store.commons.EntityUtils;
import com.epam.ta.reportportal.store.database.dao.DashboardRepository;
import com.epam.ta.reportportal.store.database.dao.WidgetRepository;
import com.epam.ta.reportportal.store.database.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.store.database.entity.dashboard.DashboardWidget;
import com.epam.ta.reportportal.store.database.entity.dashboard.DashboardWidgetId;
import com.epam.ta.reportportal.store.database.entity.project.Project;
import com.epam.ta.reportportal.store.database.entity.widget.Widget;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.dashboard.CreateDashboardRQ;
import com.epam.ta.reportportal.ws.model.dashboard.UpdateDashboardRQ;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

/**
 * @author Pavel Bortnik
 */
@Controller
@RequestMapping("/{projectName}/dashboard")
public class DashboardController {

	@Autowired
	private DashboardRepository dashboardRepository;

	@Autowired
	private WidgetRepository widgetRepository;

	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(CREATED)
	@ResponseBody
	@ApiOperation("Create dashboard for specified project")
	public EntryCreatedRS createDashboard(@PathVariable String projectName, @RequestBody @Validated CreateDashboardRQ createRQ,
			@AuthenticationPrincipal ReportPortalUser user) {

		ReportPortalUser.ProjectDetails projectDetails = EntityUtils.takeProjectDetails(user, projectName);
		Project project = new Project();
		project.setId(projectDetails.getProjectId());

		Dashboard dashboard = new Dashboard();
		dashboard.setName(createRQ.getName());
		dashboard.setDescription(createRQ.getDescription());
		dashboard.setProjectId(projectDetails.getProjectId());

		dashboardRepository.save(dashboard);

		return new EntryCreatedRS(dashboard.getId());
	}

	@RequestMapping(value = "/{dashboardId}", method = RequestMethod.PUT)
	@ResponseBody
	@ResponseStatus(OK)
	@ApiOperation("Update specified dashboard for specified project")
	public OperationCompletionRS updateDashboard(@PathVariable String projectName, @PathVariable Long dashboardId,
			@RequestBody @Validated UpdateDashboardRQ updateRQ, Principal principal, @AuthenticationPrincipal ReportPortalUser user) {

		Dashboard dashboard = dashboardRepository.findById(dashboardId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.DASHBOARD_NOT_FOUND));
		Widget widget = widgetRepository.findById(Long.valueOf(updateRQ.getAddWidget().getWidgetId()))
				.orElseThrow(() -> new ReportPortalException(ErrorType.WIDGET_NOT_FOUND));

		DashboardWidgetId id = new DashboardWidgetId();
		id.setWidgetId(widget.getId());
		id.setDashboardId(dashboard.getId());

		DashboardWidget dashboardWidget = new DashboardWidget();
		dashboardWidget.setDashboard(dashboard);
		dashboardWidget.setWidget(widget);
		dashboardWidget.setHeigth(0);
		dashboardWidget.setWidth(0);
		dashboardWidget.setPositionX(0);
		dashboardWidget.setPositionY(0);
		dashboard.addWidget(dashboardWidget);
		widget.getDashboardWidgets().add(dashboardWidget);

		dashboardRepository.save(dashboard);
		return new OperationCompletionRS("ok");
	}

}
