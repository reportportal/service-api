package com.epam.ta.reportportal.job;

import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.epam.ta.reportportal.job.CleanLogsJob.MIN_DELAY;
import static com.epam.ta.reportportal.job.PageUtil.iterateOverPages;
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

	@Autowired
	public LogCleanerServiceImpl(LogRepository logRepository, LaunchRepository launchRepository, TestItemRepository testItemRepository,
			DataStoreService dataStoreService) {
		this.logRepository = logRepository;
		this.launchRepository = launchRepository;
		this.testItemRepository = testItemRepository;
		this.dataStoreService = dataStoreService;
	}

	@Async
	@Transactional
	public void removeOutdatedLogs(Long projectId, Duration period) {
		Date endDate = Date.from(Instant.now().minusSeconds(MIN_DELAY.getSeconds()));
		AtomicLong countPerProject = new AtomicLong(0);
		iterateOverPages(pageable -> launchRepository.getIdsModifiedBefore(projectId, endDate, pageable), launches -> {
			launches.forEach(id -> {
				try (Stream<Long> ids = testItemRepository.streamTestItemIdsByLaunchId(id)) {
					ids.forEach(itemId -> {
						List<Log> logs = logRepository.findLogsWithThumbnailByTestItemIdAndPeriod(itemId, period);
						logs.stream().forEach(log -> {
							ofNullable(log.getAttachment()).ifPresent(dataStoreService::delete);
							ofNullable(log.getAttachmentThumbnail()).ifPresent(dataStoreService::delete);
						});
					});
					long count = logRepository.deleteByPeriodAndTestItemIds(period, ids.collect(Collectors.toList()));
					countPerProject.addAndGet(count);
				} catch (Exception e) {
					//do nothing
					e.printStackTrace();
				}
			});

		});
		LOGGER.info("Removed {} logs for project {}", countPerProject.get(), projectId);
	}

}
