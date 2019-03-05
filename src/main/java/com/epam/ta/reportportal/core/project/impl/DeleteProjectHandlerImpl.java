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

package com.epam.ta.reportportal.core.project.impl;

import com.epam.ta.reportportal.core.analyzer.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.impl.AnalyzerStatusCache;
import com.epam.ta.reportportal.core.analyzer.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.ProjectIndexEvent;
import com.epam.ta.reportportal.core.events.attachment.DeleteProjectAttachmentsEvent;
import com.epam.ta.reportportal.core.project.DeleteProjectHandler;
import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.dao.UserRepository;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.BulkRQ;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.OperationCompletionRS;
import com.epam.ta.reportportal.ws.model.project.DeleteProjectRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;

/**
 * @author Pavel Bortnik
 */
@Service
public class DeleteProjectHandlerImpl implements DeleteProjectHandler {

	private final ProjectRepository projectRepository;

	private final UserRepository userRepository;

	private final LogIndexer logIndexer;

	private final AnalyzerStatusCache analyzerStatusCache;

	private final MessageBus messageBus;

	private final ApplicationEventPublisher eventPublisher;

	@Autowired
	public DeleteProjectHandlerImpl(ProjectRepository projectRepository, UserRepository userRepository, LogIndexer logIndexer,
			AnalyzerStatusCache analyzerStatusCache, MessageBus messageBus, ApplicationEventPublisher eventPublisher) {
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
		this.logIndexer = logIndexer;
		this.analyzerStatusCache = analyzerStatusCache;
		this.messageBus = messageBus;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public OperationCompletionRS deleteProject(String projectName) {
		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));
		projectRepository.deleteById(project.getId());

		eventPublisher.publishEvent(new DeleteProjectAttachmentsEvent(project.getId()));

		return new OperationCompletionRS("Project with name = '" + projectName + "' has been successfully deleted.");
	}

	@Override
	public OperationCompletionRS deleteProjectIndex(String projectName, String username) {
		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));

		User user = userRepository.findByLogin(username).orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, username));

		expect(AnalyzerUtils.getAnalyzerConfig(project).isIndexingRunning(), Predicate.isEqual(false)).verify(ErrorType.FORBIDDEN_OPERATION,
				"Index can not be removed until index generation proceeds."
		);
		expect(analyzerStatusCache.getAnalyzeStatus().asMap().containsValue(project.getId()),
				Predicate.isEqual(false)
		).verify(ErrorType.FORBIDDEN_OPERATION, "Index can not be removed until index generation proceeds.");

		logIndexer.deleteIndex(project.getId());
		messageBus.publishActivity(new ProjectIndexEvent(project.getId(), project.getName(), user.getId(), false));
		return new OperationCompletionRS("Project index with name = '" + projectName + "' is successfully deleted.");
	}

	@Override
	public List<OperationCompletionRS> deleteProjects(BulkRQ<DeleteProjectRQ> deleteProjectBulkRQ) {
		return deleteProjectBulkRQ.getEntities()
				.values()
				.stream()
				.map(DeleteProjectRQ::getProjectName)
				.map(this::deleteProject)
				.collect(Collectors.toList());
	}
}
