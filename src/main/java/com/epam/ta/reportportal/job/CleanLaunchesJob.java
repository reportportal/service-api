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

import com.epam.ta.reportportal.dao.ActivityRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.job.service.LaunchCleanerService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.math.NumberUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.ta.reportportal.job.PageUtil.iterateOverContent;
import static com.epam.ta.reportportal.job.PageUtil.iterateOverPages;
import static java.time.Duration.ofSeconds;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
@Profile("!unittest")
public class CleanLaunchesJob implements Job {

	private static final Logger LOGGER = LoggerFactory.getLogger(CleanLaunchesJob.class);

	private final Integer threadsCount;
	private final Integer launchesLimit;

	private final LaunchCleanerService launchCleanerService;

	private final ProjectRepository projectRepository;
	private final ActivityRepository activityRepository;
	private final LaunchRepository launchRepository;

	@Autowired
	public CleanLaunchesJob(@Value("${rp.environment.variable.clean.launches.pool}") Integer threadsCount,
			@Value("${rp.environment.variable.clean.launches.size}") Integer launchesLimit, LaunchCleanerService launchCleanerService,
			ProjectRepository projectRepository, ActivityRepository activityRepository, LaunchRepository launchRepository) {
		this.threadsCount = threadsCount;
		this.launchesLimit = launchesLimit;
		this.launchCleanerService = launchCleanerService;
		this.projectRepository = projectRepository;
		this.activityRepository = activityRepository;
		this.launchRepository = launchRepository;
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOGGER.info("Cleaning outdated launches has been started");
		final ExecutorService executor = Executors.newFixedThreadPool(threadsCount,
				new ThreadFactoryBuilder().setNameFormat("clean-launches-job-thread-%d").build()
		);

		iterateOverPages(Sort.by(Sort.Order.asc(CRITERIA_ID)), projectRepository::findAllIdsAndProjectAttributes, projects -> {
			projects.forEach(p -> {
				try {
					proceedLaunchesCleaning(p, executor);
				} catch (Exception ex) {
					LOGGER.error("Cleaning outdated launches for project {} has been failed", p.getId(), ex);
				}
			});
		});

		executor.shutdown();
		LOGGER.info("Cleaning outdated launches has been finished");
	}

	private void proceedLaunchesCleaning(Project project, ExecutorService executorService) {
		ProjectUtils.extractAttributeValue(project, ProjectAttributeEnum.KEEP_LAUNCHES)
				.map(it -> ofSeconds(NumberUtils.toLong(it, 0L)))
				.filter(it -> !it.isZero())
				.ifPresent(period -> {
					activityRepository.deleteModifiedLaterAgo(project.getId(), period);
					cleanLaunches(project.getId(), period, executorService);
				});
	}

	private void cleanLaunches(Long projectId, Duration period, ExecutorService executorService) {
		final LocalDateTime startTimeBound = LocalDateTime.now(ZoneOffset.UTC).minus(period);

		final AtomicLong removedLaunches = new AtomicLong(0);
		final AtomicLong removedAttachments = new AtomicLong(0);
		final AtomicLong removedThumbnails = new AtomicLong(0);

		iterateOverContent(launchesLimit,
				pageable -> launchRepository.findIdsByProjectIdAndStartTimeBefore(projectId, startTimeBound, pageable.getPageSize()),
				ids -> {
					CompletableFuture.allOf(ids.stream()
							.map(id -> CompletableFuture.runAsync(() -> launchCleanerService.cleanLaunch(id,
									removedAttachments,
									removedThumbnails
							), executorService))
							.toArray(CompletableFuture[]::new)).join();
					removedLaunches.addAndGet(ids.size());
				}
		);

		LOGGER.info(
				"Cleaning outdated launches for project {} has been finished. Total launches removed: {}. Attachments removed: {}. Thumbnails removed: {}",
				projectId,
				removedLaunches.get(),
				removedAttachments.get(),
				removedThumbnails.get()
		);

	}

}
