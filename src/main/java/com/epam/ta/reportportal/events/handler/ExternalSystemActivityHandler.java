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

import javax.inject.Provider;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Andrei Varabyeu
 */
@Component
public class ExternalSystemActivityHandler {

	public static final String EXTERNAL_SYSTEM = "externalSystem";
	public static final String DELETE = "delete_bts";
	public static final String UPDATE = "update_bts";
	public static final String CREATE = "create_bts";

	private final ActivityRepository activityRepository;

	private final Provider<ActivityBuilder> activityBuilder;

	@Autowired
	public ExternalSystemActivityHandler(ActivityRepository activityRepository, Provider<ActivityBuilder> activityBuilder) {
		this.activityRepository = activityRepository;
		this.activityBuilder = activityBuilder;
	}

	@EventListener
	public void onExternalSystemCreated(ExternalSystemCreatedEvent event) {
		ExternalSystem externalSystem = event.getExternalSystem();
		String name = externalSystem.getExternalSystemType().name() + ":" + externalSystem.getProject();
		Activity activity = activityBuilder.get().addObjectName(name).addObjectType(EXTERNAL_SYSTEM)
				.addLoggedObjectRef(externalSystem.getId()).addUserRef(event.getCreatedBy()).addActionType(CREATE)
				.addProjectRef(externalSystem.getProjectRef()).build();
		activityRepository.save(activity);
	}

	@EventListener
	public void onExternalSystemUpdate(ExternalSystemUpdatedEvent event) {
		ExternalSystem externalSystem = event.getExternalSystem();
		if (externalSystem != null) {
			String name = externalSystem.getExternalSystemType().name() + ":" + externalSystem.getProject();
			Activity activity = activityBuilder.get().addObjectName(name).addObjectType(EXTERNAL_SYSTEM)
					.addLoggedObjectRef(externalSystem.getId()).addUserRef(event.getUpdatedBy()).addActionType(UPDATE)
					.addProjectRef(externalSystem.getProjectRef()).build();
			activityRepository.save(activity);
		}
	}

	@EventListener
	public void onExternalSystemDelete(ExternalSystemDeletedEvent event) {
		ExternalSystem externalSystem = event.getExternalSystem();
		if (externalSystem != null) {
			String name = externalSystem.getExternalSystemType().name() + ":" + externalSystem.getProject();
			Activity activity = activityBuilder.get().addObjectName(name).addObjectType(EXTERNAL_SYSTEM)
					.addLoggedObjectRef(externalSystem.getId()).addUserRef(event.getDeletedBy()).addActionType(DELETE)
					.addProjectRef(externalSystem.getProjectRef()).build();
			activityRepository.save(activity);
		}
	}

	@EventListener
	public void onProjectExternalSystemsDelete(ProjectExternalSystemsDeletedEvent event) {
		Iterable<ExternalSystem> externalSystems = event.getExternalSystems();
		if (null != externalSystems) {
			List<Activity> activities = StreamSupport.stream(externalSystems.spliterator(), false).map(externalSystem -> {
				String name = externalSystem.getExternalSystemType().name() + ":" + externalSystem.getProject();
				return activityBuilder.get().addObjectName(name).addObjectType(EXTERNAL_SYSTEM).addLoggedObjectRef(externalSystem.getId())
						.addUserRef(event.getDeletedBy()).addActionType(DELETE).addProjectRef(event.getProject()).build();
			}).collect(Collectors.toList());
			if (!activities.isEmpty())
				activityRepository.save(activities);
		}
	}

}