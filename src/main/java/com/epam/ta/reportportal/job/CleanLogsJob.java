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
import com.epam.ta.reportportal.entity.enums.KeepLogsDelay;
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

import static com.epam.ta.reportportal.commons.querygen.constant.ProjectCriteriaConstant.CRITERIA_ATTRIBUTE_NAME;
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
		ExecutorService executor = Executors.newFixedThreadPool(Optional.ofNullable(threadsCount).orElse(DEFAULT_THREAD_COUNT),
				new ThreadFactoryBuilder().setNameFormat("clean-logs-job-thread-%d").build()
		);

		iterateOverPages(pageable -> projectRepository.findAllIdsAndProjectAttributes(buildProjectAttributesFilter(ProjectAttributeEnum.KEEP_LOGS),
				pageable
		), projects -> projects.forEach(project -> {
			AtomicLong removedLogsCount = new AtomicLong(0);
			executor.submit(() -> {
				try {
					LOGGER.info("Cleaning outdated logs for project {} has been started", project.getId());
					proceedLogsRemoving(project, removedLogsCount);

				} catch (Exception e) {
					LOGGER.debug("Cleaning outdated logs for project {} has been failed", project.getId(), e);
				}
				LOGGER.info("Cleaning outdated logs for project {} has been finished. Total logs removed: {}",
						project.getId(),
						removedLogsCount.get()
				);
			});
		}));

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

	private Filter buildProjectAttributesFilter(ProjectAttributeEnum projectAttributeEnum) {
		return Filter.builder()
				.withTarget(Project.class)
				.withCondition(new FilterCondition(Condition.EQUALS, false, projectAttributeEnum.getAttribute(), CRITERIA_ATTRIBUTE_NAME))
				.build();
	}

	private void proceedLogsRemoving(Project project, AtomicLong removedLogsCount) {
		project.getProjectAttributes()
				.stream()
				.filter(pa -> pa.getAttribute().getName().equalsIgnoreCase(ProjectAttributeEnum.KEEP_LOGS.getAttribute()))
				.findFirst()
				.ifPresent(pa -> {
					Duration period = ofDays(KeepLogsDelay.findByName(pa.getValue())
							.orElseThrow(() -> new ReportPortalException("Incorrect keep logs delay period: " + pa.getValue()))
							.getDays());
					if (!period.isZero()) {
						logCleaner.removeOutdatedLogs(project, period, removedLogsCount);
					}
				});
	}

}
