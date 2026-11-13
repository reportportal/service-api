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

package com.epam.reportportal.base.job;

import static com.epam.reportportal.base.infrastructure.persistence.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.reportportal.base.job.PageUtil.iterateOverPages;
import static java.time.Duration.ofSeconds;

import com.epam.reportportal.base.infrastructure.persistence.dao.LaunchRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.LaunchTypeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.ProjectAttributeEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.enums.StatusEnum;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.Project;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectUtils;
import com.epam.reportportal.base.job.service.BrokenLaunchInterruptService;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.apache.commons.lang3.math.NumberUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Finds jobs with duration more than defined and finishes them with interrupted
 * {@link StatusEnum#INTERRUPTED} status
 *
 * @author Andrei Varabyeu
 */
@Service
public class InterruptBrokenLaunchesJob implements Job {

  private static final Logger LOGGER = LoggerFactory.getLogger(InterruptBrokenLaunchesJob.class);

  @Autowired
  private LaunchRepository launchRepository;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private BrokenLaunchInterruptService brokenLaunchInterruptService;

  @Override
  public void execute(JobExecutionContext context) {
    LOGGER.info("Interrupt broken launches job has been started");
    iterateOverPages(
        Sort.by(Sort.Order.asc(CRITERIA_ID)),
        projectRepository::findAllIdsAndProjectAttributes,
        projects -> projects.forEach(project -> ProjectUtils.extractAttributeValue(project,
                ProjectAttributeEnum.INTERRUPT_JOB_TIME)
            .ifPresent(it -> {
              Duration maxDuration = ofSeconds(NumberUtils.toLong(it, 0L));
              processProjectLaunches(project, maxDuration);
            }))
    );
    LOGGER.info("Interrupt broken launches job has been finished");
  }

  private void processProjectLaunches(Project project, Duration maxDuration) {
    Instant before = Instant.now().minus(maxDuration.toSeconds(), ChronoUnit.SECONDS);
    List<Long> launchIds = launchRepository.findIdsWithStatusAndStartTimeBefore(
        project.getId(),
        StatusEnum.IN_PROGRESS,
        before,
        LaunchTypeEnum.MANUAL
    );
    for (Long launchId : launchIds) {
      try {
        brokenLaunchInterruptService.interruptIfStillBroken(launchId,
            project.getOrganizationId(), maxDuration);
      } catch (Exception ex) {
        LOGGER.error("Interrupting broken launch '{}' has failed", launchId, ex);
      }
    }
  }
}
