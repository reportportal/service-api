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

import com.epam.ta.reportportal.dao.ActivityRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.project.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import static com.epam.ta.reportportal.commons.EntityUtils.TO_LOCAL_DATE_TIME;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LaunchCleanerServiceImpl implements LaunchCleanerService {

	private final LaunchRepository launchRepository;

	private final ActivityRepository activityRepository;

	@Autowired
	public LaunchCleanerServiceImpl(LaunchRepository launchRepository, ActivityRepository activityRepository) {
		this.launchRepository = launchRepository;
		this.activityRepository = activityRepository;
	}

	@Override
	@Async
	@Transactional
	public void cleanOutdatedLaunches(Project project, Duration period, AtomicLong launchesRemoved) {
		activityRepository.deleteModifiedLaterAgo(project.getId(), period);
		launchesRemoved.addAndGet(launchRepository.deleteLaunchesByProjectIdModifiedBefore(project.getId(),
				TO_LOCAL_DATE_TIME.apply(Date.from(Instant.now().minusSeconds(period.getSeconds())))
		));
	}
}
