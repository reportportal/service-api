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

package com.epam.reportportal.core.bts.handler.impl;

import static com.epam.reportportal.infrastructure.persistence.commons.Predicates.notNull;
import static com.epam.reportportal.infrastructure.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.infrastructure.rules.exception.ErrorType.BAD_REQUEST_ERROR;
import static com.epam.reportportal.infrastructure.rules.exception.ErrorType.UNABLE_POST_TICKET;
import static com.epam.reportportal.ws.converter.converters.TestItemConverter.TO_ACTIVITY_RESOURCE;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.core.bts.handler.CreateTicketHandler;
import com.epam.reportportal.core.events.domain.TicketPostedEvent;
import com.epam.reportportal.core.integration.GetIntegrationHandler;
import com.epam.reportportal.core.plugin.PluginBox;
import com.epam.reportportal.extension.bugtracking.BtsConstants;
import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.reportportal.infrastructure.model.externalsystem.PostTicketRQ;
import com.epam.reportportal.infrastructure.model.externalsystem.Ticket;
import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.model.activity.TestItemActivityResource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Default implementation of {@link CreateTicketHandler}
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
@Service
@RequiredArgsConstructor
public class CreateTicketHandlerImpl implements CreateTicketHandler {

  private final TestItemRepository testItemRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final PluginBox pluginBox;
  private final GetIntegrationHandler getIntegrationHandler;

  @Override
  public Ticket createIssue(PostTicketRQ postTicketRQ, Long integrationId,
      MembershipDetails membershipDetails, ReportPortalUser user) {
    validatePostTicketRQ(postTicketRQ);

    List<TestItem> testItems = ofNullable(postTicketRQ.getBackLinks()).map(
        links -> testItemRepository.findAllById(links.keySet())).orElseGet(Collections::emptyList);
    List<TestItemActivityResource> before =
        testItems.stream()
            .map(it -> TO_ACTIVITY_RESOURCE.apply(it, membershipDetails.getProjectId()))
            .collect(Collectors.toList());

    Integration integration =
        getIntegrationHandler.getEnabledBtsIntegration(membershipDetails, integrationId);

    expect(BtsConstants.DEFECT_FORM_FIELDS.getParam(integration.getParams()), notNull()).verify(
        BAD_REQUEST_ERROR, "There aren't any submitted BTS fields!");

    BtsExtension btsExtension =
        pluginBox.getInstance(integration.getType().getName(), BtsExtension.class).orElseThrow(
            () -> new ReportPortalException(BAD_REQUEST_ERROR,
                Suppliers.formattedSupplier("BugTracking plugin for {} isn't installed",
                    BtsConstants.PROJECT.getParam(integration.getParams())
                ).get()
            ));

    Ticket ticket = btsExtension.submitTicket(postTicketRQ, integration);

    before.forEach(it -> eventPublisher.publishEvent(
        new TicketPostedEvent(ticket, user.getUserId(), user.getUsername(), it,
            membershipDetails.getOrgId())));
    return ticket;
  }

  /**
   * Additional validations to {@link PostTicketRQ}.
   *
   * @param postTicketRQ
   */
  private void validatePostTicketRQ(PostTicketRQ postTicketRQ) {
    if (postTicketRQ.getIsIncludeLogs() || postTicketRQ.getIsIncludeScreenshots()) {
      expect(postTicketRQ.getBackLinks(), notNull()).verify(
          UNABLE_POST_TICKET,
          "Test item id should be specified, when logs required in ticket description."
      );
    }
  }
}
