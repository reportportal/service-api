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

package com.epam.ta.reportportal.ws.converter.converters;

import com.epam.ta.reportportal.entity.bts.Ticket;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.google.common.base.Preconditions;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Pavel Bortnik
 */
public final class IssueConverter {

	private IssueConverter() {
		//static only
	}

	public static final Function<Issue, IssueEntity> TO_ISSUE = from -> {
		IssueEntity issue = new IssueEntity();
		issue.setAutoAnalyzed(from.getAutoAnalyzed());
		issue.setIgnoreAnalyzer(from.getIgnoreAnalyzer());
		issue.setIssueDescription(from.getComment());
		return issue;
	};

	/**
	 * Converts external system from db to model
	 */
	public static final Function<Ticket, Issue.ExternalSystemIssue> TO_MODEL_EXTERNAL = externalSystemIssue -> {
		Issue.ExternalSystemIssue ticket = new Issue.ExternalSystemIssue();
		ticket.setTicketId(externalSystemIssue.getTicketId());
		ticket.setBtsUrl(externalSystemIssue.getBtsUrl());
		ticket.setUrl(externalSystemIssue.getUrl());
		ticket.setBtsProject(externalSystemIssue.getBtsProject());
		return ticket;
	};
	/**
	 * Converts issue from db to model
	 */
	public static final Function<IssueEntity, Issue> TO_MODEL = issueEntity -> {
		Preconditions.checkNotNull(issueEntity);
		Issue issue = new Issue();
		issue.setIssueType(issueEntity.getIssueType().getLocator());
		issue.setAutoAnalyzed(issueEntity.getAutoAnalyzed());
		issue.setIgnoreAnalyzer(issueEntity.getIgnoreAnalyzer());
		issue.setComment(issueEntity.getIssueDescription());

		Optional.ofNullable(issueEntity.getTickets()).ifPresent(tickets -> {
			issue.setExternalSystemIssues(tickets.stream().map(IssueConverter.TO_MODEL_EXTERNAL).collect(Collectors.toSet()));
		});
		return issue;
	};
}
