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
import com.epam.ta.reportportal.core.events.activity.details.ActivityDetails;
import com.epam.ta.reportportal.core.events.activity.details.HistoryField;
import com.epam.ta.reportportal.entity.Activity;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

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

		ActivityDetails details = new ActivityDetails();
		processTicketId(details);
		activity.setDetails(details);

		return activity;
	}

	private void processTicketId(ActivityDetails details) {
		String oldValue = null;
		if (testItem != null && testItem.getItemResults() != null) {
			oldValue = testItem.getItemResults()
					.getIssue()
					.getTickets()
					.stream()
					.map(t -> t.getTicketId() + ":" + t.getUrl())
					.collect(Collectors.joining(","));
		}

		String newValue = ticket.getId() + ":" + ticket.getTicketUrl();
		if (null != oldValue) {
			newValue = oldValue + "," + newValue;
		}

		details.addHistoryField("ticketId", new HistoryField<String>(oldValue, newValue));
	}

}
