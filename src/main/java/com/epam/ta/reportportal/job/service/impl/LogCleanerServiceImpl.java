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
import com.epam.ta.reportportal.job.service.AttachmentCleanerService;
import com.epam.ta.reportportal.job.service.LogCleanerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LogCleanerServiceImpl implements LogCleanerService {

	private final AttachmentCleanerService attachmentCleanerService;
	private final LogRepository logRepository;

	@Autowired
	public LogCleanerServiceImpl(AttachmentCleanerService attachmentCleanerService, LogRepository logRepository) {
		this.attachmentCleanerService = attachmentCleanerService;
		this.logRepository = logRepository;
	}

	@Override
	@Transactional
	public long removeOutdatedLogs(Long launchId, LocalDateTime startTimeBound, AtomicLong attachmentsCount, AtomicLong thumbnailsCount) {
		attachmentCleanerService.removeLaunchAttachments(launchId, attachmentsCount, thumbnailsCount);
		return logRepository.deleteLogsUnderLaunchByLogTimeBefore(launchId, startTimeBound);
	}
}
