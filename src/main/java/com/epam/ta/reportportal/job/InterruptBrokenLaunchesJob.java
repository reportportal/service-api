/*
 * Copyright 2018 EPAM Systems
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

import com.epam.ta.reportportal.commons.querygen.Condition;
import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.InterruptionJobDelay;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.commons.EntityUtils.TO_LOCAL_DATE_TIME;
import static com.epam.ta.reportportal.commons.querygen.constant.ProjectCriteriaConstant.CRITERIA_PROJECT_ATTRIBUTE_NAME;
import static com.epam.ta.reportportal.job.PageUtil.iterateOverPages;
import static java.time.Duration.ofHours;

/**
 * Finds jobs witn duration more than defined and finishes them with interrupted
 * {@link StatusEnum#INTERRUPTED} status
 *
 * @author Andrei Varabyeu
 */
@Service
public class InterruptBrokenLaunchesJob implements Job {

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

		iterateOverPages(pageable -> projectRepository.findAllIdsAndProjectAttributes(buildProjectAttributesFilter(ProjectAttributeEnum.INTERRUPT_JOB_TIME),
				pageable
		), projects -> projects.forEach(project -> {
			project.getProjectAttributes()
					.stream()
					.filter(pa -> pa.getAttribute().getName().equalsIgnoreCase(ProjectAttributeEnum.INTERRUPT_JOB_TIME.getAttribute()))
					.findFirst()
					.ifPresent(pa -> {
						Duration maxDuration = ofHours(InterruptionJobDelay.findByName(pa.getValue())
								.orElseThrow(() -> new ReportPortalException(
										"Incorrect launch interruption delay period: " + pa.getValue()))
								.getPeriod());
						try (Stream<Long> ids = launchRepository.streamIdsWithStatusModifiedBefore(project.getId(),
								StatusEnum.IN_PROGRESS,
								TO_LOCAL_DATE_TIME.apply(Date.from(Instant.now().minusSeconds(maxDuration.getSeconds())))
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
							//do nothing
						}

					});

		}));
	}

	private Filter buildProjectAttributesFilter(ProjectAttributeEnum projectAttributeEnum) {
		return Filter.builder()
				.withTarget(Project.class)
				.withCondition(new FilterCondition(Condition.EQUALS,
						false,
						projectAttributeEnum.getAttribute(),
						CRITERIA_PROJECT_ATTRIBUTE_NAME
				))
				.build();
	}

	private void interruptLaunch(Long launchId) {
		launchRepository.findById(launchId).ifPresent(launch -> {
			launch.setStatus(StatusEnum.INTERRUPTED);
			launch.setEndTime(LocalDateTime.now());
			launchRepository.save(launch);
		});
	}

	private void interruptItems(Long launchId) {
		testItemRepository.interruptInProgressItems(launchId);
		launchRepository.findById(launchId).ifPresent(l -> {
			l.setStatus(StatusEnum.INTERRUPTED);
			l.setEndTime(LocalDateTime.now());
			launchRepository.save(l);
		});

	}
}
