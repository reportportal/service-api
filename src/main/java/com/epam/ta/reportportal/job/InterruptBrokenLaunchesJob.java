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

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.commons.EntityUtils.TO_LOCAL_DATE_TIME;
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

	//	@Autowired
	//	private IRetriesLaunchHandler retriesLaunchHandler;

	@Override
	//	@Scheduled(cron = "${com.ta.reportportal.job.interrupt.broken.launches.cron}")
	public void execute(JobExecutionContext context) {

		iterateOverPages(pageable -> projectRepository.findAllIdsAndProjectAttributes(ProjectAttributeEnum.KEEP_SCREENSHOTS, pageable),
				projects -> projects.forEach(project -> {
					project.getProjectAttributes()
							.stream()
							.filter(pa -> pa.getAttribute()
									.getName()
									.equalsIgnoreCase(ProjectAttributeEnum.INTERRUPT_JOB_TIME.getAttribute()))
							.findFirst()
							.ifPresent(pa -> {
								Duration maxDuration = ofHours(InterruptionJobDelay.findByName(pa.getValue()).getPeriod());
								launchRepository.streamIdsWithStatusModifiedBefore(project.getId(),
										StatusEnum.IN_PROGRESS,
										TO_LOCAL_DATE_TIME.apply(Date.from(Instant.now().minusSeconds(maxDuration.getSeconds())))
								).forEach(launchId -> {
									if (!testItemRepository.hasItemsInStatusByLaunch(launchId, StatusEnum.IN_PROGRESS)) {
										/*
										 * There are no test items for this launch. Just INTERRUPT
										 * this launch
										 */
										interruptLaunch(launchId);
									} else {
										/*
										 * Well, there are some test items started for specified
										 * launch
										 */
										if (!testItemRepository.hasItemsInStatusAddedLately(launchId,
												maxDuration,
												StatusEnum.IN_PROGRESS
										)) {
											/*
											 * If there are logs, we have to check whether them
											 * expired
											 */
											if (testItemRepository.hasLogs(launchId, maxDuration, StatusEnum.IN_PROGRESS)) {
												/*
												 * If there are logs which are still valid
												 * (probably automation project keep writing
												 * something)
												 */
												if (!logRepository.hasLogsAddedLately(maxDuration, launchId, StatusEnum.IN_PROGRESS)) {
													interruptItems(launchId);
												}
											} else {
												/*
												 * If not just INTERRUPT all found items and launch
												 */
												interruptItems(launchId);
											}
										}
									}
								});
							});

				})
		);
	}

	private void interruptLaunch(Long launchId) {
		launchRepository.findById(launchId).ifPresent(launch -> {
			launch.setStatus(StatusEnum.INTERRUPTED);
			launch.setEndTime(LocalDateTime.now());
			launchRepository.save(launch);
		});
	}

	private void interruptItems(Long launchId) {
		testItemRepository.interruptInProgressItems(launchId);
		launchRepository.findById(launchId).ifPresent(l -> {
			l.setStatus(StatusEnum.INTERRUPTED);
			l.setEndTime(LocalDateTime.now());
			//			retriesLaunchHandler.handleRetries(l);
			launchRepository.save(l);
		});

	}
}
