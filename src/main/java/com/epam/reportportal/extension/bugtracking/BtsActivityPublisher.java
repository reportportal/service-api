package com.epam.reportportal.extension.bugtracking;

import static java.util.Optional.ofNullable;

import com.epam.reportportal.api.model.PluginCommandContext;
import com.epam.reportportal.base.core.events.domain.TicketPostedEvent;
import com.epam.reportportal.base.infrastructure.model.externalsystem.PostTicketRQ;
import com.epam.reportportal.base.infrastructure.model.externalsystem.Ticket;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.util.SecurityContextUtils;
import com.epam.reportportal.base.ws.converter.converters.TestItemConverter;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class BtsActivityPublisher {

  private final TestItemRepository testItemRepository;
  private final ApplicationEventPublisher eventPublisher;

  public BtsActivityPublisher(TestItemRepository testItemRepository, ApplicationEventPublisher eventPublisher) {
    this.testItemRepository = testItemRepository;
    this.eventPublisher = eventPublisher;
  }

  public void publishTicketPostedEvent(Ticket ticket, PostTicketRQ ticketRQ, PluginCommandContext context,
      Integration integration) {
    ofNullable(ticketRQ.getBackLinks()).map(Map::keySet)
        .map(testItemRepository::findAllById)
        .ifPresent(testItems -> {
          ReportPortalUser user = SecurityContextUtils.getPrincipal();
          Long projectId = context != null ? context.getProjectId() : null;
          Long orgId = integration.getOrganizationId();
          testItems.forEach(testItem -> eventPublisher.publishEvent(
              new TicketPostedEvent(ticket, user.getUserId(), user.getUsername(),
                  TestItemConverter.TO_ACTIVITY_RESOURCE.apply(testItem, projectId), orgId)));
        });
  }

}
