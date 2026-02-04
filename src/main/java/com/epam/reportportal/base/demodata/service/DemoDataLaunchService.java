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

package com.epam.reportportal.base.demodata.service;

import static com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum.PASSED;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.LAUNCH_NOT_FOUND;

import com.epam.reportportal.base.core.events.domain.LaunchFinishedEvent;
import com.epam.reportportal.base.core.launch.attribute.LaunchAttributeHandlerService;
import com.epam.reportportal.base.core.project.ProjectService;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.User;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.base.reporting.ItemAttributesRQ;
import com.epam.reportportal.base.reporting.Mode;
import com.epam.reportportal.base.reporting.StartLaunchRQ;
import com.epam.reportportal.base.ws.converter.builders.LaunchBuilder;
import com.google.common.collect.Sets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Service
public class DemoDataLaunchService {

  private final String[] platformValues =
      {"linux", "windows", "macos", "ios", "android", "windows mobile", "ubuntu", "mint", "arch",
          "windows 10", "windows 7", "windows server", "debian", "alpine"};

  private final LaunchRepository launchRepository;
  private final ProjectService projectService;
  private final TestItemRepository testItemRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final LaunchAttributeHandlerService launchAttributeHandlerService;

  @Autowired
  public DemoDataLaunchService(LaunchRepository launchRepository, ProjectService projectService,
      TestItemRepository testItemRepository, ApplicationEventPublisher eventPublisher,
      LaunchAttributeHandlerService launchAttributeHandlerService) {
    this.launchRepository = launchRepository;
    this.projectService = projectService;
    this.testItemRepository = testItemRepository;
    this.eventPublisher = eventPublisher;
    this.launchAttributeHandlerService = launchAttributeHandlerService;
  }

  @Transactional
  public Launch startLaunch(String name, User user,
      MembershipDetails membershipDetails) {
    StartLaunchRQ rq = new StartLaunchRQ();
    rq.setMode(Mode.DEFAULT);
    rq.setDescription(ContentUtils.getLaunchDescription());
    LocalDateTime now = LocalDateTime.now();
    rq.setName(name);
    rq.setStartTime(Instant.now());
    rq.setUuid(UUID.randomUUID().toString());
    Set<ItemAttributesRQ> attributes = Sets.newHashSet(new ItemAttributesRQ("platform",
        platformValues[new Random().nextInt(platformValues.length)]
    ), new ItemAttributesRQ(null, "demo"), new ItemAttributesRQ("build",
        "3." + now.getDayOfMonth() + "." + now.getHour() + "." + now.getMinute() + "."
            + now.getSecond()
    ));
    Launch launch = new LaunchBuilder().addStartRQ(rq).addAttributes(attributes)
        .addProject(membershipDetails.getProjectId()).get();
    launchAttributeHandlerService.handleLaunchStart(launch);
    launch.setUserId(user.getId());
    launchRepository.save(launch);
    launchRepository.refresh(launch);
    return launch;
  }

  @Transactional
  public void finishLaunch(String launchId) {
    Launch launch = launchRepository.findByUuid(launchId)
        .orElseThrow(() -> new ReportPortalException(LAUNCH_NOT_FOUND, launchId));
    if (testItemRepository.hasItemsInStatusByLaunch(launch.getId(), StatusEnum.IN_PROGRESS)) {
      testItemRepository.interruptInProgressItems(launch.getId());
    }

    launch = new LaunchBuilder(launch).addEndTime(Instant.now()).get();

    StatusEnum fromStatisticsStatus = PASSED;
    if (launchRepository.hasRootItemsWithStatusNotEqual(launch.getId(), StatusEnum.PASSED.name(),
        StatusEnum.INFO.name(), StatusEnum.WARN.name()
    )) {
      fromStatisticsStatus = StatusEnum.FAILED;
    }
    launch.setStatus(fromStatisticsStatus);

    launchRepository.save(launch);

    Project project = projectService.findProjectById(launch.getProjectId());
    eventPublisher.publishEvent(new LaunchFinishedEvent(launch, project.getOrganizationId()));
  }
}
