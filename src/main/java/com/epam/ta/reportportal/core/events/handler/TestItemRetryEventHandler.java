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

package com.epam.ta.reportportal.core.events.handler;

import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.events.item.ItemRetryEvent;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.entity.enums.LogLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Collections;

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
		//TODO done
		logIndexer.cleanIndex(event.getProjectId(),
				logRepository.findIdsUnderTestItemByLaunchIdAndTestItemIdsAndLogLevelGte(event.getLaunchId(),
						Collections.singletonList(event.getItemId()),
						LogLevel.ERROR.toInt()
				)
		);
	}
}
