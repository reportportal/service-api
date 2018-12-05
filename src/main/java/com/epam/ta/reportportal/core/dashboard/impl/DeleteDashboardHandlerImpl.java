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

package com.epam.ta.reportportal.core.dashboard.impl;

import com.epam.ta.reportportal.auth.ReportPortalUser;
import com.epam.ta.reportportal.auth.acl.ReportPortalAclHandler;
import com.epam.ta.reportportal.core.dashboard.DeleteDashboardHandler;
import com.epam.ta.reportportal.core.dashboard.GetDashboardHandler;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.DashboardDeletedEvent;
import com.epam.ta.reportportal.dao.DashboardRepository;
import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.ws.converter.converters.DashboardConverter.TO_ACTIVITY_RESOURCE;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class DeleteDashboardHandlerImpl implements DeleteDashboardHandler {

	@Autowired
	private GetDashboardHandler getDashboardHandler;

	@Autowired
	private DashboardRepository dashboardRepository;

	@Autowired
	private ReportPortalAclHandler aclHandler;

	@Autowired
	private MessageBus messageBus;

	@Override
	public OperationCompletionRS deleteDashboard(Long dashboardId, ReportPortalUser.ProjectDetails projectDetails, ReportPortalUser user) {
		Dashboard dashboard = getDashboardHandler.getDashboard(dashboardId, projectDetails, user);
		dashboardRepository.delete(dashboard);
		aclHandler.deleteAclForObject(dashboard);
		messageBus.publishActivity(new DashboardDeletedEvent(TO_ACTIVITY_RESOURCE.apply(dashboard), user.getUserId()));
		return new OperationCompletionRS("Dashboard with ID = '" + dashboardId + "' successfully deleted.");
	}
}
