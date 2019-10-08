package com.epam.ta.reportportal.core.events.handler;

import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.events.item.ItemRetryEvent;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import com.epam.ta.reportportal.entity.log.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Component
public class TestItemRetryEventHandler {

	private final LogIndexer logIndexer;

	private final LogRepository logRepository;

	@Autowired
	public TestItemRetryEventHandler(LogIndexer logIndexer, LogRepository logRepository) {
		this.logIndexer = logIndexer;
		this.logRepository = logRepository;
	}

	@Async
	@TransactionalEventListener
	public void onItemRetry(ItemRetryEvent event) {
		logIndexer.cleanIndex(event.getProjectId(),
				logRepository.findAllByTestItemItemIdInAndLogLevelIsGreaterThanEqual(Collections.singletonList(event.getItemId()),
						LogLevel.ERROR.toInt()
				)
						.stream()
						.map(Log::getId)
						.collect(Collectors.toList())
		);
	}
}
