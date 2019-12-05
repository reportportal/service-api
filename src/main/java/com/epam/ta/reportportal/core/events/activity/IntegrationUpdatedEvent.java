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
import com.epam.ta.reportportal.entity.activity.HistoryField;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.activity.IntegrationActivityResource;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.NAME;
import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.INTEGRATION;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.UPDATE_INTEGRATION;

/**
 * @author Andrei Varabyeu
 */
public class IntegrationUpdatedEvent extends AroundEvent<IntegrationActivityResource> implements ActivityEvent {

	public IntegrationUpdatedEvent() {
	}

	public IntegrationUpdatedEvent(Long userId, String userLogin, IntegrationActivityResource before, IntegrationActivityResource after) {
		super(userId, userLogin, before, after);
	}

	@Override
	public Activity toActivity() {

		HistoryField integrationNameField;
		if (StringUtils.equalsIgnoreCase(getBefore().getName(), getAfter().getName())) {
			integrationNameField = new HistoryField();
			integrationNameField.setField(NAME);
			integrationNameField.setNewValue(getAfter().getName());
		} else {
			integrationNameField = HistoryField.of(NAME, getBefore().getName(), getAfter().getName());
		}

		return new ActivityBuilder().addCreatedNow()
				.addAction(UPDATE_INTEGRATION)
				.addActivityEntityType(INTEGRATION)
				.addUserId(getUserId())
				.addUserName(getUserLogin())
				.addObjectId(getAfter().getId())
				.addObjectName(getAfter().getTypeName())
				.addProjectId(getAfter().getProjectId())
				.addHistoryField(Optional.of(integrationNameField))
				.get();
	}
}
