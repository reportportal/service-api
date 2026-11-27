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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.epam.reportportal.base.auth.userdetails.DefaultUserDetailsService;
import com.epam.reportportal.base.core.item.StartTestItemHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.reporting.StartTestItemRQ;
import com.epam.reportportal.base.reporting.async.config.MessageHeaders;
import com.epam.reportportal.base.reporting.async.message.MessageRetriever;
import com.epam.reportportal.base.util.ProjectExtractor;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

class TestItemStartMessageHandlerTest {

  private static final String ITEM_UUID = "item-uuid";

  private final MessageRetriever retriever = mock(MessageRetriever.class);
  private final StartTestItemHandler startTestItemHandler = mock(StartTestItemHandler.class);
  private final ProjectExtractor projectExtractor = mock(ProjectExtractor.class);
  private final DefaultUserDetailsService userDetailsService = mock(DefaultUserDetailsService.class);
  private final TestItemRepository testItemRepository = mock(TestItemRepository.class);

  private final TestItemStartMessageHandler handler = new TestItemStartMessageHandler(retriever,
      startTestItemHandler, projectExtractor, userDetailsService, testItemRepository);

  @Test
  void duplicateStartIsSkipped() {
    Message message = message();
    when(retriever.retrieveValid(message, StartTestItemRQ.class)).thenReturn(Optional.of(request()));
    when(testItemRepository.existsByUuid(ITEM_UUID)).thenReturn(true);

    handler.handleMessage(message);

    verifyNoInteractions(startTestItemHandler, userDetailsService, projectExtractor);
  }

  @Test
  void newChildItemIsStarted() {
    Message message = message();
    StartTestItemRQ rq = request();
    ReportPortalUser user = getRpUser("default", UserRole.USER, OrganizationRole.MEMBER,
        ProjectRole.EDITOR, 1L);
    MembershipDetails membershipDetails = MembershipDetails.builder().withProjectId(1L).build();
    when(retriever.retrieveValid(message, StartTestItemRQ.class)).thenReturn(Optional.of(rq));
    when(testItemRepository.existsByUuid(ITEM_UUID)).thenReturn(false);
    when(userDetailsService.loadUserByUsername("default")).thenReturn(user);
    when(projectExtractor.extractMembershipDetails(user, "project")).thenReturn(membershipDetails);

    handler.handleMessage(message);

    verify(startTestItemHandler).startChildItem(user, membershipDetails, rq, "parent-uuid");
    verify(startTestItemHandler, never()).startRootItem(any(), any(), any());
  }

  private static StartTestItemRQ request() {
    StartTestItemRQ rq = new StartTestItemRQ();
    rq.setUuid(ITEM_UUID);
    rq.setLaunchUuid("launch-uuid");
    return rq;
  }

  private static Message message() {
    MessageProperties properties = new MessageProperties();
    properties.setHeader(MessageHeaders.USERNAME, "default");
    properties.setHeader(MessageHeaders.PROJECT_KEY, "project");
    properties.setHeader(MessageHeaders.PARENT_ITEM_ID, "parent-uuid");
    return new Message("{}".getBytes(), properties);
  }
}
