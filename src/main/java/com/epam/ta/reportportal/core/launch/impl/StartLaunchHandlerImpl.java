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

import static com.epam.ta.reportportal.ws.converter.converters.LaunchConverter.TO_ACTIVITY_RESOURCE;

import com.epam.reportportal.extension.event.StartLaunchEvent;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.LaunchStartedEvent;
import com.epam.ta.reportportal.core.launch.StartLaunchHandler;
import com.epam.ta.reportportal.core.launch.attribute.LaunchAttributeHandlerService;
import com.epam.ta.reportportal.core.launch.rerun.RerunHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.organization.MembershipDetails;
import com.epam.ta.reportportal.ws.converter.builders.LaunchBuilder;
import com.epam.ta.reportportal.ws.reporting.StartLaunchRQ;
import com.epam.ta.reportportal.ws.reporting.StartLaunchRS;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link com.epam.ta.reportportal.core.launch.StartLaunchHandler}
 *
 * @author Andrei Varabyeu
 */
@Service
@Primary
@Transactional
public class StartLaunchHandlerImpl implements StartLaunchHandler {

  private final LaunchRepository launchRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final MessageBus messageBus;
  private final RerunHandler rerunHandler;
  private final LaunchAttributeHandlerService launchAttributeHandlerService;

  @Autowired
  public StartLaunchHandlerImpl(LaunchRepository launchRepository,
      ApplicationEventPublisher eventPublisher, MessageBus messageBus, RerunHandler rerunHandler,
      LaunchAttributeHandlerService launchAttributeHandlerService) {
    this.launchRepository = launchRepository;
    this.eventPublisher = eventPublisher;
    this.messageBus = messageBus;
    this.rerunHandler = rerunHandler;
    this.launchAttributeHandlerService = launchAttributeHandlerService;
  }

  @Override
  @Transactional
  public StartLaunchRS startLaunch(ReportPortalUser user,
      MembershipDetails membershipDetails, StartLaunchRQ request) {
    validateRoles(membershipDetails, request);

    final Launch savedLaunch = Optional.of(request.isRerun()).filter(Boolean::booleanValue)
        .map(rerun -> rerunHandler.handleLaunch(request, membershipDetails.getProjectId(), user))
        .orElseGet(() -> {
          Launch launch =
              new LaunchBuilder().addStartRQ(request).addAttributes(request.getAttributes())
                  .addProject(membershipDetails.getProjectId()).addUserId(user.getUserId()).get();
          launchAttributeHandlerService.handleLaunchStart(launch);
          launchRepository.save(launch);
          launchRepository.refresh(launch);
          return launch;
        });

    eventPublisher.publishEvent(new StartLaunchEvent(savedLaunch.getId()));
    messageBus.publishActivity(
        new LaunchStartedEvent(TO_ACTIVITY_RESOURCE.apply(savedLaunch), user.getUserId(),
            user.getUsername(), membershipDetails.getOrgId()
        ));

    StartLaunchRS response = new StartLaunchRS();
    response.setId(savedLaunch.getUuid());
    response.setNumber(savedLaunch.getNumber());
    return response;
  }
}
