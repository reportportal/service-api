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
import com.epam.ta.reportportal.entity.enums.KeepLogsDelay;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.job.service.LogCleanerService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static com.epam.ta.reportportal.job.JobUtil.buildProjectAttributesFilter;
import static com.epam.ta.reportportal.job.PageUtil.iterateOverPages;
import static java.time.Duration.ofDays;

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
	public static final int DEFAULT_THREAD_COUNT = 5;

	@Value("5")
	private Integer threadsCount;

	private final ProjectRepository projectRepository;

	private final LogCleanerService logCleaner;

	@Autowired
	public CleanLogsJob(ProjectRepository projectRepository, LogCleanerService logCleaner) {
		this.projectRepository = projectRepository;
		this.logCleaner = logCleaner;
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOGGER.info("Cleaning outdated logs has been started");
		ExecutorService executor = Executors.newFixedThreadPool(Optional.ofNullable(threadsCount).orElse(DEFAULT_THREAD_COUNT),
				new ThreadFactoryBuilder().setNameFormat("clean-logs-job-thread-%d").build()
		);

		iterateOverPages(pageable -> projectRepository.findAllIdsAndProjectAttributes(buildProjectAttributesFilter(ProjectAttributeEnum.KEEP_LOGS),
				pageable
		), projects -> CompletableFuture.allOf(projects.stream().map(project -> {
			AtomicLong removedLogsCount = new AtomicLong(0);
			return CompletableFuture.runAsync(() -> {
				try {
					LOGGER.debug("Cleaning outdated logs for project {} has been started", project.getId());
					proceedLogsRemoving(project, removedLogsCount);

				} catch (Exception e) {
					LOGGER.debug("Cleaning outdated logs for project {} has been failed", project.getId(), e);
				}
				LOGGER.debug("Cleaning outdated logs for project {} has been finished. Total logs removed: {}",
						project.getId(),
						removedLogsCount.get()
				);
			}, executor);
		}).toArray(CompletableFuture[]::new)).join());

		executor.shutdown();

	}

	private void proceedLogsRemoving(Project project, AtomicLong removedLogsCount) {
		ProjectUtils.extractAttributeValue(project, ProjectAttributeEnum.KEEP_LOGS)
				.map(it -> ofDays(KeepLogsDelay.findByName(it)
						.orElseThrow(() -> new ReportPortalException("Incorrect keep logs delay period: " + it))
						.getDays()))
				.filter(it -> !it.isZero())
				.ifPresent(it -> logCleaner.removeOutdatedLogs(project, it, removedLogsCount));
	}

}
