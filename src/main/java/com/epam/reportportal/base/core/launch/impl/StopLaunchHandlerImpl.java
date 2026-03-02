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

package com.epam.reportportal.base.core.launch.impl;

import static com.epam.reportportal.base.core.launch.util.LaunchValidator.validate;
import static com.epam.reportportal.base.core.launch.util.LaunchValidator.validateRoles;
import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum.STOPPED;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import com.epam.reportportal.base.core.events.domain.LaunchFinishedEvent;
import com.epam.reportportal.base.core.item.TestItemStatisticsService;
import com.epam.reportportal.base.core.launch.StopLaunchHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.model.BulkRQ;
import com.epam.reportportal.base.reporting.FinishExecutionRQ;
import com.epam.reportportal.base.reporting.ItemAttributeResource;
import com.epam.reportportal.base.reporting.OperationCompletionRS;
import com.epam.reportportal.base.ws.converter.builders.LaunchBuilder;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
@Transactional
@RequiredArgsConstructor
public class StopLaunchHandlerImpl implements StopLaunchHandler {

  private static final String LAUNCH_STOP_DESCRIPTION = " stopped";

  private final LaunchRepository launchRepository;
  private final TestItemRepository testItemRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final TestItemStatisticsService statisticsService;

  @Override
  public OperationCompletionRS stopLaunch(Long launchId, FinishExecutionRQ finishLaunchRQ,
      MembershipDetails membershipDetails, ReportPortalUser user, String baseUrl) {
    Launch launch = launchRepository.findById(launchId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));

    validateRoles(launch, user, membershipDetails);
    validate(launch, finishLaunchRQ);

    launch = new LaunchBuilder(launch).addDescription(
            ofNullable(finishLaunchRQ.getDescription()).orElse(
                ofNullable(launch.getDescription()).orElse("")).concat(LAUNCH_STOP_DESCRIPTION))
        .addStatus(ofNullable(finishLaunchRQ.getStatus()).orElse(STOPPED.name()))
        .addEndTime(ofNullable(finishLaunchRQ.getEndTime()).orElse(Instant.now()))
        .addAttributes(finishLaunchRQ.getAttributes())
        .addAttribute(new ItemAttributeResource("status", "stopped")).get();

    launchRepository.save(launch);
    testItemRepository.interruptInProgressItems(launch.getId());
    statisticsService.addInterruptionStatistics(launch.getId());

    eventPublisher.publishEvent(
        new LaunchFinishedEvent(launch, user.getUserId(), user.getUsername(), baseUrl,
            membershipDetails.getOrgId()));
    return new OperationCompletionRS("Launch with ID = '" + launchId + "' successfully stopped.");
  }

  @Override
  public List<OperationCompletionRS> stopLaunch(BulkRQ<Long, FinishExecutionRQ> bulkRQ,
      MembershipDetails membershipDetails, ReportPortalUser user, String baseUrl) {
    return bulkRQ.getEntities().entrySet().stream()
        .map(
            entry -> stopLaunch(entry.getKey(), entry.getValue(), membershipDetails, user, baseUrl))
        .collect(toList());
  }
}
