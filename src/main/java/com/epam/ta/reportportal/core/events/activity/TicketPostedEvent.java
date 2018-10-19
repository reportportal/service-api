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

import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;

/**
 * @author Andrei Varabyeu
 */
public class TicketPostedEvent {

	private final Ticket ticket;
	private final String itemName;
	private final String postedBy;
	private final Long testItemId;
	private final String project;

	public TicketPostedEvent(Ticket ticket, Long testItemId, String postedBy, String project, String itemName) {
		this.ticket = ticket;
		this.postedBy = postedBy;
		this.testItemId = testItemId;
		this.project = project;
		this.itemName = itemName;
	}

	public Ticket getTicket() {
		return ticket;
	}

	public String getPostedBy() {
		return postedBy;
	}

	public Long getTestItemId() {
		return testItemId;
	}

	public String getProject() {
		return project;
	}

	public String getItemName() {
		return itemName;
	}
}
