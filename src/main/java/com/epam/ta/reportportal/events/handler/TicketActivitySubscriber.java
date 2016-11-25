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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.inject.Provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.epam.ta.reportportal.database.dao.ActivityRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.Activity;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.item.issue.TestItemIssue;
import com.epam.ta.reportportal.database.entity.statistics.StatisticSubType;
import com.epam.ta.reportportal.events.ItemIssueTypeDefined;
import com.epam.ta.reportportal.events.TicketAttachedEvent;
import com.epam.ta.reportportal.events.TicketPostedEvent;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.issue.IssueDefinition;
import com.google.common.collect.ImmutableMap;

/**
 * @author Andrei Varabyeu
 */
@Component
public class TicketActivitySubscriber {

	public static final String TICKET_ID = "ticketId";
	public static final String POST_ISSUE = "post_issue";
	public static final String ATTACH_ISSUE = "attach_issue";

	public static final String UPDATE_ITEM = "update_item";
	public static final String ISSUE_TYPE = "issueType";
	public static final String COMMENT = "comment";

	private final ActivityRepository activityRepository;

	private final Provider<ActivityBuilder> activityBuilder;

	private final TestItemRepository testItemRepository;

	private final ProjectRepository projectSettingsRepository;

	@Autowired
	public TicketActivitySubscriber(ActivityRepository activityRepository, Provider<ActivityBuilder> activityBuilder,
			TestItemRepository testItemRepository, ProjectRepository projectSettingsRepository) {
		this.activityRepository = activityRepository;
		this.activityBuilder = activityBuilder;
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
		Activity.FieldValues fieldValues = Activity.FieldValues.newOne().withOldValue(oldValue).withNewValue(newValue);
		HashMap<String, Activity.FieldValues> history = new HashMap<>();
		history.put(TICKET_ID, fieldValues);
		Activity activity = activityBuilder.get().addProjectRef(event.getProject()).addActionType(POST_ISSUE)
				.addLoggedObjectRef(event.getTestItemId()).addObjectType(TestItem.TEST_ITEM).addUserRef(event.getPostedBy())
				.addHistory(history).build();
		activityRepository.save(activity);
	}

	@EventListener
	public void onTicketAttached(TicketAttachedEvent event) {
		List<Activity> activities = new ArrayList<>();
		String separator = ",";
		Iterable<TestItem> testItems = event.getBefore();
		Map<String, Activity.FieldValues> results = StreamSupport.stream(testItems.spliterator(), false)
				.filter(item -> null != item.getIssue()).collect(Collectors.toMap(TestItem::getId, item -> Activity.FieldValues.newOne()
						.withOldValue(issuesIdsToString(item.getIssue().getExternalSystemIssues(), separator))));

		Iterable<TestItem> updated = event.getAfter();

		for (TestItem testItem : updated) {
			if (null == testItem.getIssue())
				continue;
			Activity.FieldValues fieldValues = results.get(testItem.getId());
			fieldValues.withNewValue(issuesIdsToString(testItem.getIssue().getExternalSystemIssues(), separator));

			Activity activity = activityBuilder.get().addProjectRef(event.getProject()).addActionType(ATTACH_ISSUE)
					.addLoggedObjectRef(testItem.getId()).addObjectType(TestItem.TEST_ITEM).addUserRef(event.getPostedBy())
					.addHistory(ImmutableMap.<String, Activity.FieldValues> builder().put(TICKET_ID, fieldValues).build()).build();
			activities.add(activity);
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
			StatisticSubType statisticSubType = projectSettings.getConfiguration().getByLocator(issueDefinition.getIssue().getIssueType());
			String oldIssueType = projectSettings.getConfiguration().getByLocator(testItemIssue.getIssueType()).getLongName();
			String initialComment = issueDefinition.getIssue().getComment();
			String comment = (null != initialComment) ? initialComment.trim() : emptyString;
			if (null == oldIssueDescription) {
				oldIssueDescription = emptyString;
			}
			Activity activity = activityBuilder.get().addProjectRef(projectName).addLoggedObjectRef(issueDefinition.getId())
					.addObjectType(TestItem.TEST_ITEM).addActionType(UPDATE_ITEM).addUserRef(principal).build();
			HashMap<String, Activity.FieldValues> history = new HashMap<>();
			if (!oldIssueDescription.equals(comment)) {

				Activity.FieldValues fieldValues = Activity.FieldValues.newOne().withOldValue(oldIssueDescription).withNewValue(comment);
				history.put(COMMENT, fieldValues);
			}
			if (statisticSubType != null && ((null == oldIssueType) || !oldIssueType.equalsIgnoreCase(statisticSubType.getLongName()))) {
				Activity.FieldValues fieldValues = Activity.FieldValues.newOne().withOldValue(oldIssueType)
						.withNewValue(statisticSubType.getLongName());
				history.put(ISSUE_TYPE, fieldValues);
			}
			if (!history.isEmpty()) {
				activity.setHistory(history);
				activities.add(activity);
			}
		}
		return activities;
	}

}