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
import com.epam.ta.reportportal.entity.HistoryField;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.activity.WidgetActivityResource;

import java.util.Optional;
import java.util.Set;

import static com.epam.ta.reportportal.core.events.activity.ActivityAction.UPDATE_WIDGET;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.*;
import static com.epam.ta.reportportal.entity.Activity.ActivityEntityType.WIDGET;

/**
 * @author Andrei Varabyeu
 */
public class WidgetUpdatedEvent extends AroundEvent<WidgetActivityResource> implements ActivityEvent {

	private Long updatedBy;
	private String widgetOptionsBefore;
	private String widgetOptionsAfter;

	public WidgetUpdatedEvent() {
	}

	public WidgetUpdatedEvent(WidgetActivityResource before, WidgetActivityResource after, String widgetOptionsBefore,
			String widgetOptionsAfter, Long updatedBy) {
		super(before, after);
		this.updatedBy = updatedBy;
		this.widgetOptionsBefore = widgetOptionsBefore;
		this.widgetOptionsAfter = widgetOptionsAfter;
	}

	public Long getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(Long updatedBy) {
		this.updatedBy = updatedBy;
	}

	@Override
	public Activity toActivity() {
		return new ActivityBuilder().addCreatedNow()
				.addAction(UPDATE_WIDGET)
				.addActivityEntityType(WIDGET)
				.addUserId(updatedBy)
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
