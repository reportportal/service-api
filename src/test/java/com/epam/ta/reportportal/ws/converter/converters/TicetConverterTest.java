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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class TicetConverterTest {

	@Test
	void toTicket() {
		final Issue.ExternalSystemIssue issue = getIssue();
		final Ticket resource = TicetConverter.TO_TICKET.apply(issue);

		assertEquals(resource.getTicketId(), issue.getTicketId());
		assertEquals(resource.getUrl(), issue.getUrl());
		assertEquals(resource.getBtsUrl(), issue.getBtsUrl());
		assertEquals(resource.getBtsProject(), issue.getBtsProject());
	}

	private static Issue.ExternalSystemIssue getIssue() {
		Issue.ExternalSystemIssue issue = new Issue.ExternalSystemIssue();
		issue.setBtsUrl("jira.com");
		issue.setBtsUrl("project");
		issue.setTicketId("ticketId");
		issue.setUrl("https:/example.com/ticketId");
		return issue;
	}
}