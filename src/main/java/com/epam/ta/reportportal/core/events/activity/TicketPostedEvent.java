/*
 * Copyright 2018 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.core.events.activity.details.ActivityDetailsUtil.TICKET_ID;

/**
 * @author Andrei Varabyeu
 */
public class TicketPostedEvent implements ActivityEvent {

	private final Ticket ticket;
	private final Long postedBy;
	private final TestItem testItem;
	private final Long projectId;

	public TicketPostedEvent(Ticket ticket, TestItem testItem, Long postedBy, Long projectId, String itemName) {
		this.ticket = ticket;
		this.postedBy = postedBy;
		this.testItem = testItem;
		this.projectId = projectId;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		activity.setAction(ActivityAction.POST_ISSUE.toString());
		activity.setEntity(Activity.Entity.TICKET);
		activity.setUserId(postedBy);
		activity.setProjectId(projectId);
		activity.setObjectId(testItem.getItemId());

		ActivityDetails details = new ActivityDetails(testItem.getName());
		processTicketId(details);

		activity.setDetails(details);
		return activity;
	}

	private void processTicketId(ActivityDetails details) {
		String oldValue = null;
		if (testItem != null && testItem.getItemResults() != null) {
			oldValue = issuesIdsToString(testItem.getItemResults().getIssue());
		}

		String newValue = ticket.getId() + ":" + ticket.getTicketUrl();
		if (null != oldValue) {
			newValue = oldValue + "," + newValue;
		}

		details.addHistoryField(HistoryField.of(TICKET_ID, oldValue, newValue));
	}

	static String issuesIdsToString(IssueEntity issue) {
		return issue.getTickets()
				.stream()
				.map(t -> t.getTicketId().concat(":").concat(t.getUrl()))
				.collect(Collectors.joining(","));
	}

}
