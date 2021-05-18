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
import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache;
import com.epam.ta.reportportal.core.events.activity.DefectTypeDeletedEvent;
import com.epam.ta.reportportal.dao.LaunchRepository;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.google.common.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache.AUTO_ANALYZER_KEY;
import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils.getAnalyzerConfig;

/**
 * @author <a href="mailto:ihar_kahadouski@epam.com">Ihar Kahadouski</a>
 */
@Component
@Transactional
public class DefectTypeDeletedHandler {

	private final AnalyzerStatusCache analyzerStatusCache;

	private final AnalyzerServiceClient analyzerServiceClient;

	private final LaunchRepository launchRepository;

	private final LogIndexer logIndexer;

	private final ProjectRepository projectRepository;

	@Autowired
	public DefectTypeDeletedHandler(AnalyzerStatusCache analyzerStatusCache, AnalyzerServiceClient analyzerServiceClient,
			LaunchRepository launchRepository, LogIndexer logIndexer, ProjectRepository projectRepository) {
		this.analyzerStatusCache = analyzerStatusCache;
		this.analyzerServiceClient = analyzerServiceClient;
		this.launchRepository = launchRepository;
		this.logIndexer = logIndexer;
		this.projectRepository = projectRepository;
	}

	@Transactional
	@Retryable(value = ReportPortalException.class, maxAttempts = 5, backoff = @Backoff(value = 5000L))
	@TransactionalEventListener
	public void handleDefectTypeDeleted(DefectTypeDeletedEvent event) {
		Project project = projectRepository.findById(event.getProjectId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, event.getProjectId()));

		if (analyzerServiceClient.hasClients()) {
			Cache<Long, Long> analyzeStatus = analyzerStatusCache.getAnalyzeStatus(AUTO_ANALYZER_KEY)
					.orElseThrow(() -> new ReportPortalException(ErrorType.ANALYZER_NOT_FOUND, AUTO_ANALYZER_KEY));
			expect(analyzeStatus.asMap().containsValue(event.getProjectId()), equalTo(false)).verify(ErrorType.FORBIDDEN_OPERATION,
					"Index can not be removed until auto-analysis proceeds."
			);

			logIndexer.index(event.getProjectId(), getAnalyzerConfig(project));
		}
	}

}
