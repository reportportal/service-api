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

package com.epam.reportportal.base.core.dashboard.impl;

import static com.epam.reportportal.base.ws.converter.converters.DashboardConverter.TO_ACTIVITY_RESOURCE;

import com.epam.reportportal.base.core.dashboard.CreateDashboardHandler;
import com.epam.reportportal.base.core.events.domain.DashboardCreatedEvent;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.DashboardRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.dashboard.Dashboard;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.model.EntryCreatedRS;
import com.epam.reportportal.base.model.dashboard.CreateDashboardRQ;
import com.epam.reportportal.base.ws.converter.builders.DashboardBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Creates a project dashboard from a user request.
 *
 * @author Pavel Bortnik
 */
@Service
@RequiredArgsConstructor
public class CreateDashboardHandlerImpl implements CreateDashboardHandler {

  private final static int DASHBOARD_LIMIT = 3000;
  private final DashboardRepository dashboardRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  public EntryCreatedRS createDashboard(MembershipDetails membershipDetails,
      CreateDashboardRQ rq, ReportPortalUser user) {

    BusinessRule.expect(
            dashboardRepository.findAllByProjectId(membershipDetails.getProjectId()).size()
                >= DASHBOARD_LIMIT, BooleanUtils::isFalse)
        .verify(ErrorType.DASHBOARD_UPDATE_ERROR, Suppliers.formattedSupplier(
            "The limit of {} dashboards has been reached. To create a new one you need to delete at least one created previously.",
            DASHBOARD_LIMIT
        ));

    BusinessRule.expect(
        dashboardRepository.existsByNameAndOwnerAndProjectId(rq.getName(), user.getUsername(),
            membershipDetails.getProjectId()
        ), BooleanUtils::isFalse).verify(ErrorType.RESOURCE_ALREADY_EXISTS, rq.getName());

    Dashboard dashboard =
        new DashboardBuilder().addDashboardRq(rq).addProject(membershipDetails.getProjectId())
            .addOwner(user.getUsername()).get();
    dashboardRepository.save(dashboard);
    eventPublisher.publishEvent(
        new DashboardCreatedEvent(TO_ACTIVITY_RESOURCE.apply(dashboard), user.getUserId(),
            user.getUsername(), membershipDetails.getOrgId()
        ));
    return new EntryCreatedRS(dashboard.getId());
  }
}
