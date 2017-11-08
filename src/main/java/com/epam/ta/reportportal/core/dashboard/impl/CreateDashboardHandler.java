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

package com.epam.ta.reportportal.core.dashboard.impl;

import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.core.dashboard.ICreateDashboardHandler;
import com.epam.ta.reportportal.database.dao.DashboardRepository;
import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.events.DashboardCreatedEvent;
import com.epam.ta.reportportal.ws.converter.builders.DashboardBuilder;
import com.epam.ta.reportportal.ws.model.EntryCreatedRS;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.dashboard.CreateDashboardRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.inject.Provider;

/**
 * Default implementation of {@link ICreateDashboardHandler}
 *
 * @author Aliaksei_Makayed
 */
@Service
public class CreateDashboardHandler implements ICreateDashboardHandler {

	@Autowired
	private DashboardRepository dashboardRepository;

	@Autowired
	private Provider<DashboardBuilder> dashboardBuilder;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	@Override
	public EntryCreatedRS createDashboard(String projectName, CreateDashboardRQ rq, String userName) {
		// if can user login to server
		// user and project objects exists in DB, so additional check here
		// redundant.

		/* UI consolidation with trimming spaces */
		rq.setName(rq.getName().trim());

		/* Validation for unique dash name per user+project */
		Dashboard isExist = dashboardRepository.findOneByUserProject(userName, projectName, rq.getName());
		BusinessRule.expect(isExist, Predicates.isNull()).verify(ErrorType.RESOURCE_ALREADY_EXISTS, rq.getName());

		Dashboard dashboard = dashboardBuilder.get()
				.addCreateDashboardRQ(rq)
				.addSharing(userName, projectName, rq.getDescription(), rq.getShare() == null ? false : rq.getShare())
				.addProject(projectName)
				.build();
		dashboardRepository.save(dashboard);
		eventPublisher.publishEvent(new DashboardCreatedEvent(rq, userName, projectName, dashboard.getId()));
		return new EntryCreatedRS(dashboard.getId());
	}
}