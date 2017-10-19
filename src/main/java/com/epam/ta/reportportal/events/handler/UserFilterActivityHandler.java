/*
 * Copyright 2017 EPAM Systems
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
import com.epam.ta.reportportal.database.entity.filter.UserFilter;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.events.FilterDeletedEvent;
import com.epam.ta.reportportal.events.FilterUpdatedEvent;
import com.epam.ta.reportportal.events.FiltersCreatedEvent;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static com.epam.ta.reportportal.database.entity.item.ActivityEventType.*;
import static com.epam.ta.reportportal.database.entity.item.ActivityObjectType.USER_FILTER;
import static com.epam.ta.reportportal.events.handler.EventHandlerUtil.*;

/**
 * @author Pavel Bortnik
 */
@Component
public class UserFilterActivityHandler {

	private ActivityRepository activityRepository;

	@Autowired
	public UserFilterActivityHandler(ActivityRepository activityRepository) {
		this.activityRepository = activityRepository;
	}

	@EventListener
	public void onFilterCreate(FiltersCreatedEvent event) {
		List<UserFilter> filters = event.getCreatedFilters();
		filters.forEach(filter -> {
			Activity activityLog = new ActivityBuilder().addActionType(CREATE_FILTER)
					.addObjectType(USER_FILTER)
					.addObjectName(filter.getName())
					.addProjectRef(event.getProjectRef())
					.addUserRef(event.getCreatedBy())
					.addLoggedObjectRef(filter.getId())
					.addHistory(Collections.singletonList(createHistoryField(NAME, EMPTY_FIELD, filter.getName())))
					.get();
			activityRepository.save(activityLog);
		});
	}

	@EventListener
	public void onFilterUpdate(FilterUpdatedEvent event) {
		UserFilter before = event.getBefore();
		UserFilter after = event.getAfter();
		List<Activity.FieldValues> history = Lists.newArrayList();
		if (before != null) {
			processShare(history, before, !after.getAcl().getEntries().isEmpty());
			processName(history, before.getName(), after.getName());
			processDescription(history, before.getDescription(), after.getDescription());
			Activity activityLog = new ActivityBuilder().addProjectRef(after.getProjectName())
					.addObjectName(after.getName())
					.addObjectType(USER_FILTER)
					.addActionType(UPDATE_FILTER)
					.addLoggedObjectRef(after.getId())
					.addUserRef(event.getUpdatedBy())
					.addHistory(!history.isEmpty() ? history : null)
					.get();
			activityRepository.save(activityLog);
		}
	}

	@EventListener
	public void onFilterDelete(FilterDeletedEvent event) {
		UserFilter before = event.getBefore();
		Activity activityLog = new ActivityBuilder().addActionType(DELETE_FILTER)
				.addObjectType(USER_FILTER)
				.addObjectName(before.getName())
				.addProjectRef(before.getProjectName())
				.addUserRef(event.getRemovedBy())
				.addHistory(Collections.singletonList(createHistoryField(NAME, before.getName(), EMPTY_FIELD)))
				.get();
		activityRepository.save(activityLog);
	}
}
