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
import com.epam.ta.reportportal.database.entity.project.KeepLogsDelay;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.database.entity.project.KeepLogsDelay.findByName;
import static java.time.Duration.ofDays;

/**
 * Clean logs job in accordance with project settings
 *
 * @author Andrei Varabyeu
 * @author Pavel Borntik
 */
@Service
public class CleanLogsJob implements Job {

	private static final Duration MIN_DELAY = Duration.ofDays(KeepLogsDelay.TWO_WEEKS.getDays() - 1);

	private static final Duration MAX_DELAY = Duration.ofDays(KeepLogsDelay.SIX_MONTHS.getDays() + 1);

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
//	@Scheduled(cron = "${com.ta.reportportal.job.clean.logs.cron}")
	public void execute(JobExecutionContext context) {
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
		try (Stream<Launch> launchStream = streamLaunches(projectId)) {
			launchStream.forEach(launch -> {
				try (Stream<TestItem> testItemStream = testItemRepo.streamIdsByLaunch(launch.getId())) {
					logRepo.deleteByPeriodAndItemsRef(period, testItemStream.map(TestItem::getId).collect(Collectors.toList()));
				}
			});
		}
	}

	private Stream<Launch> streamLaunches(String projectId) {
		Date beginDate = Date.from(Instant.now().minusSeconds(MAX_DELAY.getSeconds()));
		Date endDate = Date.from(Instant.now().minusSeconds(MIN_DELAY.getSeconds()));
		return launchRepo.streamModifiedInRange(projectId, beginDate, endDate);
	}
}
