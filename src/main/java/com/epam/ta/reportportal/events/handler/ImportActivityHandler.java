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
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.database.entity.item.ActivityEventType;
import com.epam.ta.reportportal.database.entity.item.ActivityObjectType;
import com.epam.ta.reportportal.events.ImportFinishedEvent;
import com.epam.ta.reportportal.events.ImportStartedEvent;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Collections;

import static com.epam.ta.reportportal.events.handler.EventHandlerUtil.*;

/**
 * @author Pavel Bortnik
 */
@Component
public class ImportActivityHandler {

	@Autowired
	private ActivityRepository activityRepository;

	@EventListener
	public void onImportStart(ImportStartedEvent event) {
		Activity activity = new ActivityBuilder().addObjectName(event.getFileName())
				.addObjectType(ActivityObjectType.LAUNCH)
				.addActionType(ActivityEventType.START_IMPORT)
				.addUserRef(event.getUserName())
				.addProjectRef(event.getProjectId())
				.addHistory(Collections.singletonList(createHistoryField(NAME, EMPTY_FIELD, event.getFileName())))
				.get();
		activityRepository.save(activity);
	}

	@EventListener
	public void onImportFinish(ImportFinishedEvent event) {
		Activity activity = new ActivityBuilder().addObjectName(event.getFileName())
				.addObjectType(ActivityObjectType.LAUNCH)
				.addActionType(ActivityEventType.FINISH_IMPORT)
				.addUserRef(event.getUserName())
				.addProjectRef(event.getProjectId())
				.addHistory(Collections.singletonList(createHistoryField(NAME, EMPTY_FIELD, event.getFileName())))
				.get();
		activityRepository.save(activity);
	}

}
