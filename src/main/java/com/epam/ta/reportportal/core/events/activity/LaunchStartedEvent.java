/*
 * Copyright 2018 EPAM Systems
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
import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.activity.LaunchActivityResource;

import static com.epam.ta.reportportal.core.events.activity.ActivityAction.START_LAUNCH;
import static com.epam.ta.reportportal.entity.Activity.ActivityEntityType.LAUNCH;

/**
 * @author Andrei Varabyeu
 */
public class LaunchStartedEvent implements ActivityEvent {

	private LaunchActivityResource launchActivityResource;
	private Long startedBy;

	public LaunchStartedEvent() {
	}

	public LaunchStartedEvent(LaunchActivityResource launchActivityResource, Long startedBy) {
		this.launchActivityResource = launchActivityResource;
		this.startedBy = startedBy;
	}

	public LaunchActivityResource getLaunchActivityResource() {
		return launchActivityResource;
	}

	public void setLaunchActivityResource(LaunchActivityResource launchActivityResource) {
		this.launchActivityResource = launchActivityResource;
	}

	public Long getStartedBy() {
		return startedBy;
	}

	public void setStartedBy(Long startedBy) {
		this.startedBy = startedBy;
	}

	@Override
	public Activity toActivity() {
		return new ActivityBuilder().addCreatedNow()
				.addAction(START_LAUNCH)
				.addActivityEntityType(LAUNCH)
				.addUserId(startedBy)
				.addObjectId(launchActivityResource.getId())
				.addObjectName(launchActivityResource.getName())
				.addProjectId(launchActivityResource.getProjectId())
				.get();
	}
}
