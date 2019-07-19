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

import com.epam.ta.reportportal.core.analyzer.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.events.item.ItemFinishedEvent;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.project.AnalyzerConfig;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * @author <a href="mailto:ivan_budayeu@epam.com">Ivan Budayeu</a>
 */
@Component
public class TestItemFinishedEventHandler {

	private final ProjectRepository projectRepository;

	private final LogIndexer logIndexer;

	@Autowired
	public TestItemFinishedEventHandler(ProjectRepository projectRepository, LogIndexer logIndexer) {
		this.projectRepository = projectRepository;
		this.logIndexer = logIndexer;
	}

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener
	public void onApplicationEvent(ItemFinishedEvent itemFinishedEvent) {

		Project project = projectRepository.findById(itemFinishedEvent.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, itemFinishedEvent.getProjectId()));

		AnalyzerConfig analyzerConfig = AnalyzerUtils.getAnalyzerConfig(project);

		logIndexer.indexItemsLogs(
				itemFinishedEvent.getProjectId(),
				itemFinishedEvent.getLaunchId(),
				Lists.newArrayList(itemFinishedEvent.getItemId()),
				analyzerConfig
		);
	}
}
