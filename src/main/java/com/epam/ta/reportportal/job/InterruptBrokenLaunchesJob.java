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

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import com.epam.ta.reportportal.core.statistics.StatisticsFacadeFactory;
import com.epam.ta.reportportal.database.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import com.epam.ta.reportportal.database.dao.FailReferenceResourceRepository;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.Status;
import com.epam.ta.reportportal.database.entity.item.FailReferenceResource;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import com.epam.ta.reportportal.database.entity.project.InterruptionJobDelay;

/**
 * Finds jobs witn duration more than defined and finishes them with interrupted
 * {@link com.epam.ta.reportportal.database.entity.Status#INTERRUPTED} status
 * 
 * @author Andrei Varabyeu
 */
@Service
public class InterruptBrokenLaunchesJob implements Runnable {

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private TestItemRepository testItemRepository;

	@Autowired
	private LogRepository logRepository;

	@Autowired
	private FailReferenceResourceRepository issuesRepository;

	@Autowired
	private StatisticsFacadeFactory statisticsFacadeFactory;

	@Autowired
	private ProjectRepository projectRepository;

	@Override
	@Scheduled(cron = "${com.ta.reportportal.job.interrupt.broken.launches.cron}")
	public void run() {
		List<Project> projects = projectRepository.findAll();
		for (Project project : projects) {
			Time maxDuration = Time.hours(InterruptionJobDelay.findByName(project.getConfiguration().getInterruptJobTime()).getPeriod());
			List<Launch> launches = launchRepository.findModifiedLaterAgo(maxDuration, Status.IN_PROGRESS, project.getId());
			for (Launch launch : launches) {
				if (!launchRepository.hasItems(launch, Status.IN_PROGRESS)) {
					/*
					 * There are no test items for this launch. Just INTERRUPT
					 * this launch
					 */
					interruptLaunches(Collections.singletonList(launch));
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
							List<TestItem> itemsInProgress = testItemRepository.findInStatusItems(Status.IN_PROGRESS.name(),
									launch.getId());
							interruptItems(itemsInProgress, launch);
						}
					}
				}
			}
		}
	}

	private void interruptLaunches(List<Launch> launches) {
		for (Launch launch : launches) {
			launch.setStatus(Status.INTERRUPTED);
			launch.setEndTime(Calendar.getInstance().getTime());
			launchRepository.save(launch);
			/*
			 * Delete references on failed\skipped tests in launch. It cannot be
			 * used in main function cause break operators for valid launches.
			 * For valid launches references from FailReference collections
			 * should be kept.
			 */
			this.clearIssueReferences(launch.getId());
		}
	}

	private void interruptItems(List<TestItem> testItems, Launch launch) {
		if (testItems.isEmpty()) {
			return;
		}
		testItems.forEach(this::interruptItem);

		Launch launchReloaded = launchRepository.findOne(launch.getId());
		launchReloaded.setStatus(Status.INTERRUPTED);
		launchReloaded.setEndTime(Calendar.getInstance().getTime());
		launchRepository.save(launchReloaded);
		/*
		 * Delete references on failed\skipped tests in launch. It cannot be
		 * used in main function cause break operators for valid launches. For
		 * valid launches references from FailReference collections should be
		 * kept.
		 */
		this.clearIssueReferences(launch.getId());
	}

	/**
	 * Clear failReference collections by specified launch id
	 * 
	 * @param launchId ID of Launch
	 */
	private void clearIssueReferences(String launchId) {
		List<FailReferenceResource> issues = issuesRepository.findAllLaunchIssues(launchId);
		issuesRepository.delete(issues);
	}

	private void interruptItem(TestItem item) {
		/*
		 * If not interrupted yet
		 */
		if (!Status.INTERRUPTED.equals(item.getStatus())) {
			item.setStatus(Status.INTERRUPTED);
			item.setEndTime(Calendar.getInstance().getTime());
			item = testItemRepository.save(item);

			if (!item.hasChilds()) {
				Project project = projectRepository.findOne(launchRepository.findOne(item.getLaunchRef()).getProjectRef());
				item = statisticsFacadeFactory
						.getStatisticsFacade(project.getConfiguration().getStatisticsCalculationStrategy())
						.updateExecutionStatistics(item);
				if (null != item.getIssue()) {
					item = statisticsFacadeFactory
							.getStatisticsFacade(project.getConfiguration().getStatisticsCalculationStrategy())
							.updateIssueStatistics(item);
				}
			}

			if (null != item.getParent()) {
				interruptItem(testItemRepository.findOne(item.getParent()));
			}
		}
	}
}