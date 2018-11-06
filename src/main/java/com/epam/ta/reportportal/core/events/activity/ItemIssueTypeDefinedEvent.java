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
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.ws.model.issue.IssueDefinition;

import java.time.LocalDateTime;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.*;
import static java.util.Objects.isNull;

/**
 * @author Andrei Varabyeu
 */
public class ItemIssueTypeDefinedEvent implements ActivityEvent {

	private Long postedBy;
	private IssueDefinition issueDefinition;
	private TestItem testItem;
	private Long projectId;

	public ItemIssueTypeDefinedEvent() {
	}

	public ItemIssueTypeDefinedEvent(Long postedBy, IssueDefinition issueDefinition, TestItem testItem,
			Long projectId) {
		this.postedBy = postedBy;
		this.issueDefinition = issueDefinition;
		this.testItem = testItem;
		this.projectId = projectId;
	}

	public Long getPostedBy() {
		return postedBy;
	}

	public void setPostedBy(Long postedBy) {
		this.postedBy = postedBy;
	}

	public IssueDefinition getIssueDefinition() {
		return issueDefinition;
	}

	public void setIssueDefinition(IssueDefinition issueDefinition) {
		this.issueDefinition = issueDefinition;
	}

	public TestItem getTestItem() {
		return testItem;
	}

	public void setTestItem(TestItem testItem) {
		this.testItem = testItem;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		activity.setAction(issueDefinition.getIssue().getAutoAnalyzed() ?
				ActivityAction.ANALYZE_ITEM.getValue() :
				ActivityAction.UPDATE_ITEM.getValue());
		activity.setActivityEntityType(Activity.ActivityEntityType.ITEM_ISSUE);
		activity.setProjectId(projectId);
		activity.setUserId(postedBy);
		activity.setObjectId(issueDefinition.getId());

		ActivityDetails details = new ActivityDetails(testItem.getName());
		IssueEntity issueEntity = testItem.getItemResults().getIssue();

		HistoryField descriptionField = processIssueDescription(issueEntity.getIssueDescription());
		if (!isNull(descriptionField)) {
			details.addHistoryField(descriptionField);
		}

		HistoryField issueTypeField = processIssueTypes();
		if (issueTypeField.getOldValue().equalsIgnoreCase(issueTypeField.getNewValue())) {
			details.addHistoryField(issueTypeField);
		}

		HistoryField ignoredAnalyzerField = processIgnoredAnalyzer(issueEntity.getIgnoreAnalyzer());
		if (!isNull(ignoredAnalyzerField)) {
			details.addHistoryField(ignoredAnalyzerField);
		}

		activity.setDetails(details);
		return activity;
	}

	private HistoryField processIssueDescription(String oldIssueDescription) {
		HistoryField historyField = null;

		String initialComment = issueDefinition.getIssue().getComment();
		String comment = (null != initialComment) ? initialComment.trim() : EMPTY_STRING;

		if (null == oldIssueDescription) {
			oldIssueDescription = EMPTY_STRING;
		}

		if (!oldIssueDescription.equals(comment)) {
			historyField = HistoryField.of(COMMENT, oldIssueDescription, issueDefinition.getIssue().getComment());
		}
		return historyField;
	}

	private HistoryField processIssueTypes() {
		String oldValue = testItem.getItemResults().getIssue().getIssueType().getLongName();
		String newValue = issueDefinition.getIssue().getIssueType();
		return HistoryField.of(ISSUE_TYPE, oldValue, newValue);
	}

	private HistoryField processIgnoredAnalyzer(boolean oldIgnoredAnalyser) {
		HistoryField historyField = null;
		boolean newIgnoreAnalyzer = issueDefinition.getIssue().getIgnoreAnalyzer();
		if (oldIgnoredAnalyser != newIgnoreAnalyzer) {
			historyField = HistoryField.of(IGNORE_ANALYZER, String.valueOf(oldIgnoredAnalyser), String.valueOf(newIgnoreAnalyzer));
		}
		return historyField;
	}
}
