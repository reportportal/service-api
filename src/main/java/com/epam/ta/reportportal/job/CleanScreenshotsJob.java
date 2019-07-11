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

import com.epam.ta.reportportal.commons.querygen.Filter;
import com.epam.ta.reportportal.commons.querygen.FilterCondition;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.KeepScreenshotsDelay;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static com.epam.ta.reportportal.commons.querygen.constant.ProjectCriteriaConstant.CRITERIA_PROJECT_ATTRIBUTE_NAME;
import static com.epam.ta.reportportal.job.PageUtil.iterateOverPages;
import static java.time.Duration.ofDays;

/**
 * Clear screenshots from GridFS in accordance with projects settings
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class CleanScreenshotsJob implements Job {
	private static final Logger LOGGER = LoggerFactory.getLogger(CleanScreenshotsJob.class);

	private final ProjectRepository projectRepository;

	private final LogCleanerService logCleaner;

	@Autowired
	public CleanScreenshotsJob(ProjectRepository projectRepository, LogCleanerService logCleaner) {
		this.projectRepository = projectRepository;
		this.logCleaner = logCleaner;
	}

	@Override
	//	@Scheduled(cron = "${com.ta.reportportal.job.clean.screenshots.cron}")
	public void execute(JobExecutionContext context) {
		LOGGER.info("Cleaning outdated screenshots has been started");

		iterateOverPages(pageable -> projectRepository.findAllIdsAndProjectAttributes(
				buildProjectAttributesFilter(ProjectAttributeEnum.KEEP_SCREENSHOTS),
				pageable
		), projects -> projects.forEach(project -> {
			AtomicLong attachmentsCount = new AtomicLong(0);
			AtomicLong thumbnailsCount = new AtomicLong(0);

			try {
				LOGGER.info("Cleaning outdated screenshots for project {} has been started", project.getId());
				proceedScreenShotsCleaning(project, attachmentsCount, thumbnailsCount);
			} catch (Exception e) {
				LOGGER.error("Cleaning outdated screenshots for project {} has been failed", project.getId(), e);
			}
			LOGGER.info(
					"Cleaning outdated screenshots for project {} has been finished. {} attachments and {} thumbnails have been deleted",
					project.getId(),
					attachmentsCount.get(),
					thumbnailsCount.get()
			);
		}));

	}

	private Filter buildProjectAttributesFilter(ProjectAttributeEnum projectAttributeEnum) {
		return Filter.builder()
				.withTarget(Project.class)
				.withCondition(FilterCondition.builder().eq(CRITERIA_PROJECT_ATTRIBUTE_NAME, projectAttributeEnum.getAttribute()).build())
				.build();
	}

	private void proceedScreenShotsCleaning(Project project, AtomicLong attachmentsCount, AtomicLong thumbnailsCount) {
		project.getProjectAttributes()
				.stream()
				.filter(pa -> pa.getAttribute().getName().equalsIgnoreCase(ProjectAttributeEnum.KEEP_SCREENSHOTS.getAttribute()))
				.findFirst()
				.ifPresent(pa -> {
					Duration period = ofDays(KeepScreenshotsDelay.findByName(pa.getValue())
							.orElseThrow(() -> new ReportPortalException("Incorrect keep screenshots delay period: " + pa.getValue()))
							.getDays());
					if (!period.isZero()) {
						logCleaner.removeProjectAttachments(project, period, attachmentsCount, thumbnailsCount);
					}
				});
	}
}
