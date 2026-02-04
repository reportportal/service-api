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

package com.epam.reportportal.base.reporting.event;

import com.epam.reportportal.base.core.launch.impl.StartLaunchHandlerImpl;
import com.epam.reportportal.base.core.launch.util.LinkGenerator;
import com.epam.reportportal.base.infrastructure.events.FinishItemRqEvent;
import com.epam.reportportal.base.infrastructure.events.FinishLaunchRqEvent;
import com.epam.reportportal.base.infrastructure.events.SaveLogRqEvent;
import com.epam.reportportal.base.infrastructure.events.StartChildItemRqEvent;
import com.epam.reportportal.base.infrastructure.events.StartLaunchRqEvent;
import com.epam.reportportal.base.infrastructure.events.StartRootItemRqEvent;
import com.epam.reportportal.base.infrastructure.persistence.commons.ReportPortalUser;
import com.epam.reportportal.base.reporting.async.producer.ItemFinishProducer;
import com.epam.reportportal.base.reporting.async.producer.ItemStartProducer;
import com.epam.reportportal.base.reporting.async.producer.LaunchFinishProducer;
import com.epam.reportportal.base.reporting.async.producer.LogProducer;
import com.epam.reportportal.base.util.ProjectExtractor;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventBasedReporting {

  private final StartLaunchHandlerImpl startLaunchHandler;

  private final LaunchFinishProducer finishLaunchHandler;

  private final ItemStartProducer startTestItemHandler;

  private final ItemFinishProducer finishTestItemHandler;

  private final LogProducer createLogHandler;

  private final ProjectExtractor projectExtractor;
  private final LinkGenerator linkGenerator;

  @EventListener
  public void handleStartLaunch(StartLaunchRqEvent startLaunchRqEvent) {
    var user = extractUserPrincipal();
    var projectDetails = projectExtractor.extractMembershipDetails(user,
        startLaunchRqEvent.getProjectName());
    startLaunchHandler.startLaunch(user, projectDetails, startLaunchRqEvent.getStartLaunchRq());
  }

  @EventListener
  public void handleFinishLaunch(FinishLaunchRqEvent finishLaunchRqEvent) {
    var user = extractUserPrincipal();
    var projectDetails = projectExtractor.extractMembershipDetails(user,
        finishLaunchRqEvent.getProjectName());
    finishLaunchHandler.finishLaunch(finishLaunchRqEvent.getLaunchUuid(),
        finishLaunchRqEvent.getFinishExecutionRq(), projectDetails, user,
        extractCurrentHttpRequest().map(linkGenerator::composeBaseUrl).orElse(""));
  }

  @EventListener
  public void handleStartRootItem(StartRootItemRqEvent startRootItemRqEvent) {
    var user = extractUserPrincipal();
    var projectDetails = projectExtractor.extractMembershipDetails(user,
        startRootItemRqEvent.getProjectName());
    startTestItemHandler.startRootItem(user, projectDetails,
        startRootItemRqEvent.getStartTestItemRq());
  }

  @EventListener
  public void handleStartItem(StartChildItemRqEvent startChildItemRqEvent) {
    var user = extractUserPrincipal();
    var projectDetails = projectExtractor.extractMembershipDetails(user,
        startChildItemRqEvent.getProjectName());
    startTestItemHandler.startChildItem(user, projectDetails,
        startChildItemRqEvent.getStartTestItemRq(), startChildItemRqEvent.getParentUuid());
  }

  @EventListener
  public void handleFinishRootItem(FinishItemRqEvent finishItemRqEvent) {
    var user = extractUserPrincipal();
    var projectDetails = projectExtractor.extractMembershipDetails(user,
        finishItemRqEvent.getProjectName());
    finishTestItemHandler.finishTestItem(user, projectDetails, finishItemRqEvent.getItemUuid(),
        finishItemRqEvent.getFinishTestItemRQ());
  }

  @EventListener
  public void handleLogCreation(SaveLogRqEvent saveLogRqEvent) {
    var user = extractUserPrincipal();
    var projectDetails = projectExtractor.extractMembershipDetails(user,
        saveLogRqEvent.getProjectName());
    createLogHandler.createLog(saveLogRqEvent.getSaveLogRq(), saveLogRqEvent.getFile(),
        projectDetails);
  }

  private Optional<HttpServletRequest> extractCurrentHttpRequest() {
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
      return Optional.of(servletRequestAttributes.getRequest());
    }
    log.debug("Not called in the context of an HTTP request");
    return Optional.empty();
  }

  private ReportPortalUser extractUserPrincipal() {
    return (ReportPortalUser) SecurityContextHolder.getContext()
        .getAuthentication().getPrincipal();
  }

}
