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

package com.epam.reportportal.base.infrastructure.persistence.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.epam.reportportal.base.infrastructure.persistence.dao.TicketRepository;
import com.epam.reportportal.base.ws.BaseMvcTest;
import com.epam.reportportal.base.infrastructure.persistence.entity.bts.Ticket;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Sql("/db/fill/ticket/ticket-fill.sql")
class TicketRepositoryTest extends BaseMvcTest {

  @Autowired
  private TicketRepository repository;

  @Test
  void findByTicketId() {
    final String ticketId = "ticket_id_1";

    final Optional<Ticket> ticketOptional = repository.findByTicketId(ticketId);

    assertTrue(ticketOptional.isPresent(), "Ticket not found");
    assertEquals(ticketId, ticketOptional.get().getTicketId(), "Incorrect ticket id");
  }

  @Test
  void findByTicketIdIn() {
    List<String> ids = Arrays.asList("ticket_id_1", "ticket_id_3");

    final List<Ticket> tickets = repository.findByTicketIdIn(ids);

    assertNotNull(tickets, "Tickets not found");
    assertEquals(2, tickets.size(), "Incorrect tickets count");
    assertThat(tickets.stream().map(Ticket::getTicketId)
        .collect(Collectors.toList())).containsExactlyInAnyOrder(ids.get(0),
        ids.get(1)
    );
  }

  @Test
  void deleteById() {
    repository.deleteById(2L);
    final List<Ticket> tickets = repository.findAll();

    assertEquals(2, tickets.size());
  }

  @Test
  void findUniqueTicketsCountBefore() {
    assertEquals(1, repository.findUniqueCountByProjectBefore(1L,
        Instant.now().minus(2, ChronoUnit.DAYS)));
    assertEquals(0, repository.findUniqueCountByProjectBefore(2L,
        Instant.now().minus(2, ChronoUnit.DAYS)));
  }
}
