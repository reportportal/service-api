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
import com.google.common.base.Strings;

import java.time.LocalDateTime;
import java.util.List;

import static com.epam.ta.reportportal.core.events.activity.TicketPostedEvent.TICKET_ID;
import static com.epam.ta.reportportal.core.events.activity.TicketPostedEvent.issuesIdsToString;

/**
 * @author Andrei Varabyeu
 */
public class LinkTicketEvent extends AroundEvent<TestItem> implements ActivityEvent {

	private final Long attachedBy;
	private final Long projectId;

	public LinkTicketEvent(TestItem before, TestItem after, Long attachedBy, Long projectId) {
		super(before, after);
		this.attachedBy = attachedBy;
		this.projectId = projectId;
	}

	@Override
	public Activity toActivity() {
		Activity activity = new Activity();
		activity.setCreatedAt(LocalDateTime.now());
		activity.setAction(getAfter().getItemResults().getIssue().getAutoAnalyzed() ?
				ActivityAction.LINK_ISSUE_AA.getValue() :
				ActivityAction.LINK_ISSUE.getValue());
		activity.setEntity(Activity.Entity.TICKET);
		activity.setUserId(attachedBy);
		activity.setProjectId(projectId);

		ActivityDetails details = new ActivityDetails();

		if (getAfter().getItemResults().getIssue() != null) {
			String oldValue = issuesIdsToString(getBefore());
			String newValue = issuesIdsToString(getAfter());
			if (!oldValue.isEmpty() && !newValue.isEmpty() || !oldValue.equalsIgnoreCase(newValue)){
				if (oldValue.length() > newValue.length()){
					activity.setAction(ActivityAction.UNLINK_ISSUE.getValue());
				}
				details.addHistoryField(TICKET_ID, new HistoryField(oldValue, newValue));
			}
		}

		activity.setDetails(details);
		return activity;
	}
}
