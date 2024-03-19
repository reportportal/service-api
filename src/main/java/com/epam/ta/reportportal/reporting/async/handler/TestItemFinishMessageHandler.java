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
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.reporting.async.config.MessageHeaders;
import com.epam.ta.reportportal.reporting.async.message.MessageRetriever;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.reporting.FinishTestItemRQ;
import java.util.Map;
import java.util.Optional;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class TestItemFinishMessageHandler implements ReportingMessageHandler {

  private final MessageRetriever retriever;
  private final FinishTestItemHandler finishTestItemHandler;
  private final ProjectExtractor projectExtractor;
  private final DatabaseUserDetailsService userDetailsService;

  public TestItemFinishMessageHandler(MessageRetriever retriever,
      FinishTestItemHandler finishTestItemHandler, ProjectExtractor projectExtractor,
      DatabaseUserDetailsService userDetailsService) {
    this.retriever = retriever;
    this.finishTestItemHandler = finishTestItemHandler;
    this.projectExtractor = projectExtractor;
    this.userDetailsService = userDetailsService;
  }

  @Override
  public void handleMessage(Message message) {
    Optional<FinishTestItemRQ> finishTestItemRQ = retriever.retrieveValid(message,
        FinishTestItemRQ.class);
    Map<String, Object> headers = message.getMessageProperties().getHeaders();

    finishTestItemRQ.ifPresent(rq -> {
      String username = (String) headers.get(MessageHeaders.USERNAME);
      String projectName = (String) headers.get(MessageHeaders.PROJECT_NAME);
      String itemId = (String) headers.get(MessageHeaders.ITEM_ID);
      ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(username);

      finishTestItemHandler.finishTestItem(user,
          projectExtractor.extractProjectDetails(user, projectName), itemId, rq);
    });

  }
}
