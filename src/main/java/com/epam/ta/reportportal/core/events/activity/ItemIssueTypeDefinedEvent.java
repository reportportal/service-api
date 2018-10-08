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
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.ws.model.issue.IssueDefinition;

import java.time.LocalDateTime;

import static com.epam.ta.reportportal.core.events.activity.details.ActivityDetailsUtil.*;

/**
 * @author Andrei Varabyeu
 */
public class ItemIssueTypeDefinedEvent implements ActivityEvent {

	private final Long postedBy;
	private final IssueDefinition issueDefinition;
	private final TestItem testItem;
	private final Project project;

	public ItemIssueTypeDefinedEvent(Long postedBy, IssueDefinition issueDefinition, TestItem testItem, Project project) {
		this.postedBy = postedBy;
		this.issueDefinition = issueDefinition;
		this.testItem = testItem;
		this.project = project;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		activity.setAction(issueDefinition.getIssue().getAutoAnalyzed() ?
				ActivityAction.ANALYZE_ITEM.getValue() :
				ActivityAction.UPDATE_ITEM.getValue());
		activity.setEntity(Activity.Entity.ITEM_ISSUE);
		activity.setProjectId(project.getId());
		activity.setUserId(postedBy);

		ActivityDetails details = new ActivityDetails();
		IssueEntity issueEntity = testItem.getItemResults().getIssue();

		HistoryField descriptionField = processIssueDescription(issueEntity.getIssueDescription());
		if (!descriptionField.isEmpty()){
			details.addHistoryField(descriptionField);
		}

		HistoryField issueTypeField = processIssueTypes(issueEntity);
		if (!issueTypeField.isEmpty() && issueTypeField.getOldValue().equalsIgnoreCase(issueTypeField.getNewValue())){
			details.addHistoryField(issueTypeField);
		}

		HistoryField ignoredAnalyzerField = processIgnoredAnalyzer(issueEntity.getIgnoreAnalyzer());
		if (!ignoredAnalyzerField.isEmpty()){
			details.addHistoryField(ignoredAnalyzerField);
		}

		activity.setDetails(details);
		return activity;
	}

	private HistoryField processIssueDescription(String oldIssueDescription) {
		HistoryField historyField = new HistoryField();

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

	private HistoryField processIssueTypes(IssueEntity issueEntity) {
		HistoryField historyField = new HistoryField();
		historyField.setField(ISSUE_TYPE);
		project.getIssueTypes()
				.stream()
				.filter(i -> i.getLocator().equalsIgnoreCase(issueEntity.getIssueType().getLocator()))
				.findFirst()
				.ifPresent(it -> historyField.setOldValue(it.getLongName()));
		project.getIssueTypes()
				.stream()
				.filter(i -> i.getLocator().equalsIgnoreCase(issueDefinition.getIssue().getIssueType()))
				.findFirst()
				.ifPresent(it -> historyField.setNewValue(it.getLongName()));
		return historyField;
	}

	private HistoryField processIgnoredAnalyzer(boolean oldIgnoredAnalyser) {
		HistoryField historyField = new HistoryField();
		historyField.setField(IGNORE_ANALYZER);
		boolean newIgnoreAnalyzer = issueDefinition.getIssue().getIgnoreAnalyzer();
		if (oldIgnoredAnalyser != newIgnoreAnalyzer){
			historyField.setOldValue(String.valueOf(oldIgnoredAnalyser));
			historyField.setNewValue(String.valueOf(newIgnoreAnalyzer));
		}
		return historyField;
	}
}
