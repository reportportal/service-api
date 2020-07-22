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

package com.epam.ta.reportportal.job.service.impl;

import com.epam.ta.reportportal.dao.ActivityRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.job.service.AttachmentCleanerService;
import com.epam.ta.reportportal.job.service.LaunchCleanerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LaunchCleanerServiceImpl implements LaunchCleanerService {

	private final LaunchRepository launchRepository;

	private final ActivityRepository activityRepository;

	private final AttachmentCleanerService attachmentCleanerService;

	@Autowired
	public LaunchCleanerServiceImpl(LaunchRepository launchRepository, ActivityRepository activityRepository,
			AttachmentCleanerService attachmentCleanerService) {
		this.launchRepository = launchRepository;
		this.activityRepository = activityRepository;
		this.attachmentCleanerService = attachmentCleanerService;
	}

	@Override
	@Transactional
	public void cleanOutdatedLaunches(Project project, Duration period, AtomicLong launchesRemoved, AtomicLong attachmentsRemoved,
			AtomicLong thumbnailsRemoved) {
		activityRepository.deleteModifiedLaterAgo(project.getId(), period);
		List<Long> launchIds = launchRepository.findIdsByProjectIdAndStartTimeBefore(project.getId(),
				LocalDateTime.now(ZoneOffset.UTC).minus(period)
		);
		attachmentCleanerService.removeOutdatedLaunchesAttachments(launchIds, attachmentsRemoved, thumbnailsRemoved);
		launchRepository.deleteAllByIdIn(launchIds);
		launchesRemoved.addAndGet(launchIds.size());
	}
}
