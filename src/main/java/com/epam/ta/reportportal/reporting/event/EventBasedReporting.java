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

package com.epam.ta.reportportal.reporting.event;

import com.epam.reportportal.events.FinishItemRqEvent;
import com.epam.reportportal.events.FinishLaunchRqEvent;
import com.epam.reportportal.events.SaveLogRqEvent;
import com.epam.reportportal.events.StartChildItemRqEvent;
import com.epam.reportportal.events.StartLaunchRqEvent;
import com.epam.reportportal.events.StartRootItemRqEvent;
import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.item.FinishTestItemHandler;
import com.epam.ta.reportportal.core.item.StartTestItemHandler;
import com.epam.ta.reportportal.core.launch.FinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.StartLaunchHandler;
import com.epam.ta.reportportal.core.launch.util.LinkGenerator;
import com.epam.ta.reportportal.core.log.CreateLogHandler;
import com.epam.ta.reportportal.util.ProjectExtractor;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
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

  private final ExecutorService executorService = Executors.newSingleThreadExecutor();

  private final StartLaunchHandler startLaunchHandler;

  private final FinishLaunchHandler finishLaunchHandler;

  private final StartTestItemHandler startTestItemHandler;

  private final FinishTestItemHandler finishTestItemHandler;

  private final CreateLogHandler createLogHandler;

  private final ProjectExtractor projectExtractor;

  @EventListener
  public void handleStartLaunch(StartLaunchRqEvent startLaunchRqEvent) {
    var user = extractUserPrincipal();
    var projectDetails = projectExtractor.extractProjectDetails(user,
        startLaunchRqEvent.getProjectName());
    executorService.submit(() -> {
      startLaunchHandler.startLaunch(user, projectDetails, startLaunchRqEvent.getStartLaunchRQ());
    });
  }

  @EventListener
  public void handleFinishLaunch(FinishLaunchRqEvent finishLaunchRqEvent) {
    var user = extractUserPrincipal();
    var projectDetails = projectExtractor.extractProjectDetails(user,
        finishLaunchRqEvent.getProjectName());
    executorService.submit(() -> {
      finishLaunchHandler.finishLaunch(finishLaunchRqEvent.getLaunchUuid(),
          finishLaunchRqEvent.getFinishExecutionRQ(), projectDetails, user,
          extractCurrentHttpRequest().map(LinkGenerator::composeBaseUrl).orElse(""));
    });
  }

  @EventListener
  public void handleStartRootItem(StartRootItemRqEvent startRootItemRqEvent) {
    var user = extractUserPrincipal();
    var projectDetails = projectExtractor.extractProjectDetails(user,
        startRootItemRqEvent.getProjectName());
    executorService.submit(() -> {
      startTestItemHandler.startRootItem(user, projectDetails,
          startRootItemRqEvent.getStartTestItemRQ());
    });
  }

  @EventListener
  public void handleStartItem(StartChildItemRqEvent startChildItemRqEvent) {
    var user = extractUserPrincipal();
    var projectDetails = projectExtractor.extractProjectDetails(user,
        startChildItemRqEvent.getProjectName());
    executorService.submit(() -> {
      startTestItemHandler.startChildItem(user, projectDetails,
          startChildItemRqEvent.getStartTestItemRQ(), startChildItemRqEvent.getParentUuid());
    });
  }

  @EventListener
  public void handleFinishRootItem(FinishItemRqEvent finishItemRqEvent) {
    var user = extractUserPrincipal();
    var projectDetails = projectExtractor.extractProjectDetails(user,
        finishItemRqEvent.getProjectName());
    executorService.submit(() -> {
      finishTestItemHandler.finishTestItem(user, projectDetails, finishItemRqEvent.getItemUuid(),
          finishItemRqEvent.getFinishTestItemRQ());
    });
  }

  @EventListener
  public void handleLogCreation(SaveLogRqEvent saveLogRqEvent) {
    var user = extractUserPrincipal();
    var projectDetails = projectExtractor.extractProjectDetails(user,
        saveLogRqEvent.getProjectName());
    executorService.submit(() -> {
      createLogHandler.createLog(saveLogRqEvent.getSaveLogRQ(), saveLogRqEvent.getFile(),
          projectDetails);
    });
  }

  private Optional<HttpServletRequest> extractCurrentHttpRequest() {
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    if (requestAttributes instanceof ServletRequestAttributes) {
      return Optional.of(((ServletRequestAttributes) requestAttributes).getRequest());
    }
    log.debug("Not called in the context of an HTTP request");
    return Optional.empty();
  }

  private ReportPortalUser extractUserPrincipal() {
    return (ReportPortalUser) SecurityContextHolder.getContext()
        .getAuthentication().getPrincipal();
  }

  @PreDestroy
  public void shutdownExecutorService() {
    executorService.shutdown();
    try {
      if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
        executorService.shutdownNow();
      }
    } catch (InterruptedException ex) {
      executorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
