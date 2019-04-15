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
import com.epam.ta.reportportal.ws.model.activity.WidgetActivityResource;

import java.util.Optional;
import java.util.Set;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.*;
import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.WIDGET;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.UPDATE_WIDGET;

/**
 * @author Andrei Varabyeu
 */
public class WidgetUpdatedEvent extends AroundEvent<WidgetActivityResource> implements ActivityEvent {

	private Long userId;
	private String userLogin;
	private String widgetOptionsBefore;
	private String widgetOptionsAfter;

	public WidgetUpdatedEvent() {
	}

	public WidgetUpdatedEvent(WidgetActivityResource before, WidgetActivityResource after, Long userId, String userLogin,
			String widgetOptionsBefore, String widgetOptionsAfter) {
		super(before, after);
		this.userId = userId;
		this.userLogin = userLogin;
		this.widgetOptionsBefore = widgetOptionsBefore;
		this.widgetOptionsAfter = widgetOptionsAfter;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUserLogin() {
		return userLogin;
	}

	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}

	public String getWidgetOptionsBefore() {
		return widgetOptionsBefore;
	}

	public void setWidgetOptionsBefore(String widgetOptionsBefore) {
		this.widgetOptionsBefore = widgetOptionsBefore;
	}

	public String getWidgetOptionsAfter() {
		return widgetOptionsAfter;
	}

	public void setWidgetOptionsAfter(String widgetOptionsAfter) {
		this.widgetOptionsAfter = widgetOptionsAfter;
	}

	@Override
	public Activity toActivity() {
		return new ActivityBuilder().addCreatedNow()
				.addAction(UPDATE_WIDGET)
				.addActivityEntityType(WIDGET).addUserId(userId).addUserName(userLogin)
				.addObjectId(getAfter().getId())
				.addObjectName(getAfter().getName())
				.addProjectId(getAfter().getProjectId())
				.addHistoryField(processShared(getBefore().isShared(), getAfter().isShared()))
				.addHistoryField(processName(getBefore().getName(), getAfter().getName()))
				.addHistoryField(processDescription(getBefore().getDescription(), getAfter().getDescription()))
				.addHistoryField(processItemsCount(getBefore().getItemsCount(), getAfter().getItemsCount()))
				.addHistoryField(processFields(getBefore().getContentFields(), getAfter().getContentFields()))
				.addHistoryField(Optional.of(HistoryField.of(WIDGET_OPTIONS, widgetOptionsBefore, widgetOptionsAfter)))
				.get();
	}

	private Optional<HistoryField> processItemsCount(int before, int after) {
		if (before != after) {
			return Optional.of(HistoryField.of(ITEMS_COUNT, String.valueOf(before), String.valueOf(after)));
		}
		return Optional.empty();
	}

	private Optional<HistoryField> processFields(Set<String> before, Set<String> after) {
		if (before != null && after != null && !before.equals(after)) {
			String oldValue = String.join(", ", before);
			String newValue = String.join(", ", after);
			return Optional.of(HistoryField.of(CONTENT_FIELDS, oldValue, newValue));
		}
		return Optional.empty();
	}
}
