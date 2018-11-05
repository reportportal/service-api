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
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

	@Value("")
	private Integer threadsCount;

	@Autowired
	private LogRepository logRepository;

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private ActivityRepository activityRepository;

	@Bean(name = "cleanLogsJobBean")
	public JobDetailFactoryBean cleanLogsJob() {
		return SchedulerConfiguration.createJobDetail(this.getClass());
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		System.out.println("QWERTY");
		LOGGER.debug("Cleaning outdated logs has been started");
		ExecutorService executor = Executors.newFixedThreadPool(
				Optional.ofNullable(threadsCount).orElse(DEFAULT_THREAD_COUNT),
				new ThreadFactoryBuilder().setNameFormat("clean-logs-job-thread-%d").build()
		);

		iterateOverPages(
				pageable -> projectRepository.findAllIdsAndProjectAttributes(ProjectAttributeEnum.KEEP_LOGS, pageable),
				projects -> projects.forEach(project -> {
					executor.submit(() -> {
						try {
							LOGGER.info("Cleaning outdated logs for project {} has been started", project.getId());
							project.getProjectAttributes()
									.stream()
									.map(ProjectAttribute::getAttribute)
									.filter(attribute -> attribute.getName()
											.equalsIgnoreCase(ProjectAttributeEnum.KEEP_LOGS.getAttribute()))
									.findFirst()
									.ifPresent(attr -> {
										Duration period = ofDays(KeepLogsDelay.findByName(attr.getName()).getDays());
										if (!period.isZero()) {
											activityRepository.deleteModifiedLaterAgo(project.getId(), period);
											removeOutdatedLogs(project.getId(), period);
										}
									});

						} catch (Exception e) {
							LOGGER.debug("Cleaning outdated logs for project {} has been failed", project.getId(), e);
						}
						LOGGER.info("Cleaning outdated logs for project {} has been finished", project.getId());
					});
				})
		);

	}

	private void removeOutdatedLogs(Long projectId, Duration period) {
		Date endDate = Date.from(Instant.now().minusSeconds(MIN_DELAY.getSeconds()));
		AtomicLong countPerProject = new AtomicLong(0);
		iterateOverPages(pageable -> launchRepository.getIdsModifiedBefore(projectId, endDate, pageable), launches -> {
			launches.forEach(id -> {
				try (Stream<Long> ids = testItemRepository.streamTestItemIdsByLaunchId(id)) {
					long count = logRepository.deleteByPeriodAndTestItemIds(period, ids.collect(Collectors.toList()));
					countPerProject.addAndGet(count);
				} catch (Exception e) {
					//do nothing
				}
			});

		});
		LOGGER.info("Removed {} logs for project {}", countPerProject.get(), projectId);
	}
}
