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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.bts.Ticket;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.google.common.base.Preconditions;

import java.util.function.Function;

/**
 * @author Pavel Bortnik
 */
public final class IntegrationIssueConverter {

	private IntegrationIssueConverter() {
		//static only
	}

	public static final Function<Issue.ExternalSystemIssue, Ticket> TO_TICKET = issue -> {
		Preconditions.checkNotNull(issue);
		Ticket ticket = new Ticket();
		ticket.setBtsUrl(issue.getBtsUrl());
		ticket.setBtsProject(issue.getBtsProject());
		ticket.setTicketId(issue.getTicketId());
		ticket.setUrl(issue.getUrl());
		return ticket;
	};
}
