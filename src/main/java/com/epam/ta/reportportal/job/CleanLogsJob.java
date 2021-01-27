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
import com.epam.ta.reportportal.job.service.LogCleanerService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.math.NumberUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Clean logs job in accordance with project settings
 *
 * @author Andrei Varabyeu
 * @author Pavel Borntik
 */
@Service
@Profile("!unittest")
public class CleanLogsJob implements Job {

	private static final Logger LOGGER = LoggerFactory.getLogger(CleanLogsJob.class);

	private final Integer threadsCount;
	private final Integer launchesLimit;

	private final LogCleanerService logCleaner;

	private final ProjectRepository projectRepository;
	private final ActivityRepository activityRepository;
	private final LaunchRepository launchRepository;

	public CleanLogsJob(@Value("${rp.environment.variable.clean.logs.pool}") Integer threadsCount,
			@Value("${rp.environment.variable.clean.launches.size}") Integer launchesLimit, LogCleanerService logCleaner,
			ProjectRepository projectRepository, ActivityRepository activityRepository, LaunchRepository launchRepository) {
		this.threadsCount = threadsCount;
		this.launchesLimit = launchesLimit;
		this.logCleaner = logCleaner;
		this.projectRepository = projectRepository;
		this.activityRepository = activityRepository;
		this.launchRepository = launchRepository;
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOGGER.info("Cleaning outdated logs has been started");
		final ExecutorService executor = Executors.newFixedThreadPool(threadsCount,
				new ThreadFactoryBuilder().setNameFormat("clean-logs-job-thread-%d").build()
		);

		iterateOverPages(Sort.by(Sort.Order.asc(CRITERIA_ID)),
				projectRepository::findAllIdsAndProjectAttributes,
				projects -> projects.forEach(project -> {
					try {
						LOGGER.info("Cleaning outdated logs for project {} has been started", project.getId());
						proceedLogsRemoving(project, executor);
					} catch (Exception e) {
						LOGGER.error("Cleaning outdated logs for project {} has been failed", project.getId(), e);
					}
				})
		);

		executor.shutdown();
		LOGGER.info("Cleaning logs has been finished");
	}

	private void proceedLogsRemoving(Project project, ExecutorService executor) {
		ProjectUtils.extractAttributeValue(project, ProjectAttributeEnum.KEEP_LOGS)
				.map(it -> ofSeconds(NumberUtils.toLong(it, 0L)))
				.filter(it -> !it.isZero())
				.ifPresent(period -> {
					activityRepository.deleteModifiedLaterAgo(project.getId(), period);
					cleanLogs(project, period, executor);
				});
	}

	private void cleanLogs(Project project, Duration period, ExecutorService executor) {
		final LocalDateTime startTimeBound = LocalDateTime.now(ZoneOffset.UTC).minus(period);

		final AtomicLong removedLogsCount = new AtomicLong(0);
		final AtomicLong attachmentsCount = new AtomicLong(0);
		final AtomicLong thumbnailsCount = new AtomicLong(0);

		iterateOverContent(launchesLimit,
				pageable -> launchRepository.findIdsByProjectIdAndStartTimeBefore(project.getId(),
						startTimeBound,
						pageable.getPageSize(),
						pageable.getOffset()
				),
				launchIds -> CompletableFuture.allOf(launchIds.stream()
						.map(launchId -> CompletableFuture.supplyAsync(() -> logCleaner.removeOutdatedLogs(launchId,
								startTimeBound,
								attachmentsCount,
								thumbnailsCount
						), executor).thenAcceptAsync(removedLogsCount::addAndGet, executor))
						.toArray(CompletableFuture[]::new)).join()
		);

		LOGGER.info("Cleaning outdated logs for project {} has been finished. Removed {} logs with {} attachments and {} thumbnails",
				project.getId(),
				removedLogsCount.get(),
				attachmentsCount.get(),
				thumbnailsCount.get()
		);

	}

}
