/*
 * Copyright 2019 EPAM Systems
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
import com.google.common.base.Strings;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.TICKET_ID;
import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.TICKET;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.*;

/**
 * @author Andrei Varabyeu
 */
public class LinkTicketEvent extends AroundEvent<TestItemActivityResource> implements ActivityEvent {

	private Long userId;
	private String userLogin;

	public LinkTicketEvent() {
	}

	public LinkTicketEvent(Long userId) {
		this.userId = userId;
	}

	public LinkTicketEvent(TestItemActivityResource before, TestItemActivityResource after, Long userId) {
		super(before, after);
		this.userId = userId;
	}

	public LinkTicketEvent(TestItemActivityResource before, TestItemActivityResource after, String userLogin) {
		super(before, after);
		this.userLogin = userLogin;
	}

	public LinkTicketEvent(TestItemActivityResource before, TestItemActivityResource after, Long attachedBy, String userLogin) {
		super(before, after);
		this.userId = attachedBy;
		this.userLogin = userLogin;
	}

	public Long getUserId() {
		return userId;
	}

	@Override
	public Activity toActivity() {
		ActivityBuilder builder = new ActivityBuilder().addCreatedNow()
				.addAction(getAfter().isAutoAnalyzed() ? LINK_ISSUE_AA : LINK_ISSUE)
				.addActivityEntityType(TICKET).addUserId(userId).addUserName(userLogin)
				.addObjectId(getAfter().getId())
				.addObjectName(getAfter().getName())
				.addProjectId(getAfter().getProjectId());

		if (getAfter() != null) {
			String oldValue = getBefore().getTickets();
			String newValue = getAfter().getTickets();
			//no changes with tickets
			if (Strings.isNullOrEmpty(oldValue) && newValue.isEmpty() || oldValue.equalsIgnoreCase(newValue)) {
				return null;
			}
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
