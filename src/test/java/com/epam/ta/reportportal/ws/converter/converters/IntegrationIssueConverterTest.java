package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.bts.Ticket;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
class IntegrationIssueConverterTest {

	@Test
	void toTicket() {
		final Issue.ExternalSystemIssue issue = getIssue();
		final Ticket resource = IntegrationIssueConverter.TO_TICKET.apply(issue);

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