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

import com.epam.ta.reportportal.core.analyzer.auto.AnalyzerServiceAsync;
import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.events.AnalysisEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Component
public class StartAnalysisEventHandler {

	private final AnalyzerServiceAsync analyzerServiceAsync;

	private final LogIndexer logIndexer;

	@Autowired
	public StartAnalysisEventHandler(AnalyzerServiceAsync analyzerServiceAsync, LogIndexer logIndexer) {
		this.analyzerServiceAsync = analyzerServiceAsync;
		this.logIndexer = logIndexer;
	}

	@TransactionalEventListener
	public void handleEvent(AnalysisEvent event) {
		analyzerServiceAsync.analyze(event.getLaunch(), event.getItemIds(), event.getAnalyzerConfig())
				.thenApply(it -> logIndexer.indexItemsLogs(event.getLaunch().getProjectId(),
						event.getLaunch().getId(),
						event.getItemIds(),
						event.getAnalyzerConfig()
				));
	}

}