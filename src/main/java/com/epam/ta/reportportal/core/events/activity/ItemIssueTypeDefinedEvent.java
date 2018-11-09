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
import com.epam.ta.reportportal.entity.ActivityDetails;
import com.epam.ta.reportportal.entity.HistoryField;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.entity.item.issue.IssueType;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.*;

/**
 * @author Andrei Varabyeu
 */
public class ItemIssueTypeDefinedEvent extends AroundEvent<IssueEntity> implements ActivityEvent {

	private Long postedBy;
	private Long testItemId;
	private String testItemName;
	private Long projectId;

	public ItemIssueTypeDefinedEvent() {
	}

	public ItemIssueTypeDefinedEvent(IssueEntity before, IssueEntity after, Long postedBy, Long testItemId, String testItemName,
			Long projectId) {
		super(before, after);
		this.postedBy = postedBy;
		this.testItemId = testItemId;
		this.testItemName = testItemName;
		this.projectId = projectId;
	}

	public Long getPostedBy() {
		return postedBy;
	}

	public void setPostedBy(Long postedBy) {
		this.postedBy = postedBy;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public Long getTestItemId() {
		return testItemId;
	}

	public void setTestItemId(Long testItemId) {
		this.testItemId = testItemId;
	}

	public String getTestItemName() {
		return testItemName;
	}

	public void setTestItemName(String testItemName) {
		this.testItemName = testItemName;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		activity.setAction(getAfter().getAutoAnalyzed() ?
				ActivityAction.ANALYZE_ITEM.getValue() :
				ActivityAction.UPDATE_ITEM.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.ITEM_ISSUE);
		activity.setProjectId(projectId);
		activity.setUserId(postedBy);
		activity.setObjectId(testItemId);

		ActivityDetails details = new ActivityDetails(testItemName);

		processIssueDescription(getBefore().getIssueDescription(), getAfter().getIssueDescription()).ifPresent(details::addHistoryField);
		processIssueTypes(getBefore().getIssueType(), getAfter().getIssueType()).ifPresent(details::addHistoryField);
		processIgnoredAnalyzer(getBefore().getIgnoreAnalyzer(), getAfter().getIgnoreAnalyzer()).ifPresent(details::addHistoryField);

		activity.setDetails(details);
		return activity;
	}

	private Optional<HistoryField> processIssueDescription(String oldIssueDescription, String newIssueDescription) {
		HistoryField historyField = null;

		newIssueDescription = (null != newIssueDescription) ? newIssueDescription.trim() : EMPTY_STRING;
		oldIssueDescription = (null != oldIssueDescription) ? oldIssueDescription : EMPTY_STRING;

		if (!oldIssueDescription.equals(newIssueDescription)) {
			historyField = HistoryField.of(COMMENT, oldIssueDescription, newIssueDescription);
		}
		return Optional.ofNullable(historyField);
	}

	private Optional<HistoryField> processIssueTypes(IssueType before, IssueType after) {
		String oldValue = before.getLongName();
		String newValue = after.getLongName();
		return oldValue.equalsIgnoreCase(newValue) ? Optional.empty() : Optional.of(HistoryField.of(ISSUE_TYPE, oldValue, newValue));
	}

	private Optional<HistoryField> processIgnoredAnalyzer(Boolean before, Boolean after) {
		HistoryField historyField = null;
		if (!before.equals(after)) {
			historyField = HistoryField.of(IGNORE_ANALYZER, String.valueOf(before), String.valueOf(after));
		}
		return Optional.ofNullable(historyField);
	}
}
