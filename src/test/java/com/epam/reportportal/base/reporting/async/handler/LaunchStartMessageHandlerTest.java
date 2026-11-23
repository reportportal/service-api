/*
 * Copyright 2026 EPAM Systems
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

package com.epam.reportportal.base.reporting.async.handler;

import static com.epam.reportportal.base.ReportPortalUserUtil.getRpUser;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.auth.userdetails.DefaultUserDetailsService;
import com.epam.reportportal.base.core.launch.StartLaunchHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.launch.Launch;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.reporting.StartLaunchRQ;
import com.epam.reportportal.base.reporting.async.config.MessageHeaders;
import com.epam.reportportal.base.reporting.async.message.MessageRetriever;
import com.epam.reportportal.base.util.ProjectExtractor;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

class LaunchStartMessageHandlerTest {

  private static final String LAUNCH_UUID = "launch-uuid";

  private final MessageRetriever retriever = mock(MessageRetriever.class);
  private final StartLaunchHandler startLaunchHandler = mock(StartLaunchHandler.class);
  private final ProjectExtractor projectExtractor = mock(ProjectExtractor.class);
  private final DefaultUserDetailsService userDetailsService = mock(DefaultUserDetailsService.class);
  private final LaunchRepository launchRepository = mock(LaunchRepository.class);

  private final LaunchStartMessageHandler handler = new LaunchStartMessageHandler(retriever,
      startLaunchHandler, projectExtractor, userDetailsService, launchRepository);

  @Test
  void duplicateStartIsSkipped() {
    Message message = message();
    when(retriever.retrieveValid(message, StartLaunchRQ.class)).thenReturn(Optional.of(request(false)));
    when(launchRepository.findByUuid(LAUNCH_UUID)).thenReturn(Optional.of(new Launch()));

    handler.handleMessage(message);

    verifyNoInteractions(startLaunchHandler, userDetailsService, projectExtractor);
  }

  @Test
  void newLaunchIsStarted() {
    Message message = message();
    StartLaunchRQ rq = request(false);
    when(retriever.retrieveValid(message, StartLaunchRQ.class)).thenReturn(Optional.of(rq));
    when(launchRepository.findByUuid(LAUNCH_UUID)).thenReturn(Optional.empty());
    ReportPortalUser user = mockUser();
    MembershipDetails membershipDetails = MembershipDetails.builder().withProjectId(1L).build();
    when(projectExtractor.extractMembershipDetails(user, "project")).thenReturn(membershipDetails);

    handler.handleMessage(message);

    verify(startLaunchHandler).startLaunch(user, membershipDetails, rq);
  }

  @Test
  void rerunOfAnExistingLaunchIsStarted() {
    Message message = message();
    StartLaunchRQ rq = request(true);
    when(retriever.retrieveValid(message, StartLaunchRQ.class)).thenReturn(Optional.of(rq));
    ReportPortalUser user = mockUser();
    MembershipDetails membershipDetails = MembershipDetails.builder().withProjectId(1L).build();
    when(projectExtractor.extractMembershipDetails(user, "project")).thenReturn(membershipDetails);

    handler.handleMessage(message);

    verify(startLaunchHandler).startLaunch(user, membershipDetails, rq);
    verifyNoInteractions(launchRepository);
  }

  private ReportPortalUser mockUser() {
    ReportPortalUser user = getRpUser("default", UserRole.USER, OrganizationRole.MEMBER,
        ProjectRole.EDITOR, 1L);
    when(userDetailsService.loadUserByUsername("default")).thenReturn(user);
    return user;
  }

  private static StartLaunchRQ request(boolean rerun) {
    StartLaunchRQ rq = new StartLaunchRQ();
    rq.setUuid(LAUNCH_UUID);
    rq.setRerun(rerun);
    return rq;
  }

  private static Message message() {
    MessageProperties properties = new MessageProperties();
    properties.setHeader(MessageHeaders.USERNAME, "default");
    properties.setHeader(MessageHeaders.PROJECT_KEY, "project");
    return new Message("{}".getBytes(), properties);
  }
}
