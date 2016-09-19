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
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.events.UserCreatedEvent;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Provider;

/**
 * @author Andrei Varabyeu
 */
@Component
public class UserActivityHandler {

	public static final String CREATE_USER = "create_user";

	private final ActivityRepository activityRepository;
	private final Provider<ActivityBuilder> activityBuilder;

	@Autowired
	public UserActivityHandler(ActivityRepository activityRepository, Provider<ActivityBuilder> activityBuilder) {
		this.activityRepository = activityRepository;
		this.activityBuilder = activityBuilder;
	}

	@EventListener
	public void onUserCreated(UserCreatedEvent event) {
		Activity activity = activityBuilder.get().addActionType(CREATE_USER).addLoggedObjectRef(event.getUser().getLogin())
				.addObjectName(event.getUser().getLogin()).addObjectType("user").addUserRef(event.getCreatedBy())
				.addProjectRef(event.getUser().getDefaultProject().toLowerCase()).build();
		activityRepository.save(activity);
	}
}
