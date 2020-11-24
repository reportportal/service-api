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
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.job.service.LogCleanerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.job.PageUtil.iterateOverContent;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LogCleanerServiceImpl implements LogCleanerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogCleanerServiceImpl.class);

	private final Integer itemPageSize;

	private final LogRepository logRepository;

	private final LaunchRepository launchRepository;

	private final TestItemRepository testItemRepository;

	private final ActivityRepository activityRepository;

	private final AttachmentCleanerServiceImpl attachmentCleanerService;

	@Autowired
	public LogCleanerServiceImpl(@Value("${rp.environment.variable.clean.items.size}") Integer itemPageSize, LogRepository logRepository,
			LaunchRepository launchRepository, TestItemRepository testItemRepository, ActivityRepository activityRepository,
			AttachmentCleanerServiceImpl attachmentCleanerService) {
		this.itemPageSize = itemPageSize;
		this.logRepository = logRepository;
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.activityRepository = activityRepository;
		this.attachmentCleanerService = attachmentCleanerService;
	}

	@Override
	@Transactional
	public void removeOutdatedLogs(Project project, Duration period, AtomicLong removedLogsCount) {
		LocalDateTime endDate = LocalDateTime.now(ZoneOffset.UTC).minus(period);
		AtomicLong logsCount = new AtomicLong(0);
		AtomicLong attachmentsCount = new AtomicLong(0);
		AtomicLong thumbnailsCount = new AtomicLong(0);

		activityRepository.deleteModifiedLaterAgo(project.getId(), period);

		try (Stream<Long> launchIds = launchRepository.streamIdsByStartTimeBefore(project.getId(), endDate)) {
			launchIds.forEach(id -> {
				iterateOverContent(itemPageSize, pageable -> testItemRepository.findTestItemIdsByLaunchId(id, pageable), ids -> {
					attachmentCleanerService.removeOutdatedItemsAttachments(ids, endDate, attachmentsCount, thumbnailsCount);
					long count = logRepository.deleteByPeriodAndTestItemIds(period, ids);
					removedLogsCount.addAndGet(count);
					logsCount.addAndGet(count);
				});
				attachmentCleanerService.removeOutdatedLaunchesAttachments(Collections.singletonList(id),
						endDate,
						attachmentsCount,
						thumbnailsCount
				);
				long count = logRepository.deleteByPeriodAndLaunchIds(period, Collections.singletonList(id));
				removedLogsCount.addAndGet(count);
				logsCount.addAndGet(count);
			});
		} catch (Exception e) {
			LOGGER.error("Error during cleaning outdated logs", e);
		}

		if (logsCount.get() > 0 || attachmentsCount.get() > 0 || thumbnailsCount.get() > 0) {
			LOGGER.info("Removed {} logs for project {} with {} attachments and {} thumbnails",
					logsCount.get(),
					project.getId(),
					attachmentsCount.get(),
					thumbnailsCount.get()
			);
		}
	}
}
