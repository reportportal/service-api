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
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;

import static com.epam.ta.reportportal.core.events.activity.ActivityAction.*;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.TICKET_ID;
import static com.epam.ta.reportportal.entity.Activity.ActivityEntityType.TICKET;

/**
 * @author Andrei Varabyeu
 */
public class LinkTicketEvent extends AroundEvent<TestItemActivityResource> implements ActivityEvent {

	private Long attachedBy;

	public LinkTicketEvent() {
	}

	public LinkTicketEvent(Long attachedBy) {
		this.attachedBy = attachedBy;
	}

	public LinkTicketEvent(TestItemActivityResource before, TestItemActivityResource after, Long attachedBy) {
		super(before, after);
		this.attachedBy = attachedBy;
	}

	public Long getAttachedBy() {
		return attachedBy;
	}

	@Override
	public Activity toActivity() {
		ActivityBuilder builder = new ActivityBuilder().addCreatedNow()
				.addAction(getAfter().isAutoAnalyzed() ? LINK_ISSUE_AA : LINK_ISSUE)
				.addActivityEntityType(TICKET)
				.addUserId(attachedBy)
				.addObjectId(getAfter().getId())
				.addObjectName(getAfter().getName())
				.addProjectId(getAfter().getProjectId());

		if (getAfter() != null) {
			String oldValue = getBefore().getTickets();
			String newValue = getAfter().getTickets();
			if (!oldValue.isEmpty() && !newValue.isEmpty() || !oldValue.equalsIgnoreCase(newValue)) {
				if (oldValue.length() > newValue.length()) {
					builder.addAction(UNLINK_ISSUE);
				}
				builder.addHistoryField(TICKET_ID, oldValue, newValue);
			}
		}

		return builder.get();

	}
}
