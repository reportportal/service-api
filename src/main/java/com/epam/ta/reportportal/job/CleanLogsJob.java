/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-api
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.database.dao.*;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.project.KeepLogsDelay;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.database.entity.project.KeepLogsDelay.findByName;
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

	public static final int DEFAULT_THREAD_COUNT = 20;
	public static final long JOB_EXECUTION_TIMEOUT = 1L;
	private static final Duration MIN_DELAY = Duration.ofDays(KeepLogsDelay.TWO_WEEKS.getDays() - 1);
	private static final Logger LOGGER = LoggerFactory.getLogger(CleanLogsJob.class);

	@Autowired
	private LogRepository logRepo;

	@Autowired
	private LaunchRepository launchRepo;

	@Autowired
	private TestItemRepository testItemRepo;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private ActivityRepository activityRepository;

	@Override
	public void execute(JobExecutionContext context) {
		LOGGER.info("Cleaning outdated logs has been started");
		ExecutorService executor = Executors.newFixedThreadPool(DEFAULT_THREAD_COUNT);

		iterateOverPages(projectRepository::findAllIdsAndConfiguration, projects -> projects.forEach(project -> {
			executor.submit(() -> {
				try {
					LOGGER.info("Cleaning outdated logs for project {} has been started", project.getId());
					Duration period = ofDays(findByName(project.getConfiguration().getKeepLogs()).getDays());
					if (!period.isZero()) {
						activityRepository.deleteModifiedLaterAgo(project.getId(), period);
						removeOutdatedLogs(project.getId(), period);
					}
				} catch (Exception e) {
					LOGGER.info("Cleaning outdated logs for project {} has been failed", project.getId(), e);
				}
				LOGGER.info("Cleaning outdated logs for project {} has been finished", project.getId());

			});

		}));

		executor.shutdown();
		try {
			LOGGER.info("Awaiting cleaning outdated screenshot to finish");
			executor.awaitTermination(JOB_EXECUTION_TIMEOUT, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			throw new RuntimeException("Job Execution timeout exceeded", e);
		}
	}

	private void removeOutdatedLogs(String projectId, Duration period) {
		Date endDate = Date.from(Instant.now().minusSeconds(MIN_DELAY.getSeconds()));

		iterateOverPages(pageable -> launchRepo.findModifiedBefore(projectId, endDate, pageable), launches -> {
			launches.forEach(launch -> {
				try (Stream<TestItem> testItemStream = testItemRepo.streamIdsByLaunch(launch.getId())) {
					logRepo.deleteByPeriodAndItemsRef(period, testItemStream.map(TestItem::getId).collect(Collectors.toList()));
				} catch (Exception e) {
					//do nothing
				}
			});

		});

	}

}
