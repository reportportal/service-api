/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.extension.bugtracking;

import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.epam.reportportal.api.model.PluginCommandContext;
import com.epam.reportportal.base.core.events.domain.TicketPostedEvent;
import com.epam.reportportal.base.infrastructure.model.externalsystem.PostTicketRQ;
import com.epam.reportportal.base.infrastructure.model.externalsystem.Ticket;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItemResults;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class BtsActivityPublisherTest {

  private final TestItemRepository testItemRepository = mock(TestItemRepository.class);
  private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

  private final BtsActivityPublisher btsActivityPublisher = new BtsActivityPublisher(
      testItemRepository, eventPublisher);

  @BeforeEach
  void setUp() {
    ReportPortalUser user = getRpUser("user", UserRole.USER, OrganizationRole.MEMBER,
        ProjectRole.VIEWER, 1L);
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(user, null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldPublishEventForEachBackLinkedItem() {
    Long itemId = 1L;
    TestItem testItem = new TestItem();
    testItem.setItemId(itemId);
    testItem.setName("test item");
    TestItemResults itemResults = new TestItemResults();
    itemResults.setStatus(StatusEnum.FAILED);
    testItem.setItemResults(itemResults);

    when(testItemRepository.findAllById(anyIterable())).thenReturn(List.of(testItem));

    Ticket ticket = new Ticket();
    ticket.setId("TICKET-1");
    ticket.setTicketUrl("http://bts/TICKET-1");

    PostTicketRQ ticketRQ = new PostTicketRQ();
    ticketRQ.setBackLinks(Map.of(itemId, "http://rp/item/1"));

    PluginCommandContext context = new PluginCommandContext().projectId(2L);

    Integration integration = new Integration();
    integration.setOrganizationId(3L);

    btsActivityPublisher.publishTicketPostedEvent(ticket, ticketRQ, context, integration);

    verify(eventPublisher, times(1)).publishEvent(any(TicketPostedEvent.class));
  }

  @Test
  void shouldNotPublishEventWhenBackLinksAreNull() {
    Ticket ticket = new Ticket();
    PostTicketRQ ticketRQ = new PostTicketRQ();

    Integration integration = new Integration();
    integration.setOrganizationId(3L);

    btsActivityPublisher.publishTicketPostedEvent(ticket, ticketRQ, null, integration);

    verify(eventPublisher, never()).publishEvent(any());
  }
}
