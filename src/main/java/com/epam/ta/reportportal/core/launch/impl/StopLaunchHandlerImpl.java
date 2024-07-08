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

package com.epam.ta.reportportal.core.launch.impl;

import static com.epam.ta.reportportal.core.launch.util.LaunchValidator.validate;
import static com.epam.ta.reportportal.core.launch.util.LaunchValidator.validateRoles;
import static com.epam.ta.reportportal.entity.enums.StatusEnum.STOPPED;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.activity.LaunchFinishedEvent;
import com.epam.ta.reportportal.core.launch.StopLaunchHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.model.BulkRQ;
import com.epam.ta.reportportal.ws.converter.builders.LaunchBuilder;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.ws.reporting.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.reporting.ItemAttributeResource;
import com.epam.ta.reportportal.ws.reporting.OperationCompletionRS;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
@Transactional
public class StopLaunchHandlerImpl implements StopLaunchHandler {

  private static final String LAUNCH_STOP_DESCRIPTION = " stopped";

  private final LaunchRepository launchRepository;
  private final TestItemRepository testItemRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Autowired
  public StopLaunchHandlerImpl(LaunchRepository launchRepository,
      TestItemRepository testItemRepository, ApplicationEventPublisher eventPublisher) {
    this.launchRepository = launchRepository;
    this.testItemRepository = testItemRepository;
    this.eventPublisher = eventPublisher;
  }

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

    eventPublisher.publishEvent(
        new LaunchFinishedEvent(launch, user.getUserId(), user.getUsername(), baseUrl));
    return new OperationCompletionRS("Launch with ID = '" + launchId + "' successfully stopped.");
  }

  @Override
  public List<OperationCompletionRS> stopLaunch(BulkRQ<Long, FinishExecutionRQ> bulkRQ,
      MembershipDetails membershipDetails, ReportPortalUser user, String baseUrl) {
    return bulkRQ.getEntities().entrySet().stream()
        .map(entry -> stopLaunch(entry.getKey(), entry.getValue(), membershipDetails, user, baseUrl))
        .collect(toList());
  }
}
