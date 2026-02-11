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
import com.epam.reportportal.base.core.launch.StartLaunchHandler;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.reporting.StartLaunchRQ;
import com.epam.reportportal.base.reporting.async.config.MessageHeaders;
import com.epam.reportportal.base.reporting.async.message.MessageRetriever;
import com.epam.reportportal.base.util.ProjectExtractor;
import java.util.Map;
import java.util.Optional;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Service
public class LaunchStartMessageHandler implements
    ReportingMessageHandler {

  private final MessageRetriever retriever;
  private final StartLaunchHandler startLaunchHandler;
  private final ProjectExtractor projectExtractor;
  private final DefaultUserDetailsService userDetailsService;

  public LaunchStartMessageHandler(MessageRetriever retriever,
      StartLaunchHandler startLaunchHandler,
      ProjectExtractor projectExtractor, DefaultUserDetailsService userDetailsService) {
    this.retriever = retriever;
    this.startLaunchHandler = startLaunchHandler;
    this.projectExtractor = projectExtractor;
    this.userDetailsService = userDetailsService;
  }

  @Override
  public void handleMessage(Message message) {
    Optional<StartLaunchRQ> startLaunchRQ = retriever.retrieveValid(message, StartLaunchRQ.class);
    Map<String, Object> headers = message.getMessageProperties().getHeaders();

    startLaunchRQ.ifPresent(rq -> {
      String projectName = (String) headers
          .get(MessageHeaders.PROJECT_KEY);
      String username = (String) headers
          .get(MessageHeaders.USERNAME);
      ReportPortalUser user = (ReportPortalUser) userDetailsService.loadUserByUsername(
          username);

      startLaunchHandler.startLaunch(user,
          projectExtractor.extractMembershipDetails(user, projectName), rq);
    });
  }
}
