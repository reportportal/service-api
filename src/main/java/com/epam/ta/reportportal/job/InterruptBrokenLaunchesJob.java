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

import com.epam.ta.reportportal.core.launch.IRetriesLaunchHandler;
import com.epam.ta.reportportal.core.statistics.StatisticsFacadeFactory;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.project.InterruptionJobDelay;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.quartz.Job;
import org.quartz.JobExecutionContext;


import java.time.Duration;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.util.Predicates.IS_RETRY;
import static java.time.Duration.ofHours;

/**
 * Finds jobs witn duration more than defined and finishes them with interrupted
 * {@link com.epam.ta.reportportal.database.entity.Status#INTERRUPTED} status
 *
 * @author Andrei Varabyeu
 */
@Service
public class InterruptBrokenLaunchesJob implements Job {

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private LogRepository logRepository;

	@Autowired
	private StatisticsFacadeFactory statisticsFacadeFactory;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private IRetriesLaunchHandler retriesLaunchHandler;

	@Override
	//	@Scheduled(cron = "${com.ta.reportportal.job.interrupt.broken.launches.cron}")
	public void execute(JobExecutionContext context) {
		try (Stream<Project> projects = projectRepository.streamAllIdsAndConfiguration()) {
			projects.forEach(project -> {
				Duration maxDuration = ofHours(InterruptionJobDelay.findByName(project.getConfiguration().getInterruptJobTime())
						.getPeriod());
				launchRepository.findModifiedLaterAgo(maxDuration, Status.IN_PROGRESS, project.getId()).forEach(launch -> {
					if (!launchRepository.hasItems(launch, Status.IN_PROGRESS)) {
						/*
						 * There are no test items for this launch. Just INTERRUPT
						 * this launch
						 */
						interruptLaunch(launch);
					} else {
						/*
						 * Well, there are some test items started for specified
						 * launch
						 */

						if (!testItemRepository.hasTestItemsAddedLately(maxDuration, launch, Status.IN_PROGRESS)) {
							List<TestItem> items = testItemRepository.findModifiedLaterAgo(maxDuration, Status.IN_PROGRESS, launch);

							/*
							 * If there are logs, we have to check whether them
							 * expired
							 */
							if (testItemRepository.hasLogs(items)) {
								boolean isLaunchBroken = true;
								for (TestItem item : items) {
									/*
									 * If there are logs which are still valid
									 * (probably automation project keep writing
									 * something)
									 */
									if (logRepository.hasLogsAddedLately(maxDuration, item)) {
										isLaunchBroken = false;
										break;
									}
								}
								if (isLaunchBroken) {
									interruptItems(testItemRepository.findInStatusItems(Status.IN_PROGRESS.name(), launch.getId()), launch);
								}
							} else {
								/*
								 * If not just INTERRUPT all found items and launch
								 */
								interruptItems(testItemRepository.findInStatusItems(Status.IN_PROGRESS.name(), launch.getId()), launch);
							}
						}
					}
				});
			});
		}
	}

	private void interruptLaunch(Launch launch) {
		launch.setStatus(Status.INTERRUPTED);
		launch.setEndTime(Calendar.getInstance().getTime());
		launchRepository.save(launch);
	}

	private void interruptItems(List<TestItem> testItems, Launch launch) {
		if (testItems.isEmpty()) {
			return;
		}
		testItems.forEach(item -> interruptItem(item, launch));
		Launch launchReloaded = launchRepository.findOne(launch.getId());
		launchReloaded.setStatus(Status.INTERRUPTED);
		launchReloaded.setEndTime(Calendar.getInstance().getTime());
		retriesLaunchHandler.handleRetries(launchReloaded);
		launchRepository.save(launchReloaded);
	}

	private void interruptItem(TestItem item, Launch launch) {
		/*
		 * If not interrupted yet
		 */
		if (!Status.INTERRUPTED.equals(item.getStatus())) {
			item.setStatus(Status.INTERRUPTED);
			item.setEndTime(Calendar.getInstance().getTime());
			item = testItemRepository.save(item);

			if (!item.hasChilds() && !IS_RETRY.test(item)) {
				Project project = projectRepository.findOne(launch.getProjectRef());
				item = statisticsFacadeFactory.getStatisticsFacade(project.getConfiguration().getStatisticsCalculationStrategy())
						.updateExecutionStatistics(item);
				if (null != item.getIssue()) {
					item = statisticsFacadeFactory.getStatisticsFacade(project.getConfiguration().getStatisticsCalculationStrategy())
							.updateIssueStatistics(item);
				}
			}

			if (null != item.getParent()) {
				interruptItem(testItemRepository.findOne(item.getParent()), launch);
			}
		}
	}
}
