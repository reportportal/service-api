/*
 * Copyright 2025 EPAM Systems
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

package com.epam.reportportal.base.core.item.impl;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import com.epam.reportportal.base.core.item.ExternalTicketHandler;
import com.epam.reportportal.base.infrastructure.persistence.dao.TicketRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.bts.Ticket;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.issue.IssueEntity;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.model.item.UnlinkExternalIssueRQ;
import com.epam.reportportal.base.reporting.Issue;
import com.epam.reportportal.base.reporting.Issue.ExternalSystemIssue;
import com.epam.reportportal.base.ws.converter.converters.TicketConverter;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Links external BTS issues to test items in bulk.
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Component
public class ExternalTicketHandlerImpl implements ExternalTicketHandler {

  @Autowired
  private TicketRepository ticketRepository;

  @Override
  public void linkExternalTickets(String submitter, List<IssueEntity> issueEntities,
      List<Issue.ExternalSystemIssue> tickets) {
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
  public void updateLinking(String submitter, IssueEntity issueEntity,
      Set<Issue.ExternalSystemIssue> externalTickets) {
    ofNullable(externalTickets).ifPresent(tickets -> {
      Set<Ticket> existedTickets = collectTickets(tickets, submitter);
      issueEntity.getTickets().removeIf(it -> !existedTickets.contains(it));
      issueEntity.getTickets().addAll(existedTickets);
      existedTickets.stream().filter(it -> CollectionUtils.isEmpty(it.getIssues()))
          .forEach(it -> it.getIssues().add(issueEntity));
    });
  }

  /**
   * Finds tickets that are existed in db and removes them from request.
   *
   * @param externalIssues {@link com.epam.reportportal.base.reporting.Issue.ExternalSystemIssue}
   * @return List of existed tickets in db.
   */
  private List<Ticket> collectExistedTickets(Collection<ExternalSystemIssue> externalIssues) {
    if (CollectionUtils.isEmpty(externalIssues)) {
      return Collections.emptyList();
    }
    List<Ticket> existedTickets = ticketRepository.findByTicketIdIn(
        externalIssues.stream().map(Issue.ExternalSystemIssue::getTicketId).collect(toList()));
    List<String> existedTicketsIds =
        existedTickets.stream().map(Ticket::getTicketId).collect(toList());
    externalIssues.removeIf(it -> existedTicketsIds.contains(it.getTicketId()));
    return existedTickets;
  }

  /**
   * TODO document this
   *
   * @param externalIssues {@link com.epam.reportportal.base.reporting.Issue.ExternalSystemIssue}
   * @param username       {@link User#login}
   * @return {@link Set} of the {@link Ticket}
   */
  private Set<Ticket> collectTickets(Collection<Issue.ExternalSystemIssue> externalIssues,
      String username) {
    if (CollectionUtils.isEmpty(externalIssues)) {
      return Collections.emptySet();
    }
    return externalIssues.stream().map(it -> {
      Ticket ticket;
      Optional<Ticket> ticketOptional =
          ticketRepository.findByTicketIdAndBtsProject(it.getTicketId(), it.getBtsProject());
      if (ticketOptional.isPresent()) {
        ticket = ticketOptional.get();
        ticket.setUrl(it.getUrl());
        ticket.setBtsProject(it.getBtsProject());
        ticket.setBtsUrl(it.getBtsUrl());
        ticket.setPluginName(it.getPluginName());
      } else {
        ticket = TicketConverter.TO_TICKET.apply(it);
      }
      ticket.setSubmitter(username);
      ticket.setSubmitDate(ofNullable(it.getSubmitDate()).orElse(Instant.now()));
      return ticket;
    }).collect(toSet());
  }

  private void linkTickets(List<IssueEntity> issueEntities, List<Ticket> existedTickets,
      Set<Ticket> ticketsFromRq) {
    List<Ticket> tickets = ticketRepository.saveAll(ticketsFromRq);
    issueEntities.forEach(entity -> {
      entity.getTickets().addAll(existedTickets);
      entity.getTickets().addAll(tickets);
      entity.setAutoAnalyzed(false);
    });
  }

}
