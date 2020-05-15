/*
 * Copyright 2019 EPAM Systems
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

import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.enums.ProjectAttributeEnum;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectUtils;
import com.epam.ta.reportportal.job.service.impl.AttachmentCleanerServiceImpl;
import org.apache.commons.lang3.math.NumberUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicLong;

import static com.epam.ta.reportportal.job.JobUtil.buildProjectAttributesFilter;
import static com.epam.ta.reportportal.job.PageUtil.iterateOverPages;
import static java.time.Duration.ofSeconds;

/**
 * Clear screenshots from GridFS in accordance with projects settings
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class CleanScreenshotsJob implements Job {
	private static final Logger LOGGER = LoggerFactory.getLogger(CleanScreenshotsJob.class);

	private final ProjectRepository projectRepository;

	private final AttachmentCleanerServiceImpl attachmentCleanerService;

	@Autowired
	public CleanScreenshotsJob(ProjectRepository projectRepository, AttachmentCleanerServiceImpl attachmentCleanerService) {
		this.projectRepository = projectRepository;
		this.attachmentCleanerService = attachmentCleanerService;
	}

	@Override
	public void execute(JobExecutionContext context) {
		LOGGER.info("Cleaning outdated screenshots has been started");

		iterateOverPages(pageable -> projectRepository.findAllIdsAndProjectAttributes(buildProjectAttributesFilter(ProjectAttributeEnum.KEEP_SCREENSHOTS),
				pageable
		), projects -> projects.forEach(project -> {
			AtomicLong attachmentsCount = new AtomicLong(0);
			AtomicLong thumbnailsCount = new AtomicLong(0);

			try {
				LOGGER.debug("Cleaning outdated screenshots for project {} has been started", project.getId());
				proceedScreenShotsCleaning(project, attachmentsCount, thumbnailsCount);
			} catch (Exception e) {
				LOGGER.error("Cleaning outdated screenshots for project {} has been failed", project.getId(), e);
			}
			if (attachmentsCount.get() > 0 || thumbnailsCount.get() > 0) {
				LOGGER.info(
						"Cleaning outdated screenshots for project {} has been finished. {} attachments and {} thumbnails have been deleted",
						project.getId(),
						attachmentsCount.get(),
						thumbnailsCount.get()
				);
			}
		}));

	}

	private void proceedScreenShotsCleaning(Project project, AtomicLong attachmentsCount, AtomicLong thumbnailsCount) {
		ProjectUtils.extractAttributeValue(project, ProjectAttributeEnum.KEEP_SCREENSHOTS)
				.map(it -> ofSeconds(NumberUtils.toLong(it, 0L)))
				.filter(it -> !it.isZero())
				.map(it -> LocalDateTime.now(ZoneOffset.UTC).minus(it))
				.ifPresent(it -> attachmentCleanerService.removeProjectAttachments(project, it, attachmentsCount, thumbnailsCount));
	}
}
