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

import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.job.service.AttachmentCleanerService;
import com.epam.ta.reportportal.job.service.LogCleanerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

import static com.epam.ta.reportportal.job.PageUtil.iterateOverContent;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LogCleanerServiceImpl implements LogCleanerService {

	private final Integer itemPageSize;

	private final AttachmentCleanerService attachmentCleanerService;

	private final TestItemRepository testItemRepository;
	private final LogRepository logRepository;

	@Autowired
	public LogCleanerServiceImpl(@Value("${rp.environment.variable.clean.items.size}") Integer itemPageSize,
			AttachmentCleanerService attachmentCleanerService, TestItemRepository testItemRepository, LogRepository logRepository) {
		this.itemPageSize = itemPageSize;
		this.attachmentCleanerService = attachmentCleanerService;
		this.testItemRepository = testItemRepository;
		this.logRepository = logRepository;
	}

	@Override
	@Transactional
	public long removeOutdatedLogs(Long launchId, LocalDateTime startTimeBound, AtomicLong attachmentsCount, AtomicLong thumbnailsCount) {
		final Duration period = Duration.between(LocalDateTime.now(), startTimeBound);
		final AtomicLong logsCount = new AtomicLong(0);

		iterateOverContent(itemPageSize, pageable -> testItemRepository.findTestItemIdsByLaunchId(launchId, pageable), itemIds -> {
			attachmentCleanerService.removeOutdatedItemsAttachments(itemIds, startTimeBound, attachmentsCount, thumbnailsCount);
			long removedCount = logRepository.deleteByPeriodAndTestItemIds(period, itemIds);
			logsCount.addAndGet(removedCount);
		});

		attachmentCleanerService.removeOutdatedLaunchesAttachments(Collections.singletonList(launchId),
				startTimeBound,
				attachmentsCount,
				thumbnailsCount
		);

		long removedCount = logRepository.deleteByPeriodAndLaunchIds(period, Collections.singletonList(launchId));
		logsCount.addAndGet(removedCount);
		return logsCount.get();
	}
}
