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
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import com.google.common.base.Strings;

import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.EMPTY_STRING;
import static com.epam.ta.reportportal.core.events.activity.util.ActivityDetailsUtil.TICKET_ID;
import static com.epam.ta.reportportal.entity.activity.Activity.ActivityEntityType.TICKET;
import static com.epam.ta.reportportal.entity.activity.ActivityAction.POST_ISSUE;

/**
 * @author Andrei Varabyeu
 */
public class TicketPostedEvent extends AbstractEvent implements ActivityEvent {

	private Ticket ticket;
	private TestItemActivityResource testItemActivityResource;

	public TicketPostedEvent() {
	}

	public TicketPostedEvent(Ticket ticket, Long userId, String userLogin, TestItemActivityResource testItemActivityResource) {
		super(userId, userLogin);
		this.ticket = ticket;
		this.testItemActivityResource = testItemActivityResource;
	}

	public Ticket getTicket() {
		return ticket;
	}

	public void setTicket(Ticket ticket) {
		this.ticket = ticket;
	}

	public TestItemActivityResource getTestItemActivityResource() {
		return testItemActivityResource;
	}

	public void setTestItemActivityResource(TestItemActivityResource testItemActivityResource) {
		this.testItemActivityResource = testItemActivityResource;
	}

	@Override
	public Activity toActivity() {
		return new ActivityBuilder().addCreatedNow()
				.addAction(POST_ISSUE).addActivityEntityType(TICKET).addUserId(getUserId()).addUserName(getUserLogin())
				.addObjectId(testItemActivityResource.getId())
				.addObjectName(testItemActivityResource.getName())
				.addProjectId(testItemActivityResource.getProjectId())
				.addHistoryField(
						TICKET_ID,
						Strings.isNullOrEmpty(testItemActivityResource.getTickets()) ? EMPTY_STRING : testItemActivityResource.getTickets(),
						Strings.isNullOrEmpty(testItemActivityResource.getTickets()) ?
								ticket.getId() + ":" + ticket.getTicketUrl() :
								testItemActivityResource.getTickets() + "," + ticket.getId() + ":" + ticket.getTicketUrl()
				)
				.get();
	}
}
