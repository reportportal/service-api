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

import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.core.configs.SchedulerConfiguration;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.KeepScreenshotsDelay;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.project.ProjectAttribute;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.job.CleanLogsJob.MIN_DELAY;
import static com.epam.ta.reportportal.job.PageUtil.iterateOverPages;
import static java.time.Duration.ofDays;
import static java.util.Optional.ofNullable;

/**
 * Clear screenshots from GridFS in accordance with projects settings
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class CleanScreenshotsJob implements Job {
	private static final Logger LOGGER = LoggerFactory.getLogger(CleanScreenshotsJob.class);

	@Autowired
	private DataStoreService dataStoreService;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private LogRepository logRepository;

	@Autowired
	private LaunchRepository launchRepository;

	@Autowired
	private TestItemRepository testItemRepository;

	@Bean("cleanScreenshotsJobBean")
	public static JobDetailFactoryBean cleanScreenshotsJob() {
		return SchedulerConfiguration.createJobDetail(CleanScreenshotsJob.class);
	}

	@Override
	//	@Scheduled(cron = "${com.ta.reportportal.job.clean.screenshots.cron}")
	public void execute(JobExecutionContext context) {
		LOGGER.info("Cleaning outdated screenshots has been started");
		System.out.println("QQQQQQQQQQQQQ");

		iterateOverPages(pageable -> projectRepository.findAllIdsAndProjectAttributes(ProjectAttributeEnum.KEEP_SCREENSHOTS, pageable),
				projects -> projects.forEach(project -> {
					AtomicLong count = new AtomicLong(0);

					try {
						LOGGER.info("Cleaning outdated screenshots for project {} has been started", project.getId());

						project.getProjectAttributes()
								.stream()
								.map(ProjectAttribute::getAttribute)
								.filter(attribute -> attribute.getName()
										.equalsIgnoreCase(ProjectAttributeEnum.KEEP_SCREENSHOTS.getAttribute()))
								.findFirst()
								.ifPresent(attr -> {
									Duration period = ofDays(KeepScreenshotsDelay.findByName(attr.getName()).getDays());
									if (!period.isZero()) {

										Date endDate = Date.from(Instant.now().minusSeconds(MIN_DELAY.getSeconds()));
										iterateOverPages(pageable -> launchRepository.getIdsModifiedBefore(project.getId(),
												endDate,
												pageable
										), launchIds -> launchIds.forEach(id -> {
											try (Stream<Long> ids = testItemRepository.streamTestItemIdsByLaunchId(id)) {
												ids.forEach(itemId -> {
													List<Log> logs = logRepository.findLogsWithThumbnailByTestItemIdAndPeriod(itemId,
															period
													);
													logs.stream().forEach(log -> {
														ofNullable(log.getAttachment()).ifPresent(attachment -> {
															dataStoreService.delete(attachment);
															count.addAndGet(1);
														});
														ofNullable(log.getAttachmentThumbnail()).ifPresent(attachThumb -> {
															dataStoreService.delete(attachThumb);
															count.addAndGet(1);
														});
													});

													logRepository.clearLogsAttachmentsAndThumbnails(logs.stream()
															.map(Log::getId)
															.collect(Collectors.toList()));
												});

											} catch (Exception e) {
												//do nothing
											}
										}));
									}
								});

					} catch (Exception e) {
						LOGGER.info("Cleaning outdated screenshots for project {} has been failed", project.getId(), e);
					}
					LOGGER.info("Cleaning outdated screenshots for project {} has been finished. {} deleted", project.getId(), count.get());
				})
		);

	}
}
