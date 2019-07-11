/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.epam.ta.reportportal.core.events.activity;

import com.epam.ta.reportportal.core.events.ActivityEvent;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.activity.ProjectAttributesActivityResource;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.configEquals;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.processParameter;
import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.PROJECT;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.UPDATE_PROJECT;
import static com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum.*;

/**
 * Being triggered on after project update
 *
 * @author Andrei Varabyeu
 */
public class ProjectUpdatedEvent extends AroundEvent<ProjectAttributesActivityResource> implements ActivityEvent {

	public ProjectUpdatedEvent() {
	}

	public ProjectUpdatedEvent(ProjectAttributesActivityResource before, ProjectAttributesActivityResource after, Long userId,
			String userLogin) {
		super(userId, userLogin, before, after);
	}

	@Override
	public Activity toActivity() {
		return configEquals(getBefore().getConfig(), getAfter().getConfig(), Prefix.JOB) ?
				null :
				new ActivityBuilder().addCreatedNow()
						.addAction(UPDATE_PROJECT)
						.addActivityEntityType(PROJECT)
						.addUserId(getUserId())
						.addUserName(getUserLogin())
						.addObjectId(getAfter().getProjectId())
						.addObjectName(getAfter().getProjectName())
						.addProjectId(getAfter().getProjectId())
						.addHistoryField(processParameter(getBefore().getConfig(),
								getAfter().getConfig(),
								INTERRUPT_JOB_TIME.getAttribute()
						))
						.addHistoryField(processParameter(getBefore().getConfig(), getAfter().getConfig(), KEEP_SCREENSHOTS.getAttribute()))
						.addHistoryField(processParameter(getBefore().getConfig(), getAfter().getConfig(), KEEP_LOGS.getAttribute()))
						.addHistoryField(processParameter(getBefore().getConfig(), getAfter().getConfig(), KEEP_LAUNCHES.getAttribute()))
						.get();
	}

}
