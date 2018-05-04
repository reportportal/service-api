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

import com.epam.ta.reportportal.database.DataStorage;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.entity.project.KeepScreenshotsDelay;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

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

	@Autowired
	private DataStorage gridFS;

	@Autowired
	private ProjectRepository projectRepository;

	@Autowired
	private LogRepository logRepository;

	@Override
	//	@Scheduled(cron = "${com.ta.reportportal.job.clean.screenshots.cron}")
	public void execute(JobExecutionContext context) {
		LOGGER.info("Cleaning outdated screenshots has been started");

		iterateOverPages(projectRepository::findAllIdsAndConfiguration, projects -> projects.forEach(project -> {
			AtomicLong count = new AtomicLong(0);

			try {
				LOGGER.info("Cleaning outdated screenshots for project {} has been started", project.getId());

				Duration period = ofDays(KeepScreenshotsDelay.findByName(project.getConfiguration().getKeepScreenshots()).getDays());
				if (!period.isZero()) {
					gridFS.findModifiedLaterAgo(period, project.getId()).forEach(file -> {
						count.incrementAndGet();
						gridFS.deleteData(file.getId().toString());
						/* Clear binary_content fields from log repository */
						logRepository.removeBinaryContent(file.getId().toString());
					});
				}
			} catch (Exception e) {
				LOGGER.info("Cleaning outdated screenshots for project {} has been failed", project.getId(), e);
			}
			LOGGER.info("Cleaning outdated screenshots for project {} has been finished. {} deleted", project.getId(), count.get());
		}));

	}
}
