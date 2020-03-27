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

package com.epam.ta.reportportal.core.item.impl;

import com.epam.ta.reportportal.core.item.ExternalTicketHandler;
import com.epam.ta.reportportal.dao.TicketRepository;
import com.epam.ta.reportportal.entity.bts.Ticket;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.item.issue.IssueEntity;
import com.epam.ta.reportportal.ws.converter.converters.TicketConverter;
import com.epam.ta.reportportal.ws.model.issue.Issue;
import com.epam.ta.reportportal.ws.model.item.UnlinkExternalIssueRQ;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class ExternalTicketHandlerImpl implements ExternalTicketHandler {

	@Autowired
	private TicketRepository ticketRepository;

	@Override
	public void linkExternalTickets(String submitter, List<IssueEntity> issueEntities, List<Issue.ExternalSystemIssue> tickets) {
		List<Ticket> existedTickets = collectExistedTickets(tickets);
		Set<Ticket> ticketsFromRq = collectTickets(tickets, submitter);
		linkTickets(issueEntities, existedTickets, ticketsFromRq);
	}

	@Override
	public void unlinkExternalTickets(List<TestItem> items, UnlinkExternalIssueRQ request) {
		items.forEach(testItem -> {
			IssueEntity issue = testItem.getItemResults().getIssue();
			if (issue.getTickets().removeIf(it -> request.getTicketIds().contains(it.getTicketId()))) {
				issue.setAutoAnalyzed(false);
			}
		});
	}

	@Override
	public void updateLinking(String submitter, IssueEntity issueEntity, Set<Issue.ExternalSystemIssue> externalTickets) {
		ofNullable(externalTickets).ifPresent(tickets -> {
			Set<Ticket> existedTickets = collectTickets(tickets, submitter);
			issueEntity.getTickets().removeIf(it -> !existedTickets.contains(it));
			issueEntity.getTickets().addAll(existedTickets);
			existedTickets.stream().filter(it -> CollectionUtils.isEmpty(it.getIssues())).forEach(it -> it.getIssues().add(issueEntity));
		});
	}

	/**
	 * Finds tickets that are existed in db and removes them from request.
	 *
	 * @param externalIssues {@link com.epam.ta.reportportal.ws.model.issue.Issue.ExternalSystemIssue}
	 * @return List of existed tickets in db.
	 */
	private List<Ticket> collectExistedTickets(Collection<Issue.ExternalSystemIssue> externalIssues) {
		if (CollectionUtils.isEmpty(externalIssues)) {
			return Collections.emptyList();
		}
		List<Ticket> existedTickets = ticketRepository.findByTicketIdIn(externalIssues.stream()
				.map(Issue.ExternalSystemIssue::getTicketId)
				.collect(toList()));
		List<String> existedTicketsIds = existedTickets.stream().map(Ticket::getTicketId).collect(toList());
		externalIssues.removeIf(it -> existedTicketsIds.contains(it.getTicketId()));
		return existedTickets;
	}

	/**
	 * TODO document this
	 *
	 * @param externalIssues {@link com.epam.ta.reportportal.ws.model.issue.Issue.ExternalSystemIssue}
	 * @param username       {@link com.epam.ta.reportportal.entity.user.User#login}
	 * @return {@link Set} of the {@link Ticket}
	 */
	private Set<Ticket> collectTickets(Collection<Issue.ExternalSystemIssue> externalIssues, String username) {
		if (CollectionUtils.isEmpty(externalIssues)) {
			return Collections.emptySet();
		}
		return externalIssues.stream().map(it -> {
			Ticket ticket;
			Optional<Ticket> ticketOptional = ticketRepository.findByTicketId(it.getTicketId());
			if (ticketOptional.isPresent()) {
				ticket = ticketOptional.get();
				ticket.setUrl(it.getUrl());
				ticket.setBtsProject(it.getBtsProject());
				ticket.setBtsUrl(it.getBtsUrl());
			} else {
				ticket = TicketConverter.TO_TICKET.apply(it);
			}
			ticket.setSubmitter(username);
			ticket.setSubmitDate(ofNullable(it.getSubmitDate()).map(millis -> LocalDateTime.ofInstant(Instant.ofEpochMilli(millis),
					ZoneOffset.UTC
			)).orElse(LocalDateTime.now()));
			return ticket;
		}).collect(toSet());
	}

	private void linkTickets(List<IssueEntity> issueEntities, List<Ticket> existedTickets, Set<Ticket> ticketsFromRq) {
		List<Ticket> tickets = ticketRepository.saveAll(ticketsFromRq);
		issueEntities.forEach(entity -> {
			entity.getTickets().addAll(existedTickets);
			entity.getTickets().addAll(tickets);
			entity.setAutoAnalyzed(false);
		});
	}

}
