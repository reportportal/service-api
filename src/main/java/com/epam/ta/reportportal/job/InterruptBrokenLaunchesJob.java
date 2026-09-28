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

package com.epam.ta.reportportal.job;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.ta.reportportal.job.PageUtil.iterateOverPages;
import static java.time.Duration.ofSeconds;

import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.job.service.BrokenLaunchInterruptService;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.math.NumberUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * Finds jobs witn duration more than defined and finishes them with interrupted
 * {@link StatusEnum#INTERRUPTED} status
 *
 * @author Andrei Varabyeu
 */
@Service
@RequiredArgsConstructor
public class InterruptBrokenLaunchesJob implements Job {

  private static final Logger LOGGER = LoggerFactory.getLogger(InterruptBrokenLaunchesJob.class);

  private final LaunchRepository launchRepository;

  private final ProjectRepository projectRepository;

  private final BrokenLaunchInterruptService brokenLaunchInterruptService;

  @Override
  public void execute(JobExecutionContext context) {
    LOGGER.info("Interrupt broken launches job has been started");
    iterateOverPages(
        Sort.by(Sort.Order.asc(CRITERIA_ID)),
        projectRepository::findAllIdsAndProjectAttributes,
        projects -> projects.forEach(project -> {
          ProjectUtils.extractAttributeValue(project, ProjectAttributeEnum.INTERRUPT_JOB_TIME)
              .ifPresent(it -> {
                Duration maxDuration = ofSeconds(NumberUtils.toLong(it, 0L));
                List<Long> launchIds = launchRepository.findIdsWithStatusAndStartTimeBefore(
                    project.getId(),
                    StatusEnum.IN_PROGRESS,
                    Instant.now().minus(maxDuration.toSeconds(), ChronoUnit.SECONDS)
                );
                launchIds.forEach(launchId -> {
                  try {
                    brokenLaunchInterruptService.interruptIfStillBroken(launchId, maxDuration);
                  } catch (Exception ex) {
                    LOGGER.error("Interrupting broken launch '{}' has failed", launchId, ex);
                  }
                });
              });
        })
    );
    LOGGER.info("Interrupt broken launches job has been finished");
  }
}
