/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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
import com.epam.ta.reportportal.events.DashboardUpdatedEvent;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Provider;

import static com.epam.ta.reportportal.events.handler.WidgetActivityEventHandler.SHARE;
import static com.epam.ta.reportportal.events.handler.WidgetActivityEventHandler.UNSHARE;

/**
 * @author Andrei Varabyeu
 */
@Component
public class DashboardActivityEventHandler {

	@Autowired
	private ActivityRepository activityRepository;

	@Autowired
	private Provider<ActivityBuilder> activityBuilder;

	@EventListener
	public void onDashboardShared(DashboardUpdatedEvent event) {
		if (null != event.getUpdateRQ().getShare()) {
			final Dashboard dashboard = event.getDashboard();
			Activity activityLog = activityBuilder.get().addProjectRef(dashboard.getProjectName()).addObjectType(Dashboard.DASHBOARD)
					.addUserRef(event.getUpdatedBy()).addActionType(event.getUpdateRQ().getShare() ? SHARE : UNSHARE)
					.addLoggedObjectRef(dashboard.getId()).addObjectName(dashboard.getName()).build();
			activityRepository.save(activityLog);

		}
	}
}

