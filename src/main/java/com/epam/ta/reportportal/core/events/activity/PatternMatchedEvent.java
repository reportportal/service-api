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
import com.epam.ta.reportportal.ws.model.activity.PatternTemplateActivityResource;

import java.util.Optional;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.PATTERN_ID;
import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.PATTERN;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.PATTERN_MATCHED;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
public class PatternMatchedEvent implements ActivityEvent {

	private Long patternId;

	private Long itemId;

	private PatternTemplateActivityResource patternTemplateActivityResource;

	public PatternMatchedEvent() {
	}

	public PatternMatchedEvent(Long patternId, Long itemId, PatternTemplateActivityResource patternTemplateActivityResource) {
		this.patternId = patternId;
		this.itemId = itemId;
		this.patternTemplateActivityResource = patternTemplateActivityResource;
	}

	public Long getPatternId() {
		return patternId;
	}

	public void setPatternId(Long patternId) {
		this.patternId = patternId;
	}

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public PatternTemplateActivityResource getPatternTemplateActivityResource() {
		return patternTemplateActivityResource;
	}

	public void setPatternTemplateActivityResource(PatternTemplateActivityResource patternTemplateActivityResource) {
		this.patternTemplateActivityResource = patternTemplateActivityResource;
	}

	@Override
	public Activity toActivity() {

		HistoryField patternIdField = new HistoryField();
		patternIdField.setField(PATTERN_ID);
		patternIdField.setNewValue(String.valueOf(patternId));

		return new ActivityBuilder().addCreatedNow().addObjectId(itemId)
				.addObjectName(patternTemplateActivityResource.getName())
				.addProjectId(patternTemplateActivityResource.getProjectId())
				.addActivityEntityType(PATTERN)
				.addAction(PATTERN_MATCHED)
				.addHistoryField(Optional.of(patternIdField))
				.get();
	}
}
