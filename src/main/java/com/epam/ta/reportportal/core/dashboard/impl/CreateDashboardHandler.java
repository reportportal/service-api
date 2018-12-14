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
import com.epam.ta.reportportal.auth.acl.ShareableObjectsHandler;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.DashboardCreatedEvent;
import com.epam.ta.reportportal.dao.DashboardRepository;
import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.ws.converter.builders.DashboardBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.dashboard.CreateDashboardRQ;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.ws.converter.converters.DashboardConverter.TO_ACTIVITY_RESOURCE;

/**
 * @author Pavel Bortnik
 */
@Service
public class CreateDashboardHandler implements com.epam.ta.reportportal.core.dashboard.CreateDashboardHandler {

	@Autowired
	private DashboardRepository dashboardRepository;

	@Autowired
	private MessageBus messageBus;

	@Autowired
	private ShareableObjectsHandler aclHandler;

	@Override
	public EntryCreatedRS createDashboard(ReportPortalUser.ProjectDetails projectDetails, CreateDashboardRQ rq, ReportPortalUser user) {
		Dashboard dashboard = new DashboardBuilder().addDashboardRq(rq)
				.addProject(projectDetails.getProjectId())
				.addOwner(user.getUsername())
				.get();
		dashboardRepository.save(dashboard);
		aclHandler.initAcl(dashboard, user.getUsername(), projectDetails.getProjectId(), BooleanUtils.isTrue(rq.getShare()));
		messageBus.publishActivity(new DashboardCreatedEvent(TO_ACTIVITY_RESOURCE.apply(dashboard), user.getUserId()));
		return new EntryCreatedRS(dashboard.getId());
	}
}
