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

import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.job.JobUtil.buildProjectAttributesFilter;
import static com.epam.ta.reportportal.job.PageUtil.iterateOverPages;
import static java.time.Duration.ofSeconds;

/**
 * Finds jobs witn duration more than defined and finishes them with interrupted
 * {@link StatusEnum#INTERRUPTED} status
 *
 * @author Andrei Varabyeu
 */
@Service
public class InterruptBrokenLaunchesJob implements Job {

	private static final Logger LOGGER = LoggerFactory.getLogger(InterruptBrokenLaunchesJob.class);

	private final LaunchRepository launchRepository;

	private final TestItemRepository testItemRepository;

	private final LogRepository logRepository;

	private final ProjectRepository projectRepository;

	@Autowired
	public InterruptBrokenLaunchesJob(LaunchRepository launchRepository, TestItemRepository testItemRepository, LogRepository logRepository,
			ProjectRepository projectRepository) {
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.logRepository = logRepository;
		this.projectRepository = projectRepository;
	}

	@Override
	@Transactional
	public void execute(JobExecutionContext context) {
		LOGGER.info("Interrupt broken launches job has been started");
		iterateOverPages(pageable -> projectRepository.findAllIdsAndProjectAttributes(buildProjectAttributesFilter(ProjectAttributeEnum.INTERRUPT_JOB_TIME),
				pageable
		), projects -> projects.forEach(project -> {
			ProjectUtils.extractAttributeValue(project, ProjectAttributeEnum.INTERRUPT_JOB_TIME).ifPresent(it -> {
				Duration maxDuration = ofSeconds(NumberUtils.toLong(it, 0L));
				try (Stream<Long> ids = launchRepository.streamIdsWithStatusAndStartTimeBefore(project.getId(),
						StatusEnum.IN_PROGRESS,
						LocalDateTime.now(ZoneOffset.UTC).minus(maxDuration)
				)) {
					ids.forEach(launchId -> {
						if (!testItemRepository.hasItemsInStatusByLaunch(launchId, StatusEnum.IN_PROGRESS)) {
							/*
							 * There are no test items for this launch. Just INTERRUPT
							 * this launch
							 */
							interruptLaunch(launchId);
						} else {
							/*
							 * Well, there are some test items started for specified
							 * launch
							 */
							if (!testItemRepository.hasItemsInStatusAddedLately(launchId, maxDuration, StatusEnum.IN_PROGRESS)) {
								/*
								 * If there are logs, we have to check whether them
								 * expired
								 */
								if (testItemRepository.hasLogs(launchId, maxDuration, StatusEnum.IN_PROGRESS)) {
									/*
									 * If there are logs which are still valid
									 * (probably automation project keep writing
									 * something)
									 */
									if (!logRepository.hasLogsAddedLately(maxDuration, launchId, StatusEnum.IN_PROGRESS)) {
										interruptItems(launchId);
									}
								} else {
									/*
									 * If not just INTERRUPT all found items and launch
									 */
									interruptItems(launchId);
								}
							}
						}
					});
				} catch (Exception ex) {
					LOGGER.error("Interrupting broken launches has been failed", ex);
					//do nothing
				}
			});
		}));
	}

	private void interruptLaunch(Long launchId) {
		launchRepository.findById(launchId).ifPresent(launch -> {
			launch.setStatus(StatusEnum.INTERRUPTED);
			launch.setEndTime(LocalDateTime.now(ZoneOffset.UTC));
			launchRepository.save(launch);
		});
	}

	private void interruptItems(Long launchId) {
		testItemRepository.interruptInProgressItems(launchId);
		launchRepository.findById(launchId).ifPresent(l -> {
			l.setStatus(StatusEnum.INTERRUPTED);
			l.setEndTime(LocalDateTime.now(ZoneOffset.UTC));
			launchRepository.save(l);
		});

	}
}
