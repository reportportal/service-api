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

package com.epam.reportportal.base.reporting.async.handler;

import com.epam.reportportal.base.auth.userdetails.DefaultUserDetailsService;
import com.epam.reportportal.base.core.item.StartTestItemHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.MembershipDetails;
import com.epam.reportportal.base.reporting.StartTestItemRQ;
import com.epam.reportportal.base.reporting.async.config.MessageHeaders;
import com.epam.reportportal.base.reporting.async.message.MessageRetriever;
import com.epam.reportportal.base.util.ProjectExtractor;
import com.google.common.base.Strings;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;

/**
 * Handles RabbitMQ messages for starting a test item.
 * <p>
 * A message is delivered at least once, so a START_TEST for an item uuid that already exists is a redelivery and is
 * acknowledged without starting the item again (which would fail on the unique uuid or, on a rerun, reset a finished
 * item to IN_PROGRESS).
 *
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Slf4j
@Service
public class TestItemStartMessageHandler implements ReportingMessageHandler {

  private final MessageRetriever retriever;
  private final StartTestItemHandler startTestItemHandler;
  private final ProjectExtractor projectExtractor;
  private final DefaultUserDetailsService userDetailsService;
  private final TestItemRepository testItemRepository;

  public TestItemStartMessageHandler(MessageRetriever retriever,
      StartTestItemHandler startTestItemHandler, ProjectExtractor projectExtractor,
      DefaultUserDetailsService userDetailsService, TestItemRepository testItemRepository) {
    this.retriever = retriever;
    this.startTestItemHandler = startTestItemHandler;
    this.projectExtractor = projectExtractor;
    this.userDetailsService = userDetailsService;
    this.testItemRepository = testItemRepository;
  }

  @Override
  public void handleMessage(Message message) {
    Optional<StartTestItemRQ> startTestItemRQ = retriever.retrieveValid(message,
        StartTestItemRQ.class);
    Map<String, Object> headers = message.getMessageProperties().getHeaders();

    startTestItemRQ.ifPresent(rq -> {
      if (rq.getUuid() != null && testItemRepository.existsByUuid(rq.getUuid())) {
        log.info("Test item with uuid '{}' of launch '{}' already exists, duplicate START_TEST message is skipped",
            rq.getUuid(), rq.getLaunchUuid());
        return;
      }
      String username = (String) headers.get(MessageHeaders.USERNAME);
      String projectKey = (String) headers.get(MessageHeaders.PROJECT_KEY);
      String parentId = (String) headers.get(MessageHeaders.PARENT_ITEM_ID);

      ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
      MembershipDetails projectDetails = projectExtractor.extractMembershipDetails(user,
          projectKey);

      if (!Strings.isNullOrEmpty(parentId)) {
        startTestItemHandler.startChildItem(user, projectDetails, rq, parentId);
      } else {
        startTestItemHandler.startRootItem(user, projectDetails, rq);
      }
    });
  }

}
