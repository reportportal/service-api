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
import com.epam.ta.reportportal.entity.activity.Activity;
import com.epam.ta.reportportal.ws.converter.builders.ActivityBuilder;
import com.epam.ta.reportportal.ws.model.activity.TestItemActivityResource;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.TICKET_ID;
import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.TICKET;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.*;

/**
 * @author Andrei Varabyeu
 */
public class LinkTicketEvent extends AroundEvent<TestItemActivityResource> implements ActivityEvent {

	private Long attachedBy;

	private String nameAttachedBy;

	public LinkTicketEvent() {
	}

	public LinkTicketEvent(Long attachedBy) {
		this.attachedBy = attachedBy;
	}

	public LinkTicketEvent(TestItemActivityResource before, TestItemActivityResource after, Long attachedBy) {
		super(before, after);
		this.attachedBy = attachedBy;
	}

	public LinkTicketEvent(TestItemActivityResource before, TestItemActivityResource after, String nameAttachedBy) {
		super(before, after);
		this.nameAttachedBy = nameAttachedBy;
	}

	public LinkTicketEvent(TestItemActivityResource before, TestItemActivityResource after, Long attachedBy, String nameAttachedBy) {
		super(before, after);
		this.attachedBy = attachedBy;
		this.nameAttachedBy = nameAttachedBy;
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
				.addUserName(nameAttachedBy)
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
