/*
 *
 *  Copyright (C) 2018 EPAM Systems
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.core.configs.SchedulerConfiguration;
import com.epam.ta.reportportal.dao.*;
import com.epam.ta.reportportal.entity.enums.KeepLogsDelay;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.job.PageUtil.iterateOverPages;
import static java.time.Duration.ofDays;

/**
 * Clean logs job in accordance with project settings
 *
 * @author Andrei Varabyeu
 * @author Pavel Borntik
 */
@Service
public class CleanLogsJob implements Job {

	private static final Logger LOGGER = LoggerFactory.getLogger(CleanLogsJob.class);
	public static final int DEFAULT_THREAD_COUNT = 5;
	public static final long JOB_EXECUTION_TIMEOUT = 1L;
	public static final Duration MIN_DELAY = Duration.ofDays(KeepLogsDelay.TWO_WEEKS.getDays() - 1);

	@Value("5")
	private Integer threadsCount;

	private final ProjectRepository projectRepository;

	private final LogCleanerService logCleaner;

	private final SchedulerConfiguration.CleanLogsJobProperties cleanLogsJobProperties;

	@Autowired
	public CleanLogsJob(ProjectRepository projectRepository, LogCleanerService logCleaner,
			SchedulerConfiguration.CleanLogsJobProperties cleanLogsJobProperties) {
		this.projectRepository = projectRepository;
		this.logCleaner = logCleaner;
		this.cleanLogsJobProperties = cleanLogsJobProperties;
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		LOGGER.debug("Cleaning outdated logs has been started");
		ExecutorService executor = Executors.newFixedThreadPool(
				Optional.ofNullable(threadsCount).orElse(DEFAULT_THREAD_COUNT),
				new ThreadFactoryBuilder().setNameFormat("clean-logs-job-thread-%d").build()
		);

		iterateOverPages(
				pageable -> projectRepository.findAllIdsAndProjectAttributes(ProjectAttributeEnum.KEEP_LOGS, pageable),
				projects -> projects.forEach(project -> {
					AtomicLong removedLogsCount = new AtomicLong(0);
					executor.submit(() -> {
						try {
							LOGGER.info("Cleaning outdated logs for project {} has been started", project.getId());
							project.getProjectAttributes()
									.stream()
									.filter(pa -> pa.getAttribute()
											.getName()
											.equalsIgnoreCase(ProjectAttributeEnum.KEEP_LOGS.getAttribute()))
									.findFirst()
									.ifPresent(pa -> {
										Duration period = ofDays(KeepLogsDelay.findByName(pa.getValue()).getDays());
										if (!period.isZero()) {
											logCleaner.removeOutdatedLogs(project, period, removedLogsCount);
										}
									});

						} catch (Exception e) {
							LOGGER.debug("Cleaning outdated logs for project {} has been failed", project.getId(), e);
						}
						LOGGER.info(
								"Cleaning outdated logs for project {} has been finished. Total logs removed: {}",
								project.getId(),
								removedLogsCount.get()
						);
					});
				})
		);

		try {
			executor.shutdown();
			if (!executor.awaitTermination(cleanLogsJobProperties.getTimeout(), TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			LOGGER.debug("Waiting for tasks execution has been failed", e);
		} finally {
			executor.shutdownNow();
		}

	}

}
