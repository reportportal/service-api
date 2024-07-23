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
import com.epam.ta.reportportal.core.launch.FinishLaunchHandler;
import com.epam.ta.reportportal.reporting.async.config.MessageHeaders;
import com.epam.ta.reportportal.reporting.async.message.MessageRetriever;
import com.epam.ta.reportportal.util.ProjectExtractor;
import com.epam.ta.reportportal.ws.reporting.FinishExecutionRQ;
import java.util.Map;
import java.util.Optional;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class LaunchFinishMessageHandler implements ReportingMessageHandler {

  private final MessageRetriever retriever;
  private final FinishLaunchHandler finishLaunchHandler;
  private final ProjectExtractor projectExtractor;
  private final DatabaseUserDetailsService userDetailsService;

  public LaunchFinishMessageHandler(MessageRetriever retriever,
      FinishLaunchHandler finishLaunchHandler,
      ProjectExtractor projectExtractor, DatabaseUserDetailsService userDetailsService) {
    this.retriever = retriever;
    this.finishLaunchHandler = finishLaunchHandler;
    this.projectExtractor = projectExtractor;
    this.userDetailsService = userDetailsService;
  }

  @Override
  public void handleMessage(Message message) {
    Optional<FinishExecutionRQ> finishExecutionRQ = retriever.retrieveValid(message,
        FinishExecutionRQ.class);
    Map<String, Object> headers = message.getMessageProperties().getHeaders();

    finishExecutionRQ.ifPresent(rq -> {

      String username = (String) headers.get(MessageHeaders.USERNAME);
      String projectName = (String) headers.get(MessageHeaders.PROJECT_KEY);
      String launchId = (String) headers.get(MessageHeaders.LAUNCH_ID);
      String baseUrl = (String) headers.get(MessageHeaders.BASE_URL);

      ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(username);
      finishLaunchHandler.finishLaunch(launchId, rq,
          projectExtractor.extractMembershipDetails(user, projectName), user, baseUrl);

    });
  }
}
