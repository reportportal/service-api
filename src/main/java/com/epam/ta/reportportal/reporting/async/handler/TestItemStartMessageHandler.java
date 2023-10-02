/*
 * Copyright 2023 EPAM Systems
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

package com.epam.ta.reportportal.reporting.async.handler;

import com.epam.ta.reportportal.auth.basic.DatabaseUserDetailsService;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.reporting.async.config.MessageHeaders;
import com.epam.ta.reportportal.reporting.async.message.MessageRetriever;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.model.StartTestItemRQ;
import com.google.common.base.Strings;
import java.util.Map;
import java.util.Optional;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class TestItemStartMessageHandler implements ReportingMessageHandler {

  private final MessageRetriever retriever;
  private final StartTestItemHandler startTestItemHandler;
  private final ProjectExtractor projectExtractor;
  private final DatabaseUserDetailsService userDetailsService;

  public TestItemStartMessageHandler(MessageRetriever retriever,
      StartTestItemHandler startTestItemHandler, ProjectExtractor projectExtractor,
      DatabaseUserDetailsService userDetailsService) {
    this.retriever = retriever;
    this.startTestItemHandler = startTestItemHandler;
    this.projectExtractor = projectExtractor;
    this.userDetailsService = userDetailsService;
  }

  @Override
  public void handleMessage(Message message) {
    Optional<StartTestItemRQ> startTestItemRQ = retriever.retrieveValid(message,
        StartTestItemRQ.class);
    Map<String, Object> headers = message.getMessageProperties().getHeaders();

    startTestItemRQ.ifPresent(rq -> {
      String username = (String) headers.get(MessageHeaders.USERNAME);
      String projectName = (String) headers.get(MessageHeaders.PROJECT_NAME);
      String parentId = (String) headers.get(MessageHeaders.PARENT_ITEM_ID);

      ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
      ReportPortalUser.ProjectDetails projectDetails = projectExtractor.extractProjectDetails(user,
          projectName);

      if (!Strings.isNullOrEmpty(parentId)) {
        startTestItemHandler.startChildItem(user, projectDetails, rq, parentId);
      } else {
        startTestItemHandler.startRootItem(user, projectDetails, rq);
      }
    });
  }

}
