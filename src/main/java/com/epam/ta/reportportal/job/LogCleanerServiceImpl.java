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
import com.epam.ta.reportportal.dao.ActivityRepository;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
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

	@Autowired
	public LogCleanerServiceImpl(LogRepository logRepository, LaunchRepository launchRepository, TestItemRepository testItemRepository,
			DataStoreService dataStoreService, ActivityRepository activityRepository) {
		this.logRepository = logRepository;
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.dataStoreService = dataStoreService;
		this.activityRepository = activityRepository;
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
					ids.forEach(itemId -> {
						List<Log> logs = logRepository.findLogsWithThumbnailByTestItemIdAndPeriod(itemId, period);
						removeAttachmentsOfLogs(logs, attachmentsCount, thumbnailsCount);
					});
					long count = logRepository.deleteByPeriodAndTestItemIds(period, ids.collect(Collectors.toList()));
					removedLogsCount.addAndGet(count);
					removedLogsInThreadCount.addAndGet(count);
				} catch (Exception e) {
					//do nothing
				}
			});
		} catch (Exception e) {
			//do nothing
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
						logRepository.clearLogsAttachmentsAndThumbnails(logs.stream().map(Log::getId).collect(Collectors.toList()));
					});
				} catch (Exception e) {
					//do nothing
				}
			});
		} catch (Exception e) {
			//do nothing
		}
	}

	private void removeAttachmentsOfLogs(Collection<Log> logs, AtomicLong attachmentsCount, AtomicLong thumbnailsCount) {
		logs.stream().forEach(log -> {
			try {
				ofNullable(log.getAttachment()).ifPresent(filePath -> {
					dataStoreService.delete(filePath);
					attachmentsCount.addAndGet(1L);
				});
				ofNullable(log.getAttachmentThumbnail()).ifPresent(filePath -> {
					dataStoreService.delete(filePath);
					thumbnailsCount.addAndGet(1L);
				});
			} catch (Exception ex) {
				LOGGER.debug("Error has occurred during the attachments removing", ex);
				//do nothing, because error that has occurred during the removing of current attachment shouldn't affect others
			}

		});
	}

}
