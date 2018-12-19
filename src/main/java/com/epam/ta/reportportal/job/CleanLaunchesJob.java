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
import com.epam.ta.reportportal.core.configs.SchedulerConfiguration;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.KeepLaunchDelay;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.epam.ta.reportportal.commons.querygen.constant.ProjectCriteriaConstant.CRITERIA_PROJECT_ATTRIBUTE_NAME;
import static com.epam.ta.reportportal.job.PageUtil.iterateOverPages;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class CleanLaunchesJob implements Job {

	public static final int DEFAULT_THREAD_COUNT = 5;

	private static final Logger LOGGER = LoggerFactory.getLogger(CleanLaunchesJob.class);

	private final ProjectRepository projectRepository;

	private final LogCleanerService logCleaner;

	private final SchedulerConfiguration.CleanLaunchesJobProperties cleanLaunchesJobProperties;

	private final LaunchCleanerService launchCleaner;

	@Value("5")
	private Integer threadsCount;

	@Autowired
	public CleanLaunchesJob(ProjectRepository projectRepository, LogCleanerService logCleaner,
			SchedulerConfiguration.CleanLaunchesJobProperties cleanLaunchesJobProperties, LaunchCleanerService launchCleaner) {
		this.projectRepository = projectRepository;
		this.logCleaner = logCleaner;
		this.cleanLaunchesJobProperties = cleanLaunchesJobProperties;
		this.launchCleaner = launchCleaner;
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOGGER.debug("Cleaning outdated logs has been started");
		ExecutorService executor = Executors.newFixedThreadPool(Optional.ofNullable(threadsCount).orElse(DEFAULT_THREAD_COUNT),
				new ThreadFactoryBuilder().setNameFormat("clean-launches-job-thread-%d").build()
		);

		iterateOverPages(

				pageable -> projectRepository.findAllIdsAndProjectAttributes(
						buildProjectAttributesFilter(ProjectAttributeEnum.KEEP_LAUNCHES),
						pageable
				), projects -> projects.forEach(project -> {
					AtomicLong removedLaunchesCount = new AtomicLong(0);
					AtomicLong removedAttachmentsCount = new AtomicLong(0);
					AtomicLong removedThumbnailsCount = new AtomicLong(0);
					executor.submit(() -> {

						try {
							proceedLaunchesCleaning(project, removedLaunchesCount, removedAttachmentsCount, removedThumbnailsCount);
						} catch (Exception e) {
							LOGGER.debug("Cleaning outdated launches for project {} has been failed", project.getId(), e);
						}

						LOGGER.info(
								"Cleaning outdated launches for project {} has been finished. Total launches removed: {}. Attachments removed: {}. Thumbnails removed: {}",
								project.getId(),
								removedLaunchesCount.get(),
								removedAttachmentsCount.get(),
								removedThumbnailsCount.get()
						);

					});

				}));

		try {
			executor.shutdown();
			if (!executor.awaitTermination(cleanLaunchesJobProperties.getTimeout(), TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			LOGGER.debug("Waiting for launch removing tasks execution has been failed", e);
		} finally {
			executor.shutdownNow();
		}
	}

	private Filter buildProjectAttributesFilter(ProjectAttributeEnum projectAttributeEnum) {
		return Filter.builder()
				.withTarget(Project.class)
				.withCondition(new FilterCondition(Condition.EQUALS, false, projectAttributeEnum.getAttribute(), CRITERIA_PROJECT_ATTRIBUTE_NAME))
				.build();
	}

	private void proceedLaunchesCleaning(Project project, AtomicLong removedLaunchesCount, AtomicLong removedAttachmentsCount,
			AtomicLong removedThumbnailsCount) {
		project.getProjectAttributes()
				.stream()
				.filter(pa -> pa.getAttribute().getName().equalsIgnoreCase(ProjectAttributeEnum.KEEP_LAUNCHES.getAttribute()))
				.findFirst()
				.ifPresent(pa -> {

					KeepLaunchDelay delay = KeepLaunchDelay.findByName(pa.getValue())
							.orElseThrow(() -> new ReportPortalException("Incorrect keep launch delay period: " + pa.getValue()));

					Duration period = Duration.ofDays(delay.getDays());
					if (!period.isZero()) {
						logCleaner.removeProjectAttachments(project, period, removedAttachmentsCount, removedThumbnailsCount);
						launchCleaner.cleanOutdatedLaunches(project, period, removedLaunchesCount);
					}

				});
	}

}
