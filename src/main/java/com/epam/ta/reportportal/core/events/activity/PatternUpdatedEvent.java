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
import com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil;
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.activity.PatternTemplateActivityResource;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.processBoolean;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.processName;
import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.PATTERN;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.UPDATE_PATTERN;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PatternUpdatedEvent extends AroundEvent<PatternTemplateActivityResource> implements ActivityEvent {

	public PatternUpdatedEvent() {
	}

	public PatternUpdatedEvent(Long userId, String userLogin, PatternTemplateActivityResource before,
			PatternTemplateActivityResource after) {
		super(userId, userLogin, before, after);
	}

	@Override
	public Activity toActivity() {
		return new ActivityBuilder().addCreatedNow()
				.addObjectId(getAfter().getId())
				.addObjectName(getAfter().getName())
				.addUserId(getUserId())
				.addUserName(getUserLogin())
				.addProjectId(getAfter().getProjectId())
				.addAction(UPDATE_PATTERN)
				.addActivityEntityType(PATTERN)
				.addHistoryField(processName(getBefore().getName(), getAfter().getName()))
				.addHistoryField(processBoolean(ActivityDetailsUtil.ENABLED, getBefore().isEnabled(), getAfter().isEnabled()))
				.get();
	}
}
