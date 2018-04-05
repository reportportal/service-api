/*
 * Copyright 2017 EPAM Systems
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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.store.database.entity.bts.Ticket;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.google.common.base.Preconditions;

import java.util.function.Function;

/**
 * @author Pavel Bortnik
 */
public final class ExternalSystemIssueConverter {

	private ExternalSystemIssueConverter() {
		//static only
	}

	public static final Function<Issue.ExternalSystemIssue, Ticket> TO_TICKET = issue -> {
		Preconditions.checkNotNull(issue);
		Ticket ticket = new Ticket();
		ticket.setBugTrackingSystemId(issue.getExternalSystemId());
		ticket.setTicketId(issue.getTicketId());
		ticket.setUrl(issue.getUrl());
		return ticket;
	};
}
