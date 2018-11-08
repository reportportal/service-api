/*
 * Copyright 2016 EPAM Systems
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
