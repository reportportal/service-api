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
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;

import java.util.Optional;

import static com.epam.ta.reportportal.core.events.activity.ActivityAction.ANALYZE_ITEM;
import static com.epam.ta.reportportal.core.events.activity.ActivityAction.UPDATE_ITEM;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.*;
import static com.epam.ta.reportportal.entity.Activity.ActivityEntityType.ITEM_ISSUE;

/**
 * @author Andrei Varabyeu
 */
public class ItemIssueTypeDefinedEvent extends AroundEvent<TestItemActivityResource> implements ActivityEvent {

	private Long postedBy;

	public ItemIssueTypeDefinedEvent() {
	}

	public ItemIssueTypeDefinedEvent(TestItemActivityResource before, TestItemActivityResource after, Long postedBy) {
		super(before, after);
		this.postedBy = postedBy;
	}

	public Long getPostedBy() {
		return postedBy;
	}

	public void setPostedBy(Long postedBy) {
		this.postedBy = postedBy;
	}

	@Override
	public Activity toActivity() {
		return new ActivityBuilder().addCreatedNow()
				.addAction(getAfter().isAutoAnalyzed() ? ANALYZE_ITEM : UPDATE_ITEM)
				.addActivityEntityType(ITEM_ISSUE)
				.addUserId(postedBy)
				.addObjectId(getAfter().getId())
				.addObjectName(getAfter().getName())
				.addProjectId(getAfter().getProjectId())
				.addHistoryField(processIssueDescription(getBefore().getIssueDescription(), getAfter().getIssueDescription()))
				.addHistoryField(processIssueTypes(getBefore().getIssueTypeLongName(), getAfter().getIssueTypeLongName()))
				.addHistoryField(processIgnoredAnalyzer(getBefore().isIgnoreAnalyzer(), getAfter().isIgnoreAnalyzer()))
				.get();
	}

	private Optional<HistoryField> processIssueDescription(String before, String after) {
		HistoryField historyField = null;

		after = (null != after) ? after.trim() : EMPTY_STRING;
		before = (null != before) ? before : EMPTY_STRING;

		if (!before.equals(after)) {
			historyField = HistoryField.of(COMMENT, before, after);
		}
		return Optional.ofNullable(historyField);
	}

	private Optional<HistoryField> processIssueTypes(String before, String after) {
		return before.equalsIgnoreCase(after) ? Optional.empty() : Optional.of(HistoryField.of(ISSUE_TYPE, before, after));
	}

	private Optional<HistoryField> processIgnoredAnalyzer(Boolean before, Boolean after) {
		if (!before.equals(after)) {
			return Optional.of(HistoryField.of(IGNORE_ANALYZER, String.valueOf(before), String.valueOf(after)));
		}
		return Optional.empty();
	}
}
