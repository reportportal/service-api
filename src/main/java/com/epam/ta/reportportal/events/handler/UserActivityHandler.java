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
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.events.UserCreatedEvent;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Collections;

import static com.epam.ta.reportportal.database.entity.item.ActivityEventType.CREATE_USER;
import static com.epam.ta.reportportal.database.entity.item.ActivityObjectType.USER;
import static com.epam.ta.reportportal.events.handler.EventHandlerUtil.*;

/**
 * @author Andrei Varabyeu
 */
@Component
public class UserActivityHandler {

	private final ActivityRepository activityRepository;

	@Autowired
	public UserActivityHandler(ActivityRepository activityRepository) {
		this.activityRepository = activityRepository;
	}

	@EventListener
	public void onUserCreated(UserCreatedEvent event) {
		Activity activity = new ActivityBuilder().addActionType(CREATE_USER)
				.addLoggedObjectRef(event.getUser().getLogin())
				.addObjectName(event.getUser().getLogin())
				.addObjectType(USER)
				.addUserRef(event.getCreatedBy())
				.addProjectRef(event.getUser().getDefaultProject().toLowerCase())
				.addHistory(Collections.singletonList(createHistoryField(NAME, EMPTY_FIELD, event.getUser().getLogin())))
				.get();
		activityRepository.save(activity);
	}
}
