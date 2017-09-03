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
package com.epam.ta.reportportal.events;

import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;

/**
 * @author Andrei Varabyeu
 */
public class TicketPostedEvent {

	private final Ticket ticket;
	private final String itemName;
	private final String postedBy;
	private final String testItemId;
	private final String project;

	public TicketPostedEvent(Ticket ticket, String testItemId, String postedBy, String project, String itemName) {
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

	public String getTestItemId() {
		return testItemId;
	}

	public String getProject() {
		return project;
	}

	public String getItemName() {
		return itemName;
	}
}
