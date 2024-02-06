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

package com.epam.ta.reportportal.core.imprt.impl;

import static java.util.Optional.ofNullable;

import com.epam.ta.reportportal.commons.ReportPortalUser;
import com.epam.ta.reportportal.core.launch.FinishLaunchHandler;
import com.epam.ta.reportportal.core.launch.StartLaunchHandler;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.model.launch.LaunchImportRQ;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.FinishExecutionRQ;
import com.epam.ta.reportportal.ws.model.attribute.ItemAttributesRQ;
import com.epam.ta.reportportal.ws.model.launch.Mode;
import com.epam.ta.reportportal.ws.model.launch.StartLaunchRQ;
import com.google.common.collect.Sets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Component
public abstract class AbstractImportStrategy implements ImportStrategy {

  public static final String SKIPPED_IS_NOT_ISSUE = "skippedIsNotIssue";
  protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractImportStrategy.class);
  private static final LocalDateTime initialStartTime = new Date(0)
      .toInstant()
      .atZone(ZoneId.systemDefault())
      .toLocalDateTime();
  protected static final ExecutorService service = Executors.newFixedThreadPool(5);

  private StartLaunchHandler startLaunchHandler;

  private FinishLaunchHandler finishLaunchHandler;

  private LaunchRepository launchRepository;

  @Autowired
  public void setStartLaunchHandler(StartLaunchHandler startLaunchHandler) {
    this.startLaunchHandler = startLaunchHandler;
  }

  @Autowired
  public void setFinishLaunchHandler(FinishLaunchHandler finishLaunchHandler) {
    this.finishLaunchHandler = finishLaunchHandler;
  }

  @Autowired
  public void setLaunchRepository(LaunchRepository launchRepository) {
    this.launchRepository = launchRepository;
  }

  protected ParseResults processResults(CompletableFuture... futures) {
    ParseResults results = new ParseResults();
    Arrays.stream(futures).map(it -> (ParseResults) it.join()).forEach(res -> {
      results.checkAndSetStartLaunchTime(res.getStartTime());
      results.increaseDuration(res.getDuration());
    });
    return results;
  }

  protected String startLaunch(ReportPortalUser.ProjectDetails projectDetails,
      ReportPortalUser user, String launchName, LaunchImportRQ rq) {
    StartLaunchRQ startLaunchRQ = new StartLaunchRQ();
    startLaunchRQ.setStartTime(ofNullable(rq.getStartTime()).orElse(initialStartTime));
    startLaunchRQ.setName(ofNullable(rq.getName()).orElse(launchName));
    ofNullable(rq.getDescription()).ifPresent(startLaunchRQ::setDescription);
    startLaunchRQ.setMode(ofNullable(rq.getMode()).orElse(Mode.DEFAULT));
    startLaunchRQ.setAttributes(ofNullable(rq.getAttributes()).orElse(Sets.newHashSet()));
    return startLaunchHandler.startLaunch(user, projectDetails, startLaunchRQ).getId();
  }

  protected void finishLaunch(String launchId, ReportPortalUser.ProjectDetails projectDetails,
      ReportPortalUser user, ParseResults results, String baseUrl) {
    FinishExecutionRQ finishExecutionRQ = new FinishExecutionRQ();
    finishExecutionRQ.setEndTime(results.getEndTime());
    finishLaunchHandler.finishLaunch(launchId, finishExecutionRQ, projectDetails, user, baseUrl);
    Launch launch = launchRepository.findByUuid(launchId)
        .orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND, launchId));
    launch.setStartTime(results.getStartTime());
    launchRepository.save(launch);
  }

  protected Boolean isSkippedNotIssue(Set<ItemAttributesRQ> attributes) {
    return ofNullable(attributes).orElse(Collections.emptySet()).stream().filter(
            attribute -> SKIPPED_IS_NOT_ISSUE.equals(attribute.getKey()) && attribute.isSystem())
        .findAny().filter(itemAttributesRQ -> Boolean.parseBoolean(itemAttributesRQ.getValue()))
        .isPresent();
  }

  /**
   * Got a cause exception message if it has any.
   *
   * @param e Exception
   * @return Clean exception message
   */
  protected String cleanMessage(Exception e) {
    if (e.getCause() != null) {
      return e.getCause().getMessage();
    }
    return e.getMessage();
  }

  /*
   * if the importing results do not contain initial timestamp a launch gets
   * a default date if the launch is broken, time should be updated to not to broke
   * the statistics
   */
  protected void updateBrokenLaunch(String savedLaunchId) {
    if (savedLaunchId != null) {
      Launch launch = launchRepository.findByUuid(savedLaunchId)
          .orElseThrow(() -> new ReportPortalException(ErrorType.LAUNCH_NOT_FOUND));
      launch.setStartTime(LocalDateTime.now());
      launch.setStatus(StatusEnum.INTERRUPTED);
      launchRepository.save(launch);
    }
  }
}
