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
package com.epam.ta.reportportal.events.handler;

import com.epam.ta.reportportal.database.dao.ActivityRepository;
import com.epam.ta.reportportal.database.entity.Dashboard;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.events.DashboardCreatedEvent;
import com.epam.ta.reportportal.events.DashboardDeletedEvent;
import com.epam.ta.reportportal.events.DashboardUpdatedEvent;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.dashboard.CreateDashboardRQ;
import com.epam.ta.reportportal.ws.model.dashboard.UpdateDashboardRQ;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static com.epam.ta.reportportal.database.entity.item.ActivityEventType.*;
import static com.epam.ta.reportportal.database.entity.item.ActivityObjectType.DASHBOARD;
import static com.epam.ta.reportportal.events.handler.EventHandlerUtil.*;

/**
 * @author Andrei Varabyeu
 * @author Pavel Bortnik
 */
@Component
public class DashboardActivityEventHandler {

	@Autowired
	private ActivityRepository activityRepository;

	@EventListener
	public void onDashboardUpdate(DashboardUpdatedEvent event) {
		Dashboard dashboard = event.getDashboard();
		UpdateDashboardRQ updateRQ = event.getUpdateRQ();
		List<Activity.FieldValues> history = Lists.newArrayList();
		if (dashboard != null) {
			processShare(history, dashboard, updateRQ.getShare());
			processName(history, dashboard.getName(), updateRQ.getName());
			processDescription(history, dashboard.getDescription(), updateRQ.getDescription());
			if (!history.isEmpty()) {
				Activity activityLog = new ActivityBuilder().addActionType(UPDATE_DASHBOARD)
						.addObjectType(DASHBOARD)
						.addObjectName(dashboard.getName())
						.addProjectRef(dashboard.getProjectName())
						.addLoggedObjectRef(dashboard.getId())
						.addUserRef(event.getUpdatedBy())
						.addHistory(history)
						.get();
				activityRepository.save(activityLog);
			}
		}
	}

	@EventListener
	public void onDashboardCreate(DashboardCreatedEvent event) {
		CreateDashboardRQ createDashboardRQ = event.getCreateDashboardRQ();
		Activity activityLog = new ActivityBuilder().addActionType(CREATE_DASHBOARD)
				.addObjectType(DASHBOARD)
				.addObjectName(createDashboardRQ.getName())
				.addProjectRef(event.getProjectRef())
				.addUserRef(event.getCreatedBy())
				.addLoggedObjectRef(event.getDashboardId())
				.addHistory(Collections.singletonList(createHistoryField(NAME, EMPTY_FIELD, createDashboardRQ.getName())))
				.get();
		activityRepository.save(activityLog);
	}

	@EventListener
	public void onDashboardDelete(DashboardDeletedEvent event) {
		Dashboard dashboard = event.getBefore();
		Activity activityLog = new ActivityBuilder().addActionType(DELETE_DASHBOARD)
				.addObjectType(DASHBOARD)
				.addObjectName(dashboard.getName())
				.addProjectRef(dashboard.getProjectName())
				.addUserRef(event.getRemovedBy())
				.addHistory(Collections.singletonList(createHistoryField(NAME, dashboard.getName(), EMPTY_FIELD)))
				.get();
		activityRepository.save(activityLog);
	}

}

