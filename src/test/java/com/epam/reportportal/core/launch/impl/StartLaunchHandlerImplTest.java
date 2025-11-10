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

package com.epam.reportportal.core.launch.impl;

import static com.epam.reportportal.ReportPortalUserUtil.getRpUser;
import static com.epam.reportportal.infrastructure.persistence.util.MembershipUtils.rpUserToMembership;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.epam.reportportal.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.core.events.MessageBus;
import com.epam.reportportal.core.launch.attribute.LaunchAttributeHandlerService;
import com.epam.reportportal.core.launch.rerun.RerunHandler;
import com.epam.reportportal.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.infrastructure.persistence.dao.UserRepository;
import com.epam.reportportal.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.ws.converter.builders.LaunchBuilder;
import com.epam.reportportal.reporting.Mode;
import com.epam.reportportal.reporting.StartLaunchRQ;
import com.epam.reportportal.reporting.StartLaunchRS;
import java.time.Instant;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@ExtendWith(MockitoExtension.class)
class StartLaunchHandlerImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private LaunchRepository launchRepository;

  @Mock
  private MessageBus messageBus;

  @Mock
  private RerunHandler rerunHandler;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Mock
  private LaunchAttributeHandlerService launchAttributeHandlerService;

  @Mock
  private LaunchBuilder launchBuilder;

  @InjectMocks
  private StartLaunchHandlerImpl startLaunchHandlerImpl;

  @Test
  void startLaunch() {
    final ReportPortalUser rpUser = getRpUser("test", UserRole.ADMINISTRATOR, OrganizationRole.MEMBER,
        ProjectRole.EDITOR, 1L);

    StartLaunchRQ startLaunchRQ = new StartLaunchRQ();
    startLaunchRQ.setStartTime(Instant.now());
    startLaunchRQ.setName("test");

    Launch launch = new Launch();
    launch.setId(1L);

    doAnswer(invocation -> {
      Launch l = invocation.getArgument(0);
      l.setId(1L);
      return l;
    }).when(launchRepository).save(any(Launch.class));

    final StartLaunchRS startLaunchRS =
        startLaunchHandlerImpl.startLaunch(rpUser, rpUserToMembership(rpUser),
            startLaunchRQ
        );

    verify(launchRepository, times(1)).refresh(any(Launch.class));
    verify(eventPublisher, times(1)).publishEvent(any());
    assertNotNull(startLaunchRS);
  }

  @Test
  @Disabled("waiting for requirements")
  void startLaunchForCustomerRoleAndDebugMode() {
    // given
    final ReportPortalUser rpUser = getRpUser("test", UserRole.USER, OrganizationRole.MEMBER, ProjectRole.VIEWER, 1L);

    StartLaunchRQ startLaunchRQ = new StartLaunchRQ();
    startLaunchRQ.setName("name");
    startLaunchRQ.setStartTime(Instant.now());
    startLaunchRQ.setUuid("some-uuid");
    startLaunchRQ.setMode(Mode.DEBUG);

    Launch mockLaunch = new Launch();
    mockLaunch.setId(1L);
    mockLaunch.setUuid("some-uuid");

    final ReportPortalException exception = assertThrows(ReportPortalException.class,
        () -> startLaunchHandlerImpl.startLaunch(rpUser, rpUserToMembership(rpUser), startLaunchRQ));
    assertEquals("Forbidden operation.", exception.getMessage());
  }
}
