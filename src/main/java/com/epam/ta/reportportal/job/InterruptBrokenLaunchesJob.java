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

import com.epam.ta.reportportal.core.statistics.StatisticsFacadeFactory;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.InterruptionJobDelay;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.enums.StatusEnum;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.launch.Launch;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.job.PageUtil.iterateOverPages;
import static java.time.Duration.ofHours;
import static java.util.Optional.ofNullable;

/**
 * Finds jobs witn duration more than defined and finishes them with interrupted
 * {@link StatusEnum#INTERRUPTED} status
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
	private ProjectRepository projectRepository;

	@Autowired
	private IRetriesLaunchHandler retriesLaunchHandler;

	@Override
	//	@Scheduled(cron = "${com.ta.reportportal.job.interrupt.broken.launches.cron}")
	public void execute(JobExecutionContext context) {

		iterateOverPages(pageable -> projectRepository.findAllIdsAndProjectAttributes(ProjectAttributeEnum.KEEP_SCREENSHOTS, pageable),
				projects -> projects.forEach(project -> {
					project.getProjectAttributes()
							.stream()
							.map(ProjectAttribute::getAttribute)
							.filter(attribute -> attribute.getName()
									.equalsIgnoreCase(ProjectAttributeEnum.INTERRUPT_JOB_TIME.getAttribute()))
							.findFirst()
							.ifPresent(attr -> {
								Duration maxDuration = ofHours(InterruptionJobDelay.findByName(attr.getName()).getPeriod());
								launchRepository.findModifiedLaterAgo(maxDuration, StatusEnum.IN_PROGRESS, project.getId())
										.forEach(launch -> {
											if (!launchRepository.hasItems(launch, StatusEnum.IN_PROGRESS)) {
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

												if (!testItemRepository.hasTestItemsAddedLately(
														maxDuration,
														launch,
														StatusEnum.IN_PROGRESS
												)) {
													List<TestItem> items = testItemRepository.findModifiedLaterAgo(
															maxDuration,
															StatusEnum.IN_PROGRESS,
															launch
													);

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
															interruptItems(launch);
														}
													} else {
														/*
														 * If not just INTERRUPT all found items and launch
														 */
														interruptItems(launch);
													}
												}
											}
										});
							});

				})
		);
	}

	private void interruptLaunch(Launch launch) {
		launch.setStatus(StatusEnum.INTERRUPTED);
		launch.setEndTime(LocalDateTime.now());
		launchRepository.save(launch);
	}

	private void interruptItems(Launch launch) {
		testItemRepository.interruptInProgressItems(launch.getId());
		launchRepository.findById(launch.getId()).ifPresent(l -> {
			l.setStatus(StatusEnum.INTERRUPTED);
			l.setEndTime(LocalDateTime.now());
			retriesLaunchHandler.handleRetries(l);
			launchRepository.save(l);
		});

	}
}
