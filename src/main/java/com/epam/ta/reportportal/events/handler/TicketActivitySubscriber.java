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
package com.epam.ta.reportportal.events.handler;

import com.epam.ta.reportportal.database.dao.ActivityRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.database.entity.item.ActivityEventType;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.database.entity.statistics.StatisticSubType;
import com.epam.ta.reportportal.events.ItemIssueTypeDefined;
import com.epam.ta.reportportal.events.TicketAttachedEvent;
import com.epam.ta.reportportal.events.TicketPostedEvent;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.issue.IssueDefinition;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.epam.ta.reportportal.database.entity.item.ActivityEventType.*;
import static com.epam.ta.reportportal.database.entity.item.ActivityObjectType.TEST_ITEM;
import static com.epam.ta.reportportal.events.handler.EventHandlerUtil.createHistoryField;

/**
 * @author Andrei Varabyeu
 */
@Component
public class TicketActivitySubscriber {

	public static final String TICKET_ID = "ticketId";
	public static final String ISSUE_TYPE = "issueType";
	public static final String IGNORE_ANALYZER = "ignoreAnalyzer";
	public static final String COMMENT = "comment";

	private final ActivityRepository activityRepository;

	private final TestItemRepository testItemRepository;

	private final ProjectRepository projectSettingsRepository;

	@Autowired
	public TicketActivitySubscriber(ActivityRepository activityRepository, TestItemRepository testItemRepository,
			ProjectRepository projectSettingsRepository) {
		this.activityRepository = activityRepository;
		this.testItemRepository = testItemRepository;
		this.projectSettingsRepository = projectSettingsRepository;
	}

	@EventListener
	public void onTicketPosted(TicketPostedEvent event) {
		TestItem testItem = testItemRepository.findOne(event.getTestItemId());
		String oldValue = null;

		String separator = ",";
		if ((null != testItem) && (null != testItem.getIssue())) {
			oldValue = issuesIdsToString(testItem.getIssue().getExternalSystemIssues(), separator);
		}
		String newValue;
		if (null == oldValue) {
			newValue = event.getTicket().getId() + ":" + event.getTicket().getTicketUrl();
		} else {
			newValue = oldValue + separator + event.getTicket().getId() + ":" + event.getTicket().getTicketUrl();
		}
		List<Activity.FieldValues> history = Lists.newArrayList();
		Activity.FieldValues fieldValues = Activity.FieldValues.newOne().withField(TICKET_ID).withOldValue(oldValue).withNewValue(newValue);
		history.add(fieldValues);
		Activity activity = new ActivityBuilder().addProjectRef(event.getProject())
				.addActionType(POST_ISSUE)
				.addLoggedObjectRef(event.getTestItemId())
				.addObjectType(TEST_ITEM)
				.addObjectName(event.getItemName())
				.addUserRef(event.getPostedBy())
				.addHistory(history)
				.get();
		activityRepository.save(activity);
	}

	@EventListener
	public void onTicketAttached(TicketAttachedEvent event) {
		List<Activity> activities = new ArrayList<>();
		String separator = ",";
		Iterable<TestItem> testItems = event.getBefore();
		Map<String, Activity.FieldValues> results = StreamSupport.stream(testItems.spliterator(), false)
				.filter(item -> null != item.getIssue())
				.collect(Collectors.toMap(TestItem::getId, item -> Activity.FieldValues.newOne()
						.withOldValue(issuesIdsToString(item.getIssue().getExternalSystemIssues(), separator))));

		Iterable<TestItem> updated = event.getAfter();

		for (TestItem testItem : updated) {
			if (null == testItem.getIssue()) {
				continue;
			}
			Activity.FieldValues fieldValues = results.get(testItem.getId());

			String newValue = issuesIdsToString(testItem.getIssue().getExternalSystemIssues(), separator);
			if (newValue != null) {
				fieldValues.withField(TICKET_ID).withNewValue(newValue);
				ActivityEventType type = testItem.getIssue().isAutoAnalyzed() ? ATTACH_ISSUE_AA : ATTACH_ISSUE;
				Activity activity = new ActivityBuilder().addProjectRef(event.getProject())
						.addActionType(type)
						.addLoggedObjectRef(testItem.getId())
						.addObjectType(TEST_ITEM)
						.addObjectName(testItem.getName())
						.addUserRef(event.getPostedBy())
						.addHistory(Collections.singletonList(fieldValues))
						.get();
				activities.add(activity);
			}
		}
		activityRepository.save(activities);
	}

	@EventListener
	public void onIssueTypeDefined(ItemIssueTypeDefined itemIssueTypeDefined) {
		Map<IssueDefinition, TestItem> data = itemIssueTypeDefined.getBefore();
		List<Activity> activities = processTestItemIssues(itemIssueTypeDefined.getProject(), itemIssueTypeDefined.getPostedBy(), data);
		if (!activities.isEmpty()) {
			activityRepository.save(activities);
		}
	}

	private String issuesIdsToString(Set<TestItemIssue.ExternalSystemIssue> externalSystemIssues, String separator) {
		if (null != externalSystemIssues && !externalSystemIssues.isEmpty()) {
			return externalSystemIssues.stream()
					.map(externalSystemIssue -> externalSystemIssue.getTicketId().concat(":").concat(externalSystemIssue.getUrl()))
					.collect(Collectors.joining(separator));
		}
		return null;
	}

	private List<Activity> processTestItemIssues(String projectName, String principal, Map<IssueDefinition, TestItem> data) {
		String emptyString = "";
		List<Activity> activities = new ArrayList<>();
		final Project projectSettings = projectSettingsRepository.findOne(projectName);
		Set<Map.Entry<IssueDefinition, TestItem>> entries = data.entrySet();
		for (Map.Entry<IssueDefinition, TestItem> entry : entries) {
			IssueDefinition issueDefinition = entry.getKey();
			TestItem testItem = entry.getValue();
			TestItemIssue testItemIssue = testItem.getIssue();
			String oldIssueDescription = testItemIssue.getIssueDescription();
			boolean oldIgnoreAnalyzer = testItemIssue.isIgnoreAnalyzer();
			StatisticSubType statisticSubType = projectSettings.getConfiguration().getByLocator(issueDefinition.getIssue().getIssueType());
			String oldIssueType = projectSettings.getConfiguration().getByLocator(testItemIssue.getIssueType()).getLongName();
			String initialComment = issueDefinition.getIssue().getComment();
			String comment = (null != initialComment) ? initialComment.trim() : emptyString;
			if (null == oldIssueDescription) {
				oldIssueDescription = emptyString;
			}

			ActivityEventType type = issueDefinition.getIssue().getAutoAnalyzed() ? ANALYZE_ITEM : UPDATE_ITEM;
			Activity activity = new ActivityBuilder().addProjectRef(projectName)
					.addLoggedObjectRef(issueDefinition.getId())
					.addObjectType(TEST_ITEM)
					.addObjectName(testItem.getName())
					.addActionType(type)
					.addUserRef(principal)
					.get();
			List<Activity.FieldValues> history = Lists.newArrayList();
			if (!oldIssueDescription.equals(comment)) {
				Activity.FieldValues fieldValues = createHistoryField(COMMENT, oldIssueDescription, comment);
				history.add(fieldValues);
			}
			if (statisticSubType != null && ((null == oldIssueType) || !oldIssueType.equalsIgnoreCase(statisticSubType.getLongName()))) {
				Activity.FieldValues fieldValues = createHistoryField(ISSUE_TYPE, oldIssueType, statisticSubType.getLongName());
				history.add(fieldValues);
			}
			if (oldIgnoreAnalyzer != issueDefinition.getIssue().getIgnoreAnalyzer()) {
				Activity.FieldValues field = createHistoryField(IGNORE_ANALYZER, String.valueOf(oldIgnoreAnalyzer),
						String.valueOf(issueDefinition.getIssue().getIgnoreAnalyzer())
				);
				history.add(field);
			}
			if (!history.isEmpty()) {
				activity.setHistory(history);
				activities.add(activity);
			}
		}
		return activities;
	}

}