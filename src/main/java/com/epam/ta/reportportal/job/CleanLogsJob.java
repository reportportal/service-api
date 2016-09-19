/*
 * Copyright 2016 EPAM Systems
 * 
 * 
 * This file is part of EPAM Report Portal.
 * https://github.com/epam/ReportPortal
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

import static com.epam.ta.reportportal.database.Time.days;
import static com.epam.ta.reportportal.database.entity.project.KeepLogsDelay.findByName;

import com.epam.ta.reportportal.database.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.epam.ta.reportportal.database.dao.ActivityRepository;
import com.epam.ta.reportportal.database.dao.LaunchRepository;
import com.epam.ta.reportportal.database.dao.LogRepository;
import com.epam.ta.reportportal.database.dao.ProjectRepository;
import com.epam.ta.reportportal.database.dao.TestItemRepository;

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
		projectRepository.findAll().forEach(project -> {
			Time period = days(findByName(project.getConfiguration().getKeepLogs()).getDays());
			activityRepository.deleteModifiedLaterAgo(project.getId(), period);
			removeOutdatedLogs(project.getId(), period);
		});
	}

	private void removeOutdatedLogs(String projectId, Time period) {
		launchRepo.findLaunchIdsByProjectId(projectId).stream().map(launch -> testItemRepo.findIdsByLaunch(launch.getId()))
				.map(testItems -> logRepo.findModifiedLaterAgo(period, testItems)).forEach(logs -> logRepo.delete(logs));
	}
}