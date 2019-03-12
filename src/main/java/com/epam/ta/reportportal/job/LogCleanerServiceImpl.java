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

import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.dao.*;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.entity.project.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.commons.EntityUtils.TO_LOCAL_DATE_TIME;
import static com.epam.ta.reportportal.job.CleanLogsJob.MIN_DELAY;
import static java.util.Optional.ofNullable;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Service
public class LogCleanerServiceImpl implements LogCleanerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogCleanerServiceImpl.class);

	private final LogRepository logRepository;

	private final LaunchRepository launchRepository;

	private final TestItemRepository testItemRepository;

	private final DataStoreService dataStoreService;

	private final ActivityRepository activityRepository;

	private final AttachmentRepository attachmentRepository;

	@Autowired
	public LogCleanerServiceImpl(LogRepository logRepository, LaunchRepository launchRepository, TestItemRepository testItemRepository,
			DataStoreService dataStoreService, ActivityRepository activityRepository, AttachmentRepository attachmentRepository) {
		this.logRepository = logRepository;
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.dataStoreService = dataStoreService;
		this.activityRepository = activityRepository;
		this.attachmentRepository = attachmentRepository;
	}

	@Override
	@Async
	@Transactional
	public void removeOutdatedLogs(Project project, Duration period, AtomicLong removedLogsCount) {
		Date endDate = Date.from(Instant.now().minusSeconds(MIN_DELAY.getSeconds()));
		AtomicLong removedLogsInThreadCount = new AtomicLong(0);
		AtomicLong attachmentsCount = new AtomicLong(0);
		AtomicLong thumbnailsCount = new AtomicLong(0);

		activityRepository.deleteModifiedLaterAgo(project.getId(), period);

		try (Stream<Long> launchIds = launchRepository.streamIdsModifiedBefore(project.getId(), TO_LOCAL_DATE_TIME.apply(endDate))) {
			launchIds.forEach(id -> {
				try (Stream<Long> ids = testItemRepository.streamTestItemIdsByLaunchId(id)) {
					List<Long> itemIds = ids.peek(itemId -> {
						List<Log> logs = logRepository.findLogsWithThumbnailByTestItemIdAndPeriod(itemId, period);
						removeAttachmentsOfLogs(logs, attachmentsCount, thumbnailsCount);
					}).collect(Collectors.toList());
					long count = logRepository.deleteByPeriodAndTestItemIds(period, itemIds);
					removedLogsCount.addAndGet(count);
					removedLogsInThreadCount.addAndGet(count);
				} catch (Exception e) {
					LOGGER.error("Error during cleaning outdated logs {}", e);
				}
			});
		} catch (Exception e) {
			LOGGER.error("Error during cleaning outdated logs {}", e);
		}

		LOGGER.info(
				"Removed {} logs for project {} with {} attachments and {} thumbnails",
				removedLogsInThreadCount,
				project.getId(),
				attachmentsCount.get(),
				thumbnailsCount.get()
		);
	}

	@Override
	@Async
	@Transactional
	public void removeProjectAttachments(Project project, Duration period, AtomicLong removedAttachmentsCount,
			AtomicLong removedThumbnailsCount) {
		Date endDate = Date.from(Instant.now().minusSeconds(MIN_DELAY.getSeconds()));
		try (Stream<Long> launchIds = launchRepository.streamIdsModifiedBefore(project.getId(), TO_LOCAL_DATE_TIME.apply(endDate))) {
			launchIds.forEach(id -> {
				try (Stream<Long> ids = testItemRepository.streamTestItemIdsByLaunchId(id)) {
					ids.forEach(itemId -> {
						List<Log> logs = logRepository.findLogsWithThumbnailByTestItemIdAndPeriod(itemId, period);
						removeAttachmentsOfLogs(logs, removedAttachmentsCount, removedThumbnailsCount);
					});
				} catch (Exception e) {
					//do nothing
					LOGGER.error("Error during cleaning project attachments {}", e);
				}
			});
		} catch (Exception e) {
			//do nothing
			LOGGER.error("Error during cleaning project attachments {}", e);
		}
	}

	private void removeAttachmentsOfLogs(Collection<Log> logs, AtomicLong attachmentsCount, AtomicLong thumbnailsCount) {
		logs.forEach(log -> {
			try {
				ofNullable(log.getAttachment()).ifPresent(attachment -> {

					attachmentRepository.deleteById(attachment.getId());

					ofNullable(attachment.getFileId()).ifPresent(fileId -> {
						dataStoreService.delete(fileId);
						attachmentsCount.addAndGet(1L);
					});
					ofNullable(attachment.getThumbnailId()).ifPresent(fileId -> {
						dataStoreService.delete(fileId);
						thumbnailsCount.addAndGet(1L);
					});

				});
			} catch (Exception ex) {
				LOGGER.debug("Error has occurred during the attachments removing", ex);
				//do nothing, because error that has occurred during the removing of current attachment shouldn't affect others
			}

		});
	}

}
