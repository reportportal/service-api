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

import com.epam.ta.reportportal.core.analyzer.auto.LogIndexer;
import com.epam.ta.reportportal.core.analyzer.auto.client.AnalyzerServiceClient;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache;
import com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerUtils;
import com.epam.ta.reportportal.core.events.MessageBus;
import com.epam.ta.reportportal.core.events.activity.ProjectIndexEvent;
import com.epam.ta.reportportal.core.project.DeleteProjectHandler;
import com.epam.ta.reportportal.core.project.content.remover.ProjectContentRemover;
import com.epam.ta.reportportal.dao.*;
import com.epam.ta.reportportal.entity.item.issue.IssueType;
import com.epam.ta.reportportal.entity.project.Project;
import com.epam.ta.reportportal.entity.project.ProjectIssueType;
import com.epam.ta.reportportal.entity.user.User;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.*;
import com.google.common.cache.Cache;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.core.analyzer.auto.impl.AnalyzerStatusCache.AUTO_ANALYZER_KEY;

/**
 * @author Pavel Bortnik
 */
@Service
public class DeleteProjectHandlerImpl implements DeleteProjectHandler {

	private final ProjectRepository projectRepository;

	private final UserRepository userRepository;

	private final LogIndexer logIndexer;

	private final AnalyzerServiceClient analyzerServiceClient;

	private final AnalyzerStatusCache analyzerStatusCache;

	private final MessageBus messageBus;

	private final AttachmentRepository attachmentRepository;

	private final IssueTypeRepository issueTypeRepository;

	private final ProjectContentRemover projectContentRemover;

	private final LogRepository logRepository;

	@Autowired
	public DeleteProjectHandlerImpl(ProjectRepository projectRepository, UserRepository userRepository, LogIndexer logIndexer,
			AnalyzerServiceClient analyzerServiceClient, AnalyzerStatusCache analyzerStatusCache, MessageBus messageBus,
			AttachmentRepository attachmentRepository, IssueTypeRepository issueTypeRepository, ProjectContentRemover projectContentRemover,
			LogRepository logRepository) {
		this.projectRepository = projectRepository;
		this.userRepository = userRepository;
		this.logIndexer = logIndexer;
		this.analyzerServiceClient = analyzerServiceClient;
		this.analyzerStatusCache = analyzerStatusCache;
		this.messageBus = messageBus;
		this.attachmentRepository = attachmentRepository;
		this.issueTypeRepository = issueTypeRepository;
		this.projectContentRemover = projectContentRemover;
		this.logRepository = logRepository;
	}

	@Override
	public OperationCompletionRS deleteProject(Long projectId) {
		Project project = projectRepository.findById(projectId)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectId));
		return deleteProject(project);
	}

	@Override
	public OperationCompletionRS deleteProjectIndex(String projectName, String username) {
		expect(analyzerServiceClient.hasClients(), Predicate.isEqual(true)).verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"There are no analyzer deployed."
		);

		Project project = projectRepository.findByName(projectName)
				.orElseThrow(() -> new ReportPortalException(ErrorType.PROJECT_NOT_FOUND, projectName));

		User user = userRepository.findByLogin(username).orElseThrow(() -> new ReportPortalException(ErrorType.USER_NOT_FOUND, username));

		expect(AnalyzerUtils.getAnalyzerConfig(project).isIndexingRunning(), Predicate.isEqual(false)).verify(ErrorType.FORBIDDEN_OPERATION,
				"Index can not be removed until index generation proceeds."
		);

		Cache<Long, Long> analyzeStatus = analyzerStatusCache.getAnalyzeStatus(AUTO_ANALYZER_KEY)
				.orElseThrow(() -> new ReportPortalException(ErrorType.ANALYZER_NOT_FOUND, AUTO_ANALYZER_KEY));
		expect(analyzeStatus.asMap().containsValue(project.getId()), Predicate.isEqual(false)).verify(ErrorType.FORBIDDEN_OPERATION,
				"Index can not be removed until index generation proceeds."
		);

		logIndexer.deleteIndex(project.getId());
		messageBus.publishActivity(new ProjectIndexEvent(project.getId(), project.getName(), user.getId(), user.getLogin(), false));
		return new OperationCompletionRS("Project index with name = '" + projectName + "' is successfully deleted.");
	}

	@Override
	public DeleteBulkRS deleteProjects(DeleteBulkRQ deleteBulkRQ) {
		List<ReportPortalException> exceptions = Lists.newArrayList();
		List<Long> notFound = Lists.newArrayList();
		List<Long> deleted = Lists.newArrayList();
		deleteBulkRQ.getIds().forEach(projectId -> {
			try {
				Optional<Project> project = projectRepository.findById(projectId);
				if (project.isPresent()) {
					deleteProject(project.get());
					deleted.add(projectId);
				} else {
					notFound.add(projectId);
				}
			} catch (ReportPortalException ex) {
				exceptions.add(ex);
			}
		});
		return new DeleteBulkRS(deleted, notFound, exceptions.stream().map(ex -> {
			ErrorRS errorResponse = new ErrorRS();
			errorResponse.setErrorType(ex.getErrorType());
			errorResponse.setMessage(ex.getMessage());
			return errorResponse;
		}).collect(Collectors.toList()));
	}

	private OperationCompletionRS deleteProject(Project project) {
		Set<Long> defaultIssueTypeIds = issueTypeRepository.getDefaultIssueTypes()
				.stream()
				.map(IssueType::getId)
				.collect(Collectors.toSet());
		Set<IssueType> issueTypesToRemove = project.getProjectIssueTypes()
				.stream()
				.map(ProjectIssueType::getIssueType)
				.filter(issueType -> !defaultIssueTypeIds.contains(issueType.getId()))
				.collect(Collectors.toSet());
		projectContentRemover.removeContent(project);
		projectRepository.delete(project);
		issueTypeRepository.deleteAll(issueTypesToRemove);
		logIndexer.deleteIndex(project.getId());
		logRepository.deleteByProjectId(project.getId());
		attachmentRepository.moveForDeletionByProjectId(project.getId());
		return new OperationCompletionRS("Project with id = '" + project.getId() + "' has been successfully deleted.");
	}
}
