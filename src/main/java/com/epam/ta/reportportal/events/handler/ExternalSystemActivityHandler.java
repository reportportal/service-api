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
import com.epam.ta.reportportal.database.entity.ExternalSystem;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.events.ExternalSystemCreatedEvent;
import com.epam.ta.reportportal.events.ExternalSystemDeletedEvent;
import com.epam.ta.reportportal.events.ExternalSystemUpdatedEvent;
import com.epam.ta.reportportal.events.ProjectExternalSystemsDeletedEvent;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.epam.ta.reportportal.database.entity.item.ActivityEventType.*;
import static com.epam.ta.reportportal.database.entity.item.ActivityObjectType.EXTERNAL_SYSTEM;
import static com.epam.ta.reportportal.events.handler.EventHandlerUtil.*;

/**
 * @author Andrei Varabyeu
 */
@Component
public class ExternalSystemActivityHandler {

	private final ActivityRepository activityRepository;

	@Autowired
	public ExternalSystemActivityHandler(ActivityRepository activityRepository) {
		this.activityRepository = activityRepository;
	}

	@EventListener
	public void onExternalSystemCreated(ExternalSystemCreatedEvent event) {
		ExternalSystem externalSystem = event.getExternalSystem();
		String name = externalSystem.getExternalSystemType() + ":" + externalSystem.getProject();
		Activity activity = new ActivityBuilder().addObjectName(name)
				.addObjectType(EXTERNAL_SYSTEM)
				.addLoggedObjectRef(externalSystem.getId())
				.addUserRef(event.getCreatedBy())
				.addActionType(CREATE_BTS)
				.addProjectRef(externalSystem.getProjectRef())
				.addHistory(Collections.singletonList(createHistoryField(NAME, EMPTY_FIELD, name)))
				.get();
		activityRepository.save(activity);
	}

	@EventListener
	public void onExternalSystemUpdate(ExternalSystemUpdatedEvent event) {
		ExternalSystem externalSystem = event.getExternalSystem();
		if (externalSystem != null) {
			String name = externalSystem.getExternalSystemType() + ":" + externalSystem.getProject();
			Activity activity = new ActivityBuilder().addObjectName(name)
					.addObjectType(EXTERNAL_SYSTEM)
					.addLoggedObjectRef(externalSystem.getId())
					.addUserRef(event.getUpdatedBy())
					.addActionType(UPDATE_BTS)
					.addProjectRef(externalSystem.getProjectRef())
					.get();
			activityRepository.save(activity);
		}
	}

	@EventListener
	public void onExternalSystemDelete(ExternalSystemDeletedEvent event) {
		ExternalSystem externalSystem = event.getExternalSystem();
		if (externalSystem != null) {
			String name = externalSystem.getExternalSystemType() + ":" + externalSystem.getProject();
			Activity activity = new ActivityBuilder().addObjectName(name)
					.addObjectType(EXTERNAL_SYSTEM)
					.addLoggedObjectRef(externalSystem.getId())
					.addUserRef(event.getDeletedBy())
					.addActionType(DELETE_BTS)
					.addProjectRef(externalSystem.getProjectRef())
					.addHistory(Collections.singletonList(createHistoryField(NAME, name, EMPTY_FIELD)))
					.get();
			activityRepository.save(activity);
		}
	}

	@EventListener
	public void onProjectExternalSystemsDelete(ProjectExternalSystemsDeletedEvent event) {
		Iterable<ExternalSystem> externalSystems = event.getExternalSystems();
		if (null != externalSystems) {
			List<Activity> activities = StreamSupport.stream(externalSystems.spliterator(), false).map(externalSystem -> {
				String name = externalSystem.getExternalSystemType() + ":" + externalSystem.getProject();
				return new ActivityBuilder().addObjectName(name)
						.addObjectType(EXTERNAL_SYSTEM)
						.addLoggedObjectRef(externalSystem.getId())
						.addUserRef(event.getDeletedBy())
						.addActionType(DELETE_BTS)
						.addProjectRef(event.getProject())
						.addHistory(Collections.singletonList(createHistoryField(NAME, name, EMPTY_FIELD)))
						.get();
			}).collect(Collectors.toList());
			if (!activities.isEmpty()) {
				activityRepository.save(activities);
			}
		}
	}

}