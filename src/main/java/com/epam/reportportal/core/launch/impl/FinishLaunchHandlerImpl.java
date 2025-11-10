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

package com.epam.reportportal.core.launch.impl;

import static com.epam.reportportal.core.launch.util.LaunchValidator.validate;
import static com.epam.reportportal.core.launch.util.LaunchValidator.validateRoles;
import static com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum.FAILED;
import static com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum.PASSED;
import static com.epam.reportportal.infrastructure.rules.exception.ErrorType.LAUNCH_NOT_FOUND;

import com.epam.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.reportportal.core.hierarchy.FinishHierarchyHandler;
import com.epam.reportportal.core.launch.FinishLaunchHandler;
import com.epam.reportportal.core.launch.util.LinkGenerator;
import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.model.launch.FinishLaunchRS;
import com.epam.reportportal.reporting.FinishExecutionRQ;
import com.epam.reportportal.ws.converter.builders.LaunchBuilder;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link FinishLaunchHandler}
 *
 * @author Pave Bortnik
 */
@Service
@Primary
@Transactional
public class FinishLaunchHandlerImpl implements FinishLaunchHandler {

  private final LaunchRepository launchRepository;
  private final FinishHierarchyHandler<Launch> finishHierarchyHandler;
  private final ApplicationEventPublisher eventPublisher;
  private final LinkGenerator linkGenerator;

  @Autowired
  public FinishLaunchHandlerImpl(LaunchRepository launchRepository,
      @Qualifier("finishLaunchHierarchyHandler")
      FinishHierarchyHandler<Launch> finishHierarchyHandler,
      ApplicationEventPublisher eventPublisher, LinkGenerator linkGenerator) {
    this.launchRepository = launchRepository;
    this.finishHierarchyHandler = finishHierarchyHandler;
    this.eventPublisher = eventPublisher;
    this.linkGenerator = linkGenerator;
  }

  @Override
  public FinishLaunchRS finishLaunch(String launchId, FinishExecutionRQ finishLaunchRQ,
      MembershipDetails membershipDetails, ReportPortalUser user, String baseUrl) {
    Launch launch = launchRepository.findByUuid(launchId)
        .orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId));

    validateRoles(launch, user, membershipDetails);
    validate(launch, finishLaunchRQ);

    Optional<StatusEnum> status = StatusEnum.fromValue(finishLaunchRQ.getStatus());

    Long id = launch.getId();

    final int finishedCount =
        finishHierarchyHandler.finishDescendants(launch, status.orElse(StatusEnum.INTERRUPTED),
            finishLaunchRQ.getEndTime(), user, membershipDetails
        );
    if (finishedCount > 0) {
      launch.setStatus(launchRepository.hasRootItemsWithStatusNotEqual(id, StatusEnum.PASSED.name(),
          StatusEnum.INFO.name(), StatusEnum.WARN.name()
      ) ? FAILED : PASSED);
    } else {
      launch.setStatus(status.orElseGet(() ->
          launchRepository.hasRootItemsWithStatusNotEqual(id, StatusEnum.PASSED.name(),
              StatusEnum.INFO.name(), StatusEnum.WARN.name()
          ) ? FAILED : PASSED));
    }

    launch = new LaunchBuilder(launch)
        .addDescription(buildDescription(launch.getDescription(), finishLaunchRQ.getDescription()))
        .addAttributes(finishLaunchRQ.getAttributes()).addEndTime(finishLaunchRQ.getEndTime())
        .get();

    String launchLink = linkGenerator.generateLaunchLink(baseUrl, membershipDetails.getProjectKey(),
        String.valueOf(launch.getId())
    );

    eventPublisher.publishEvent(new LaunchFinishedEvent(launch, user, baseUrl, membershipDetails.getOrgId()));

    FinishLaunchRS response = new FinishLaunchRS();
    response.setId(launch.getUuid());
    response.setNumber(launch.getNumber());
    response.setLink(launchLink);
    return response;
  }

  private String buildDescription(String existDescription, String fromRequestDescription) {
    if (null != existDescription) {
      return null != fromRequestDescription ? existDescription + " " + fromRequestDescription :
          existDescription;
    } else {
      return fromRequestDescription;
    }
  }

}
