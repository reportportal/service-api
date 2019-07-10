/*
 * Copyright 2019 EPAM Systems
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
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.dashboard.CreateDashboardHandler;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.DashboardCreatedEvent;
import com.epam.ta.reportportal.dao.DashboardRepository;
import com.epam.ta.reportportal.entity.dashboard.Dashboard;
import com.epam.ta.reportportal.ws.converter.builders.DashboardBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.dashboard.CreateDashboardRQ;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.epam.ta.reportportal.ws.converter.converters.DashboardConverter.TO_ACTIVITY_RESOURCE;

/**
 * @author Pavel Bortnik
 */
@Service
public class CreateDashboardHandlerImpl implements CreateDashboardHandler {

	private final DashboardRepository dashboardRepository;
	private final MessageBus messageBus;
	private final ShareableObjectsHandler aclHandler;

	@Autowired
	public CreateDashboardHandlerImpl(DashboardRepository dashboardRepository, MessageBus messageBus, ShareableObjectsHandler aclHandler) {
		this.dashboardRepository = dashboardRepository;
		this.messageBus = messageBus;
		this.aclHandler = aclHandler;
	}

	@Override
	public EntryCreatedRS createDashboard(ReportPortalUser.ProjectDetails projectDetails, CreateDashboardRQ rq, ReportPortalUser user) {

		BusinessRule.expect(dashboardRepository.existsByNameAndOwnerAndProjectId(rq.getName(),
				user.getUsername(),
				projectDetails.getProjectId()
		), BooleanUtils::isFalse).verify(ErrorType.RESOURCE_ALREADY_EXISTS, rq.getName());

		Dashboard dashboard = new DashboardBuilder().addDashboardRq(rq)
				.addProject(projectDetails.getProjectId())
				.addOwner(user.getUsername())
				.get();
		dashboardRepository.save(dashboard);
		aclHandler.initAcl(dashboard, user.getUsername(), projectDetails.getProjectId(), BooleanUtils.isTrue(rq.getShare()));
		messageBus.publishActivity(new DashboardCreatedEvent(TO_ACTIVITY_RESOURCE.apply(dashboard), user.getUserId(), user.getUsername()));
		return new EntryCreatedRS(dashboard.getId());
	}
}
