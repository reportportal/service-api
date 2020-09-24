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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static com.epam.ta.reportportal.commons.querygen.constant.GeneralCriteriaConstant.CRITERIA_ID;
import static com.epam.ta.reportportal.job.PageUtil.iterateOverPages;
import static java.time.Duration.ofSeconds;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
@Profile("!unittest")
public class CleanLaunchesJob implements Job {

	public static final int DEFAULT_THREAD_COUNT = 5;

	private static final Logger LOGGER = LoggerFactory.getLogger(CleanLaunchesJob.class);

	private final ProjectRepository projectRepository;

	private final LaunchCleanerService launchCleaner;

	@Value("5")
	private Integer threadsCount;

	public CleanLaunchesJob(ProjectRepository projectRepository, LaunchCleanerService launchCleaner) {
		this.projectRepository = projectRepository;
		this.launchCleaner = launchCleaner;
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOGGER.info("Cleaning outdated launches has been started");
		ExecutorService executor = Executors.newFixedThreadPool(Optional.ofNullable(threadsCount).orElse(DEFAULT_THREAD_COUNT),
				new ThreadFactoryBuilder().setNameFormat("clean-launches-job-thread-%d").build()
		);

		iterateOverPages(
				Sort.by(Sort.Order.asc(CRITERIA_ID)), projectRepository::findAllIdsAndProjectAttributes,
				projects -> CompletableFuture.allOf(projects.stream().map(project -> {
					AtomicLong removedLaunchesCount = new AtomicLong(0);
					AtomicLong removedAttachmentsCount = new AtomicLong(0);
					AtomicLong removedThumbnailsCount = new AtomicLong(0);
					return CompletableFuture.runAsync(() -> {
						try {
							proceedLaunchesCleaning(project, removedLaunchesCount, removedAttachmentsCount, removedThumbnailsCount);
						} catch (Exception e) {
							LOGGER.error("Cleaning outdated launches for project {} has been failed", project.getId(), e);
						}

						if (removedLaunchesCount.get() > 0 || removedAttachmentsCount.get() > 0 || removedThumbnailsCount.get() > 0) {
							LOGGER.info(
									"Cleaning outdated launches for project {} has been finished. Total launches removed: {}. Attachments removed: {}. Thumbnails removed: {}",
									project.getId(),
									removedLaunchesCount.get(),
									removedAttachmentsCount.get(),
									removedThumbnailsCount.get()
							);
						}

					}, executor);
				}).toArray(CompletableFuture[]::new)).join()
		);

		executor.shutdown();
		LOGGER.info("Cleaning outdated launches has been finished");
	}

	private void proceedLaunchesCleaning(Project project, AtomicLong removedLaunches, AtomicLong removedAttachments,
			AtomicLong removedThumbnails) {
		ProjectUtils.extractAttributeValue(project, ProjectAttributeEnum.KEEP_LAUNCHES)
				.map(it -> ofSeconds(NumberUtils.toLong(it, 0L)))
				.filter(it -> !it.isZero())
				.ifPresent(it -> launchCleaner.cleanOutdatedLaunches(project, it, removedLaunches, removedAttachments, removedThumbnails));
	}

}
