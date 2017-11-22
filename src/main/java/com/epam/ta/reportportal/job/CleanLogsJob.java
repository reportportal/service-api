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
import com.epam.ta.reportportal.database.entity.Launch;
import com.epam.ta.reportportal.database.entity.Project;
import com.epam.ta.reportportal.database.entity.item.TestItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.database.entity.project.KeepLogsDelay.findByName;
import static java.time.Duration.ofDays;

/**
 * Clean logs job in accordance with project settings
 *
 * @author Andrei_Ramanchuk
 */
@Service
public class CleanLogsJob implements Runnable {

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
	@Scheduled(cron = "${com.ta.reportportal.job.clean.logs.cron}")
	public void run() {
		try (Stream<Project> stream = projectRepository.streamAllIdsAndConfiguration()) {
			stream.forEach(project -> {
				Duration period = ofDays(findByName(project.getConfiguration().getKeepLogs()).getDays());
				if (!period.isZero()) {
					activityRepository.deleteModifiedLaterAgo(project.getId(), period);
					removeOutdatedLogs(project.getId(), period);
				}
			});
		}
	}

	private void removeOutdatedLogs(String projectId, Duration period) {
		try (Stream<Launch> launchStream = launchRepo.streamIdsByProject(projectId)) {
			launchStream.forEach(launch -> {
				try (Stream<TestItem> testItemStream = testItemRepo.streamIdsByLaunch(launch.getId())) {
					logRepo.deleteByPeriodAndItemsRef(period, testItemStream.map(TestItem::getId).collect(Collectors.toList()));
				}
			});
		}
	}
}