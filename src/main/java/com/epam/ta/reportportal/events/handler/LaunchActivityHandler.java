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
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.events.LaunchDeletedEvent;
import com.epam.ta.reportportal.events.LaunchFinishForcedEvent;
import com.epam.ta.reportportal.events.LaunchFinishedEvent;
import com.epam.ta.reportportal.events.LaunchStartedEvent;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Collections;

import static com.epam.ta.reportportal.database.entity.item.ActivityEventType.*;
import static com.epam.ta.reportportal.database.entity.item.ActivityObjectType.LAUNCH;
import static com.epam.ta.reportportal.events.handler.EventHandlerUtil.*;

/**
 * @author Andrei Varabyeu
 */
@Component
public class LaunchActivityHandler {

	private static final String DELIMITER = " #";

	private final ActivityRepository activityRepository;

	@Autowired
	public LaunchActivityHandler(ActivityRepository activityRepository) {
		this.activityRepository = activityRepository;
	}

	@EventListener
	public void onLaunchFinish(LaunchFinishedEvent event) {
		afterLaunchFinished(event.getLaunch(), event.getLaunch().getUserRef());
	}

	@EventListener
	public void onLaunchForceFinish(LaunchFinishForcedEvent event) {
		afterLaunchFinished(event.getLaunch(), event.getForcedBy());
	}

	@EventListener
	public void onLaunchStart(LaunchStartedEvent event) {
		Launch launch = event.getLaunch();
		if (Mode.DEBUG != event.getLaunch().getMode()) {
			String name = launch.getName() + DELIMITER + launch.getNumber();
			Activity activityLog = new ActivityBuilder().addUserRef(launch.getUserRef())
					.addProjectRef(launch.getProjectRef().toLowerCase())
					.addActionType(START_LAUNCH)
					.addObjectType(LAUNCH)
					.addLoggedObjectRef(launch.getId())
					.addObjectName(name)
					.addHistory(Collections.singletonList(createHistoryField(NAME, EMPTY_FIELD, name)))
					.get();
			activityRepository.save(activityLog);
		}
	}

	@EventListener
	public void onDeleteLaunch(LaunchDeletedEvent event) {
		Launch launch = event.getLaunch();
		if (null != launch && launch.getMode() == Mode.DEFAULT) {
			String name = launch.getName() + DELIMITER + launch.getNumber();
			Activity activity = new ActivityBuilder().addUserRef(event.getDeletedBy())
					.addProjectRef(event.getLaunch().getProjectRef())
					.addActionType(DELETE_LAUNCH)
					.addObjectType(LAUNCH)
					.addLoggedObjectRef(launch.getId())
					.addObjectName(name)
					.addHistory(Collections.singletonList(createHistoryField(NAME, name, EMPTY_FIELD)))
					.get();
			activityRepository.save(activity);
		}
	}

	private void afterLaunchFinished(Launch launch, String finishedBy) {
		if (launch.getMode() != Mode.DEBUG) {
			String name = launch.getName() + DELIMITER + launch.getNumber();
			Activity activityLog = new ActivityBuilder().addUserRef(finishedBy)
					.addProjectRef(launch.getProjectRef())
					.addActionType(FINISH_LAUNCH)
					.addObjectType(LAUNCH)
					.addLoggedObjectRef(launch.getId())
					.addObjectName(name)
					.addHistory(Collections.singletonList(createHistoryField(NAME, name, name)))
					.get();
			activityRepository.save(activityLog);
		}
	}

}
